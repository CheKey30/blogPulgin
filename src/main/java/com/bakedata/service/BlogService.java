package com.bakedata.service;

import com.bakedata.Main;
import com.bakedata.beans.ConfigHandler;
import com.bakedata.beans.FileHandler;
import com.bakedata.beans.SshHandler;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BlogService {
    final static Logger logger = LoggerFactory.getLogger(BlogService.class);
    private static FileHandler fileHandler;
    private static ConfigHandler configHandler;
    private static SshHandler sshHandler;

    public static boolean loadConfig() {
        configHandler = new ConfigHandler();
        configHandler.loadConf("config.yml");
        return !configHandler.getConfigs().isEmpty();
    }

    public static boolean articleTransService(String path) {
        String filePath = path;
        // find the article need to be posted
        fileHandler = new FileHandler(filePath);
        if (!fileHandler.getArticle().exists()) {
            logger.error("article does not exist, please check the path");
            return false;
        }
        try {
            fileHandler.hexoArticle();
            logger.info("article trans success!");
            return true;
        } catch (IOException e) {
            logger.info("article trans failed: " + e.getMessage());
            return false;
        }
    }

    public static boolean remoteUpdate() {
        try {
            sshHandler = new SshHandler((int) configHandler.getConfigs().get("port"), (String) configHandler.getConfigs().get("server-ip"), (String) configHandler.getConfigs().get("username"), (String) configHandler.getConfigs().get("password"));
            if (fileHandler.isNewFile()) {
                String command = String.format("cd %s ; hexo new \"%s\"", configHandler.getConfigs().get("hexo-root"), fileHandler.getFolder().getName());
                sshHandler.execCommand(command);
            }
            // send article to server
            String[] imagePaths = fileHandler.getUploadFilePaths();
            String[] imageNames = fileHandler.getUploadFileNames();
            String remoteImgPath = configHandler.getConfigs().get("hexo-post") + fileHandler.getFolder().getName();
            logger.info(remoteImgPath);

            for (int i = 0; i < imagePaths.length; i++) {
                sshHandler.uploadFile(remoteImgPath + "/" + imageNames[i], imagePaths[i]);
            }
            sshHandler.uploadFile(configHandler.getConfigs().get("hexo-post") + "/" + fileHandler.getArticle().getName(), fileHandler.getArticle().getAbsolutePath());

            // rebuild the blog
            String rebuild = String.format("cd %s ; hexo g ; hexo d", (String) configHandler.getConfigs().get("hexo-root"));
            sshHandler.execCommand(rebuild);
            return true;
        } catch (IOException | JSchException e) {
            logger.error(e.getMessage());
            return false;
        } finally {
            sshHandler.closeSession();
        }
    }
}
