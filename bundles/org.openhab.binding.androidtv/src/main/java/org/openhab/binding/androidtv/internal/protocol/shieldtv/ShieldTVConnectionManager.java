/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.shieldtv;

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.androidtv.internal.ShieldTVHandler;
import org.openhab.binding.androidtv.internal.utils.AndroidTVPKI;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShieldTVConnectionManager} is responsible for handling connections via the shieldtv protocol
 *
 * Significant portions reused from Lutron binding with permission from Bob A.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVConnectionManager {
    private static final int DEFAULT_RECONNECT_SECONDS = 60;
    private static final int DEFAULT_HEARTBEAT_SECONDS = 5;
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 30;
    private static final String DEFAULT_KEYSTORE_PASSWORD = "secret";
    private static final int DEFAULT_PORT = 8987;

    private static final String STATUS_INITIALIZING = "Initializing";

    private final Logger logger = LoggerFactory.getLogger(ShieldTVConnectionManager.class);

    private ScheduledExecutorService scheduler;

    private final ShieldTVHandler handler;
    private ShieldTVConfiguration config;

    private @NonNullByDefault({}) SSLSocketFactory sslSocketFactory;
    private @Nullable SSLSocket sslSocket;
    private @Nullable BufferedWriter writer;
    private @Nullable BufferedReader reader;

    private @NonNullByDefault({}) SSLServerSocketFactory sslServerSocketFactory;
    private @Nullable Socket shimServerSocket;
    private @Nullable BufferedWriter shimWriter;
    private @Nullable BufferedReader shimReader;

    private @NonNullByDefault({}) ShieldTVMessageParser messageParser;

    private final BlockingQueue<ShieldTVCommand> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<ShieldTVCommand> shimQueue = new LinkedBlockingQueue<>();

    private @Nullable Future<?> asyncInitializeTask;
    private @Nullable Future<?> shimAsyncInitializeTask;

    private @Nullable Thread senderThread;
    private @Nullable Thread readerThread;
    private @Nullable Thread shimSenderThread;
    private @Nullable Thread shimReaderThread;

    private @Nullable ScheduledFuture<?> keepAliveJob;
    private @Nullable ScheduledFuture<?> keepAliveReconnectJob;
    private @Nullable ScheduledFuture<?> connectRetryJob;
    private final Object keepAliveReconnectLock = new Object();

    private StringBuffer sbReader = new StringBuffer();
    private StringBuffer sbShimReader = new StringBuffer();
    private String lastMsg = "";
    private String thisMsg = "";
    private boolean inMessage = false;
    private String msgType = "";

    private boolean isLoggedIn = false;

    private String hostName = "";
    private String currentApp = "";
    private String deviceId = "";
    private String arch = "";

    private AndroidTVPKI androidtvPKI = new AndroidTVPKI();
    private byte[] encryptionKey;

    private Map<String, String> appNameDB = new HashMap<>();
    private Map<String, String> appURLDB = new HashMap<>();

    public ShieldTVConnectionManager(ShieldTVHandler handler, ShieldTVConfiguration config) {
        messageParser = new ShieldTVMessageParser(this);
        this.config = config;
        this.handler = handler;
        this.scheduler = handler.getScheduler();
        this.encryptionKey = androidtvPKI.generateEncryptionKey();
        initalize();
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        handler.setThingProperty("Device Name", hostName);
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setDeviceID(String deviceId) {
        this.deviceId = deviceId;
        handler.setThingProperty("Device ID", deviceId);
    }

    public String getDeviceID() {
        return this.deviceId;
    }

    public void setArch(String arch) {
        this.arch = arch;
        handler.setThingProperty("Architectures", arch);
    }

    public String getArch() {
        return this.arch;
    }

    public void setCurrentApp(String currentApp) {
        this.currentApp = currentApp;
        handler.updateChannelState(CHANNEL_APP, new StringType(currentApp));

        String appName = "";
        String appURL = "";

        try {
            appName = appNameDB.get(currentApp);
            appURL = appURLDB.get(currentApp);
        } catch (NullPointerException e) {
            logger.debug("Null Pointer Exception", e);
            logger.info("Unknown Android App: {}", currentApp);
        } finally {
            handler.updateChannelState(CHANNEL_APPNAME, new StringType(appName));
            handler.updateChannelState(CHANNEL_APPURL, new StringType(appURL));
        }
    }

    public String getCurrentApp() {
        return this.currentApp;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
        if (!this.isLoggedIn) {
            // Only run this if we aren't already logged in
            handler.checkThingStatus();
            sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("080b120308cd08"))); // Get Hostname
            sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08f30712020805"))); // No Reply
            sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08f10712020800"))); // Get App DB
        }
    }

    public boolean getLoggedIn() {
        return this.isLoggedIn;
    }

    public void setKeys(String privKey, String cert) {
        try {
            androidtvPKI.setKeys(privKey, encryptionKey, cert);
            androidtvPKI.saveKeyStore(config.keystorePassword, encryptionKey);
        } catch (GeneralSecurityException e) {
            logger.debug("General security exception", e);
        } catch (IOException e) {
            logger.debug("IO Exception", e);
        } catch (Exception e) {
            logger.debug("General Exception", e);

        }
    }

    public void setAppDB(Map<String, String> appNameDB, Map<String, String> appURLDB) {
        this.appNameDB = appNameDB;
        this.appURLDB = appURLDB;
        logger.debug("App DB Populated");
        logger.trace("Handler appNameDB: {} appURLDB: {}", this.appNameDB.toString(), this.appURLDB.toString());
    }

    private TrustManager[] defineNoOpTrustManager() {
        return new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType) {
                logger.debug("Assuming client certificate is valid");
                if (chain != null && logger.isTraceEnabled()) {
                    for (int cert = 0; cert < chain.length; cert++) {
                        logger.trace("Subject DN: {}", chain[cert].getSubjectX500Principal());
                        logger.trace("Issuer DN: {}", chain[cert].getIssuerX500Principal());
                        logger.trace("Serial number {}:", chain[cert].getSerialNumber());
                    }
                }
            }

            @Override
            public void checkServerTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType) {
                logger.debug("Assuming server certificate is valid");
                if (chain != null && logger.isTraceEnabled()) {
                    for (int cert = 0; cert < chain.length; cert++) {
                        logger.trace("Subject DN: {}", chain[cert].getSubjectX500Principal());
                        logger.trace("Issuer DN: {}", chain[cert].getIssuerX500Principal());
                        logger.trace("Serial number: {}", chain[cert].getSerialNumber());
                    }
                }
            }

            @Override
            public X509Certificate @Nullable [] getAcceptedIssuers() {
                return null;
            }
        } };
    }

    private void initalize() {
        SSLContext sslContext;

        String folderName = OpenHAB.getUserDataFolder() + "/androidtv";
        File folder = new File(folderName);

        if (!folder.exists()) {
            logger.debug("Creating directory {}", folderName);
            folder.mkdirs();
        }

        config.port = (config.port > 0) ? config.port : DEFAULT_PORT;
        config.reconnect = (config.reconnect > 0) ? config.reconnect : DEFAULT_RECONNECT_SECONDS;
        config.heartbeat = (config.heartbeat > 0) ? config.heartbeat : DEFAULT_HEARTBEAT_SECONDS;
        config.delay = (config.delay < 0) ? 0 : config.delay;
        config.shim = (config.shim) ? true : false;
        config.shimNewKeys = (config.shimNewKeys) ? true : false;

        config.keystoreFileName = (!config.keystoreFileName.equals("")) ? config.keystoreFileName
                : folderName + "/shieldtv." + ((config.shim) ? "shim." : "") + handler.getThing().getUID().getId()
                        + ".keystore";
        config.keystorePassword = (!config.keystorePassword.equals("")) ? config.keystorePassword
                : DEFAULT_KEYSTORE_PASSWORD;

        androidtvPKI.setKeystoreFileName(config.keystoreFileName);
        androidtvPKI.setAlias("nvidia");

        try {
            File keystoreFile = new File(config.keystoreFileName);

            if (!keystoreFile.exists() || config.shimNewKeys) {
                androidtvPKI.generateNewKeyPair(encryptionKey);
                androidtvPKI.saveKeyStore(config.keystorePassword, this.encryptionKey);
            } else {
                androidtvPKI.loadFromKeyStore(config.keystorePassword, this.encryptionKey);
            }

            logger.trace("Initializing SSL Context");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(androidtvPKI.getKeyStore(config.keystorePassword, this.encryptionKey),
                    config.keystorePassword.toCharArray());

            TrustManager[] trustManagers = defineNoOpTrustManager();

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustManagers, null);

            sslSocketFactory = sslContext.getSocketFactory();
            if (!config.shim) {
                asyncInitializeTask = scheduler.submit(this::connect);
            } else {
                shimAsyncInitializeTask = scheduler.submit(this::shimInitalize);
            }

        } catch (NoSuchAlgorithmException | IOException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Error initializing keystore");
            logger.debug("Error initializing keystore", e);
        } catch (UnrecoverableKeyException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Key unrecoverable with supplied password");
        } catch (GeneralSecurityException e) {
            logger.debug("General security exception", e);
        } catch (Exception e) {
            logger.debug("General exception", e);
        }
    }

    public synchronized void connect() {

        try {
            logger.debug("Opening ShieldTV SSL connection to {}:{}", config.ipAddress, config.port);
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(config.ipAddress, config.port);
            sslSocket.startHandshake();
            writer = new BufferedWriter(
                    new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.ISO_8859_1));
            reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.ISO_8859_1));
            this.sslSocket = sslSocket;
        } catch (UnknownHostException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
            return;
        } catch (IllegalArgumentException e) {
            // port out of valid range
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid port number");
            return;
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while establishing ShieldTV connection");
            Thread.currentThread().interrupt();
            return;
        } catch (IOException e) {
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error opening ShieldTV SSL connection. Check log.");
            logger.info("Error opening ShieldTV SSL connection: {}", e.getMessage());
            disconnect(false);
            scheduleConnectRetry(config.reconnect); // Possibly a temporary problem. Try again later.
            return;
        }

        handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, STATUS_INITIALIZING);

        Thread readerThread = new Thread(this::readerThreadJob, "ShieldTV reader");
        readerThread.setDaemon(true);
        readerThread.start();
        this.readerThread = readerThread;

        Thread senderThread = new Thread(this::senderThreadJob, "ShieldTV sender");
        senderThread.setDaemon(true);
        senderThread.start();
        this.senderThread = senderThread;

        if (!config.shim) {
            logger.debug("Starting ShieldTV keepalive job with interval {}", config.heartbeat);
            keepAliveJob = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, config.heartbeat, config.heartbeat,
                    TimeUnit.SECONDS);

            String login = ShieldTVRequest.encodeMessage(ShieldTVRequest.loginRequest());
            sendCommand(new ShieldTVCommand(login));
        }
    }

    public synchronized void shimInitalize() {

        AndroidTVPKI shimPKI = new AndroidTVPKI();
        byte[] shimEncryptionKey = shimPKI.generateEncryptionKey();
        SSLContext sslContext;

        try {
            shimPKI.generateNewKeyPair(shimEncryptionKey);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(shimPKI.getKeyStore(config.keystorePassword, shimEncryptionKey),
                    config.keystorePassword.toCharArray());
            TrustManager[] trustManagers = defineNoOpTrustManager();
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustManagers, null);
            this.sslServerSocketFactory = sslContext.getServerSocketFactory();

            logger.debug("Opening ShieldTV shim on port {}", config.port);
            ServerSocket sslServerSocket = this.sslServerSocketFactory.createServerSocket(config.port);

            while (true) {
                logger.debug("Waiting for shim connection...");
                Socket serverSocket = sslServerSocket.accept();
                disconnect(true);
                connect();
                SSLSession session = ((SSLSocket) serverSocket).getSession();
                Certificate[] cchain2 = session.getLocalCertificates();
                for (int i = 0; i < cchain2.length; i++) {
                    logger.trace("Connection from: {}", ((X509Certificate) cchain2[i]).getSubjectX500Principal());
                }

                logger.trace("Peer host is {}", session.getPeerHost());
                logger.trace("Cipher is {}", session.getCipherSuite());
                logger.trace("Protocol is {}", session.getProtocol());
                logger.trace("ID is {}", new BigInteger(session.getId()));
                logger.trace("Session created in {}", session.getCreationTime());
                logger.trace("Session accessed in {}", session.getLastAccessedTime());

                shimWriter = new BufferedWriter(
                        new OutputStreamWriter(serverSocket.getOutputStream(), StandardCharsets.ISO_8859_1));
                shimReader = new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream(), StandardCharsets.ISO_8859_1));
                this.shimServerSocket = serverSocket;

                Thread readerThread = new Thread(this::shimReaderThreadJob, "ShieldTV shim reader");
                readerThread.setDaemon(true);
                readerThread.start();
                this.shimReaderThread = readerThread;

                Thread senderThread = new Thread(this::shimSenderThreadJob, "ShieldTV shim sender");
                senderThread.setDaemon(true);
                senderThread.start();
                this.shimSenderThread = senderThread;

            }
        } catch (Exception e) {
            logger.trace("Shim initalization exception", e);
            return;
        }
    }

    private void scheduleConnectRetry(long waitSeconds) {
        logger.debug("Scheduling ShieldTV connection retry in {} seconds", waitSeconds);
        connectRetryJob = scheduler.schedule(this::connect, waitSeconds, TimeUnit.SECONDS);
    }

    /**
     * Disconnect from bridge, cancel retry and keepalive jobs, stop reader and writer threads, and
     * clean up.
     *
     * @param interruptAll Set if reconnect task should be interrupted if running. Should be false when calling from
     *            connect or reconnect, and true when calling from dispose.
     */
    private synchronized void disconnect(boolean interruptAll) {
        logger.debug("Disconnecting ShieldTV");

        ScheduledFuture<?> connectRetryJob = this.connectRetryJob;
        if (connectRetryJob != null) {
            connectRetryJob.cancel(true);
        }
        ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
        if (keepAliveJob != null) {
            keepAliveJob.cancel(true);
        }

        Thread senderThread = this.senderThread;
        if (senderThread != null && senderThread.isAlive()) {
            senderThread.interrupt();
        }

        Thread readerThread = this.readerThread;
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }

        Thread shimSenderThread = this.shimSenderThread;
        if (shimSenderThread != null && shimSenderThread.isAlive()) {
            shimSenderThread.interrupt();
        }

        Thread shimReaderThread = this.shimReaderThread;
        if (shimReaderThread != null && shimReaderThread.isAlive()) {
            shimReaderThread.interrupt();
        }

        SSLSocket sslSocket = this.sslSocket;
        if (sslSocket != null) {
            try {
                sslSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing ShieldTV SSL socket: {}", e.getMessage());
            }
            this.sslSocket = null;
        }
        BufferedReader reader = this.reader;
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.debug("Error closing reader: {}", e.getMessage());
            }
        }
        BufferedWriter writer = this.writer;
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.debug("Error closing writer: {}", e.getMessage());
            }
        }

        Socket shimServerSocket = this.shimServerSocket;
        if (shimServerSocket != null) {
            try {
                shimServerSocket.close();
            } catch (IOException e) {
                logger.debug("Error closing ShieldTV SSL socket: {}", e.getMessage());
            }
            this.shimServerSocket = null;
        }
        BufferedReader shimReader = this.shimReader;
        if (shimReader != null) {
            try {
                shimReader.close();
            } catch (IOException e) {
                logger.debug("Error closing shimReader: {}", e.getMessage());
            }
        }
        BufferedWriter shimWriter = this.shimWriter;
        if (shimWriter != null) {
            try {
                shimWriter.close();
            } catch (IOException e) {
                logger.debug("Error closing shimWriter: {}", e.getMessage());
            }
        }
    }

    private synchronized void reconnect() {
        logger.debug("Attempting to reconnect to the ShieldTV");
        isLoggedIn = false;
        handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "reconnecting");
        disconnect(false);
        connect();
    }

    /**
     * Method executed by the message sender thread (senderThread)
     */
    private void senderThreadJob() {
        logger.debug("Command sender thread started");
        try {
            while (!Thread.currentThread().isInterrupted() && writer != null) {
                ShieldTVCommand command = sendQueue.take();
                // logger.trace("Sending ShieldTV command {}", command);

                try {
                    BufferedWriter writer = this.writer;
                    if (writer != null) {
                        logger.trace("Raw ShieldTV command decodes as: {}",
                                ShieldTVRequest.decodeMessage(command.toString()));
                        writer.write(command.toString());
                        writer.flush();
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted while sending to ShieldTV");
                    handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Interrupted");
                    break; // exit loop and terminate thread
                } catch (IOException e) {
                    logger.warn("Communication error, will try to reconnect ShieldTV. Error: {}", e.getMessage());
                    handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    sendQueue.add(command); // Requeue command
                    reconnect();
                    break; // reconnect() will start a new thread; terminate this one
                }
                if (config.delay > 0) {
                    Thread.sleep(config.delay); // introduce delay to throttle send rate
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("Command sender thread exiting");
        }
    }

    private void shimSenderThreadJob() {
        logger.debug("Shim sender thread started");
        try {
            while (!Thread.currentThread().isInterrupted() && shimWriter != null) {
                ShieldTVCommand command = shimQueue.take();

                try {
                    BufferedWriter writer = this.shimWriter;
                    if (writer != null) {
                        logger.trace("Shim received from shield: {}",
                                ShieldTVRequest.decodeMessage(command.toString()));
                        writer.write(command.toString());
                        writer.flush();
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Shim interrupted while sending.");
                    break; // exit loop and terminate thread
                } catch (IOException e) {
                    logger.warn("Shim communication error. Error: {}", e.getMessage());
                    break; // reconnect() will start a new thread; terminate this one
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            logger.debug("Command sender thread exiting");
        }
    }

    private void flushReader() {
        if (!inMessage && (sbReader.length() > 0)) {
            sbReader.setLength(sbReader.length() - 2);
            messageParser.handleMessage(sbReader.toString());
            if (config.shim) {
                sendShim(new ShieldTVCommand(ShieldTVRequest.encodeMessage(sbReader.toString())));
            }
            sbReader.setLength(0);
            sbReader.append(lastMsg.toString());
        }
        sbReader.append(thisMsg.toString());
        lastMsg = thisMsg;
    }

    private void finishReaderMessage() {
        sbReader.append(thisMsg.toString());
        lastMsg = "";
        inMessage = false;
        messageParser.handleMessage(sbReader.toString());
        if (config.shim) {
            sendShim(new ShieldTVCommand(ShieldTVRequest.encodeMessage(sbReader.toString())));
        }
        sbReader.setLength(0);
    }

    private String fixMessage(String tempMsg) {
        if (tempMsg.length() % 2 > 0) {
            tempMsg = "0" + tempMsg;
        }
        return tempMsg;
    }

    /**
     * Method executed by the message reader thread (readerThread)
     */
    private void readerThreadJob() {
        logger.debug("Message reader thread started");
        try {
            BufferedReader reader = this.reader;
            while (!Thread.interrupted() && reader != null) {
                thisMsg = fixMessage(Integer.toHexString(reader.read()));
                if (thisMsg.equals("ffffffff")) {
                    // Shield has crashed the connection. Disconnect hard.
                    reconnect();
                    break;
                }
                if (lastMsg.equals("08") && !inMessage) {
                    flushReader();
                    inMessage = true;
                    msgType = thisMsg;
                } else if (lastMsg.equals("18") && thisMsg.equals(msgType) && inMessage) {
                    if (!msgType.startsWith("0")) {
                        sbReader.append(thisMsg.toString());
                        thisMsg = fixMessage(Integer.toHexString(reader.read()));
                    }
                    finishReaderMessage();
                } else if (msgType.equals("00") && (sbReader.toString().length() == 16)) {
                    // keepalive messages don't have delimiters but are always 18 in length
                    finishReaderMessage();
                } else {
                    sbReader.append(thisMsg.toString());
                    lastMsg = thisMsg;
                }
            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "I/O Error");
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Runtime exception");
        } finally {
            logger.debug("Message reader thread exiting");
        }
    }

    private void shimReaderThreadJob() {
        logger.debug("Shim reader thread started");
        String thisShimMsg = "";
        int thisShimRawMsg = 0;
        int payloadRemain = 0;
        int payloadBlock = 0;
        String thisShimMsgType = "";
        boolean inShimMessage = false;
        try {
            BufferedReader reader = this.shimReader;
            while (!Thread.interrupted() && reader != null) {
                thisShimRawMsg = reader.read();
                thisShimMsg = fixMessage(Integer.toHexString(thisShimRawMsg));
                if (thisShimMsg.equals("ffffffff")) {
                    disconnect(true);
                    break;
                }
                if (!inShimMessage) {
                    // Beginning of payload
                    sbShimReader.setLength(0);
                    sbShimReader.append(thisShimMsg.toString());
                    inShimMessage = true;
                    payloadBlock++;
                } else if ((payloadBlock == 1) && (thisShimMsg.equals("00"))) {
                    sbShimReader.append(thisShimMsg.toString());
                    payloadRemain = 8;
                    thisShimMsgType = thisShimMsg;
                    while (payloadRemain > 1) {
                        thisShimMsg = fixMessage(Integer.toHexString(reader.read()));
                        sbShimReader.append(thisShimMsg.toString());
                        payloadRemain--;
                        payloadBlock++;
                    }
                    payloadRemain--;
                    payloadBlock++;
                } else if ((payloadBlock == 1) && (thisShimMsg.startsWith("f1") || thisShimMsg.startsWith("f3"))) {
                    sbShimReader.append(thisShimMsg.toString());
                    payloadRemain = 6;
                    thisShimMsgType = thisShimMsg;
                    while (payloadRemain > 1) {
                        thisShimMsg = fixMessage(Integer.toHexString(reader.read()));
                        sbShimReader.append(thisShimMsg.toString());
                        payloadRemain--;
                        payloadBlock++;
                    }
                    payloadRemain--;
                    payloadBlock++;
                } else if (payloadBlock == 1) {
                    thisShimMsgType = thisShimMsg;
                    sbShimReader.append(thisShimMsg.toString());
                    payloadBlock++;
                } else if (payloadBlock == 2) {
                    sbShimReader.append(thisShimMsg.toString());
                    payloadBlock++;
                } else if (payloadBlock == 3) {
                    // Length of remainder of packet
                    payloadRemain = thisShimRawMsg;
                    sbShimReader.append(thisShimMsg.toString());
                    payloadBlock++;
                } else if (payloadBlock == 4) {
                    sbShimReader.append(thisShimMsg.toString());
                    if (thisShimMsgType.equals("e9") || thisShimMsgType.equals("f0")) {
                        payloadRemain = thisShimRawMsg + 1;
                    }
                    while (payloadRemain > 1) {
                        thisShimMsg = fixMessage(Integer.toHexString(reader.read()));
                        sbShimReader.append(thisShimMsg.toString());
                        payloadRemain--;
                        payloadBlock++;
                    }
                    payloadRemain--;
                    payloadBlock++;

                }

                if ((payloadBlock > 5) && (payloadRemain == 0)) {
                    logger.trace("Shim sending to shield: {}", sbShimReader.toString());
                    sendQueue.add(new ShieldTVCommand(ShieldTVRequest.encodeMessage(sbShimReader.toString())));
                    inShimMessage = false;
                    payloadBlock = 0;
                    payloadRemain = 0;
                    sbShimReader.setLength(0);
                }

            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "I/O Error");
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            handler.updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Runtime exception");
        } finally {
            logger.debug("Message reader thread exiting");
        }
    }

    private void sendKeepAlive() {
        logger.trace("Sending ShieldTV keepalive query");
        String keepalive = ShieldTVRequest.encodeMessage(ShieldTVRequest.keepAlive());
        sendCommand(new ShieldTVCommand(keepalive));
        reconnectTaskSchedule();
    }

    /**
     * Schedules the reconnect task keepAliveReconnectJob to execute in KEEPALIVE_TIMEOUT_SECONDS. This should
     * be
     * cancelled by calling reconnectTaskCancel() if a valid response is received from the bridge.
     */
    private void reconnectTaskSchedule() {
        synchronized (keepAliveReconnectLock) {
            keepAliveReconnectJob = scheduler.schedule(this::keepAliveTimeoutExpired, KEEPALIVE_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Cancels the reconnect task keepAliveReconnectJob.
     */
    private void reconnectTaskCancel(boolean interrupt) {
        synchronized (keepAliveReconnectLock) {
            ScheduledFuture<?> keepAliveReconnectJob = this.keepAliveReconnectJob;
            if (keepAliveReconnectJob != null) {
                logger.trace("Canceling ShieldTV scheduled reconnect job.");
                keepAliveReconnectJob.cancel(interrupt);
                this.keepAliveReconnectJob = null;
            }
        }
    }

    /**
     * Executed by keepAliveReconnectJob if it is not cancelled by the LEAP message parser calling
     * validMessageReceived() which in turn calls reconnectTaskCancel().
     */
    private void keepAliveTimeoutExpired() {
        logger.debug("ShieldTV keepalive response timeout expired. Initiating reconnect.");
        reconnect();
    }

    public void validMessageReceived() {
        reconnectTaskCancel(true); // Got a good message, so cancel reconnect task.
    }

    public void sendCommand(ShieldTVCommand command) {
        if ((!config.shim) && (!command.toString().equals(""))) {
            sendQueue.add(command);
        }
    }

    public void sendShim(ShieldTVCommand command) {
        if (!command.toString().equals("")) {
            shimQueue.add(command);
        }
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Command received: {}", channelUID.getId().toString());

        if (CHANNEL_KEYPRESS.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                // All key presses require a down and up command to simulate the button being pushed down and then back
                // up
                // This probably needs to be handled separately for remote controls TODO
                switch (command.toString()) {
                    case "KEY_UP":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202ce01")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202ce01")));
                        break;
                    case "KEY_DOWN":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d801")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d801")));
                        break;
                    case "KEY_RIGHT":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d401")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d401")));
                        break;
                    case "KEY_LEFT":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d201")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d201")));
                        break;
                    case "KEY_ENTER":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202c205")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202c205")));
                        break;
                    case "KEY_HOME":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d802")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d802")));
                        break;
                    case "KEY_BACK":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202bc02")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202bc02")));
                        break;
                    case "KEY_MENU":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a280132029602")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a280232029602")));
                        break;
                    case "KEY_PLAYPAUSE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202F604")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202F604")));
                        break;
                    case "KEY_REWIND":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202D002")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202D002")));
                        break;
                    case "KEY_FORWARD":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202A003")));
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202A003")));
                        break;
                    case "KEY_UP_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202ce01")));
                        break;
                    case "KEY_DOWN_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d801")));
                        break;
                    case "KEY_RIGHT_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d401")));
                        break;
                    case "KEY_LEFT_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d201")));
                        break;
                    case "KEY_ENTER_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202c205")));
                        break;
                    case "KEY_HOME_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202d802")));
                        break;
                    case "KEY_BACK_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202bc02")));
                        break;
                    case "KEY_MENU_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a280132029602")));
                        break;
                    case "KEY_PLAYPAUSE_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202F604")));
                        break;
                    case "KEY_REWIND_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202D002")));
                        break;
                    case "KEY_FORWARD_PRESS":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28013202A003")));
                        break;
                    case "KEY_UP_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202ce01")));
                        break;
                    case "KEY_DOWN_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d801")));
                        break;
                    case "KEY_RIGHT_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d401")));
                        break;
                    case "KEY_LEFT_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d201")));
                        break;
                    case "KEY_ENTER_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202c205")));
                        break;
                    case "KEY_HOME_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202d802")));
                        break;
                    case "KEY_BACK_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202bc02")));
                        break;
                    case "KEY_MENU_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a280232029602")));
                        break;
                    case "KEY_PLAYPAUSE_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202F604")));
                        break;
                    case "KEY_REWIND_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202D002")));
                        break;
                    case "KEY_FORWARD_RELEASE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08e907120c08141001200a28023202A003")));
                        break;
                    case "KEY_POWER":
                        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08e907120808141005201e401e")));
                        break;
                    case "KEY_POWERON":
                        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08e907120808141005201e4010")));
                        break;
                    case "KEY_GOOGLE":
                        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08e907120808141005201e401f")));
                        break;
                    case "KEY_VOLUP":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08f007120c08031208080110031a020102")));
                        break;
                    case "KEY_VOLDOWN":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08f007120c08031208080110011a020102")));
                        break;
                    case "KEY_MUTE":
                        sendCommand(new ShieldTVCommand(
                                ShieldTVRequest.encodeMessage("08f007120c08031208080110021a020102")));
                        break;
                    default:
                        logger.trace("Unknown Keypress: {}", command.toString());
                }
            }
        } else if (CHANNEL_PINCODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (!isLoggedIn) {
                    // Do PIN for shieldtv protocol
                    logger.trace("ShieldTV PIN Process Started");
                    String pin = ShieldTVRequest.pinRequest(command.toString());
                    String message = ShieldTVRequest.encodeMessage(pin);
                    sendCommand(new ShieldTVCommand(message));
                }
            }
        } else if (CHANNEL_RAW.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String message = ShieldTVRequest.encodeMessage(command.toString());
                logger.trace("Raw Message Decodes as: {}", ShieldTVRequest.decodeMessage(message));
                sendCommand(new ShieldTVCommand(message));
            }
        } else if (CHANNEL_RAWMSG.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                messageParser.handleMessage(command.toString());
            }
        } else if (CHANNEL_APP.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String message = ShieldTVRequest.encodeMessage(ShieldTVRequest.startApp(command.toString()));
                sendCommand(new ShieldTVCommand(message));
            }
        }
    }

    public void dispose() {
        Future<?> asyncInitializeTask = this.asyncInitializeTask;
        if (asyncInitializeTask != null && !asyncInitializeTask.isDone()) {
            asyncInitializeTask.cancel(true); // Interrupt async init task if it isn't done yet
        }
        Future<?> shimAsyncInitializeTask = this.shimAsyncInitializeTask;
        if (shimAsyncInitializeTask != null && !shimAsyncInitializeTask.isDone()) {
            shimAsyncInitializeTask.cancel(true); // Interrupt async init task if it isn't done yet
        }
        disconnect(true);
    }
}
