package io.flic.fliclib.javaclient;

import io.flic.fliclib.javaclient.enums.CreateConnectionChannelError;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Implements a FlicClient over a TCP Socket.
 *
 * When this class is constructed, a socket connection is established.
 *
 * You may then send commands to the server and set timers.
 *
 * Once you are ready with the initialization you must call the {@link #handleEvents()} method which is a main loop that never exits, unless the socket is closed.
 *
 * For a more detailed description of all commands, events and enums, check the protocol specification.
 */
public class FlicClient {
    private Socket socket;
    private InputStream socketInputStream;
    private OutputStream socketOutputStream;

    private ConcurrentHashMap<Integer, ButtonScanner> scanners = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ButtonConnectionChannel> connectionChannels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ScanWizard> scanWizards = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, BatteryStatusListener> batteryStatusListeners = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<GetInfoResponseCallback> getInfoResponseCallbackQueue = new ConcurrentLinkedQueue<>();
    private ArrayDeque<GetButtonInfoResponseCallback> getButtonInfoResponseCallbackQueue = new ArrayDeque<>();

    private volatile GeneralCallbacks generalCallbacks = new GeneralCallbacks();

    private ConcurrentSkipListMap<Long, TimerTask> timers = new ConcurrentSkipListMap<>();

    private Thread handleEventsThread;

    /**
     * Create a FlicClient and connect to the specified hostName and TCP port
     *
     * @param hostName
     * @param port
     * @throws UnknownHostException
     * @throws IOException
     */
    public FlicClient(String hostName, int port) throws UnknownHostException, IOException {
        socket = new Socket(hostName, port);
        socket.setKeepAlive(true);
        socketInputStream = socket.getInputStream();
        socketOutputStream = socket.getOutputStream();
    }

    /**
     * Create a FlicClient and connect to the specified hostName using the default TCP port
     *
     * @param hostName
     * @throws UnknownHostException
     * @throws IOException
     */
    public FlicClient(String hostName) throws UnknownHostException, IOException {
        this(hostName, 5551);
    }

    /**
     * Close the socket.
     *
     * From this point any use of this FlicClient is illegal.
     * The {@link #handleEvents()} will return as soon as the closing is done.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        runOnHandleEventsThread(new TimerTask() {
            @Override
            public void run() throws IOException {
                socket.close();
            }
        });
    }

    /**
     * Set general callbacks to be called upon receiving some specific events.
     *
     * @param callbacks
     */
    public void setGeneralCallbacks(GeneralCallbacks callbacks) {
        if (callbacks == null) {
            callbacks = new GeneralCallbacks();
        }
        generalCallbacks = callbacks;
    }

    /**
     * Get info about the current state of the server.
     *
     * The server will send back its information directly and the callback will be called once the response arrives.
     *
     * @param callback
     * @throws IOException
     */
    public void getInfo(GetInfoResponseCallback callback) throws IOException {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }
        getInfoResponseCallbackQueue.add(callback);

        CmdGetInfo pkt = new CmdGetInfo();
        sendPacket(pkt);
    }

    /**
     * Get button info for a verified button.
     *
     * The server will send back its information directly and the callback will be called once the response arrives.
     * Responses will arrive in the same order as requested.
     *
     * If the button isn't verified, the data sent to callback will be null.
     *
     * @param bdaddr The bluetooth address.
     * @param callback Callback for the response.
     * @throws IOException
     */
    public void getButtonInfo(final Bdaddr bdaddr, final GetButtonInfoResponseCallback callback) throws IOException {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }
        // Run on events thread to ensure ordering if multiple requests are issued at the same time
        runOnHandleEventsThread(new TimerTask() {
            @Override
            public void run() throws IOException {
                getButtonInfoResponseCallbackQueue.add(callback);

                CmdGetButtonInfo pkt = new CmdGetButtonInfo();
                pkt.bdaddr = bdaddr;
                sendPacket(pkt);
            }
        });
    }

    /**
     * Add a scanner.
     *
     * The scan will start directly once the scanner is added.
     *
     * @param buttonScanner
     * @throws IOException
     */
    public void addScanner(ButtonScanner buttonScanner) throws IOException {
        if (buttonScanner == null) {
            throw new IllegalArgumentException("buttonScanner is null");
        }
        if (scanners.putIfAbsent(buttonScanner.scanId, buttonScanner) != null) {
            throw new IllegalArgumentException("Button scanner already added");
        }

        CmdCreateScanner pkt = new CmdCreateScanner();
        pkt.scanId = buttonScanner.scanId;
        sendPacket(pkt);
    }

    /**
     * Remove a scanner.
     *
     * @param buttonScanner The same scanner that was used in {@link #addScanner(ButtonScanner)}
     * @throws IOException
     */
    public void removeScanner(ButtonScanner buttonScanner) throws IOException {
        if (buttonScanner == null) {
            throw new IllegalArgumentException("buttonScanner is null");
        }
        if (scanners.remove(buttonScanner.scanId) == null) {
            throw new IllegalArgumentException("Button scanner was never added");
        }

        CmdRemoveScanner pkt = new CmdRemoveScanner();
        pkt.scanId = buttonScanner.scanId;
        sendPacket(pkt);
    }
    
    /**
     * Add a scan wizard.
     *
     * The scan wizard will start directly once the scan wizard is added.
     *
     * @param scanWizard
     * @throws IOException
     */
    public void addScanWizard(ScanWizard scanWizard) throws IOException {
        if (scanWizard == null) {
            throw new IllegalArgumentException("scanWizard is null");
        }
        if (scanWizards.putIfAbsent(scanWizard.scanWizardId, scanWizard) != null) {
            throw new IllegalArgumentException("Scan wizard already added");
        }
        
        CmdCreateScanWizard pkt = new CmdCreateScanWizard();
        pkt.scanWizardId = scanWizard.scanWizardId;
        sendPacket(pkt);
    }
    
    /**
     * Cancel a scan wizard.
     *
     * This will cancel an ongoing scan wizard.
     *
     * If cancelled due to this request, the result of the scan wizard will be WizardCancelledByUser.
     *
     * @param scanWizard The same scan wizard that was used in {@link #addScanWizard(ScanWizard)}
     * @throws IOException
     */
    public void cancelScanWizard(ScanWizard scanWizard) throws IOException {
        if (scanWizard == null) {
            throw new IllegalArgumentException("scanWizard is null");
        }
        
        CmdCancelScanWizard pkt = new CmdCancelScanWizard();
        pkt.scanWizardId = scanWizard.scanWizardId;
        sendPacket(pkt);
    }

    /**
     * Adds a connection channel to a specific Flic button.
     *
     * This will start listening for a specific Flic button's connection and button events.
     * Make sure the Flic is either in public mode (by holding it down for 7 seconds) or already verified before calling this method.
     *
     * The {@link ButtonConnectionChannel.Callbacks#onCreateConnectionChannelResponse}
     * method will be called after this command has been received by the server.
     *
     * You may have as many connection channels as you wish for a specific Flic Button.
     *
     * @param channel
     * @throws IOException
     */
    public void addConnectionChannel(ButtonConnectionChannel channel) throws IOException {
        if (channel == null) {
            throw new IllegalArgumentException("channel is null");
        }
        if (connectionChannels.putIfAbsent(channel.connId, channel) != null) {
            throw new IllegalArgumentException("Connection channel already added");
        }

        synchronized (channel.lock) {
            channel.client = this;

            CmdCreateConnectionChannel pkt = new CmdCreateConnectionChannel();
            pkt.connId = channel.connId;
            pkt.bdaddr = channel.getBdaddr();
            pkt.latencyMode = channel.getLatencyMode();
            pkt.autoDisconnectTime = channel.getAutoDisconnectTime();
            sendPacket(pkt);
        }
    }

    /**
     * Remove a connection channel.
     *
     * This will stop listening for new events for a specific connection channel that has previously been added.
     * Note: The effect of this command will take place at the time the {@link ButtonConnectionChannel.Callbacks#onRemoved} event arrives.
     *
     * @param channel
     * @throws IOException
     */
    public void removeConnectionChannel(ButtonConnectionChannel channel) throws IOException {
        if (channel == null) {
            throw new IllegalArgumentException("channel is null");
        }

        CmdRemoveConnectionChannel pkt = new CmdRemoveConnectionChannel();
        pkt.connId = channel.connId;
        sendPacket(pkt);
    }

    /**
     * Force disconnection or cancel pending connection of a specific Flic button.
     *
     * This removes all connection channels for all clients connected to the server for this specific Flic button.
     *
     * @param bdaddr
     * @throws IOException
     */
    public void forceDisconnect(Bdaddr bdaddr) throws IOException {
        if (bdaddr == null) {
            throw new IllegalArgumentException("bdaddr is null");
        }

        CmdForceDisconnect pkt = new CmdForceDisconnect();
        pkt.bdaddr = bdaddr;
        sendPacket(pkt);
    }
    
    /**
     * Delete a button.
     *
     * @param bdaddr
     * @throws IOException
     */
    public void deleteButton(Bdaddr bdaddr) throws IOException {
        if (bdaddr == null) {
            throw new IllegalArgumentException("bdaddr is null");
        }

        CmdDeleteButton pkt = new CmdDeleteButton();
        pkt.bdaddr = bdaddr;
        sendPacket(pkt);
    }

    /**
     * Add a battery status listener.
     *
     * @param listener
     * @throws IOException
     */
    public void addBatteryStatusListener(BatteryStatusListener listener) throws IOException {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        if (batteryStatusListeners.putIfAbsent(listener.listenerId, listener) != null) {
            throw new IllegalArgumentException("Battery status listener already added");
        }

        CmdCreateBatteryStatusListener pkt = new CmdCreateBatteryStatusListener();
        pkt.listenerId = listener.listenerId;
        pkt.bdaddr = listener.getBdaddr();
        sendPacket(pkt);
    }

    /**
     * Remove a battery status listener
     *
     * @param listener
     * @throws IOException
     */
    public void removeBatteryStatusListener(BatteryStatusListener listener) throws IOException {
        if (listener == null) {
            throw new IllegalArgumentException("buttonScanner is null");
        }
        if (batteryStatusListeners.remove(listener.listenerId) == null) {
            throw new IllegalArgumentException("Battery status listener was never added");
        }

        CmdRemoveBatteryStatusListener pkt = new CmdRemoveBatteryStatusListener();
        pkt.listenerId = listener.listenerId;
        sendPacket(pkt);
    }

    void sendPacket(CommandPacket packet) throws IOException {
        byte[] bytes = packet.construct();
        synchronized (socketOutputStream) {
            socketOutputStream.write(bytes);
        }
    }

    /**
     * Set a timer.
     *
     * This timer task will run after the specified timeoutMillis on the thread that handles the events.
     *
     * @param timeoutMillis
     * @param timerTask
     * @throws IOException
     */
    public void setTimer(int timeoutMillis, TimerTask timerTask) throws IOException {
        long pointInTime = System.nanoTime() + timeoutMillis * 1000000L;
        while (timers.putIfAbsent(pointInTime, timerTask) != null) {
            pointInTime++;
        }
        if (handleEventsThread != Thread.currentThread()) {
            CmdPing pkt = new CmdPing();
            pkt.pingId = 0;
            sendPacket(pkt);
        }
    }

    /**
     * Run a task on the thread that handles the events.
     *
     * @param task
     * @throws IOException
     */
    public void runOnHandleEventsThread(TimerTask task) throws IOException {
        if (handleEventsThread == Thread.currentThread()) {
            task.run();
        } else {
            setTimer(0, task);
        }
    }

    /**
     * Start the main loop for this client.
     *
     * This method will not return until the socket has been closed.
     * Once it has returned, any use of this FlicClient is illegal.
     *
     * @throws IOException
     */
    public void handleEvents() throws IOException {
        handleEventsThread = Thread.currentThread();
        while (!Thread.currentThread().isInterrupted()) {
            Map.Entry<Long, TimerTask> firstTimer = timers.firstEntry();
            long timeout = 0;
            if (firstTimer != null) {
                timeout = firstTimer.getKey() - System.nanoTime();
                if (timeout <= 0) {
                    timers.remove(firstTimer.getKey(), firstTimer.getValue());
                    firstTimer.getValue().run();
                    continue;
                }
            }

            if (socket.isClosed()) {
                break;
            }

            int len0;
            socket.setSoTimeout((int)(timeout / 1000000));
            try {
                len0 = socketInputStream.read();
            } catch (SocketTimeoutException e) {
                continue;
            }
            int len1 = socketInputStream.read();
            int len = len0 | (len1 << 8);
            if ((len >> 16) == -1) {
                break;
            }
            if (len == 0) {
                continue;
            }
            byte[] pkt = new byte[len];

            int pos = 0;
            while (pos < len) {
                int nbytes = socketInputStream.read(pkt, pos, len - pos);
                if (nbytes == -1) {
                    break;
                }
                pos += nbytes;
            }
            if (len == 1) {
                continue;
            }
            dispatchPacket(pkt);
        }
        socket.close();
    }

    private void dispatchPacket(byte[] packet) throws IOException {
        int opcode = packet[0];
        switch (opcode) {
            case EventPacket.EVT_ADVERTISEMENT_PACKET_OPCODE: {
                EvtAdvertisementPacket pkt = new EvtAdvertisementPacket();
                pkt.parse(packet);
                ButtonScanner scanner = scanners.get(pkt.scanId);
                if (scanner != null) {
                    scanner.onAdvertisementPacket(pkt.addr, pkt.name, pkt.rssi, pkt.isPrivate, pkt.alreadyVerified, pkt.alreadyConnectedToThisDevice, pkt.alreadyConnectedToOtherDevice);
                }
                break;
            }
            case EventPacket.EVT_CREATE_CONNECTION_CHANNEL_RESPONSE_OPCODE: {
                EvtCreateConnectionChannelResponse pkt = new EvtCreateConnectionChannelResponse();
                pkt.parse(packet);
                ButtonConnectionChannel channel = connectionChannels.get(pkt.connId);
                if (channel != null) {
                    if (pkt.connectionChannelError != CreateConnectionChannelError.NoError) {
                        connectionChannels.remove(channel.connId);
                    }
                    channel.callbacks.onCreateConnectionChannelResponse(channel, pkt.connectionChannelError, pkt.connectionStatus);
                }
                break;
            }
            case EventPacket.EVT_CONNECTION_STATUS_CHANGED_OPCODE: {
                EvtConnectionStatusChanged pkt = new EvtConnectionStatusChanged();
                pkt.parse(packet);
                ButtonConnectionChannel channel = connectionChannels.get(pkt.connId);
                if (channel != null) {
                    channel.callbacks.onConnectionStatusChanged(channel, pkt.connectionStatus, pkt.disconnectReason);
                }
                break;
            }
            case EventPacket.EVT_CONNECTION_CHANNEL_REMOVED_OPCODE: {
                EvtConnectionChannelRemoved pkt = new EvtConnectionChannelRemoved();
                pkt.parse(packet);
                ButtonConnectionChannel channel = connectionChannels.get(pkt.connId);
                if (channel != null) {
                    connectionChannels.remove(channel.connId);
                    channel.callbacks.onRemoved(channel, pkt.removedReason);
                }
                break;
            }
            case EventPacket.EVT_BUTTON_UP_OR_DOWN_OPCODE:
            case EventPacket.EVT_BUTTON_CLICK_OR_HOLD_OPCODE:
            case EventPacket.EVT_BUTTON_SINGLE_OR_DOUBLE_CLICK_OPCODE:
            case EventPacket.EVT_BUTTON_SINGLE_OR_DOUBLE_CLICK_OR_HOLD_OPCODE: {
                EvtButtonEvent pkt = new EvtButtonEvent();
                pkt.parse(packet);
                ButtonConnectionChannel channel = connectionChannels.get(pkt.connId);
                if (channel != null) {
                    if (opcode == EventPacket.EVT_BUTTON_UP_OR_DOWN_OPCODE) {
                        channel.callbacks.onButtonUpOrDown(channel, pkt.clickType, pkt.wasQueued, pkt.timeDiff);
                    } else if (opcode == EventPacket.EVT_BUTTON_CLICK_OR_HOLD_OPCODE) {
                        channel.callbacks.onButtonClickOrHold(channel, pkt.clickType, pkt.wasQueued, pkt.timeDiff);
                    } else if (opcode == EventPacket.EVT_BUTTON_SINGLE_OR_DOUBLE_CLICK_OPCODE) {
                        channel.callbacks.onButtonSingleOrDoubleClick(channel, pkt.clickType, pkt.wasQueued, pkt.timeDiff);
                    } else if (opcode == EventPacket.EVT_BUTTON_SINGLE_OR_DOUBLE_CLICK_OR_HOLD_OPCODE) {
                        channel.callbacks.onButtonSingleOrDoubleClickOrHold(channel, pkt.clickType, pkt.wasQueued, pkt.timeDiff);
                    }
                }
                break;
            }
            case EventPacket.EVT_NEW_VERIFIED_BUTTON_OPCODE: {
                EvtNewVerifiedButton pkt = new EvtNewVerifiedButton();
                pkt.parse(packet);
                GeneralCallbacks gc = generalCallbacks;
                if (gc != null) {
                    gc.onNewVerifiedButton(pkt.bdaddr);
                }
                break;
            }
            case EventPacket.EVT_GET_INFO_RESPONSE_OPCODE: {
                EvtGetInfoResponse pkt = new EvtGetInfoResponse();
                pkt.parse(packet);
                getInfoResponseCallbackQueue.remove().onGetInfoResponse(pkt.bluetoothControllerState, pkt.myBdAddr, pkt.myBdAddrType, pkt.maxPendingConnections, pkt.maxConcurrentlyConnectedButtons, pkt.currentPendingConnections, pkt.currentlyNoSpaceForNewConnections, pkt.bdAddrOfVerifiedButtons);
                break;
            }
            case EventPacket.EVT_NO_SPACE_FOR_NEW_CONNECTION_OPCODE: {
                EvtNoSpaceForNewConnection pkt = new EvtNoSpaceForNewConnection();
                pkt.parse(packet);
                GeneralCallbacks gc = generalCallbacks;
                if (gc != null) {
                    gc.onNoSpaceForNewConnection(pkt.maxConcurrentlyConnectedButtons);
                }
                break;
            }
            case EventPacket.EVT_GOT_SPACE_FOR_NEW_CONNECTION_OPCODE: {
                EvtGotSpaceForNewConnection pkt = new EvtGotSpaceForNewConnection();
                pkt.parse(packet);
                GeneralCallbacks gc = generalCallbacks;
                if (gc != null) {
                    gc.onGotSpaceForNewConnection(pkt.maxConcurrentlyConnectedButtons);
                }
                break;
            }
            case EventPacket.EVT_BLUETOOTH_CONTROLLER_STATE_CHANGE_OPCODE: {
                EvtBluetoothControllerStateChange pkt = new EvtBluetoothControllerStateChange();
                pkt.parse(packet);
                GeneralCallbacks gc = generalCallbacks;
                if (gc != null) {
                    gc.onBluetoothControllerStateChange(pkt.state);
                }
                break;
            }
            case EventPacket.EVT_GET_BUTTON_INFO_RESPONSE_OPCODE: {
                EvtGetButtonInfoResponse pkt = new EvtGetButtonInfoResponse();
                pkt.parse(packet);
                getButtonInfoResponseCallbackQueue.remove().onGetButtonInfoResponse(pkt.bdaddr, pkt.uuid, pkt.color, pkt.serialNumber);
                break;
            }
            case EventPacket.EVT_SCAN_WIZARD_FOUND_PRIVATE_BUTTON_OPCODE: {
                EvtScanWizardFoundPrivateButton pkt = new EvtScanWizardFoundPrivateButton();
                pkt.parse(packet);
                ScanWizard wizard = scanWizards.get(pkt.scanWizardId);
                if (wizard != null) {
                    wizard.onFoundPrivateButton();
                }
                break;
            }
            case EventPacket.EVT_SCAN_WIZARD_FOUND_PUBLIC_BUTTON_OPCODE: {
                EvtScanWizardFoundPublicButton pkt = new EvtScanWizardFoundPublicButton();
                pkt.parse(packet);
                ScanWizard wizard = scanWizards.get(pkt.scanWizardId);
                if (wizard != null) {
                    wizard.bdaddr = pkt.addr;
                    wizard.name = pkt.name;
                    wizard.onFoundPublicButton(wizard.bdaddr, wizard.name);
                }
                break;
            }
            case EventPacket.EVT_SCAN_WIZARD_BUTTON_CONNECTED_OPCODE: {
                EvtScanWizardButtonConnected pkt = new EvtScanWizardButtonConnected();
                pkt.parse(packet);
                ScanWizard wizard = scanWizards.get(pkt.scanWizardId);
                if (wizard != null) {
                    wizard.onButtonConnected(wizard.bdaddr, wizard.name);
                }
                break;
            }
            case EventPacket.EVT_SCAN_WIZARD_COMPLETED_OPCODE: {
                EvtScanWizardCompleted pkt = new EvtScanWizardCompleted();
                pkt.parse(packet);
                ScanWizard wizard = scanWizards.get(pkt.scanWizardId);
                scanWizards.remove(pkt.scanWizardId);
                if (wizard != null) {
                    Bdaddr bdaddr = wizard.bdaddr;
                    String name = wizard.name;
                    wizard.bdaddr = null;
                    wizard.name = null;
                    wizard.onCompleted(pkt.result, bdaddr, name);
                }
                break;
            }
            case EventPacket.EVT_BUTTON_DELETED_OPCODE: {
                EvtButtonDeleted pkt = new EvtButtonDeleted();
                pkt.parse(packet);
                GeneralCallbacks gc = generalCallbacks;
                if (gc != null) {
                    gc.onButtonDeleted(pkt.bdaddr, pkt.deletedByThisClient);
                }
                break;
            }
            case EventPacket.EVT_BATTERY_STATUS_OPCODE: {
                EvtBatteryStatus pkt = new EvtBatteryStatus();
                pkt.parse(packet);
                BatteryStatusListener listener = batteryStatusListeners.get(pkt.listenerId);
                if (listener != null) {
                    listener.callbacks.onBatteryStatus(listener.getBdaddr(), pkt.batteryPercentage, pkt.timestamp);
                }
                break;
            }
        }
    }
}
