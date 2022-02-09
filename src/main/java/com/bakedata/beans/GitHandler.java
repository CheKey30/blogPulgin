package com.bakedata.beans;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class GitHandler {
    final static Logger logger = LoggerFactory.getLogger(GitHandler.class);
    private static String LOCAL_REPO_PATH;

    public GitHandler(ConfigHandler configHandler) {
        LOCAL_REPO_PATH = (String) configHandler.getConfigs().get("local-repo-path");
    }

    public boolean commitAndPush() {
        Runtime run = Runtime.getRuntime();
        File wd = new File("/bin");
        System.out.println(wd);
        Process proc = null;
        try {
            proc = run.exec("/bin/bash", null, wd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (proc != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
            out.println("cd "+LOCAL_REPO_PATH);
            out.println("git pull");
            out.println("git add .");
            out.println("git commit -m \"auto update\"");
            out.println("git push");
            out.println("exit");//这个命令必须执行，否则in流不结束。
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
                proc.waitFor();
                in.close();
                out.close();
                proc.destroy();
                return true;
            } catch (Exception e) {
                logger.error(e.getMessage());
                return false;
            }
        }
        return false;
    }


    public static void main(String[] args) {
        ConfigHandler configHandler = new ConfigHandler();
        configHandler.loadConf("config.yml");
        GitHandler gitHandler = new GitHandler(configHandler);
        gitHandler.commitAndPush();
    }
}
