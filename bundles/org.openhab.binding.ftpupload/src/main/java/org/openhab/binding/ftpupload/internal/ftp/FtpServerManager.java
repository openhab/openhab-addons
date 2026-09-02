/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.ftpupload.internal.ftp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.DataConnectionConfiguration;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerConfigurationException;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContext;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple FTP server implementation to receive files via FTP.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class FtpServerManager {

    public static final int DEFAULT_PORT = 2121;

    private final Logger logger = LoggerFactory.getLogger(FtpServerManager.class);

    private int port = DEFAULT_PORT;
    private @Nullable DataConnectionConfiguration dataConnectionConfiguration;
    int idleTimeout;

    private @Nullable FtpServer server;
    private List<FtpServerEventListener> listeners;
    private MyFTPLet myFTPLet = new MyFTPLet();
    private FTPUserManager ftpUserManager;
    private String ftpStartUpErrorReason = "";

    public FtpServerManager() {
        listeners = new ArrayList<>();
        ftpUserManager = new FTPUserManager();
    }

    public void startServer(int port, int idleTimeout, DataConnectionConfiguration dataConnectionConfiguration)
            throws FtpException {
        stopServer();
        this.port = port;
        this.idleTimeout = idleTimeout;
        this.dataConnectionConfiguration = dataConnectionConfiguration;
        ftpUserManager.setIdleTimeout(idleTimeout);
        initServer();
    }

    public void stopServer() {
        FtpServer server = this.server;
        if (server != null) {
            server.stop();
        }
    }

    public String getStartUpErrorReason() {
        return ftpStartUpErrorReason;
    }

    public synchronized void addEventListener(FtpServerEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void addAuthenticationCredentials(String username, String password)
            throws IllegalArgumentException {
        ftpUserManager.addAuthenticationCredentials(username, password);
    }

    public synchronized void removeAuthenticationCredentials(String username) {
        ftpUserManager.removeAuthenticationCredentials(username);
    }

    public synchronized void removeEventListener(FtpServerEventListener listener) {
        listeners.remove(listener);
    }

    private void sendMsgToListeners(String userName, String filename, byte[] data) {
        Iterator<FtpServerEventListener> iterator = listeners.iterator();

        while (iterator.hasNext()) {
            try {
                iterator.next().fileReceived(userName, filename, data);
            } catch (Exception e) {
                // catch all exceptions give all handlers a fair chance of handling the messages
                logger.debug("Event listener invoking error: {}", e.getMessage());
            }
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

    private void initServer() throws FtpException {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();

        listenerFactory.setPort(port);
        listenerFactory.setIdleTimeout(idleTimeout);
        listenerFactory.setDataConnectionConfiguration(dataConnectionConfiguration);

        Listener listener = listenerFactory.createListener();

        serverFactory.addListener("default", listener);

        Map<String, Ftplet> ftplets = new LinkedHashMap<>();
        myFTPLet = new MyFTPLet();

        ftplets.put("ftplet", myFTPLet);

        serverFactory.setFtplets(ftplets);
        serverFactory.setFileSystem(new FileSystemFactory() {
            @Override
            public FileSystemView createFileSystemView(@NonNullByDefault({}) User user) throws FtpException {
                logger.debug("createFileSystemView: {}", user.getName());
                return new SimpleFileSystemView();
            }
        });

        // set the user manager
        serverFactory.setUserManager(ftpUserManager);
        FtpServer server = serverFactory.createServer();

        try {
            server.start();
            this.server = server;
            ftpStartUpErrorReason = "";
        } catch (FtpException | FtpServerConfigurationException e) {
            ftpStartUpErrorReason = "Failed to start FTP server";
            String message = e.getMessage();
            if (message != null && !message.isEmpty()) {
                ftpStartUpErrorReason += ": " + message;
            }
            throw e;
        }
    }

    @NonNullByDefault({})
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
            logger.trace("destroy");
        }

        @Override
        public FtpletResult onConnect(FtpSession session) throws FtpException, IOException {
            logger.debug("User connected to FtpServer");
            return super.onConnect(session);
        }

        @Override
        public FtpletResult onUploadEnd(final FtpSession session, final FtpRequest request)
                throws FtpException, IOException {
            String userRoot = session.getUser().getHomeDirectory();
            String currDir = session.getFileSystemView().getWorkingDirectory().getAbsolutePath();
            String fileName = request.getArgument();

            logger.debug("File {} upload to FTP server", userRoot + currDir + "/" + fileName);

            SimpleFtpFile file = (SimpleFtpFile) session.getFileSystemView().getFile(fileName);
            byte[] data = file.getData();

            sendMsgToListeners(session.getUser().getName(), fileName, data);
            return FtpletResult.SKIP;
        }
    }
}
