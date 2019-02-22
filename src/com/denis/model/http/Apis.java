package com.denis.model.http;

public class Apis {

    public final static String HTTP_PROTOCOL = "http://";
    public static String HTTP_HOST = "101.231.201.56";
    public static String HTTP_PORT = "6080";
    public final static String HTTP_CLIENT_APP = "/kmclient";
    public final static String HTTP_APPROVAL = "/sso";
    public final static String APP_VERSION = "1.0.0";
    public final static String DOWNLOAD_APP = "download/main.jar";

    public static String GET_MAIL_LOG() {
        return HTTP_PROTOCOL + HTTP_HOST + ":" + HTTP_PORT + HTTP_CLIENT_APP;
    }

    public static String MAIL_LOG_PARAM = "action=insertMailLog&domainAccount=%s&curmailbox=%s&mail=%s&result=%s";

    public static String GET_UPDATE_INFO() {
//        return HTTP_PROTOCOL + HTTP_HOST + ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=update";
        return "http://192.168.0.31:8888?action=update";
    }

    public static String GET_APP_DOWNLOAD_URL() {
//        return HTTP_PROTOCOL + HTTP_HOST + ":" + HTTP_PORT + HTTP_CLIENT_APP + "/" + DOWNLOAD_APP;
        return "http://192.168.0.31:8888" + "/" + DOWNLOAD_APP;
    }

    public static String GET_SYS_TITLE() {
      return HTTP_PROTOCOL + HTTP_HOST + ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=getSysTitle";
    }

    public static String GET_MAILBOX_INFO() {
      return HTTP_PROTOCOL + HTTP_HOST + ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=describeMailboxInfo&domainAccount=%s";
    }

    public static String GET_USER_INFO() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=describeUserInfo&domainAccount=%s";
    }

    public static String GET_SYS_SECURITY_LEVEL() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=describeSysSecurityLevel";
    }

    public static String GET_MAIL_SECURITY_LEVEL() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=describeSecurityLevels";
    }

    public static String GET_USER_LEVEL() {
      return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=describeUserLevels";
    }

    public static String GET_ADDRESSBOOK() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=describeAddressList";
    }

    public static String CHECK_SEND() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=checkSend&from=%s&to=%s&mailSecurityLevel=%s&domainAccount=%s";
    }

    public static String GET_PENDING_APPROVE_LIST() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_APPROVAL + "?domainAccount=%s&menumark=20&fid=15&frame=1";
    }

    public static String GET_MAIL_APPROVAL() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_APPROVAL + "?menumark=1&frame=1&domainAccount=%s";
    }

    public static String GET_MAIL_APPROVAL_CNT() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=getApproveNumber&domainAccount=%s";
    }

    public static String GET_MAIL_APPROVAL_STATUS() {
        return HTTP_PROTOCOL + HTTP_HOST +  ":" + HTTP_PORT + HTTP_CLIENT_APP + "?action=getApproveStatus&domainAccount=%s&muid=%s&curmailbox=%s";
    }
}
