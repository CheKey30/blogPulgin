import beans.FileHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;


public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        if(args.length<1){
            logger.error("file path missing");
        }
        String filePath = args[0];
        // find the article need to be posted
        FileHandler fileHandler = new FileHandler(filePath);
        if(!fileHandler.getArticle().exists()){
            logger.error("article does not exist, please check the path");
            return;
        }

        // convert the article into hexo format
        try {
            fileHandler.hexoArticle();
        }catch (IOException e){
            logger.error(String.valueOf(e));
        }


        // new article in blog
        // send article to server
        // rebuild the blog
    }
}
