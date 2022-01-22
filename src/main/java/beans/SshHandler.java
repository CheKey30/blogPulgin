package beans;

import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class SshHandler {
    final static Logger logger = LoggerFactory.getLogger(SshHandler.class);
    private int port;
    private String ip;
    private String userName;
    private String password;
    private Session session;
    private boolean logined = false;

    public SshHandler(int port, String ip, String userName, String password) {
        this.port = port;
        this.userName = userName;
        this.password = password;
        // get connection
        JSch jSch = new JSch();
        try {
            session = jSch.getSession(userName, ip, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(20000);
            ;
            logined = true;
            logger.info("connected to remote server");
        } catch (JSchException e) {
            logined = false;
            logger.error("connected failed");
            logger.error(String.valueOf(e));
        }
    }

    public void closeSession() {
        if (session != null) {
            session.disconnect();
        }
    }

    public void execCommand(String command) throws IOException {
        logger.info("execute command: " + command);
        InputStream in = null;
        Channel channel = null;
        try {
            if (command != null) {
                channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect();
                in = channel.getInputStream();
                String processDataStream = processDataStream(in);
                logger.info("process result: " + processDataStream);
            }
        } catch (JSchException | IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    private String processDataStream(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String result = "";
        long startTime = System.currentTimeMillis();
        boolean getRes = false;
        try {
            do {
                while ((result = br.readLine()) != null) {
                    sb.append(result);
                    getRes = true;
                }
            } while (!getRes && System.currentTimeMillis() - startTime <= 1000);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            br.close();
        }
        return sb.toString();
    }


    // remoteDir must contain the file name !!!!
    public void uploadFile(String remoteDir, String localFile) {
        try {
            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            File file = new File(localFile);
            FileInputStream in = new FileInputStream(file);
            channelSftp.put(in, remoteDir, ChannelSftp.OVERWRITE);
            in.close();
            channelSftp.exit();
            logger.info("upload file "+localFile+" success!");
        } catch (JSchException | SftpException | IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SshHandler h = new SshHandler(22,"106.15.58.50","root","Ck142857285714");
        h.uploadFile("/usr/local/hexo/source/_posts/快学Scala-1-基础.md","/Users/shuchen/blogArticles/BakeData/读书笔记/快学Scala/快学Scala-1-基础.md");

    }
}
