/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.shieldtv.internal;

import static org.openhab.binding.shieldtv.internal.ShieldTVBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
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
import org.openhab.binding.shieldtv.internal.protocol.shieldtv.ShieldTVCommand;
import org.openhab.binding.shieldtv.internal.protocol.shieldtv.ShieldTVMessageParser;
import org.openhab.binding.shieldtv.internal.protocol.shieldtv.ShieldTVMessageParserCallbacks;
import org.openhab.binding.shieldtv.internal.protocol.shieldtv.ShieldTVRequest;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShieldTVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * Significant portions reused from Lutron binding with permission from Bob A.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVHandler extends BaseThingHandler implements ShieldTVMessageParserCallbacks {

    private static final int DEFAULT_RECONNECT_MINUTES = 5;
    private static final int DEFAULT_HEARTBEAT_SECONDS = 5;
    private static final long KEEPALIVE_TIMEOUT_SECONDS = 30;

    private static final String STATUS_INITIALIZING = "Initializing";

    private final Logger logger = LoggerFactory.getLogger(ShieldTVHandler.class);

    private @Nullable ShieldTVConfiguration config;
    private int reconnectInterval;
    private int heartbeatInterval;
    private int sendDelay;

    private @NonNullByDefault({}) SSLSocketFactory sslsocketfactory;
    private @Nullable SSLSocket sslsocket;
    private @Nullable BufferedWriter writer;
    private @Nullable BufferedReader reader;

    private @NonNullByDefault({}) ShieldTVMessageParser shieldtvMessageParser;

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

    private ShieldTVPKI shieldtvPKI = new ShieldTVPKI();

    public ShieldTVHandler(Thing thing) {
        super(thing);
        shieldtvMessageParser = new ShieldTVMessageParser(this);
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
        thing.setProperty("Device Name", hostName);
    }

    public String getHostName() {
        return this.hostName;
    }

    public void setCurrentApp(String currentApp) {
        this.currentApp = currentApp;
        updateState(CHANNEL_APP, new StringType(currentApp));
    }

    public String getCurrentApp() {
        return this.currentApp;
    }

    public void setLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    public boolean getLoggedIn() {
        return this.isLoggedIn;
    }

    public void setKeys(String privKey, String cert) {
        shieldtvPKI.setKeys(privKey, cert);
        shieldtvPKI.saveKeys();
    }

    @Override
    public void initialize() {
        SSLContext sslContext;
        String folderName = OpenHAB.getUserDataFolder() + "/shieldtv";
        File folder = new File(folderName);

        if (!folder.exists()) {
            logger.debug("Creating directory {}", folderName);
            folder.mkdirs();
        }

        config = getConfigAs(ShieldTVConfiguration.class);

        String ipAddress = config.ipAddress;
        String keystoreFileName = folderName + "/shieldtv." + getThing().getUID().getId() + ".keystore";
        String keystorePassword = (config.keystorePassword == null) ? "" : config.keystorePassword;

        if (ipAddress == null || ipAddress.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "shieldtv address not specified");
            return;
        }

        if (keystorePassword.equals("")) {
            keystorePassword = "secret";
        }

        shieldtvPKI.setKeystore(keystoreFileName, keystorePassword);

        File keystoreFile = new File(keystoreFileName);

        if (!keystoreFile.exists()) {
            shieldtvPKI.createKeystore();
        }

        reconnectInterval = (config.reconnect > 0) ? config.reconnect : DEFAULT_RECONNECT_MINUTES;
        heartbeatInterval = (config.heartbeat > 0) ? config.heartbeat : DEFAULT_HEARTBEAT_SECONDS;
        sendDelay = (config.delay < 0) ? 0 : config.delay;

        try (FileInputStream keystoreInputStream = new FileInputStream(shieldtvPKI.getKeystoreFileName())) {
            logger.trace("Initializing keystore");
            KeyStore keystore = KeyStore.getInstance("JKS");

            keystore.load(keystoreInputStream, shieldtvPKI.getKeystorePassword().toCharArray());

            logger.trace("Initializing SSL Context");
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, shieldtvPKI.getKeystorePassword().toCharArray());

            TrustManager[] trustManagers = defineNoOpTrustManager();

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), trustManagers, null);

            sslsocketfactory = sslContext.getSocketFactory();
        } catch (FileNotFoundException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Keystore file not found");
            return;
        } catch (CertificateException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Certificate exception");
            return;
        } catch (UnrecoverableKeyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Key unrecoverable with supplied password");
            return;
        } catch (KeyManagementException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Key management exception");
            logger.debug("Key management exception", e);
            return;
        } catch (KeyStoreException | NoSuchAlgorithmException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Error initializing keystore");
            logger.debug("Error initializing keystore", e);
            return;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Connecting");
        asyncInitializeTask = scheduler.submit(this::connect); // start the async connect task
    }

    private TrustManager[] defineNoOpTrustManager() {
        return new TrustManager[] { new X509TrustManager() {
            @Override
            public void checkClientTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType) {
                logger.debug("Assuming client certificate is valid");
                if (chain != null && logger.isTraceEnabled()) {
                    for (int cert = 0; cert < chain.length; cert++) {
                        logger.trace("Subject DN: {}", chain[cert].getSubjectDN());
                        logger.trace("Issuer DN: {}", chain[cert].getIssuerDN());
                        logger.trace("Serial number {}:", chain[cert].getSerialNumber());
                    }
                }
            }

            @Override
            public void checkServerTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType) {
                logger.debug("Assuming server certificate is valid");
                if (chain != null && logger.isTraceEnabled()) {
                    for (int cert = 0; cert < chain.length; cert++) {
                        logger.trace("Subject DN: {}", chain[cert].getSubjectDN());
                        logger.trace("Issuer DN: {}", chain[cert].getIssuerDN());
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

    private synchronized void connect() {

        try {
            logger.debug("Opening SSL connection to {}:{}", config.ipAddress, config.port);
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(config.ipAddress, config.port);
            sslsocket.startHandshake();
            writer = new BufferedWriter(
                    new OutputStreamWriter(sslsocket.getOutputStream(), StandardCharsets.ISO_8859_1));
            reader = new BufferedReader(new InputStreamReader(sslsocket.getInputStream(), StandardCharsets.ISO_8859_1));
            this.sslsocket = sslsocket;
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknown host");
            return;
        } catch (IllegalArgumentException e) {
            // port out of valid range
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid port number");
            return;
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while establishing connection");
            Thread.currentThread().interrupt();
            return;
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error opening SSL connection. Check log.");
            logger.info("Error opening SSL connection: {}", e.getMessage());
            disconnect(false);
            scheduleConnectRetry(reconnectInterval); // Possibly a temporary problem. Try again later.
            return;
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, STATUS_INITIALIZING);

        Thread readerThread = new Thread(this::readerThreadJob, "ShiledTV reader");
        readerThread.setDaemon(true);
        readerThread.start();
        this.readerThread = readerThread;

        Thread senderThread = new Thread(this::senderThreadJob, "ShieldTV sender");
        senderThread.setDaemon(true);
        senderThread.start();
        this.senderThread = senderThread;

        logger.debug("Starting keepalive job with interval {}", heartbeatInterval);
        keepAliveJob = scheduler.scheduleWithFixedDelay(this::sendKeepAlive, heartbeatInterval, heartbeatInterval,
                TimeUnit.SECONDS);

        String login = ShieldTVRequest.encodeMessage(ShieldTVRequest.loginRequest());
        sendCommand(new ShieldTVCommand(login));

        // updateStatus(ThingStatus.ONLINE);
    }

    private void scheduleConnectRetry(long waitMinutes) {
        logger.debug("Scheduling connection retry in {} minutes", waitMinutes);
        connectRetryJob = scheduler.schedule(this::connect, waitMinutes, TimeUnit.MINUTES);
    }

    /**
     * Disconnect from bridge, cancel retry and keepalive jobs, stop reader and writer threads, and clean up.
     *
     * @param interruptAll Set if reconnect task should be interrupted if running. Should be false when calling from
     *            connect or reconnect, and true when calling from dispose.
     */
    private synchronized void disconnect(boolean interruptAll) {
        logger.debug("Disconnecting");

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
        SSLSocket sslsocket = this.sslsocket;
        if (sslsocket != null) {
            try {
                sslsocket.close();
            } catch (IOException e) {
                logger.debug("Error closing SSL socket: {}", e.getMessage());
            }
            this.sslsocket = null;
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
        logger.debug("Attempting to reconnect to the shieldtv");
        isLoggedIn = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "reconnecting");
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
                logger.trace("Sending command {}", command);

                try {
                    BufferedWriter writer = this.writer;
                    if (writer != null) {
                        logger.trace("Raw command decodes as: {}", ShieldTVRequest.decodeMessage(command.toString()));
                        writer.write(command.toString());
                        writer.flush();
                    }
                } catch (InterruptedIOException e) {
                    logger.debug("Interrupted while sending");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Interrupted");
                    break; // exit loop and terminate thread
                } catch (IOException e) {
                    logger.warn("Communication error, will try to reconnect. Error: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    sendQueue.add(command); // Requeue command
                    reconnect();
                    break; // reconnect() will start a new thread; terminate this one
                }
                if (sendDelay > 0) {
                    Thread.sleep(sendDelay); // introduce delay to throttle send rate
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
            shieldtvMessageParser.handleMessage(sbReader.toString());
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
        shieldtvMessageParser.handleMessage(sbReader.toString());
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
            while (!Thread.interrupted() && reader != null
                    && (thisMsg = fixMessage(Integer.toHexString(reader.read()))) != null) {
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
            if (thisMsg == null) {
                logger.debug("End of input stream detected");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Connection lost");
            }
        } catch (InterruptedIOException e) {
            logger.debug("Interrupted while reading");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Interrupted");
        } catch (IOException e) {
            logger.debug("I/O error while reading from stream: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Runtime exception in reader thread", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } finally {
            logger.debug("Message reader thread exiting");
        }
    }

    private void sendKeepAlive() {
        logger.trace("Sending keepalive query");
        String keepalive = ShieldTVRequest.encodeMessage(ShieldTVRequest.keepAlive());
        sendCommand(new ShieldTVCommand(keepalive));
        reconnectTaskSchedule();
    }

    public void checkInitialized() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        if (statusInfo.getStatus() == ThingStatus.OFFLINE && STATUS_INITIALIZING.equals(statusInfo.getDescription())) {
            if (isLoggedIn) {
                updateStatus(ThingStatus.ONLINE);
                sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("080b120308cd08")));
                sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08f30712020805")));
                sendCommand(new ShieldTVCommand(ShieldTVRequest.encodeMessage("08f10712020800")));
            }
        }
    }

    /**
     * Schedules the reconnect task keepAliveReconnectJob to execute in KEEPALIVE_TIMEOUT_SECONDS. This should be
     * cancelled by calling reconnectTaskCancel() if a valid response is received from the bridge.
     */
    private void reconnectTaskSchedule() {
        synchronized (keepAliveReconnectLock) {
            keepAliveReconnectJob = scheduler.schedule(this::keepaliveTimeoutExpired, KEEPALIVE_TIMEOUT_SECONDS,
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
                logger.trace("Canceling scheduled reconnect job.");
                keepAliveReconnectJob.cancel(interrupt);
                this.keepAliveReconnectJob = null;
            }
        }
    }

    /**
     * Executed by keepAliveReconnectJob if it is not cancelled by the LEAP message parser calling
     * validMessageReceived() which in turn calls reconnectTaskCancel().
     */
    private void keepaliveTimeoutExpired() {
        logger.debug("Keepalive response timeout expired. Initiating reconnect.");
        reconnect();
    }

    @Override
    public void validMessageReceived() {
        reconnectTaskCancel(true); // Got a good message, so cancel reconnect task.
    }

    public void sendCommand(@Nullable ShieldTVCommand command) {
        if (command != null) {
            sendQueue.add(command);
        }
    }

    @Override
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
                    default:
                        logger.trace("Unknown Keypress: {}", command.toString());
                }
            }
        } else if (CHANNEL_PINCODE.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String pin = ShieldTVRequest.pinRequest(command.toString());
                String message = ShieldTVRequest.encodeMessage(pin);
                logger.trace("Raw Message Decodes as: {}", ShieldTVRequest.decodeMessage(message));
                sendCommand(new ShieldTVCommand(message));
            }
        } else if (CHANNEL_RAW.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String message = ShieldTVRequest.encodeMessage(command.toString());
                logger.trace("Raw Message Decodes as: {}", ShieldTVRequest.decodeMessage(message));
                sendCommand(new ShieldTVCommand(message));
            }
        } else if (CHANNEL_RAWMSG.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                shieldtvMessageParser.handleMessage(command.toString());
            }
        } else if (CHANNEL_APP.equals(channelUID.getId())) {
            if (command instanceof StringType) {
                String message = ShieldTVRequest.encodeMessage(ShieldTVRequest.startApp(command.toString()));
                sendCommand(new ShieldTVCommand(message));
            }
        }
    }
}
