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
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
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
    private static final int DEFAULT_RECONNECT_MINUTES = 5;
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

    private @NonNullByDefault({}) ShieldTVMessageParser messageParser;

    private final BlockingQueue<ShieldTVCommand> sendQueue = new LinkedBlockingQueue<>();

    private @Nullable Future<?> asyncInitializeTask;

    private @Nullable Thread senderThread;
    private @Nullable Thread readerThread;

    private @Nullable ScheduledFuture<?> keepAliveJob;
    private @Nullable ScheduledFuture<?> keepAliveReconnectJob;
    private @Nullable ScheduledFuture<?> connectRetryJob;
    private final Object keepAliveReconnectLock = new Object();

    private StringBuffer sbReader = new StringBuffer();
    private String lastMsg = "";
    private String thisMsg = "";
    private int inMessage = 0;

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
        if (isLoggedIn) {
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
        config.reconnect = (config.reconnect > 0) ? config.reconnect : DEFAULT_RECONNECT_MINUTES;
        config.heartbeat = (config.heartbeat > 0) ? config.heartbeat : DEFAULT_HEARTBEAT_SECONDS;
        config.delay = (config.delay < 0) ? 0 : config.delay;

        config.keystoreFileName = (!config.keystoreFileName.equals("")) ? config.keystoreFileName
                : folderName + "/shieldtv." + handler.getThing().getUID().getId() + ".keystore";
        config.keystorePassword = (!config.keystorePassword.equals("")) ? config.keystorePassword
                : DEFAULT_KEYSTORE_PASSWORD;

        androidtvPKI.setKeystoreFileName(config.keystoreFileName);
        androidtvPKI.setAlias("nvidia");

        try {
            File keystoreFile = new File(config.keystoreFileName);

            if (!keystoreFile.exists()) {
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

            asyncInitializeTask = scheduler.submit(this::connect);

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

        Thread readerThread = new Thread(this::readerThreadJob, "ShiledTV reader");
        readerThread.setDaemon(true);
        readerThread.start();
        this.readerThread = readerThread;

        Thread senderThread = new Thread(this::senderThreadJob, "ShieldTV sender");
        senderThread.setDaemon(true);
        senderThread.start();
        this.senderThread = senderThread;

        logger.debug("Starting ShieldTV keepalive job with interval {}", config.heartbeat);
        keepAliveJob = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, config.heartbeat, config.heartbeat,
                TimeUnit.SECONDS);

        String login = ShieldTVRequest.encodeMessage(ShieldTVRequest.loginRequest());
        sendCommand(new ShieldTVCommand(login));
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling ShieldTV connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
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

    private void flushReader() {
        if ((inMessage == 0) && (sbReader.length() > 0)) {
            sbReader.setLength(sbReader.length() - 2);
            messageParser.handleMessage(sbReader.toString());
            sbReader.setLength(0);
            sbReader.append(lastMsg.toString());
        }
        sbReader.append(thisMsg.toString());
        lastMsg = thisMsg;
    }

    private void finishReaderMessage() {
        sbReader.append(thisMsg.toString());
        lastMsg = "";
        inMessage = 0;
        messageParser.handleMessage(sbReader.toString());
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
                if (lastMsg.equals("08") && thisMsg.equals("0a") && inMessage == 0) {
                    flushReader();
                    inMessage = 1;
                } else if (lastMsg.equals("18") && thisMsg.equals("0a") && inMessage == 1) {
                    finishReaderMessage();
                } else if (lastMsg.equals("08") && thisMsg.equals("0b") && inMessage == 0) {
                    flushReader();
                    inMessage = 2;
                } else if (lastMsg.equals("18") && thisMsg.equals("0b") && inMessage == 2) {
                    finishReaderMessage();
                } else if (lastMsg.equals("08") && thisMsg.equals("f1") && inMessage == 0) {
                    flushReader();
                    inMessage = 3;
                } else if (lastMsg.equals("18") && thisMsg.equals("f1") && inMessage == 3) {
                    sbReader.append(thisMsg.toString());
                    thisMsg = fixMessage(Integer.toHexString(reader.read()));
                    finishReaderMessage();
                } else if (lastMsg.equals("08") && thisMsg.equals("ec") && inMessage == 0) {
                    flushReader();
                    inMessage = 4;
                } else if (lastMsg.equals("18") && thisMsg.equals("ec") && inMessage == 4) {
                    sbReader.append(thisMsg.toString());
                    thisMsg = fixMessage(Integer.toHexString(reader.read()));
                    finishReaderMessage();
                } else if (lastMsg.equals("08") && thisMsg.equals("00") && inMessage == 0) {
                    flushReader();
                    inMessage = 5;
                } else if (lastMsg.equals("d1") && thisMsg.equals("30") && inMessage == 5) {
                    finishReaderMessage();
                } else if (lastMsg.equals("08") && thisMsg.equals("f0") && inMessage == 0) {
                    flushReader();
                    inMessage = 6;
                } else if (lastMsg.equals("18") && thisMsg.equals("f0") && inMessage == 6) {
                    sbReader.append(thisMsg.toString());
                    thisMsg = fixMessage(Integer.toHexString(reader.read()));
                    finishReaderMessage();
                } else if (lastMsg.equals("08") && thisMsg.equals("f3") && inMessage == 0) {
                    flushReader();
                    inMessage = 7;
                } else if (lastMsg.equals("18") && thisMsg.equals("f3") && inMessage == 7) {
                    sbReader.append(thisMsg.toString());
                    thisMsg = fixMessage(Integer.toHexString(reader.read()));
                    finishReaderMessage();
                } else if (lastMsg.equals("08") && thisMsg.equals("e9") && inMessage == 0) {
                    flushReader();
                    inMessage = 10;
                } else if (sbReader.length() == 32 && inMessage == 10) {
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

    public void sendCommand(@Nullable ShieldTVCommand command) {
        if (command != null) {
            sendQueue.add(command);
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
        disconnect(true);
    }
}
