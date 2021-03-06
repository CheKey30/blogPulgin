package com.bakedata.beans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileHandler {
    final static Logger logger = LoggerFactory.getLogger(FileHandler.class);
    private File article;
    private File tmpArticle;
    private boolean newFile;
    private File folder;
    private int imgNum = 1;
    private boolean success = true;

    public FileHandler(String filePath) {
        article = new File(filePath);
        String folderPath = filePath.replace(".md", "");
        String tmpArticlePath = filePath.replace(".md", "_2.md");
        folder = new File(folderPath);
        newFile = !folder.exists();
        if (newFile) {
            logger.info(String.format("create new image folder %s", folder.mkdirs()));
        }
        tmpArticle = new File(tmpArticlePath);
    }

    public File getArticle() {
        return article;
    }

    public void setArticle(File article) {
        this.article = article;
    }

    public boolean isNewFile() {
        return newFile;
    }

    public void setNewFile(boolean newFile) {
        this.newFile = newFile;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public void hexoArticle() throws IOException {
        // create article reader
        InputStreamReader read = new InputStreamReader(
                new FileInputStream(article), StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(read);

        // create a tmpArticle writer
        if (!tmpArticle.createNewFile()) {
            logger.error(String.format("create temp article file failed, name: %s", article.getName()));
            tmpArticle.delete();
            return;
        }

        OutputStreamWriter write = new OutputStreamWriter(
                new FileOutputStream(tmpArticle), StandardCharsets.UTF_8);
        BufferedWriter bufferedWriter = new BufferedWriter(write);

        String lineTxt = bufferedReader.readLine();
        if (lineTxt == null || !lineTxt.contains("tags")) {
            logger.error("article must start with tags:tag1,tag2,tag3...");
            tmpArticle.delete();
            return;
        } else {
            writeHead(lineTxt, bufferedWriter);
        }

        while ((lineTxt = bufferedReader.readLine()) != null) {
            if (lineTxt.contains("![??????]")) {
                imageTransfer(lineTxt, bufferedWriter);
            } else {
                bufferedWriter.write(lineTxt);
                bufferedWriter.newLine();
            }
        }
        bufferedReader.close();
        read.close();
        bufferedWriter.close();
        write.close();
        tmpArticle.renameTo(article);
//        article.delete();
    }

    private void imageTransfer(String lineTxt, BufferedWriter bufferedWriter) throws IOException {
        String img = lineTxt.substring(lineTxt.indexOf("(") + 1, lineTxt.indexOf(")"));
        String imgCore = img.substring(img.indexOf(",") + 1, img.length());
        if (generateImage(imgCore)) {
            bufferedWriter.write("![](" + (imgNum - 1) + ".png)");
            bufferedWriter.newLine();
        } else {
            logger.error("generate img failed");
            success = false;
        }

    }

    public boolean generateImage(String imgStr) {
        if (imgStr == null) {
            return false;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            byte[] b = decoder.decode(imgStr);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            String imgFilePath = folder.getPath() + "/" + imgNum + ".png";
            OutputStream out = new FileOutputStream(imgFilePath);
            out.write(b);
            out.flush();
            out.close();
            imgNum++;
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private void writeHead(String lineTxt, BufferedWriter bufferedWriter) throws IOException {
        String[] tags;
        if(lineTxt.contains(":")){
            tags = lineTxt.split(":")[1].split(",");
        }else if(lineTxt.contains("???")){
            tags = lineTxt.split("???")[1].split(",");
        }else {
            logger.error("tags line is wrong, please check the article");
            tmpArticle.delete();
            return;
        }
        String[] categories = article.getPath().split("/");
        bufferedWriter.write("---");
        bufferedWriter.newLine();
        bufferedWriter.write("title: " + article.getName().replace(".md", ""));
        bufferedWriter.newLine();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        bufferedWriter.write("date: " + formatter.format(date));
        bufferedWriter.newLine();
        bufferedWriter.write("categories:");
        bufferedWriter.newLine();
        boolean startCat = false;
        for (int i = 0; i < categories.length - 1; i++) {
            if ("BakeData".equals(categories[i])) {
                startCat = true;
            } else if (startCat) {
                bufferedWriter.write("- " + categories[i]);
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.write("tags:");
        bufferedWriter.newLine();
        for (String tag : tags) {
            bufferedWriter.write("- " + tag.trim());
            bufferedWriter.newLine();
        }
        bufferedWriter.write("---");
        bufferedWriter.newLine();
        bufferedWriter.write("");
        bufferedWriter.newLine();
    }

    public String[] getUploadFilePaths() {
        File[] tmpList = folder.listFiles();
        String[] res = new String[tmpList.length];
        for (int i = 0; i < tmpList.length; i++) {
            res[i] = tmpList[i].getAbsolutePath();
        }
        return res;
    }

    public String[] getUploadFileNames() {
        File[] tmpList = folder.listFiles();
        String[] res = new String[tmpList.length];
        for (int i = 0; i < tmpList.length; i++) {
            res[i] = tmpList[i].getName();
        }
        return res;
    }
}
