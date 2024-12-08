/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static org.openhab.binding.androidtv.internal.protocol.shieldtv.ShieldTVConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
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
import org.openhab.binding.androidtv.internal.AndroidTVHandler;
import org.openhab.binding.androidtv.internal.AndroidTVTranslationProvider;
import org.openhab.binding.androidtv.internal.utils.AndroidTVPKI;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
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
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 30;
    private static final String DEFAULT_KEYSTORE_PASSWORD = "secret";
    private static final int DEFAULT_PORT = 8987;

    private final Logger logger = LoggerFactory.getLogger(ShieldTVConnectionManager.class);

    private ScheduledExecutorService scheduler;

    private final AndroidTVHandler handler;
    private ShieldTVConfiguration config;
    private final AndroidTVTranslationProvider translationProvider;

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
    private final Object connectionLock = new Object();
    private int periodicUpdate;

    private @Nullable ScheduledFuture<?> deviceHealthJob;
    private boolean isOnline = true;

    private StringBuffer sbReader = new StringBuffer();
    private StringBuffer sbShimReader = new StringBuffer();
    private String lastMsg = "";
    private String thisMsg = "";
    private boolean inMessage = false;
    private String msgType = "";

    private boolean disposing = false;
    private boolean isLoggedIn = false;
    private String statusMessage = "";

    private String hostName = "";
    private String currentApp = "";
    private String deviceId = "";
    private String arch = "";

    private AndroidTVPKI androidtvPKI = new AndroidTVPKI();
    private byte[] encryptionKey;

    private boolean appDBPopulated = false;
    private Map<String, String> appNameDB = new HashMap<>();
    private Map<String, String> appURLDB = new HashMap<>();

    public ShieldTVConnectionManager(AndroidTVHandler handler, ShieldTVConfiguration config) {
        messageParser = new ShieldTVMessageParser(this);
        this.config = config;
        this.handler = handler;
        this.translationProvider = handler.getTranslationProvider();
        this.scheduler = handler.getScheduler();
        this.encryptionKey = androidtvPKI.generateEncryptionKey();
        initialize();
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        handler.setThingProperty("deviceName", hostName);
    }

    public String getHostName() {
        return hostName;
    }

    public String getThingID() {
        return handler.getThingID();
    }

    public void setDeviceID(String deviceId) {
        this.deviceId = deviceId;
        handler.setThingProperty("deviceID", deviceId);
    }

    public String getDeviceID() {
        return deviceId;
    }

    public void setArch(String arch) {
        this.arch = arch;
        handler.setThingProperty("architectures", arch);
    }

    public String getArch() {
        return arch;
    }

    public void setCurrentApp(String currentApp) {
        this.currentApp = currentApp;
        handler.updateChannelState(CHANNEL_APP, new StringType(currentApp));

        if (this.appDBPopulated) {
            String appName = "";
            String appURL = "";

            if (appNameDB.get(currentApp) != null) {
                appName = appNameDB.get(currentApp);
                handler.updateChannelState(CHANNEL_APPNAME, new StringType(appName));
            } else {
                logger.info("Unknown Android App: {}", currentApp);
                handler.updateChannelState(CHANNEL_APPNAME, new StringType(""));
            }

            if (appURLDB.get(currentApp) != null) {
                appURL = appURLDB.get(currentApp);
                handler.updateChannelState(CHANNEL_APPURL, new StringType(appURL));
            } else {
                handler.updateChannelState(CHANNEL_APPURL, new StringType(""));
            }
        }
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    private void setStatus(boolean isLoggedIn) {
        if (isLoggedIn) {
            setStatus(isLoggedIn, "online.online");
        } else {
            setStatus(isLoggedIn, "offline.unknown");
        }
    }

    private void setStatus(boolean isLoggedIn, String statusMessage) {
        String translatedMessage = translationProvider.getText(statusMessage);
        if ((this.isLoggedIn != isLoggedIn) || (!this.statusMessage.equals(translatedMessage))) {
            this.isLoggedIn = isLoggedIn;
            this.statusMessage = translatedMessage;
            handler.checkThingStatus();
        }
    }

    public String getCurrentApp() {
        return currentApp;
    }

    private void sendPeriodicUpdate() {
        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("080b120308cd08"))); // Get Hostname
        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08f30712020805"))); // No Reply
        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08f10712020800"))); // Get App DB
        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08ec0712020806"))); // Get App
    }

    public void setLoggedIn(boolean isLoggedIn) {
        if (!this.isLoggedIn && isLoggedIn) {
            sendPeriodicUpdate();
        }

        if (this.isLoggedIn != isLoggedIn) {
            setStatus(isLoggedIn);
        }
    }

    public boolean getLoggedIn() {
        return isLoggedIn;
    }

    private boolean servicePing() {
        int timeout = 500;

        SocketAddress socketAddress = new InetSocketAddress(config.ipAddress, config.shieldtvPort);
        try (Socket socket = new Socket()) {
            socket.connect(socketAddress, timeout);
            return true;
        } catch (ConnectException | SocketTimeoutException | NoRouteToHostException ignored) {
            return false;
        } catch (IOException ignored) {
            // IOException is thrown by automatic close() of the socket.
            // This should actually never return a value as we should return true above already
            return true;
        }
    }

    private void checkHealth() {
        boolean isOnline;
        if (!isLoggedIn) {
            isOnline = servicePing();
        } else {
            isOnline = true;
        }
        logger.debug("{} - Device Health - Online: {} - Logged In: {}", handler.getThingID(), isOnline, isLoggedIn);
        if (isOnline != this.isOnline) {
            this.isOnline = isOnline;
            if (isOnline) {
                logger.debug("{} - Device is back online.  Attempting reconnection.", handler.getThingID());
                reconnect();
            }
        }
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
        this.appDBPopulated = true;
        logger.debug("{} - App DB Populated", handler.getThingID());
        logger.trace("{} - Handler appNameDB: {} appURLDB: {}", handler.getThingID(), this.appNameDB, this.appURLDB);
        handler.updateCDP(CHANNEL_APP, this.appNameDB);
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
                        logger.trace("Serial number: {}", chain[cert].getSerialNumber());
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

    private void initialize() {
        SSLContext sslContext;

        String folderName = OpenHAB.getUserDataFolder() + "/androidtv";
        File folder = new File(folderName);

        if (!folder.exists()) {
            logger.debug("Creating directory {}", folderName);
            folder.mkdirs();
        }

        config.shieldtvPort = (config.shieldtvPort > 0) ? config.shieldtvPort : DEFAULT_PORT;

        config.keystoreFileName = (!config.keystoreFileName.equals("")) ? config.keystoreFileName
                : folderName + "/shieldtv." + ((config.shim) ? "shim." : "") + handler.getThing().getUID().getId()
                        + ".keystore";
        config.keystorePassword = (!config.keystorePassword.equals("")) ? config.keystorePassword
                : DEFAULT_KEYSTORE_PASSWORD;

        androidtvPKI.setKeystoreFileName(config.keystoreFileName);
        androidtvPKI.setAlias("nvidia");

        deviceHealthJob = scheduler.scheduleWithFixedDelay(this::checkHealth, config.heartbeat, config.heartbeat,
                TimeUnit.SECONDS);

        try {
            File keystoreFile = new File(config.keystoreFileName);

            if (!keystoreFile.exists() || config.shimNewKeys) {
                androidtvPKI.generateNewKeyPair(encryptionKey);
                androidtvPKI.saveKeyStore(config.keystorePassword, this.encryptionKey);
            } else {
                androidtvPKI.loadFromKeyStore(config.keystorePassword, this.encryptionKey);
            }

            logger.trace("{} - Initializing SSL Context", handler.getThingID());
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
                shimAsyncInitializeTask = scheduler.submit(this::shimInitialize);
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            setStatus(false, "offline.error-initalizing-keystore");
            logger.debug("Error initializing keystore", e);
        } catch (UnrecoverableKeyException e) {
            setStatus(false, "offline.key-unrecoverable-with-supplied-password");
        } catch (GeneralSecurityException e) {
            logger.debug("General security exception", e);
        } catch (Exception e) {
            logger.debug("General exception", e);
        }
    }

    public void connect() {
        synchronized (connectionLock) {
            if (isOnline) {
                try {
                    logger.debug("{} - Opening ShieldTV SSL connection to {}:{}", handler.getThingID(),
                            config.ipAddress, config.shieldtvPort);
                    SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(config.ipAddress,
                            config.shieldtvPort);
                    sslSocket.startHandshake();
                    writer = new BufferedWriter(
                            new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.ISO_8859_1));
                    reader = new BufferedReader(
                            new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.ISO_8859_1));
                    this.sslSocket = sslSocket;
                } catch (UnknownHostException e) {
                    setStatus(false, "offline.unknown-host");
                    return;
                } catch (IllegalArgumentException e) {
                    // port out of valid range
                    setStatus(false, "offline.invalid-port-number");
                    return;
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted while establishing ShieldTV connection");
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    setStatus(false, "offline.error-opening-ssl-connection-check-log");
                    logger.info("{} - Error opening SSL connection to {}:{} {}", handler.getThingID(), config.ipAddress,
                            config.shieldtvPort, e.getMessage());
                    disconnect(false);
                    scheduleConnectRetry(config.reconnect); // Possibly a temporary problem. Try again later.
                    return;
                }

                setStatus(false, "offline.initializing");

                Thread readerThread = new Thread(this::readerThreadJob,
                        "OH-binding-" + handler.getThingUID() + "-ShieldTVReader");
                readerThread.setDaemon(true);
                readerThread.start();
                this.readerThread = readerThread;

                Thread senderThread = new Thread(this::senderThreadJob,
                        "OH-binding-" + handler.getThingUID() + "-ShieldTVSender");
                senderThread.setDaemon(true);
                senderThread.start();
                this.senderThread = senderThread;

                if (!config.shim) {
                    this.periodicUpdate = 20;
                    logger.debug("{} - Starting ShieldTV keepalive job with interval {}", handler.getThingID(),
                            config.heartbeat);
                    keepAliveJob = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, config.heartbeat,
                            config.heartbeat, TimeUnit.SECONDS);

                    String login = ShieldTVRequest.encodeMessage(ShieldTVRequest.loginRequest());
                    sendCommand(new ShieldTVCommand(login));
                }
            } else {
                scheduleConnectRetry(config.reconnect); // Possibly a temporary problem. Try again later.
            }
        }
    }

    public void shimInitialize() {
        synchronized (connectionLock) {
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

                logger.debug("{} - Opening ShieldTV shim on port {}", handler.getThingID(), config.shieldtvPort);
                ServerSocket sslServerSocket = this.sslServerSocketFactory.createServerSocket(config.shieldtvPort);

                while (true) {
                    logger.debug("{} - Waiting for shim connection...", handler.getThingID());
                    Socket serverSocket = sslServerSocket.accept();
                    disconnect(false);
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

                    Thread readerThread = new Thread(this::shimReaderThreadJob,
                            "OH-binding-" + handler.getThingUID() + "-ShieldTVShimReader");
                    readerThread.setDaemon(true);
                    readerThread.start();
                    this.shimReaderThread = readerThread;

                    Thread senderThread = new Thread(this::shimSenderThreadJob,
                            "OH-binding-" + handler.getThingUID() + "-ShieldTVShimSender");
                    senderThread.setDaemon(true);
                    senderThread.start();
                    this.shimSenderThread = senderThread;
                }
            } catch (Exception e) {
                logger.trace("Shim initalization exception", e);
                return;
            }
        }
    }

    private void scheduleConnectRetry(long waitSeconds) {
        logger.trace("{} - Scheduling ShieldTV connection retry in {} seconds", handler.getThingID(), waitSeconds);
        connectRetryJob = scheduler.schedule(this::connect, waitSeconds, TimeUnit.SECONDS);
    }

    /**
     * Disconnect from bridge, cancel retry and keepalive jobs, stop reader and writer threads, and
     * clean up.
     *
     * @param interruptAll Set if reconnect task should be interrupted if running. Should be false when calling from
     *            connect or reconnect, and true when calling from dispose.
     */
    private void disconnect(boolean interruptAll) {
        synchronized (connectionLock) {
            logger.debug("{} - Disconnecting ShieldTV", handler.getThingID());

            this.isLoggedIn = false;

            ScheduledFuture<?> connectRetryJob = this.connectRetryJob;
            if (connectRetryJob != null) {
                connectRetryJob.cancel(true);
            }
            ScheduledFuture<?> keepAliveJob = this.keepAliveJob;
            if (keepAliveJob != null) {
                keepAliveJob.cancel(true);
            }

            reconnectTaskCancel(interruptAll); // May be called from keepAliveReconnectJob thread

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
    }

    private void reconnect() {
        synchronized (connectionLock) {
            if (!this.disposing) {
                logger.debug("{} - Attempting to reconnect to the ShieldTV", handler.getThingID());
                setStatus(false, "offline.reconnecting");
                disconnect(false);
                connect();
            }
        }
    }

    /**
     * Method executed by the message sender thread (senderThread)
     */
    private void senderThreadJob() {
        logger.debug("{} - Command sender thread started", handler.getThingID());
        try {
            while (!Thread.currentThread().isInterrupted() && writer != null) {
                ShieldTVCommand command = sendQueue.take();

                try {
                    BufferedWriter writer = this.writer;
                    if (writer != null) {
                        logger.trace("{} - Raw ShieldTV command decodes as: {}", handler.getThingID(),
                                ShieldTVRequest.decodeMessage(command.toString()));
                        writer.write(command.toString());
                        writer.flush();
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted while sending to ShieldTV");
                    setStatus(false, "offline.interrupted");
                    break; // exit loop and terminate thread
                } catch (IOException e) {
                    logger.warn("{} - Communication error, will try to reconnect ShieldTV. Error: {}",
                            handler.getThingID(), e.getMessage());
                    setStatus(false, "offline.communication-error-will-try-to-reconnect");
                    sendQueue.add(command); // Requeue command
                    this.isLoggedIn = false;
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
            logger.debug("{} - Command sender thread exiting", handler.getThingID());
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
            sbReader.append(lastMsg);
        }
        sbReader.append(thisMsg);
        lastMsg = thisMsg;
    }

    private void finishReaderMessage() {
        sbReader.append(thisMsg);
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
        logger.debug("{} - Message reader thread started", handler.getThingID());
        try {
            BufferedReader reader = this.reader;
            while (!Thread.interrupted() && reader != null) {
                thisMsg = fixMessage(Integer.toHexString(reader.read()));
                if (HARD_DROP.equals(thisMsg)) {
                    // Shield has crashed the connection. Disconnect hard.
                    logger.debug("{} - readerThreadJob received ffffffff.  Disconnecting hard.", handler.getThingID());
                    this.isLoggedIn = false;
                    reconnect();
                    break;
                }
                if (DELIMITER_08.equals(lastMsg) && !inMessage) {
                    flushReader();
                    inMessage = true;
                    msgType = thisMsg;
                } else if (DELIMITER_18.equals(lastMsg) && thisMsg.equals(msgType) && inMessage) {
                    if (!msgType.startsWith(DELIMITER_0)) {
                        sbReader.append(thisMsg);
                        thisMsg = fixMessage(Integer.toHexString(reader.read()));
                    }
                    finishReaderMessage();
                } else if (DELIMITER_00.equals(msgType) && (sbReader.toString().length() == 16)) {
                    // keepalive messages don't have delimiters but are always 18 in length
                    finishReaderMessage();
                } else {
                    sbReader.append(thisMsg);
                    lastMsg = thisMsg;
                }
            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            setStatus(false, "offline.interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            setStatus(false, "offline.io-error");
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            setStatus(false, "offline.runtime-exception");
        } finally {
            logger.debug("{} - Message reader thread exiting", handler.getThingID());
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
                if (HARD_DROP.equals(thisShimMsg)) {
                    disconnect(false);
                    break;
                }
                if (!inShimMessage) {
                    // Beginning of payload
                    sbShimReader.setLength(0);
                    sbShimReader.append(thisShimMsg);
                    inShimMessage = true;
                    payloadBlock++;
                } else if ((payloadBlock == 1) && (DELIMITER_00.equals(thisShimMsg))) {
                    sbShimReader.append(thisShimMsg);
                    payloadRemain = 8;
                    thisShimMsgType = thisShimMsg;
                    while (payloadRemain > 1) {
                        thisShimMsg = fixMessage(Integer.toHexString(reader.read()));
                        sbShimReader.append(thisShimMsg);
                        payloadRemain--;
                        payloadBlock++;
                    }
                    payloadRemain--;
                    payloadBlock++;
                } else if ((payloadBlock == 1)
                        && (thisShimMsg.startsWith(DELIMITER_F1) || thisShimMsg.startsWith(DELIMITER_F3))) {
                    sbShimReader.append(thisShimMsg);
                    payloadRemain = 6;
                    thisShimMsgType = thisShimMsg;
                    while (payloadRemain > 1) {
                        thisShimMsg = fixMessage(Integer.toHexString(reader.read()));
                        sbShimReader.append(thisShimMsg);
                        payloadRemain--;
                        payloadBlock++;
                    }
                    payloadRemain--;
                    payloadBlock++;
                } else if (payloadBlock == 1) {
                    thisShimMsgType = thisShimMsg;
                    sbShimReader.append(thisShimMsg);
                    payloadBlock++;
                } else if (payloadBlock == 2) {
                    sbShimReader.append(thisShimMsg);
                    payloadBlock++;
                } else if (payloadBlock == 3) {
                    // Length of remainder of packet
                    payloadRemain = thisShimRawMsg;
                    sbShimReader.append(thisShimMsg);
                    payloadBlock++;
                } else if (payloadBlock == 4) {
                    sbShimReader.append(thisShimMsg);
                    logger.trace("PB4 SSR {} TSMT {} TSM {} PR {}", sbShimReader.toString(), thisShimMsgType,
                            thisShimMsg, payloadRemain);
                    if (DELIMITER_E9.equals(thisShimMsgType) || DELIMITER_F0.equals(thisShimMsgType)
                            || DELIMITER_EC.equals(thisShimMsgType)) {
                        payloadRemain = thisShimRawMsg + 1;
                    }
                    while (payloadRemain > 1) {
                        thisShimMsg = fixMessage(Integer.toHexString(reader.read()));
                        sbShimReader.append(thisShimMsg);
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
            setStatus(false, "offline.interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            setStatus(false, "offline.io-error");
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            setStatus(false, "offline.runtime-exception");
        } finally {
            logger.debug("Message reader thread exiting");
        }
    }

    private void sendKeepAlive() {
        logger.trace("{} - Sending ShieldTV keepalive query", handler.getThingID());
        String keepalive = ShieldTVRequest.encodeMessage(ShieldTVRequest.keepAlive());
        sendCommand(new ShieldTVCommand(keepalive));
        if (isLoggedIn) {
            sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08ec0712020806"))); // Get App
            if (this.periodicUpdate <= 1) {
                sendPeriodicUpdate();
                this.periodicUpdate = 20;
            } else {
                periodicUpdate--;
            }
        }
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
                logger.trace("{} - Canceling ShieldTV scheduled reconnect job.", handler.getThingID());
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
        logger.debug("{} - ShieldTV keepalive response timeout expired. Initiating reconnect.", handler.getThingID());
        reconnect();
    }

    public void validMessageReceived() {
        reconnectTaskCancel(true); // Got a good message, so cancel reconnect task.
    }

    public void sendCommand(ShieldTVCommand command) {
        if ((!config.shim) && (!command.isEmpty())) {
            sendQueue.add(command);
        }
    }

    public void sendShim(ShieldTVCommand command) {
        if (!command.isEmpty()) {
            shimQueue.add(command);
        }
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{} - Command received: {}", handler.getThingID(), channelUID.getId());

        if (CHANNEL_KEYPRESS.equals(channelUID.getId())) {
            if (command instanceof StringType) {
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
                    case "KEY_SUBMIT":
                        sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08e9071209081410012001320138")));
                        break;
                }
                if (command.toString().length() == 5) {
                    // Account for KEY_(ASCII Character)
                    String keyPress = "08ec07120708011201"
                            + ShieldTVRequest.decodeMessage(new String("" + command.toString().charAt(4))) + "1801";
                    sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage(keyPress)));
                } else {
                    logger.trace("Unknown Keypress: {}", command.toString());
                }
            }
        } else if (CHANNEL_PINCODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (!isLoggedIn) {
                    // Do PIN for shieldtv protocol
                    logger.debug("{} - ShieldTV PIN Process Started", handler.getThingID());
                    String pin = ShieldTVRequest.pinRequest(command.toString());
                    String message = ShieldTVRequest.encodeMessage(pin);
                    sendCommand(new ShieldTVCommand(message));
                }
            }
        } else if (CHANNEL_DEBUG.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (command.toString().startsWith("RAW", 9)) {
                    String newCommand = command.toString().substring(13);
                    String message = ShieldTVRequest.encodeMessage(newCommand);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Raw Message Decodes as: {}", ShieldTVRequest.decodeMessage(message));
                    }
                    sendCommand(new ShieldTVCommand(message));
                } else if (command.toString().startsWith("MSG", 9)) {
                    String newCommand = command.toString().substring(13);
                    messageParser.handleMessage(newCommand);
                }
            }
        } else if (CHANNEL_APP.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String message = ShieldTVRequest.encodeMessage(ShieldTVRequest.startApp(command.toString()));
                sendCommand(new ShieldTVCommand(message));
            }
        } else if (CHANNEL_KEYBOARD.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String entry = ShieldTVRequest.keyboardEntry(command.toString());
                logger.trace("Keyboard Entry {}", entry);
                String message = ShieldTVRequest.encodeMessage(entry);
                sendCommand(new ShieldTVCommand(message));
                sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08e9071209081410012001320138")));
            }
        }
    }

    public void dispose() {
        this.disposing = true;

        Future<?> asyncInitializeTask = this.asyncInitializeTask;
        if (asyncInitializeTask != null) {
            asyncInitializeTask.cancel(true); // Interrupt async init task if it isn't done yet
        }
        Future<?> shimAsyncInitializeTask = this.shimAsyncInitializeTask;
        if (shimAsyncInitializeTask != null) {
            shimAsyncInitializeTask.cancel(true); // Interrupt async init task if it isn't done yet
        }
        ScheduledFuture<?> deviceHealthJob = this.deviceHealthJob;
        if (deviceHealthJob != null) {
            deviceHealthJob.cancel(true);
        }
        disconnect(true);
    }
}
