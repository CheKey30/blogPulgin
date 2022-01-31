package com.bakedata.beans;

public class GitHandler {
    private static String LOCAL_REPO_PATH ;
    private static String LOCAL_REPOGIT_CONFIG;
    private static String REMOTE_REPO_URI;
    private static String INIT_LOCAL_CODE_DIR;
    private static String LOCAL_CODE_CT_SQL_DIR;
    private static String BRANCH_NAME ;
    private static String GIT_USERNAME;
    private static String GIT_PASSWORD ;

    public GitHandler(ConfigHandler configHandler){
        this.LOCAL_REPO_PATH = (String) configHandler.getConfigs().get("local-repo-path");

    }
}
