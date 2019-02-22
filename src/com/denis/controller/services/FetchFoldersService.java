package com.denis.controller.services;

import javax.mail.Folder;

import com.denis.controller.AbstractController;
import com.denis.controller.ModelAccess;
import com.denis.model.EmailAccountBean;
import com.denis.model.folder.EmailFolderBean;
import com.denis.view.ViewFactory;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class FetchFoldersService extends Service<Void>{
	
	private static int  NUMBER_OF_FETCHFOLDERSERVICES_ACTIVE = 0;
	private EmailFolderBean<String> foldersRoot;
	private EmailAccountBean emailAccountBean;
	private ModelAccess modelAccess;
	public AbstractController parent;
	
	public FetchFoldersService(EmailFolderBean<String> foldersRoot, EmailAccountBean emailAccountBean, ModelAccess modelAccess) {
		this.foldersRoot = foldersRoot;
		this.emailAccountBean = emailAccountBean;
		this.modelAccess = modelAccess;
		
		this.setOnSucceeded(e->{
			NUMBER_OF_FETCHFOLDERSERVICES_ACTIVE--;
		});
	}

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>(){
			@Override
			protected Void call() throws Exception {
				NUMBER_OF_FETCHFOLDERSERVICES_ACTIVE++;
				if (ViewFactory.defaultFactory.wasLogout()) {
					System.out.println("main scene was not initialized!!!");
					return null;
				}
				if(emailAccountBean != null){
					Folder[] folders = emailAccountBean.getStore().getDefaultFolder().list();
					for(Folder folder: folders){

//						PrototypeController controller = (PrototypeController)parent;
//						controller.updateMailBox(folder.getName());

						if (ViewFactory.defaultFactory.wasLogout()) {
							System.out.println("main scene was not initialized!!!");
							return null;
						}
						
						EmailFolderBean<String> item = new EmailFolderBean<String>(folder.getName(), folder.getFullName());
						foldersRoot.getChildren().add(item);
						item.setExpanded(true);
						modelAccess.addFolder(folder);
						System.out.println("added " +  folder.getName());
					}
				}
				return null;
			}
			
		};
	}

	public static boolean noServicesActive(){
		System.out.println("number of services active: " + NUMBER_OF_FETCHFOLDERSERVICES_ACTIVE);
		return NUMBER_OF_FETCHFOLDERSERVICES_ACTIVE==0;
	}

}
