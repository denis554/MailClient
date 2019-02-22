package com.denis;

import com.denis.controller.AbstractController;
import com.denis.controller.PrototypeController;
import com.denis.daemon.WindowsSendToReceiver;
import com.denis.model.GlobalVariables.GlobalVariables;
import com.denis.model.http.Apis;
import com.denis.view.ViewFactory;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.denis.model.GlobalVariables.GlobalVariables.*;

public class App extends Application{


	public static PrototypeController globalPrototypeController = null;

	// a timer allowing the tray icon to provide a periodic notification event.
	private static Timer notificationTimer = new Timer();

	// format used to display the current time in a tray icon notification.
	private static DateFormat timeFormat = SimpleDateFormat.getTimeInstance();

	static java.awt.SystemTray tray;
	static java.awt.TrayIcon trayIcon;

	private static final int SINGLE_INSTANCE_LISTENER_PORT = 9999;
	private static final String SINGLE_INSTANCE_FOCUS_MESSAGE = "focus";

	private static final String instanceId = UUID.randomUUID().toString();

	// We define a pause before focusing on an existing instance
	// because sometimes the command line or window launching the instance
	// might take focus back after the second instance execution complete
	// so we introduce a slight delay before focusing on the original window
	// so that the original window can retain focus.
	private static final int FOCUS_REQUEST_PAUSE_MILLIS = 500;

	/**
	 * track the log
	 * @param body
	 */
	public static void trackLog(String body) {
		trackLog(body, true);
	}

    /**
     * track the log in App
     * @param body
     */
	public static void trackLog(String body, boolean isAdded) {

		if (!isAdded || !isRegApp)
			return;

	    try {
            File attachF = new File(APP_DATA_DIR, LOG_FILE);
            if (!attachF.exists())
				attachF.createNewFile();

			OutputStreamWriter pw =
					new OutputStreamWriter(new FileOutputStream(attachF,true), StandardCharsets.UTF_8);

			Date date = new Date();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd(EEEE) hh:mm a");
			LocalDateTime locTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			String curTime = locTime.format(formatter);

            pw.write(curTime);
			pw.write("------------");
			pw.write(body);
			pw.write("\n");

			pw.flush();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

	/**
	 * set arguments
	 * @param args
	 */
	public static void setArgs(List<String> args) {
		try {
			PrintWriter pw = null;
			if (isRegApp) {
				File attachF = new File(APP_DATA_DIR, APP_ATTACH_FILE);
				if (attachF.exists()) {
					attachF.delete();
				}
				attachF.createNewFile();
				pw = new PrintWriter(attachF);
			}
			for (String s: args) {

				System.out.println("command = " + s);
				if (s.startsWith("ip:")) {
					Apis.HTTP_HOST = s.substring("ip:".length());
					System.out.println("Apis.HTTP_HOST = " + Apis.HTTP_HOST);
				}
				if (s.startsWith("local:zh")) {
					AbstractController.setLocale(Locale.CHINA);
					System.out.println("Local = " + Locale.CHINA);
				}

				if (s.startsWith("reg:clr")) {
					clearRegKey();
					isRegApp = isRegisteredApp();
					System.out.println("isRegApp = " + isRegApp);
				}

				if (isRegApp) {
					File f = new File(s);
					if (f.isFile() && !f.isHidden() && f.exists()) {
						pw.write(s);
						pw.write("\n");
					}
				}
			}
			if (isRegApp) {
				pw.flush();
				pw.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			killProcess(getRuntimePid());
		}
	}

	@Override
	public void init() throws Exception {
		super.init();
		CountDownLatch instanceCheckLatch = new CountDownLatch(1);

		Thread instanceListener = new Thread(() -> {
			try (ServerSocket serverSocket = new ServerSocket(SINGLE_INSTANCE_LISTENER_PORT, 10)) {
				instanceCheckLatch.countDown();

				while (true) {
					try {
						Socket clientSocket = serverSocket.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "utf-8"));
						String input = in.readLine();
						System.out.println("Received single instance listener message: " + input);
						if (input.startsWith(SINGLE_INSTANCE_FOCUS_MESSAGE) && primaryStage != null) {
							Thread.sleep(FOCUS_REQUEST_PAUSE_MILLIS);
							Platform.runLater(() -> {
								System.out.println("To front " + instanceId);
								primaryStage.setIconified(false);
								primaryStage.show();
								primaryStage.toFront();

								killProcess();

								if (GlobalVariables.mainController != null)
									GlobalVariables.mainController.parseAttachFile();

							});
						}
					} catch (IOException e) {
						System.out.println("Single instance listener unable to process focus message from client");
						e.printStackTrace();
					}
				}
			} catch(java.net.BindException b) {
				System.out.println("SingleInstanceApp already running");

				try (
						Socket clientSocket = new Socket(InetAddress.getLocalHost(), SINGLE_INSTANCE_LISTENER_PORT);
						PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()))
				) {
					System.out.println("Requesting existing app to focus");
					out.println(SINGLE_INSTANCE_FOCUS_MESSAGE + " requested by " + instanceId);
				} catch (IOException e) {
					e.printStackTrace();
				}

				System.out.println("Aborting execution for instance " + instanceId);
				Platform.exit();
			} catch(Exception e) {
				System.out.println(e.toString());
			} finally {
				instanceCheckLatch.countDown();
			}
		}, "instance-listener");
		instanceListener.setDaemon(true);
		instanceListener.start();

