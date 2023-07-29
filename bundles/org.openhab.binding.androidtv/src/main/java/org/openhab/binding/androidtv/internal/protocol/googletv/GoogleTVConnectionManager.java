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
package org.openhab.binding.androidtv.internal.protocol.googletv;

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;
import static org.openhab.binding.androidtv.internal.protocol.googletv.GoogleTVConstants.*;

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
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
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
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GoogleTVConnectionManager} is responsible for handling connections via the googletv protocol
 *
 * Significant portions reused from Lutron binding with permission from Bob A.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class GoogleTVConnectionManager {
    private static final int DEFAULT_RECONNECT_SECONDS = 60;
    private static final int DEFAULT_HEARTBEAT_SECONDS = 5;
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 30;
    private static final String DEFAULT_KEYSTORE_PASSWORD = "secret";
    private static final String DEFAULT_MODE = "NORMAL";
    private static final String PIN_MODE = "PIN";
    private static final int DEFAULT_PORT = 6466;
    private static final int PIN_DELAY = 1000;

    private final Logger logger = LoggerFactory.getLogger(GoogleTVConnectionManager.class);

    private ScheduledExecutorService scheduler;

    private final AndroidTVHandler handler;
    private GoogleTVConfiguration config;
    private final AndroidTVTranslationProvider translationProvider;

    private @NonNullByDefault({}) SSLSocketFactory sslSocketFactory;
    private @Nullable SSLSocket sslSocket;
    private @Nullable BufferedWriter writer;
    private @Nullable BufferedReader reader;

    private @NonNullByDefault({}) SSLServerSocketFactory sslServerSocketFactory;
    private @Nullable Socket shimServerSocket;
    private @Nullable BufferedWriter shimWriter;
    private @Nullable BufferedReader shimReader;

    private @Nullable GoogleTVConnectionManager connectionManager;
    private @Nullable GoogleTVConnectionManager childConnectionManager;
    private @NonNullByDefault({}) GoogleTVMessageParser messageParser;

    private final BlockingQueue<GoogleTVCommand> sendQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<GoogleTVCommand> shimQueue = new LinkedBlockingQueue<>();

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

    private @Nullable ScheduledFuture<?> deviceHealthJob;
    private boolean isOnline = true;

    private StringBuffer sbReader = new StringBuffer();
    private StringBuffer sbShimReader = new StringBuffer();
    private String thisMsg = "";

    private X509Certificate @Nullable [] shimX509ClientChain;
    private Certificate @Nullable [] shimClientChain;
    private Certificate @Nullable [] shimServerChain;
    private Certificate @Nullable [] shimClientLocalChain;

    private boolean disposing = false;
    private boolean isLoggedIn = false;
    private String statusMessage = "";
    private String pinHash = "";
    private String shimPinHash = "";

    private boolean power = false;
    private String volCurr = "00";
    private String volMax = "ff";
    private boolean volMute = false;
    private String audioMode = "";
    private String currentApp = "";
    private String manufacturer = "";
    private String model = "";
    private String androidVersion = "";
    private String remoteServer = "";
    private String remoteServerVersion = "";

    private AndroidTVPKI androidtvPKI = new AndroidTVPKI();
    private byte[] encryptionKey;

    public GoogleTVConnectionManager(AndroidTVHandler handler, GoogleTVConfiguration config) {
        messageParser = new GoogleTVMessageParser(this);
        this.config = config;
        this.handler = handler;
        this.translationProvider = handler.getTranslationProvider();
        this.connectionManager = this;
        this.scheduler = handler.getScheduler();
        this.encryptionKey = androidtvPKI.generateEncryptionKey();
        initialize();
    }

    public GoogleTVConnectionManager(AndroidTVHandler handler, GoogleTVConfiguration config,
            GoogleTVConnectionManager connectionManager) {
        messageParser = new GoogleTVMessageParser(this);
        this.config = config;
        this.handler = handler;
        this.translationProvider = handler.getTranslationProvider();
        this.connectionManager = connectionManager;
        this.scheduler = handler.getScheduler();
        this.encryptionKey = androidtvPKI.generateEncryptionKey();
        initialize();
    }

    public AndroidTVHandler getHandler() {
        return handler;
    }

    public String getThingID() {
        return handler.getThingID();
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        handler.setThingProperty("manufacturer", manufacturer);
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setModel(String model) {
        this.model = model;
        handler.setThingProperty("model", model);
    }

    public String getModel() {
        return model;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
        handler.setThingProperty("androidVersion", androidVersion);
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setRemoteServer(String remoteServer) {
        this.remoteServer = remoteServer;
        handler.setThingProperty("remoteServer", remoteServer);
    }

    public String getRemoteServer() {
        return remoteServer;
    }

    public void setRemoteServerVersion(String remoteServerVersion) {
        this.remoteServerVersion = remoteServerVersion;
        handler.setThingProperty("remoteServerVersion", remoteServerVersion);
    }

    public String getRemoteServerVersion() {
        return remoteServerVersion;
    }

    public void setPower(boolean power) {
        this.power = power;
        logger.debug("{} - Setting power to {}", handler.getThingID(), power);
        if (power) {
            handler.updateChannelState(CHANNEL_POWER, OnOffType.ON);
        } else {
            handler.updateChannelState(CHANNEL_POWER, OnOffType.OFF);
        }
    }

    public boolean getPower() {
        return power;
    }

    public void setVolCurr(String volCurr) {
        this.volCurr = volCurr;
        int max = Integer.parseInt(this.volMax, 16);
        int volume = ((Integer.parseInt(volCurr, 16) * 100) / max);
        handler.updateChannelState(CHANNEL_VOLUME, new PercentType(volume));
    }

    public String getVolCurr() {
        return volCurr;
    }

    public void setVolMax(String volMax) {
        this.volMax = volMax;
    }

    public String getVolMax() {
        return volMax;
    }

    public void setVolMute(String volMute) {
        if (DELIMITER_00.equals(volMute)) {
            this.volMute = false;
            handler.updateChannelState(CHANNEL_MUTE, OnOffType.OFF);
        } else if (DELIMITER_01.equals(volMute)) {
            this.volMute = true;
            handler.updateChannelState(CHANNEL_MUTE, OnOffType.ON);
        }
    }

    public boolean getVolMute() {
        return volMute;
    }

    public void setAudioMode(String audioMode) {
        this.audioMode = audioMode;
    }

    public String getAudioMode() {
        return audioMode;
    }

    public void setCurrentApp(String currentApp) {
        this.currentApp = currentApp;
        handler.updateChannelState(CHANNEL_APP, new StringType(currentApp));
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

    public void setLoggedIn(boolean isLoggedIn) {
        if (this.isLoggedIn != isLoggedIn) {
            setStatus(isLoggedIn);
        }
    }

    public boolean getLoggedIn() {
        return isLoggedIn;
    }

    private boolean servicePing() {
        int timeout = 500;

        SocketAddress socketAddress = new InetSocketAddress(config.ipAddress, config.port);
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
        logger.debug("{} - Device Health - Online: {} - Logged In: {} - Mode: {}", handler.getThingID(), isOnline,
                isLoggedIn, config.mode);
        if (isOnline != this.isOnline) {
            this.isOnline = isOnline;
            if (isOnline) {
                logger.debug("{} - Device is back online.  Attempting reconnection.", handler.getThingID());
                reconnect();
            }
        }
    }

    private void setShimX509ClientChain(X509Certificate @Nullable [] shimX509ClientChain) {
        try {
            this.shimX509ClientChain = shimX509ClientChain;
            logger.trace("Setting shimX509ClientChain {}", config.port);
            if (shimX509ClientChain != null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Subject DN: {}", shimX509ClientChain[0].getSubjectX500Principal());
                    logger.trace("Issuer DN: {}", shimX509ClientChain[0].getIssuerX500Principal());
                    logger.trace("Serial number: {}", shimX509ClientChain[0].getSerialNumber());
                    logger.trace("Cert: {}", GoogleTVRequest
                            .decodeMessage(GoogleTVUtils.byteArrayToString(shimX509ClientChain[0].getEncoded())));
                }
                androidtvPKI.setCaCert(shimX509ClientChain[0]);
                androidtvPKI.saveKeyStore(config.keystorePassword, this.encryptionKey);

            }
        } catch (Exception e) {
            logger.trace("setShimX509ClientChain Exception", e);
        }
    }

    private void startChildConnectionManager(int port, String mode) {
        GoogleTVConfiguration childConfig = new GoogleTVConfiguration();
        childConfig.ipAddress = config.ipAddress;
        childConfig.port = port;
        childConfig.reconnect = config.reconnect;
        childConfig.heartbeat = config.heartbeat;
        childConfig.keystoreFileName = config.keystoreFileName;
        childConfig.keystorePassword = config.keystorePassword;
        childConfig.delay = config.delay;
        childConfig.shim = config.shim;
        childConfig.mode = mode;
        logger.debug("{} - startChildConnectionManager parent config: {} {} {}", handler.getThingID(), config.port,
                config.mode, config.shim);
        logger.debug("{} - startChildConnectionManager child config: {} {} {}", handler.getThingID(), childConfig.port,
                childConfig.mode, childConfig.shim);
        childConnectionManager = new GoogleTVConnectionManager(this.handler, childConfig, this);
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
                X509Certificate[] x509ClientChain = shimX509ClientChain;
                if (x509ClientChain != null && logger.isTraceEnabled()) {
                    logger.debug("Returning shimX509ClientChain for getAcceptedIssuers");
                    for (int cert = 0; cert < x509ClientChain.length; cert++) {
                        logger.trace("Subject DN: {}", x509ClientChain[cert].getSubjectX500Principal());
                        logger.trace("Issuer DN: {}", x509ClientChain[cert].getIssuerX500Principal());
                        logger.trace("Serial number: {}", x509ClientChain[cert].getSerialNumber());
                    }
                    return x509ClientChain;
                } else {
                    logger.debug("Returning empty certificate for getAcceptedIssuers");
                    return new X509Certificate[0];
                }
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

        config.port = (config.port > 0) ? config.port : DEFAULT_PORT;
        config.mode = (!config.mode.equals("")) ? config.mode : DEFAULT_MODE;

        config.keystoreFileName = (!config.keystoreFileName.equals("")) ? config.keystoreFileName
                : folderName + "/googletv." + ((config.shim) ? "shim." : "") + handler.getThing().getUID().getId()
                        + ".keystore";
        config.keystorePassword = (!config.keystorePassword.equals("")) ? config.keystorePassword
                : DEFAULT_KEYSTORE_PASSWORD;

        androidtvPKI.setKeystoreFileName(config.keystoreFileName);
        androidtvPKI.setAlias("nvidia");

        if (config.mode.equals(DEFAULT_MODE)) {
            deviceHealthJob = scheduler.scheduleWithFixedDelay(this::checkHealth, config.heartbeat, config.heartbeat,
                    TimeUnit.SECONDS);
        }

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
            if (isOnline || config.mode.equals(PIN_MODE)) {
                try {
                    logger.debug("{} - Opening GoogleTV SSL connection to {}:{} {}", handler.getThingID(),
                            config.ipAddress, config.port, config.mode);
                    SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(config.ipAddress, config.port);
                    sslSocket.startHandshake();
                    this.shimServerChain = ((SSLSocket) sslSocket).getSession().getPeerCertificates();
                    writer = new BufferedWriter(
                            new OutputStreamWriter(sslSocket.getOutputStream(), StandardCharsets.ISO_8859_1));
                    reader = new BufferedReader(
                            new InputStreamReader(sslSocket.getInputStream(), StandardCharsets.ISO_8859_1));
                    this.sslSocket = sslSocket;
                    this.sendQueue.clear();
                    logger.debug("{} - Connection to {}:{} {} successful", handler.getThingID(), config.ipAddress,
                            config.port, config.mode);
                } catch (UnknownHostException e) {
                    setStatus(false, "offline.unknown-host");
                    logger.debug("{} - Unknown host {}", handler.getThingID(), config.ipAddress);
                    return;
                } catch (IllegalArgumentException e) {
                    // port out of valid range
                    setStatus(false, "offline.invalid-port-number");
                    logger.debug("{} - Invalid port number {}:{}", handler.getThingID(), config.ipAddress, config.port);
                    return;
                } catch (InterruptedIOException e) {
                    logger.debug("{} - Interrupted while establishing GoogleTV connection", handler.getThingID());
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    String message = e.getMessage();
                    if ((message != null) && (message.contains("certificate_unknown"))
                            && (!config.mode.equals(PIN_MODE)) && (!config.shim)) {
                        setStatus(false, "offline.pin-process-incomplete");
                        logger.debug("{} - GoogleTV PIN Process Incomplete", handler.getThingID());
                        reconnectTaskCancel(true);
                        startChildConnectionManager(this.config.port + 1, PIN_MODE);
                    } else if ((message != null) && (message.contains("certificate_unknown")) && (config.shim)) {
                        logger.debug("Shim cert_unknown I/O error while connecting: {}", e.getMessage());
                        Socket shimServerSocket = this.shimServerSocket;
                        if (shimServerSocket != null) {
                            try {
                                shimServerSocket.close();
                            } catch (IOException ex) {
                                logger.debug("Error closing GoogleTV SSL socket: {}", ex.getMessage());
                            }
                            this.shimServerSocket = null;
                        }
                    } else {
                        setStatus(false, "offline.error-opening-ssl-connection-check-log");
                        logger.info("{} - Error opening SSL connection to {}:{} {}", handler.getThingID(),
                                config.ipAddress, config.port, e.getMessage());
                        disconnect(false);
                        scheduleConnectRetry(config.reconnect); // Possibly a temporary problem. Try again later.
                    }
                    return;
                }

                setStatus(false, "offline.initializing");

                logger.trace("{} - Starting Reader Thread for {}:{}", handler.getThingID(), config.ipAddress,
                        config.port);

                Thread readerThread = new Thread(this::readerThreadJob, "GoogleTV reader " + handler.getThingID());
                readerThread.setDaemon(true);
                readerThread.start();
                this.readerThread = readerThread;

                logger.trace("{} - Starting Sender Thread for {}:{}", handler.getThingID(), config.ipAddress,
                        config.port);

                Thread senderThread = new Thread(this::senderThreadJob, "GoogleTV sender " + handler.getThingID());
                senderThread.setDaemon(true);
                senderThread.start();
                this.senderThread = senderThread;

                logger.trace("{} - Checking for PIN MODE for {}:{} {}", handler.getThingID(), config.ipAddress,
                        config.port, config.mode);

                if (config.mode.equals(PIN_MODE)) {
                    logger.trace("{} - Sending PIN Login to {}:{}", handler.getThingID(), config.ipAddress,
                            config.port);
                    // Send app name and device name
                    sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage(GoogleTVRequest.loginRequest(1))));
                    // Unknown but required
                    sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage(GoogleTVRequest.loginRequest(2))));
                    // Don't send pin request yet, let user send REQUEST via PINCODE channel
                } else {
                    logger.trace("{} - Not PIN Mode {}:{} {}", handler.getThingID(), config.ipAddress, config.port,
                            config.mode);
                }
            } else {
                scheduleConnectRetry(config.reconnect); // Possibly a temporary problem. Try again later.
            }
        }
    }

    public void shimInitialize() {
        synchronized (connectionLock) {
            SSLContext sslContext;

            try {
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmf.init(androidtvPKI.getKeyStore(config.keystorePassword, this.encryptionKey),
                        config.keystorePassword.toCharArray());

                TrustManager[] trustManagers = defineNoOpTrustManager();

                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), trustManagers, null);
                this.sslServerSocketFactory = sslContext.getServerSocketFactory();

                logger.trace("Opening GoogleTV shim on port {}", config.port);
                SSLServerSocket sslServerSocket = (SSLServerSocket) this.sslServerSocketFactory
                        .createServerSocket(config.port);
                if (this.config.mode.equals(DEFAULT_MODE)) {
                    sslServerSocket.setNeedClientAuth(true);
                } else {
                    sslServerSocket.setWantClientAuth(true);
                }

                while (true) {
                    logger.trace("Waiting for shim connection... {}", config.port);
                    if (this.config.mode.equals(DEFAULT_MODE) && (childConnectionManager == null)) {
                        logger.trace("Starting childConnectionManager {}", config.port);
                        startChildConnectionManager(this.config.port + 1, PIN_MODE);
                    }
                    SSLSocket serverSocket = (SSLSocket) sslServerSocket.accept();
                    logger.trace("shimInitialize accepted {}", config.port);
                    try {
                        serverSocket.startHandshake();
                        logger.trace("shimInitialize startHandshake {}", config.port);
                        connect();
                        logger.trace("shimInitialize connected {}", config.port);

                        SSLSession session = serverSocket.getSession();
                        Certificate[] cchain2 = session.getPeerCertificates();
                        this.shimClientChain = cchain2;
                        Certificate[] cchain3 = session.getLocalCertificates();
                        this.shimClientLocalChain = cchain3;

                        X509Certificate[] shimX509ClientChain = new X509Certificate[cchain2.length];

                        for (int i = 0; i < cchain2.length; i++) {
                            logger.trace("Connection from: {}",
                                    ((X509Certificate) cchain2[i]).getSubjectX500Principal());
                            shimX509ClientChain[i] = ((X509Certificate) cchain2[i]);
                            if (this.config.mode.equals(DEFAULT_MODE) && logger.isTraceEnabled()) {
                                logger.trace("Cert: {}", GoogleTVRequest.decodeMessage(
                                        GoogleTVUtils.byteArrayToString(((X509Certificate) cchain2[i]).getEncoded())));
                            }
                        }

                        if (this.config.mode.equals(PIN_MODE)) {
                            this.shimX509ClientChain = shimX509ClientChain;
                            GoogleTVConnectionManager connectionManager = this.connectionManager;
                            if (connectionManager != null) {
                                connectionManager.setShimX509ClientChain(shimX509ClientChain);
                            }
                        }

                        if (cchain3 != null) {
                            for (int i = 0; i < cchain3.length; i++) {
                                logger.trace("Connection from: {}",
                                        ((X509Certificate) cchain3[i]).getSubjectX500Principal());
                            }
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
                        this.shimQueue.clear();

                        Thread readerThread = new Thread(this::shimReaderThreadJob, "GoogleTV shim reader");
                        readerThread.setDaemon(true);
                        readerThread.start();
                        this.shimReaderThread = readerThread;

                        Thread senderThread = new Thread(this::shimSenderThreadJob, "GoogleTV shim sender");
                        senderThread.setDaemon(true);
                        senderThread.start();
                        this.shimSenderThread = senderThread;
                    } catch (Exception e) {
                        logger.trace("Shim initalization exception {}", config.port);
                        logger.trace("Shim initalization exception", e);
                    }
                }
            } catch (Exception e) {
                logger.trace("Shim initalization exception {}", config.port);
                logger.trace("Shim initalization exception", e);

                return;
            }
        }
    }

    private void scheduleConnectRetry(long waitSeconds) {
        logger.trace("{} - Scheduling GoogleTV connection retry in {} seconds", handler.getThingID(), waitSeconds);
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
            logger.debug("{} - Disconnecting GoogleTV", handler.getThingID());

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
                    logger.debug("Error closing GoogleTV SSL socket: {}", e.getMessage());
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
                    logger.debug("Error closing GoogleTV SSL socket: {}", e.getMessage());
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
                logger.debug("{} - Attempting to reconnect to the GoogleTV", handler.getThingID());
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
        logger.debug("{} - Command sender thread started {}", handler.getThingID(), config.port);
        try {
            while (!Thread.currentThread().isInterrupted() && writer != null) {
                GoogleTVCommand command = sendQueue.take();

                try {
                    BufferedWriter writer = this.writer;
                    if (writer != null) {
                        logger.trace("{} - Raw GoogleTV command decodes as: {}", handler.getThingID(),
                                GoogleTVRequest.decodeMessage(command.toString()));
                        writer.write(command.toString());
                        writer.flush();
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted while sending to GoogleTV");
                    setStatus(false, "offline.interrupted");
                    break; // exit loop and terminate thread
                } catch (IOException e) {
                    logger.warn("{} - Communication error, will try to reconnect GoogleTV. Error: {}",
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
            logger.debug("{} - Command sender thread exiting {}", handler.getThingID(), config.port);
        }
    }

    private void shimSenderThreadJob() {
        logger.debug("Shim sender thread started");
        try {
            while (!Thread.currentThread().isInterrupted() && shimWriter != null) {
                GoogleTVCommand command = shimQueue.take();

                try {
                    BufferedWriter writer = this.shimWriter;
                    if (writer != null) {
                        logger.trace("Shim received from google: {}",
                                GoogleTVRequest.decodeMessage(command.toString()));
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

    /**
     * Method executed by the message reader thread (readerThread)
     */
    private void readerThreadJob() {
        logger.debug("{} - Message reader thread started {}", handler.getThingID(), config.port);
        try {
            BufferedReader reader = this.reader;
            int length = 0;
            int current = 0;
            while (!Thread.interrupted() && reader != null) {
                thisMsg = GoogleTVRequest.fixMessage(Integer.toHexString(reader.read()));
                if (HARD_DROP.equals(thisMsg)) {
                    // Google has crashed the connection. Disconnect hard.
                    logger.debug("{} - readerThreadJob received ffffffff.  Disconnecting hard.", handler.getThingID());
                    this.isLoggedIn = false;
                    reconnect();
                    break;
                }
                if (length == 0) {
                    length = Integer.parseInt(thisMsg.toString(), 16);
                    logger.trace("{} - readerThreadJob message length {}", handler.getThingID(), length);
                    current = 0;
                    sbReader = new StringBuffer();
                    sbReader.append(thisMsg.toString());
                } else {
                    sbReader.append(thisMsg.toString());
                    current += 1;
                }

                if ((length > 0) && (current == length)) {
                    logger.trace("{} - GoogleTV Message: {} {}", handler.getThingID(), length, sbReader.toString());
                    messageParser.handleMessage(sbReader.toString());
                    if (config.shim) {
                        String thisCommand = interceptMessages(sbReader.toString());
                        shimQueue.add(new GoogleTVCommand(GoogleTVRequest.encodeMessage(thisCommand)));
                    }
                    length = 0;
                }
            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            setStatus(false, "offline.interrupted");
        } catch (IOException e) {
            String message = e.getMessage();
            if ((message != null) && (message.contains("certificate_unknown")) && (!config.mode.equals(PIN_MODE))
                    && (!config.shim)) {
                setStatus(false, "offline.pin-process-incomplete");
                logger.debug("{} - GoogleTV PIN Process Incomplete", handler.getThingID());
                reconnectTaskCancel(true);
                startChildConnectionManager(this.config.port + 1, PIN_MODE);
            } else if ((message != null) && (message.contains("certificate_unknown")) && (config.shim)) {
                logger.debug("Shim cert_unknown I/O error while reading from stream: {}", e.getMessage());
                Socket shimServerSocket = this.shimServerSocket;
                if (shimServerSocket != null) {
                    try {
                        shimServerSocket.close();
                    } catch (IOException ex) {
                        logger.debug("Error closing GoogleTV SSL socket: {}", ex.getMessage());
                    }
                    this.shimServerSocket = null;
                }
            } else {
                logger.debug("I/O error while reading from stream: {}", e.getMessage());
                setStatus(false, "offline.io-error");
            }
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            setStatus(false, "offline.runtime-exception");
        } finally {
            logger.debug("{} - Message reader thread exiting {}", handler.getThingID(), config.port);
        }
    }

    private String interceptMessages(String message) {
        if (message.startsWith("080210c801c202", 2)) {
            // intercept PIN hash and replace with valid shim hash
            int length = this.pinHash.length() / 2;
            String len1 = GoogleTVRequest.fixMessage(Integer.toHexString(length + 2));
            String len2 = GoogleTVRequest.fixMessage(Integer.toHexString(length));
            String reply = "080210c801c202" + len1 + "0a" + len2 + this.pinHash;
            String replyLength = GoogleTVRequest.fixMessage(Integer.toHexString(reply.length() / 2));
            String finalReply = replyLength + reply;
            logger.trace("Message Intercepted: {}", message);
            logger.trace("Message chagnged to: {}", finalReply);
            return finalReply;
        } else if (message.startsWith("080210c801ca02", 2)) {
            // intercept PIN hash and replace with valid shim hash
            int length = this.shimPinHash.length() / 2;
            String len1 = GoogleTVRequest.fixMessage(Integer.toHexString(length + 2));
            String len2 = GoogleTVRequest.fixMessage(Integer.toHexString(length));
            String reply = "080210c801ca02" + len1 + "0a" + len2 + this.shimPinHash;
            String replyLength = GoogleTVRequest.fixMessage(Integer.toHexString(reply.length() / 2));
            String finalReply = replyLength + reply;
            logger.trace("Message Intercepted: {}", message);
            logger.trace("Message chagnged to: {}", finalReply);
            return finalReply;
        } else {
            // don't intercept message
            return message;
        }
    }

    private void shimReaderThreadJob() {
        logger.debug("Shim reader thread started {}", config.port);
        try {
            BufferedReader reader = this.shimReader;
            String thisShimMsg = "";
            int length = 0;
            int current = 0;
            while (!Thread.interrupted() && reader != null) {
                thisShimMsg = GoogleTVRequest.fixMessage(Integer.toHexString(reader.read()));
                if (HARD_DROP.equals(thisShimMsg)) {
                    // Google has crashed the connection. Disconnect hard.
                    disconnect(false);
                    break;
                }
                if (length == 0) {
                    length = Integer.parseInt(thisShimMsg.toString(), 16);
                    logger.trace("shimReaderThreadJob message length {}", length);
                    current = 0;
                    sbShimReader = new StringBuffer();
                    sbShimReader.append(thisShimMsg.toString());
                } else {
                    sbShimReader.append(thisShimMsg.toString());
                    current += 1;
                }
                if ((length > 0) && (current == length)) {
                    logger.trace("Shim GoogleTV Message: {} {}", length, sbShimReader.toString());
                    String thisCommand = interceptMessages(sbShimReader.toString());
                    sendQueue.add(new GoogleTVCommand(GoogleTVRequest.encodeMessage(thisCommand)));
                    length = 0;
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
            logger.debug("Shim message reader thread exiting {}", config.port);
        }
    }

    public void sendKeepAlive(String request) {
        String keepalive = GoogleTVRequest.encodeMessage(GoogleTVRequest.keepAlive(request));
        logger.debug("{} - Sending GoogleTV keepalive - request {} - response {}", handler.getThingID(), request,
                GoogleTVRequest.decodeMessage(keepalive));
        sendCommand(new GoogleTVCommand(keepalive));
        reconnectTaskSchedule();
    }

    /**
     * Schedules the reconnect task keepAliveReconnectJob to execute in KEEPALIVE_TIMEOUT_SECONDS. This should
     * be
     * cancelled by calling reconnectTaskCancel() if a valid response is received from the bridge.
     */
    private void reconnectTaskSchedule() {
        synchronized (keepAliveReconnectLock) {
            logger.trace("{} - Scheduling Reconnect Job for {}", handler.getThingID(), KEEPALIVE_TIMEOUT_SECONDS);
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
                logger.trace("{} - Canceling GoogleTV scheduled reconnect job.", handler.getThingID());
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
        logger.debug("{} - GoogleTV keepalive response timeout expired. Initiating reconnect.", handler.getThingID());
        reconnect();
    }

    public void validMessageReceived() {
        reconnectTaskCancel(true); // Got a good message, so cancel reconnect task.
    }

    public void finishPinProcess() {
        GoogleTVConnectionManager connectionManager = this.connectionManager;
        GoogleTVConnectionManager childConnectionManager = this.childConnectionManager;
        if ((connectionManager != null) && (config.mode.equals(PIN_MODE)) && (!config.shim)) {
            disconnect(false);
            connectionManager.finishPinProcess();
        } else if ((childConnectionManager != null) && (config.mode.equals(DEFAULT_MODE)) && (!config.shim)) {
            childConnectionManager.dispose();
            reconnect();
        }
    }

    public void sendCommand(GoogleTVCommand command) {
        if ((!config.shim) && (!command.isEmpty())) {
            int length = command.toString().length();
            String hexLength = GoogleTVRequest.encodeMessage(GoogleTVRequest.fixMessage(Integer.toHexString(length)));
            String message = hexLength + command.toString();
            GoogleTVCommand lenCommand = new GoogleTVCommand(message);
            sendQueue.add(lenCommand);
        }
    }

    public void sendShim(GoogleTVCommand command) {
        if (!command.isEmpty()) {
            shimQueue.add(command);
        }
    }

    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{} - Command received: {}", handler.getThingID(), channelUID.getId());

        if (CHANNEL_KEYPRESS.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (command.toString().length() == 5) {
                    // Account for KEY_(ASCII Character)
                    String keyPress = "aa01071a0512031a01"
                            + GoogleTVRequest.decodeMessage(new String("" + command.toString().charAt(4)));
                    sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage(keyPress)));
                    return;
                }

                String message = "";
                String suffix = "";
                String shortCommand = command.toString();
                if (command.toString().endsWith("_PRESS")) {
                    suffix = "1001";
                    shortCommand = "KEY_" + command.toString().split("_")[1];
                } else if (command.toString().endsWith("_RELEASE")) {
                    suffix = "1002";
                    shortCommand = "KEY_" + command.toString().split("_")[1];
                } else {
                    suffix = "1003";
                }

                switch (shortCommand) {
                    case "KEY_UP":
                        message = "52040813" + suffix;
                        break;
                    case "KEY_DOWN":
                        message = "52040814" + suffix;
                        break;
                    case "KEY_RIGHT":
                        message = "52040816" + suffix;
                        break;
                    case "KEY_LEFT":
                        message = "52040815" + suffix;
                        break;
                    case "KEY_ENTER":
                        message = "52040817" + suffix;
                        break;
                    case "KEY_HOME":
                        message = "52040803" + suffix;
                        break;
                    case "KEY_BACK":
                        message = "52040804" + suffix;
                        break;
                    case "KEY_MENU":
                        message = "52040852" + suffix;
                        break;
                    case "KEY_PLAY":
                        message = "5204087E" + suffix;
                        break;
                    case "KEY_PAUSE":
                        message = "5204087F" + suffix;
                        break;
                    case "KEY_PLAYPAUSE":
                        message = "52040855" + suffix;
                        break;
                    case "KEY_STOP":
                        message = "52040856" + suffix;
                        break;
                    case "KEY_NEXT":
                        message = "52040857" + suffix;
                        break;
                    case "KEY_PREVIOUS":
                        message = "52040858" + suffix;
                        break;
                    case "KEY_REWIND":
                        message = "52040859" + suffix;
                        break;
                    case "KEY_FORWARD":
                        message = "5204085A" + suffix;
                        break;
                    case "KEY_POWER":
                        message = "5204081a" + suffix;
                        break;
                    case "KEY_VOLUP":
                        message = "52040818" + suffix;
                        break;
                    case "KEY_VOLDOWN":
                        message = "52040819" + suffix;
                        break;
                    case "KEY_MUTE":
                        message = "5204085b" + suffix;
                        break;
                    default:
                        logger.debug("Unknown Key {}", command);
                        return;
                }
                sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage(message)));
            }
        } else if (CHANNEL_KEYCODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String shortCommand = command.toString().split("_")[0];
                int commandInt = Integer.parseInt(shortCommand, 10);
                String suffix = "";
                if (commandInt > 255) {
                    suffix = "02";
                    commandInt -= 256;
                } else if (commandInt > 127) {
                    suffix = "01";
                }

                String key = Integer.toHexString(commandInt) + suffix;

                if ((key.length() % 2) == 1) {
                    key = "0" + key;
                }

                key = "08" + key;

                if (command.toString().endsWith("_PRESS")) {
                    key = key + "1001";
                } else if (command.toString().endsWith("_RELEASE")) {
                    key = key + "1002";
                } else {
                    key = key + "1003";
                }

                String length = "0" + (key.length() / 2);
                String message = "52" + length + key;

                logger.trace("Sending KEYCODE {} as {}", key, message);
                sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage(message)));
            }

        } else if (CHANNEL_PINCODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                try {
                    Certificate[] shimClientChain = this.shimClientChain;
                    Certificate[] shimServerChain = this.shimServerChain;
                    Certificate[] shimClientLocalChain = this.shimClientLocalChain;
                    if (config.mode.equals(DEFAULT_MODE)) {
                        if ((!isLoggedIn) && (command.toString().equals("REQUEST"))
                                && (childConnectionManager == null)) {
                            setStatus(false, "offline.user-forced-pin-process");
                            logger.debug("{} - User Forced PIN Process", handler.getThingID());
                            disconnect(true);
                            startChildConnectionManager(config.port + 1, PIN_MODE);
                            try {
                                Thread.sleep(PIN_DELAY);
                            } catch (InterruptedException e) {
                                logger.trace("InterruptedException", e);
                            }
                        }
                        GoogleTVConnectionManager childConnectionManager = this.childConnectionManager;
                        if (childConnectionManager != null) {
                            childConnectionManager.handleCommand(channelUID, command);
                        } else {
                            logger.debug("{} - Child Connection Manager unavailable.", handler.getThingID());
                        }
                    } else if ((config.mode.equals(PIN_MODE)) && (!config.shim)) {
                        if (!isLoggedIn) {
                            if (command.toString().equals("REQUEST")) {
                                sendCommand(new GoogleTVCommand(
                                        GoogleTVRequest.encodeMessage(GoogleTVRequest.pinRequest(command.toString()))));
                            } else if (shimServerChain != null) {
                                this.pinHash = GoogleTVUtils.validatePIN(command.toString(), androidtvPKI.getCert(),
                                        shimServerChain[0]);
                                sendCommand(new GoogleTVCommand(
                                        GoogleTVRequest.encodeMessage(GoogleTVRequest.pinRequest(this.pinHash))));
                            }
                        }
                    } else if ((config.mode.equals(PIN_MODE)) && (config.shim)) {
                        if ((shimClientChain != null) && (shimServerChain != null) && (shimClientLocalChain != null)) {
                            this.pinHash = GoogleTVUtils.validatePIN(command.toString(), androidtvPKI.getCert(),
                                    shimServerChain[0]);
                            this.shimPinHash = GoogleTVUtils.validatePIN(command.toString(), shimClientChain[0],
                                    shimClientLocalChain[0]);
                        }
                    }
                } catch (CertificateException e) {
                    logger.trace("PIN CertificateException", e);
                }
            }
        } else if (CHANNEL_POWER.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                if ((power && command.equals(OnOffType.OFF)) || (!power && command.equals(OnOffType.ON))) {
                    sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage("5204081a1003")));
                }
            } else if (command instanceof StringType) {
                if ((power && command.toString().equals("OFF")) || (!power && command.toString().equals("ON"))) {
                    sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage("5204081a1003")));
                }
            }
        } else if (CHANNEL_MUTE.equals(channelUID.getId())) {
            if (command instanceof OnOffType) {
                if ((volMute && command.equals(OnOffType.OFF)) || (!volMute && command.equals(OnOffType.ON))) {
                    sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage("5204085b1003")));
                }
            }
        } else if (CHANNEL_DEBUG.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                if (command.toString().startsWith("RAW", 9)) {
                    String newCommand = command.toString().substring(13);
                    String message = GoogleTVRequest.encodeMessage(newCommand);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Raw Message Decodes as: {}", GoogleTVRequest.decodeMessage(message));
                    }
                    sendCommand(new GoogleTVCommand(message));
                } else if (command.toString().startsWith("MSG", 9)) {
                    String newCommand = command.toString().substring(13);
                    messageParser.handleMessage(newCommand);
                }
            }
        } else if (CHANNEL_KEYBOARD.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String keyPress = "";
                for (int i = 0; i < command.toString().length(); i++) {
                    keyPress = "aa01071a0512031a01"
                            + GoogleTVRequest.decodeMessage(String.valueOf(command.toString().charAt(i)));
                    sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage(keyPress)));
                }
            }
        } else if (CHANNEL_PLAYER.equals(channelUID.getId())) {
            String message = "";
            if (command == PlayPauseType.PAUSE || command == OnOffType.OFF) {
                message = "5204087F1003";
            } else if (command == PlayPauseType.PLAY || command == OnOffType.ON) {
                message = "5204087E1003";
            } else if (command == NextPreviousType.NEXT) {
                message = "520408571003";
            } else if (command == NextPreviousType.PREVIOUS) {
                message = "520408581003";
            } else if (command == RewindFastforwardType.FASTFORWARD) {
                message = "5204085A1003";
            } else if (command == RewindFastforwardType.REWIND) {
                message = "520408591003";
            }
            sendCommand(new GoogleTVCommand(GoogleTVRequest.encodeMessage(message)));
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
        GoogleTVConnectionManager childConnectionManager = this.childConnectionManager;
        if (childConnectionManager != null) {
            childConnectionManager.dispose();
        }
        disconnect(true);
    }
}
