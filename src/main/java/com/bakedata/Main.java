package com.bakedata;

import com.bakedata.service.BlogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 1) {
            logger.error("file path missing");
            return;
        }

        if (!BlogService.loadConfig()) {
            logger.error("load config failed");
            return;
        }
        // default full flow
        if (args.length == 1) {
            if (!BlogService.articleTransService(args[0])) {
                logger.error("article trans failed");
                return;
            }
            if (!BlogService.remoteUpdate()) {
                logger.error("update remote server failed");
                return;
            }
            if (!BlogService.gitUpdate()) {
                logger.error("update git repository failed");
            }
            //only update website and git
        } else if (args.length == 2 && "-rg".equals(args[1])) {
            if (!BlogService.remoteUpdate()) {
                logger.error("update remote server failed");
                return;
            }
            if (!BlogService.gitUpdate()) {
                logger.error("update git repository failed");
            }
            // only update git
        } else if (args.length == 2 && "-g".equals(args[1])){
            if(!BlogService.gitUpdate()){
                logger.error("git update failed");
            }
        }
    }
}