		try {
			instanceCheckLatch.await();
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}

	/**
	 * make the process id file
	 */
	public static void makeRuntimePid() {
		try {
			if (APP_PID_DIR != null && APP_PID_DIR.trim().length() > 0) {
				String pid = getRuntimePid();
				File pF = new File(APP_PID_DIR, pid);
				if (!pF.getParentFile().exists())
					pF.getParentFile().mkdirs();
				pF.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * get current pid
	 * @return
	 */
	public static String getRuntimePid() {
		String vmName = ManagementFactory.getRuntimeMXBean().getName();
		int p = vmName.indexOf("@");
		return vmName.substring(0, p);
	}

	/**
	 * kill the process if the multi process exists
	 */
	public void killProcess() {
		String curPid = getRuntimePid();
		System.out.println("current---pid===" + curPid);
		File[] files = new File(APP_PID_DIR).listFiles();
		for (File eachF: files) {
			String fName = eachF.getName();
			if (!fName.equalsIgnoreCase(curPid)) {
				killProcess(fName);
			}
			eachF.delete();
		}
	}

	/**
	 * kill process by the pid
	 * @param pid
	 */
	public static void killProcess(String pid) {

		try {

			System.out.println("process---id===" + pid);
			Process proc = Runtime.getRuntime().exec("taskkill /PID " + pid + " /F");

			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(proc.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			String s = null;
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		try {
//			String outputFileName = System.getProperty("user.home");
//			File appDir = new File(outputFileName , "MailClient");
//			appDir.mkdirs();
//			APP_DATA_DIR = appDir.getPath();
//			APP_PID_DIR = APP_DATA_DIR + File.separator + "pid";
			WindowsSendToReceiver.start();

            //registry handle
			isRegApp = isRegisteredApp();
            if (isRegApp) {
				loadAppDataPath();
				makeRuntimePid();
			}
			setArgs(Arrays.asList(args));
		} catch (Exception e) {
			e.printStackTrace();
		}

		//for test
//			AbstractController.setLocale(Locale.CHINA);
		launch(args);
	}

    /**
     * set the app data directory
     */
	public static void loadAppDataPath() {
		try {
			String productName = Advapi32Util.registryGetStringValue(
					WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion", "ProductName");
			System.out.printf("Product Name: %s\n", productName);

//			// Read an int (& 0xFFFFFFFFL for large unsigned int)
//			int timeout = Advapi32Util.registryGetIntValue(
//					WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Windows", "ShutdownWarningDialogTimeout");
//			System.out.printf("Shutdown Warning Dialog Timeout: %d (%d as unsigned long)\n", timeout, timeout & 0xFFFFFFFFL);

			APP_DATA_DIR = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, APP_REG_KEY, APP_DB_PATH_KEY);
			String dbName = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, APP_REG_KEY, APP_DB_NAME_KEY);
			if (dbName != null && dbName.trim().length() > 0)
				DB_NAME = dbName;
			if (APP_DATA_DIR != null && APP_DATA_DIR.trim().length() > 0) {
				APP_PID_DIR = APP_DATA_DIR + File.separator + "pid";
				File fDir = new File(APP_DATA_DIR);
				if (!fDir.exists()) {
					fDir.mkdirs();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * change the app path
     * @param path
     */
	public static void setAppPath(String path, String name) {
        // Create a key and write a string
		try {
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, APP_REG_KEY, APP_DB_PATH_KEY, path);
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, APP_REG_KEY, APP_DB_NAME_KEY, name);
			loadAppDataPath();
			makeRuntimePid();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    /**
     * set the app windows system registry key
     * @param path
     */
	public static void setRegisterApp(String path, String name) {
        // Create a key and write a string
		try {
			Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, APP_REG_KEY);
			setAppPath(path, name);
			isRegApp = isRegisteredApp();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	/**
	 * clear the reg path
	 */
	static void clearRegKey() {
		try {
			Advapi32Util.registrySetStringValue(WinReg.HKEY_CURRENT_USER, APP_REG_KEY, APP_DB_PATH_KEY, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * check the app registration status on current running windows system
     * @return
     */
    public static boolean isRegisteredApp() {
        try {
            String path = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER, APP_REG_KEY, APP_DB_PATH_KEY);
            if (path == null || path.trim().length() == 0)
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	    return true;
    }


	/**
	 * set the app title
	 * @param userName
	 */
	public static void setAppTitle(String userName) {

		String appName = getAppName();

		if (appName == null || appName.isEmpty())
			appName = AbstractController.getString("app_name");

		if (GlobalVariables.sysTitle != null && GlobalVariables.sysTitle.length() > 0)
			appName = GlobalVariables.sysTitle;

		String formattedAppName;
		String appSuffix = " V3.0 (" + AbstractController.getString("security") +") - " + userName;

		if (appName.length() > 0)
			formattedAppName = appName + appSuffix;
		else
			formattedAppName = AbstractController.getString("app_name") + appSuffix;

		primaryStage.setTitle(formattedAppName);
	}

	@Override
	public void start(Stage primaryStage) {

		Platform.setImplicitExit(false);

		GlobalVariables.primaryStage = primaryStage;
		setAppTitle(PrototypeController.getPCName());

		Scene scene = ViewFactory.defaultFactory.getPrototypeScene();
		KeyCombination keyCtrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
		KeyCombination keyCtrlD = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
		KeyCombination keyCtrlP = new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN);
		KeyCombination keyCtrlR = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
		KeyCombination keyCtrlShiftR = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
		KeyCombination keyCtrlF = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
		KeyCombination keyCtrlShiftS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
		globalPrototypeController = ((FXMLLoader) scene.getUserData()).getController();

		try {
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					//new mail
					FXMLLoader loader = (FXMLLoader) scene.getUserData();
					PrototypeController prototypeController = loader.getController();
					if (keyCtrlN.match(event)) {//new mail
						prototypeController.handleClickedOnNewMailMenuButton();
					}
					if (keyCtrlD.match(event)) {//delete item
						prototypeController.handleClickedOnDeleteMenuButton();
					}
					if (keyCtrlP.match(event)) {//mail print
						prototypeController.handleClickedOnPrintMenuButton();
					}
					if (keyCtrlR.match(event)) {//mail reply
						prototypeController.handleClickedOnReplyMenuButton();
					}
					if (keyCtrlShiftR.match(event)) {//mail reply all
						prototypeController.handleClickedOnReplyAllMenuButton();
					}
					if (keyCtrlF.match(event)) {//mail forward
						prototypeController.handleClickedOnForwardMenuButton();
					}
					if (keyCtrlShiftS.match(event)) {//mail send/receive
						prototypeController.handleClickedOnSendReceiveMenuButton();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				event.consume();
				GlobalVariables.primaryStage.hide();
			}
		});
		primaryStage.getIcons().add(ViewFactory.defaultFactory.resolveIconImageWithName("images/logo.png"));
		primaryStage.setScene(scene);
		primaryStage.show();
		addAppToTray(App.this);
//		WindowsSendToReceiver.start();
	}

	/**
	 * get the app name from the config file
	 * @return
	 */
	public static String getAppName() {
		File conf = new File(GlobalVariables.CONF_FILE);
		if (conf.isFile() && !conf.isHidden() && conf.exists()) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(conf), "UTF-8"));
				String name = br.readLine();
				return name;
			} catch (Exception e) {
			}
		}
		return "";
	}

	/**
	 * Sets up a system tray icon for the application.
	 */
	private static void addAppToTray(App instance) {
		try {
			// ensure awt toolkit is initialized.
			java.awt.Toolkit.getDefaultToolkit();

			// app requires system tray support, just exit if there is no support.
			if (!java.awt.SystemTray.isSupported()) {
				System.out.println("No system tray support, application exiting.");
				return;
			}

			// set up a system tray icon.
			tray = java.awt.SystemTray.getSystemTray();
			java.awt.Image image = ImageIO.read(instance.getClass().getResourceAsStream("/com/denis/view/images/logo16.png"));
			trayIcon = new java.awt.TrayIcon(image);

			// if the user double-clicks on the tray icon, show the main app stage.
			trayIcon.addActionListener(event -> Platform.runLater(instance::showStage));

			// if the user selects the default menu item (which includes the app name),
			// show the main app stage.
			java.awt.MenuItem openItem = new java.awt.MenuItem(AbstractController.getString("Open"));
			openItem.addActionListener(event -> Platform.runLater(instance::showStage));

			// the convention for tray icons seems to be to set the default icon for opening
			// the application stage in a bold font.
			java.awt.Font defaultFont = java.awt.Font.decode(null);
			java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
			openItem.setFont(boldFont);

			// to really exit the application, the user must go to the system tray icon
			// and select the exit option, this will shutdown JavaFX and remove the
			// tray icon (removing the tray icon will also shut down AWT).
			java.awt.MenuItem exitItem = new java.awt.MenuItem(AbstractController.getString("exit"));
			exitItem.addActionListener(event -> {
				Platform.runLater(()->{
					GlobalVariables.removeAllNoNeedDrafts();
					saveDrafts();
					GlobalVariables.mainController.shutdown();
				});
			});

			// setup the popup menu for the application.
			final java.awt.PopupMenu popup = new java.awt.PopupMenu();
			popup.add(openItem);
			popup.addSeparator();
			popup.add(exitItem);
			trayIcon.setPopupMenu(popup);

			// create a timer which periodically displays a notification message.
			notificationTimer.schedule(
					new TimerTask() {
						@Override
						public void run() {
							javax.swing.SwingUtilities.invokeLater(() ->
									trayIcon.displayMessage(
											AbstractController.getString("app_name"),
											String.format(AbstractController.getString("tray_msg"), timeFormat.format(new Date())),
											java.awt.TrayIcon.MessageType.INFO
									)
							);
							notificationTimer.cancel();
						}
					},
					1000,
					1000
			);

			// add the application tray icon to the system tray.
			tray.add(trayIcon);
		} catch (java.awt.AWTException | IOException e) {
			System.out.println("Unable to init system tray");
			e.printStackTrace();
		}
	}

	/**
	 * remove the tray icon
	 */
	public static void removeTray() {
		tray.remove(trayIcon);
	}

	/**
	 * save Drafts
	 */
	public static void saveDrafts() {
		if (GlobalVariables.isExistDrafts()) {
			GlobalVariables.askSaveDraft();
			saveDrafts();
		}
	}

	/**
	 * Shows the application stage and ensures that it is brought ot the front of all stages.
	 */
	private void showStage() {
		if (GlobalVariables.primaryStage != null) {
			GlobalVariables.primaryStage.show();
			GlobalVariables.primaryStage.toFront();
		}
	}
}