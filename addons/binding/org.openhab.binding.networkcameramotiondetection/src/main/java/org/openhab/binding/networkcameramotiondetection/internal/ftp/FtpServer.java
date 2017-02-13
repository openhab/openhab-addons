/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.networkcameramotiondetection.internal.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple FTP server implementation to receive images from Network camera.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class FtpServer {

    private Logger logger = LoggerFactory.getLogger(FtpServer.class);
    private int port = 2121;
    int idleTimeout = 60;

    private org.apache.ftpserver.FtpServer server;
    private static List<FtpServerEventListener> listeners = new ArrayList<FtpServerEventListener>();

    private HashMap<String, UsernamePassword> authenticationData = new HashMap<String, UsernamePassword>();

    private MyFTPLet myFTPLet;

    private class UsernamePassword {
        private String username;
        private String password;

        UsernamePassword(String username, String password) {
            this.setUsername(username);
            this.setPassword(password);
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public FtpServer() {

    }

    public void startServer(int port, int idleTimeout) {
        stopServer();
        this.port = port;
        this.idleTimeout = idleTimeout;
        initServer();
    }

    public void stopServer() {
        if (server != null) {
            logger.info("Stopping FTP server");
            server.stop();
        }
    }

    public synchronized void addEventListener(FtpServerEventListener rfxComEventListener) {
        if (!listeners.contains(rfxComEventListener)) {
            listeners.add(rfxComEventListener);
        }
    }

    public synchronized void addAuthenticationCredentials(String username, String password) throws RuntimeException {

        if (authenticationData.containsKey(username)) {
            throw new RuntimeException("Crerentials for user '" + username + "' already exists!");
        }

        authenticationData.put(username, new UsernamePassword(username, password));
    }

    public synchronized void removeAuthenticationCredentials(String username) throws RuntimeException {

        authenticationData.remove(username);
    }

    public synchronized void removeEventListener(FtpServerEventListener listener) {
        listeners.remove(listener);
    }

    void sendMsgToListeners(String userName, byte[] data) {
        try {
            Iterator<FtpServerEventListener> iterator = listeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().fileReceived(userName, data);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error: ", e.getMessage());
        }
    }

    public void printStats() {
        FtpStatistics ftpStats = myFTPLet.getStats();

        logger.debug("TotalConnectionNumber: {}", ftpStats.getTotalConnectionNumber());
        logger.debug("TotalLoginNumber: {}", ftpStats.getTotalLoginNumber());
        logger.debug("TotalFailedLoginNumber: {}", ftpStats.getTotalFailedLoginNumber());
        logger.debug("TotalUploadNumber: {}", ftpStats.getTotalUploadNumber());
        logger.debug("TotalUploadSize: {}", ftpStats.getTotalUploadSize());

        logger.debug("CurrentConnectionNumber: {}", ftpStats.getCurrentConnectionNumber());
        logger.debug("CurrentLoginNumber: {}", ftpStats.getCurrentLoginNumber());
    }

    private void initServer() {
        logger.info("Starting FTP server, port={}, idleTimeout={}", port, idleTimeout);

        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(port);
        listenerFactory.setIdleTimeout(idleTimeout);

        Listener listener = listenerFactory.createListener();

        serverFactory.addListener("default", listener);

        Map<String, Ftplet> ftplets = new LinkedHashMap<String, Ftplet>();
        myFTPLet = new MyFTPLet();

        ftplets.put("ftplet", myFTPLet);

        serverFactory.setFtplets(ftplets);
        serverFactory.setFileSystem(new FileSystemFactory() {
            @Override
            public FileSystemView createFileSystemView(User user) throws FtpException {
                logger.debug("createFileSystemView: {}", user.getName());
                return new MyFileSystemView();
            }
        });

        // set the user manager
        serverFactory.setUserManager(new FTPUserManager());
        server = serverFactory.createServer();
        try {
            server.start();
        } catch (FtpException e) {
            e.printStackTrace();
        }
    }

    private class MyFTPLet extends DefaultFtplet {
        FtpletContext ftpletContext;

        public FtpStatistics getStats() {
            return ftpletContext.getFtpStatistics();
        }

        @Override
        public void init(FtpletContext ftpletContext) throws FtpException {
            this.ftpletContext = ftpletContext;

        }

        @Override
        public void destroy() {

        }

        @Override
        public FtpletResult onConnect(FtpSession session) throws FtpException, IOException {
            System.out.println("User connected to FtpServer");
            return super.onConnect(session);
        }

        @Override
        public FtpletResult onUploadEnd(final FtpSession session, final FtpRequest request)
                throws FtpException, IOException {

            String userRoot = session.getUser().getHomeDirectory();
            String currDir = session.getFileSystemView().getWorkingDirectory().getAbsolutePath();
            String fileName = request.getArgument();

            logger.debug("File {} upload to FTP server", userRoot + currDir + fileName);

            MyFtpFile file = (MyFtpFile) session.getFileSystemView().getFile(fileName);
            byte[] data = file.getData();

            sendMsgToListeners(session.getUser().getName(), data);
            return FtpletResult.SKIP;
        }
    }

    private class FTPUser implements User {
        private final String login;

        public FTPUser(final String login) {
            this.login = login;
        }

        @Override
        public AuthorizationRequest authorize(final AuthorizationRequest authRequest) {
            logger.trace("authorize: {}", authRequest);
            return authRequest;
        }

        @Override
        public boolean getEnabled() {
            logger.trace("getEnabled");
            return true;
        }

        @Override
        public String getHomeDirectory() {
            logger.trace("getHomeDirectory");
            return "/";
        }

        @Override
        public int getMaxIdleTime() {
            logger.trace("getMaxIdleTime");
            return idleTimeout;
        }

        @Override
        public String getName() {
            logger.trace("getName");
            return this.login;
        }

        @Override
        public List<Authority> getAuthorities() {
            logger.trace("getAuthorities");
            return null;
        }

        @Override
        public List<Authority> getAuthorities(Class<? extends Authority> arg0) {
            logger.trace("getAuthorities: {}", arg0);
            return null;
        }

        @Override
        public String getPassword() {
            logger.trace("getPassword");
            return null;
        }
    }

    private class FTPUserManager implements UserManager {
        @Override
        public User authenticate(final Authentication inAuth) throws AuthenticationFailedException {
            logger.trace("authenticate: {}", inAuth);

            UsernamePasswordAuthentication upa = (UsernamePasswordAuthentication) inAuth;
            String login = upa.getUsername();
            String password = upa.getPassword();

            if (!autheticate(login, password)) {
                throw new AuthenticationFailedException();
            }

            return new FTPUser(login);
        }

        private boolean autheticate(String login, String password) {
            boolean result = false;

            if (login != null && password != null) {

                UsernamePassword credential = authenticationData.get(login);

                if (credential != null) {
                    if (login.equals(credential.getUsername()) && password.equals(credential.getPassword())) {
                        return true;
                    }
                }
            }
            return result;
        }

        @Override
        public User getUserByName(final String login) throws FtpException {
            logger.trace("getUserByName: {}", login);
            return new FTPUser(login);
        }

        @Override
        public void delete(String arg0) throws FtpException {
            logger.trace("delete: {}", arg0);
        }

        @Override
        public boolean doesExist(String arg0) throws FtpException {
            logger.trace("doesExist: {}", arg0);
            return false;
        }

        @Override
        public String getAdminName() throws FtpException {
            logger.trace("getAdminName");
            return null;
        }

        @Override
        public String[] getAllUserNames() throws FtpException {
            logger.trace("getAllUserNames");
            return null;
        }

        @Override
        public boolean isAdmin(String arg0) throws FtpException {
            logger.trace("isAdmin: {}", arg0);
            return false;
        }

        @Override
        public void save(User arg0) throws FtpException {
            logger.trace("save: {}", arg0);
        }
    }

    private class MyFileSystemView implements FileSystemView {

        MyFtpFile file = new MyFtpFile();

        @Override
        public boolean changeWorkingDirectory(String arg0) throws FtpException {
            logger.trace("changeWorkingDirectory: {}", arg0);
            return true;
        }

        @Override
        public void dispose() {
            logger.trace("dispose");
        }

        @Override
        public FtpFile getFile(String arg0) throws FtpException {
            logger.trace("getFile: {}", arg0);
            return file;
        }

        @Override
        public FtpFile getHomeDirectory() throws FtpException {
            logger.trace("getHomeDirectory");
            return new MyFtpFile();
        }

        @Override
        public FtpFile getWorkingDirectory() throws FtpException {
            logger.trace("getWorkingDirectory");
            return new MyFtpFile();
        }

        @Override
        public boolean isRandomAccessible() throws FtpException {
            logger.trace("isRandomAccessible");
            return false;
        }

    }

    private class MyFtpFile implements FtpFile {
        MyOutputStream file;

        public byte[] getData() {
            return file.getData();
        }

        @Override
        public InputStream createInputStream(long arg0) throws IOException {
            logger.trace("createInputStream: {}", arg0);
            return null;
        }

        @Override
        public OutputStream createOutputStream(long arg0) throws IOException {
            logger.trace("createOutputStream: {}", arg0);
            file = new MyOutputStream();
            return file;
        }

        @Override
        public boolean delete() {
            logger.trace("delete");
            return false;
        }

        @Override
        public boolean doesExist() {
            logger.trace("doesExist");
            return false;
        }

        @Override
        public String getAbsolutePath() {
            logger.trace("getAbsolutePath");
            return "/";
        }

        @Override
        public String getGroupName() {
            logger.trace("getGroupName");
            return null;
        }

        @Override
        public long getLastModified() {
            logger.trace("getLastModified");
            return 0;
        }

        @Override
        public int getLinkCount() {
            logger.trace("getLinkCount");
            return 0;
        }

        @Override
        public String getName() {
            logger.trace("getName");
            return "";
        }

        @Override
        public String getOwnerName() {
            logger.trace("getOwnerName");
            return null;
        }

        @Override
        public long getSize() {
            logger.trace("getSize");
            return 0;
        }

        @Override
        public boolean isDirectory() {
            logger.trace("isDirectory");
            return false;
        }

        @Override
        public boolean isFile() {
            logger.trace("isFile");
            return false;
        }

        @Override
        public boolean isHidden() {
            logger.trace("isHidden");
            return false;
        }

        @Override
        public boolean isReadable() {
            logger.trace("isReadable");
            return false;
        }

        @Override
        public boolean isRemovable() {
            logger.trace("isRemovable");
            return false;
        }

        @Override
        public boolean isWritable() {
            logger.trace("isWritable");
            return true;
        }

        @Override
        public List<FtpFile> listFiles() {
            logger.trace("listFiles");
            return null;
        }

        @Override
        public boolean mkdir() {
            logger.trace("mkdir");
            return false;
        }

        @Override
        public boolean move(FtpFile arg0) {
            logger.trace("move: {}", arg0);
            return false;
        }

        @Override
        public boolean setLastModified(long arg0) {
            logger.trace("setLastModified: {}", arg0);
            return false;
        }

    }

    private class MyOutputStream extends OutputStream {

        private StringBuilder data = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            data.append(String.format("%02X", (byte) b));
        }

        public byte[] getData() {
            try {
                byte[] d = DatatypeConverter.parseHexBinary(data.toString());
                logger.debug("File len: {}", d.length);
                return d;
            } catch (Exception e) {
                logger.debug("Exception occured during data conversion: ", e.getMessage());
            }
            return null;
        }
    }
}
