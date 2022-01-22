import beans.ConfigHandler;
import beans.FileHandler;

import beans.SshHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("file path missing");
        }
        String filePath = args[0];
        // find the article need to be posted
        FileHandler fileHandler = new FileHandler(filePath);
        if (!fileHandler.getArticle().exists()) {
            logger.error("article does not exist, please check the path");
            return;
        }


        try {
            // convert the article into hexo format
            fileHandler.hexoArticle();
            // read config
            ConfigHandler configHandler = new ConfigHandler();
            configHandler.loadConf("config.yml");
            // connect remote server
            SshHandler sshHandler = new SshHandler((int)configHandler.getConfigs().get("port"),(String)configHandler.getConfigs().get("server-ip"),(String) configHandler.getConfigs().get("username"),(String) configHandler.getConfigs().get("password"));
            // new article in blog
            if (fileHandler.isNewFile()) {
                String command = String.format("cd %s ; hexo new \"%s\"",configHandler.getConfigs().get("hexo-root"),fileHandler.getFolder().getName());
                sshHandler.execCommand(command);
            }

            // send article to server
            String[] imagePaths = fileHandler.getUploadFilePaths();
            String[] imageNames = fileHandler.getUploadFileNames();
            String remoteImgPath = configHandler.getConfigs().get("hexo-post")+fileHandler.getFolder().getName();
            logger.info(remoteImgPath);

            for(int i=0;i<imagePaths.length;i++){
                sshHandler.uploadFile(remoteImgPath+"/"+imageNames[i],imagePaths[i]);
            }
            sshHandler.uploadFile((String) configHandler.getConfigs().get("hexo-post")+"/"+fileHandler.getArticle().getName(),fileHandler.getArticle().getAbsolutePath());

            // rebuild the blog
            String rebuild = String.format("cd %s ; hexo g ; hexo d",(String) configHandler.getConfigs().get("hexo-root"));
            sshHandler.execCommand(rebuild);
            sshHandler.closeSession();
            logger.info("-------------update success!---------------");
        } catch (IOException e) {
            logger.error(String.valueOf(e));
        }

    }
}
