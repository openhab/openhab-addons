/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.handler;

import static org.openhab.binding.max.MaxBinding.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.max.MaxBinding;
import org.openhab.binding.max.config.MaxCubeBridgeConfiguration;
import org.openhab.binding.max.internal.command.A_Command;
import org.openhab.binding.max.internal.command.C_Command;
import org.openhab.binding.max.internal.command.CubeCommand;
import org.openhab.binding.max.internal.command.F_Command;
import org.openhab.binding.max.internal.command.L_Command;
import org.openhab.binding.max.internal.command.M_Command;
import org.openhab.binding.max.internal.command.N_Command;
import org.openhab.binding.max.internal.command.Q_Command;
import org.openhab.binding.max.internal.command.S_Command;
import org.openhab.binding.max.internal.command.T_Command;
import org.openhab.binding.max.internal.command.UdpCubeCommand;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.device.DeviceConfiguration;
import org.openhab.binding.max.internal.device.DeviceInformation;
import org.openhab.binding.max.internal.device.DeviceType;
import org.openhab.binding.max.internal.device.HeatingThermostat;
import org.openhab.binding.max.internal.device.RoomInformation;
import org.openhab.binding.max.internal.device.ThermostatModeType;
import org.openhab.binding.max.internal.exceptions.UnprocessableMessageException;
import org.openhab.binding.max.internal.message.C_Message;
import org.openhab.binding.max.internal.message.F_Message;
import org.openhab.binding.max.internal.message.H_Message;
import org.openhab.binding.max.internal.message.L_Message;
import org.openhab.binding.max.internal.message.M_Message;
import org.openhab.binding.max.internal.message.Message;
import org.openhab.binding.max.internal.message.MessageProcessor;
import org.openhab.binding.max.internal.message.MessageType;
import org.openhab.binding.max.internal.message.N_Message;
import org.openhab.binding.max.internal.message.S_Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MaxCubeBridgeHandler} is the handler for a MAX! Cube and connects it
 * to the framework. All {@link MaxDevicesHandler}s use the
 * {@link MaxCubeBridgeHandler} to execute the actual commands.
 *
 * @author Marcel Verpaalen - Initial contribution OH2 version
 * @author Andreas Heil (info@aheil.de) - OH1 version
 * @author Bernd Michael Helm (bernd.helm at helmundwalter.de) - Exclusive mode
 *
 */
public class MaxCubeBridgeHandler extends BaseBridgeHandler {

    public MaxCubeBridgeHandler(Bridge br) {
        super(br);
    }

    private Logger logger = LoggerFactory.getLogger(MaxCubeBridgeHandler.class);

    /** The refresh interval which is used to poll given MAX! Cube */
    private long refreshInterval = 30;
    ScheduledFuture<?> refreshJob;

    /** timeout on network connection **/
    private static final int NETWORK_TIMEOUT = 10000;

    private ArrayList<Device> devices = new ArrayList<Device>();
    private ArrayList<RoomInformation> rooms;
    private HashSet<String> lastActiveDevices = new HashSet<String>();

    /** MAX! Thermostat default off temperature */
    private static final DecimalType DEFAULT_OFF_TEMPERATURE = new DecimalType(4.5);

    /** MAX! Thermostat default on temperature */
    private static final DecimalType DEFAULT_ON_TEMPERATURE = new DecimalType(30.5);

    private ArrayList<DeviceConfiguration> configurations = new ArrayList<DeviceConfiguration>();

    /** maximum queue size that we're allowing */
    private static final int MAX_COMMANDS = 50;
    private ArrayBlockingQueue<SendCommand> commandQueue = new ArrayBlockingQueue<SendCommand>(MAX_COMMANDS);

    private SendCommand lastCommandId = null;

    private String ipAddress;
    private int port;
    private boolean exclusive;
    private int maxRequestsPerConnection;
    private String ntpServer1;
    private String ntpServer2;
    private int requestCount = 0;
    private boolean propertiesSet = false;
    private boolean roomPropertiesSet = false;

    MessageProcessor messageProcessor = new MessageProcessor();

    /**
     * Duty cycle of the cube
     */
    private int dutyCycle = 0;

    /**
     * The available memory slots of the cube
     */
    private int freeMemorySlots;

    /**
     * connection socket and reader/writer for execute method
     */
    private Socket socket = null;
    private BufferedReader reader = null;
    private OutputStreamWriter writer = null;

    private boolean previousOnline = false;

    private Set<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArraySet<>();

    private ScheduledFuture<?> pollingJob;
    private Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            refreshData();
        }
    };
    private ScheduledFuture<?> sendCommandJob;
    private long sendCommandInterval = 5;
    private Runnable sendCommandRunnable = new Runnable() {
        @Override
        public void run() {
            sendCommands();
        }
    };

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refresh command received.");
            refreshData();
        } else {
            logger.warn("No bridge commands defined. Cannot process '{}'.", command.toString());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopAutomaticRefresh();
        clearDeviceList();
        socketClose();
        super.dispose();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MAX! Cube bridge handler.");

        MaxCubeBridgeConfiguration configuration = getConfigAs(MaxCubeBridgeConfiguration.class);
        port = configuration.port;
        ipAddress = configuration.ipAddress;
        refreshInterval = configuration.refreshInterval;
        exclusive = configuration.exclusive;
        maxRequestsPerConnection = configuration.maxRequestsPerConnection;
        ntpServer1 = configuration.ntpServer1;
        ntpServer2 = configuration.ntpServer2;
        logger.debug("Cube IP         {}.", ipAddress);
        logger.debug("Port            {}.", port);
        logger.debug("RefreshInterval {}.", refreshInterval);
        logger.debug("Exclusive mode  {}.", exclusive);
        logger.debug("Max Requests    {}.", maxRequestsPerConnection);

        previousOnline = true; // To trigger offline in case no connection @ startup
        startAutomaticRefresh();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        boolean refresh = true;
        logger.debug("MAX! Cube {}: Configuration update received", getThing().getThingTypeUID());

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            logger.debug("MAX! Cube {}: Configuration update {} to {}", getThing().getThingTypeUID(),
                    configurationParameter.getKey(), configurationParameter.getValue());
            if (configurationParameter.getKey().startsWith("ntp")) {
                sendNtpUpdate(configurationParameters);
                if (configurationParameters.size() == 1) {
                    refresh = false;
                }
            }
            if (configurationParameter.getKey().startsWith("action-")) {
                if (configurationParameter.getValue().toString().equals(BUTTON_ACTION_VALUE)) {
                    configurationParameter.setValue(BigDecimal.valueOf(BUTTON_NOACTION_VALUE));
                    if (configurationParameter.getKey().equals(ACTION_CUBE_REBOOT)) {
                        cubeReboot();
                    }
                    if (configurationParameter.getKey().equals(ACTION_CUBE_RESET)) {
                        cubeConfigReset();
                        refresh = false;
                    }
                }
            }

            configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
        }

        // Persist changes and restart with new parameters
        updateConfiguration(configuration);

        if (refresh) {
            stopAutomaticRefresh();
            clearDeviceList();
            socketClose();
            initialize();
        }
    }

    private void cubeConfigReset() {
        logger.info("Resetting configuration for MAX! Cube {}", getThing().getUID());
        sendCubeCommand(new A_Command());
        for (Device di : devices) {
            for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                try {
                    deviceStatusListener.onDeviceRemoved(this, di);
                } catch (Exception e) {
                    logger.error("An exception occurred while calling the DeviceStatusListener", e);
                    unregisterDeviceStatusListener(deviceStatusListener);
                }
            }
        }
        clearDeviceList();
        propertiesSet = false;
        roomPropertiesSet = false;

    }

    /**
     *
     */
    private void cubeReboot() {
        logger.info("Rebooting MAX! Cube {}", getThing().getThingTypeUID());
        MaxCubeBridgeConfiguration maxConfiguration = getConfigAs(MaxCubeBridgeConfiguration.class);
        UdpCubeCommand reset = new UdpCubeCommand(UdpCubeCommand.udpCommandType.RESET, maxConfiguration.serialNumber);
        reset.setIpAddress(maxConfiguration.ipAddress);
        reset.send();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Rebooting");

    }

    public void deviceInclusion() {
        if (previousOnline) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Inclusion");
            logger.info("Start MAX! inclusion mode for 60 seconds");
            try {
                socket.setSoTimeout((80000));
                if (!sendCubeCommand(new N_Command())) {
                    logger.debug("Error during Inclusion mode");
                }
                logger.info("End MAX! inclusion mode");
                socket.setSoTimeout((NETWORK_TIMEOUT));
            } catch (SocketException e) {
                logger.debug("Timeout during MAX! inclusion mode");
            }
        } else {
            logger.warn("Need to be online to start inclusion mode");
        }
    }

    private synchronized void startAutomaticRefresh() {
        if (pollingJob == null || pollingJob.isCancelled()) {
            pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, refreshInterval, TimeUnit.SECONDS);
        }
        if (sendCommandJob == null || sendCommandJob.isCancelled()) {
            sendCommandJob = scheduler.scheduleWithFixedDelay(sendCommandRunnable, 0, sendCommandInterval,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * stops the refreshing jobs
     */
    private void stopAutomaticRefresh() {
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
        if (sendCommandJob != null && !sendCommandJob.isCancelled()) {
            sendCommandJob.cancel(true);
            sendCommandJob = null;
        }
    }

    /**
     * Takes a command from the command queue and send it to
     * {@link executeCommand} for execution.
     *
     */
    private synchronized void sendCommands() {

        SendCommand sendCommand = commandQueue.poll();
        if (sendCommand != null) {
            CubeCommand cmd = sendCommand.getCubeCommand();
            if (cmd == null) {
                cmd = getCommand(sendCommand);
            }
            if (cmd != null) {
                // Actual sending of the data to the Max! Cube Lan Gateway
                logger.debug("Command {} ({}:{}) sent to MAX! Cube at IP: {}", sendCommand.getId(),
                        sendCommand.getKey(), sendCommand.getCommandText(), ipAddress);

                if (sendCubeCommand(cmd)) {
                    logger.trace("Command {} ({}:{}) completed for MAX! Cube at IP: {}", sendCommand.getId(),
                            sendCommand.getKey(), sendCommand.getCommandText(), ipAddress);
                } else {
                    logger.warn("Error sending command {} ({}:{}) to MAX! Cube at IP: {}", sendCommand.getId(),
                            sendCommand.getKey(), sendCommand.getCommandText(), ipAddress);
                }
            }
        }
    }

    /**
     * initiates read data from the MAX! Cube bridge
     */
    private void refreshData() {

        try {
            if (sendCubeCommand(new L_Command())) {
                updateStatus(ThingStatus.ONLINE);
                previousOnline = true;
                for (Device di : devices) {
                    if (lastActiveDevices != null && lastActiveDevices.contains(di.getSerialNumber())) {
                        for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                            try {
                                deviceStatusListener.onDeviceStateChanged(getThing().getUID(), di);
                            } catch (Exception e) {
                                logger.error("An exception occurred while calling the DeviceStatusListener", e);
                                unregisterDeviceStatusListener(deviceStatusListener);
                            }
                        }
                    }
                    // New device, not seen before, pass to Discovery
                    else {
                        for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                            try {
                                deviceStatusListener.onDeviceAdded(getThing(), di);
                                di.setUpdated(true);
                                deviceStatusListener.onDeviceStateChanged(getThing().getUID(), di);
                            } catch (Exception e) {
                                logger.error("An exception occurred while calling the DeviceStatusListener", e);
                            }
                            lastActiveDevices.add(di.getSerialNumber());
                        }
                    }
                }
            } else if (previousOnline) {
                onConnectionLost();
            }

        } catch (Exception e) {
            logger.debug("Unexpected exception occurred during execution: {}", e.getMessage(), e);
        }
    }

    public void onConnectionLost() {
        logger.debug("Bridge connection lost. Updating thing status to OFFLINE.");
        previousOnline = false;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        for (Thing thing : getThing().getThings()) {
            ThingHandler handler = thing.getHandler();
            if (handler != null && handler instanceof MaxDevicesHandler) {
                ((MaxDevicesHandler) handler).setForceRefresh();
            }
        }
        clearDeviceList();
    }

    public void onConnection() {
        logger.debug("Bridge connected. Updating thing status to ONLINE.");
        updateStatus(ThingStatus.ONLINE);
    }

    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new NullPointerException("It's not allowed to pass a null deviceStatusListener.");
        }
        boolean result = deviceStatusListeners.add(deviceStatusListener);
        if (result) {
            // onUpdate();
        }
        return result;
    }

    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new NullPointerException("It's not allowed to pass a null deviceStatusListener.");
        }
        boolean result = deviceStatusListeners.remove(deviceStatusListener);
        if (result) {
            clearDeviceList();
        }
        return result;
    }

    public void clearDeviceList() {
        lastActiveDevices.clear();
    }

    /**
     * Connects to the Max! Cube Lan gateway and send a command to Cube
     * and process the message
     *
     * @param {@link CubeCommand}
     * @return boolean success
     */
    private synchronized boolean sendCubeCommand(CubeCommand command) {
        synchronized (MaxCubeBridgeHandler.class) {
            boolean sendSuccess = false;
            try {
                if (socket == null || socket.isClosed()) {
                    this.socketConnect();
                } else {
                    if (maxRequestsPerConnection > 0 && requestCount >= maxRequestsPerConnection) {
                        logger.debug("maxRequestsPerConnection reached, reconnecting.");
                        socket.close();
                        this.socketConnect();
                    } else {

                        if (requestCount == 0) {
                            logger.debug("Connect to MAX! Cube");
                            readliness("L:");

                        }
                        if (!(requestCount == 0 && command instanceof L_Command)) {

                            logger.debug("Sending request #{} to MAX! Cube", this.requestCount);
                            if (writer == null) {
                                logger.warn("Can't write to MAX! Cube");
                                this.socketConnect();
                            }

                            writer.write(command.getCommandString());
                            logger.trace("Write string to Max! Cube {}: {}", ipAddress, command.getCommandString());
                            writer.flush();
                            if (command.getReturnStrings() != null) {
                                readliness(command.getReturnStrings());
                            } else {
                                socketClose();
                            }
                        }
                    }
                }

                requestCount++;
                sendSuccess = true;

                if (!exclusive) {
                    socketClose();
                }
            } catch (ConnectException e) {
                logger.debug("Connection timed out on {} port {}", ipAddress, port);
                sendSuccess = false;
                socketClose(); // reconnect on next execution
            } catch (UnknownHostException e) {
                logger.debug("Host error occurred during execution: {}", e.getMessage());
                sendSuccess = false;
                socketClose(); // reconnect on next execution
            } catch (IOException e) {
                logger.debug("IO error occurred during execution: {}", e.getMessage());
                sendSuccess = false;
                socketClose(); // reconnect on next execution
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                sendSuccess = false;
                socketClose(); // reconnect on next execution
            }
            return sendSuccess;
        }
    }

    /**
     * Read line from the Cube and process the message.
     *
     * @param terminator String with ending messagetype e.g. L:
     * @throws IOException
     */
    private void readliness(String terminator) throws IOException {
        if (terminator == null) {
            return;
        }
        boolean cont = true;
        while (cont) {
            String raw = reader.readLine();
            if (raw != null) {
                logger.trace("message block: '{}'", raw);
                try {
                    this.messageProcessor.addReceivedLine(raw);
                    if (this.messageProcessor.isMessageAvailable()) {
                        Message message = this.messageProcessor.pull();
                        processMessage(message);

                    }
                } catch (UnprocessableMessageException e) {
                    if (raw.contentEquals("M:")) {
                        logger.info("No Rooms information found. Configure your MAX! Cube: {}", ipAddress);
                        this.messageProcessor.reset();
                    } else {
                        logger.info("Message could not be processed: '{}' from MAX! Cube lan gateway: {}:", raw,
                                ipAddress);
                        this.messageProcessor.reset();
                    }
                } catch (Exception e) {
                    logger.info("Error while handling message block: '{}' from MAX! Cube lan gateway: {}:", raw,
                            ipAddress, e.getMessage(), e);
                    this.messageProcessor.reset();
                }
                if (terminator == null || raw.startsWith(terminator)) {
                    cont = false;
                }
            } else {
                cont = false;
            }
        }
    }

    /**
     * Processes the message
     *
     * @param Message
     *            the decoded message data
     */
    private void processMessage(Message message) {

        if (message != null) {
            message.debug(logger);
            if (message.getType() == MessageType.H) {
                int freeMemorySlotsMsg = ((H_Message) message).getFreeMemorySlots();
                int dutyCycleMsg = ((H_Message) message).getDutyCycle();
                if (freeMemorySlotsMsg != freeMemorySlots || dutyCycleMsg != dutyCycle) {
                    freeMemorySlots = freeMemorySlotsMsg;
                    dutyCycle = dutyCycleMsg;
                    updateCubeState();
                }
                if (!propertiesSet) {
                    setProperties((H_Message) message);
                    queueCommand(new SendCommand("Cube(" + getThing().getUID().getId() + ")", new F_Command(),
                            "Request NTP info"));
                }

            }
            if (message.getType() == MessageType.M) {
                M_Message msg = (M_Message) message;
                rooms = new ArrayList<RoomInformation>(msg.rooms);

                if (!roomPropertiesSet) {
                    setProperties(msg);
                }
                setProperties(msg);
                for (DeviceInformation di : msg.devices) {
                    DeviceConfiguration c = null;
                    for (DeviceConfiguration conf : configurations) {
                        if (conf.getSerialNumber().equalsIgnoreCase(di.getSerialNumber())) {
                            c = conf;
                            break;
                        }
                    }

                    if (c != null) {
                        configurations.remove(c);
                    }

                    c = DeviceConfiguration.create(di);
                    configurations.add(c);
                    c.setRoomId(di.getRoomId());
                    String roomName = "";
                    for (RoomInformation room : msg.rooms) {
                        if (room.getPosition() == di.getRoomId()) {
                            roomName = room.getName();
                        }
                    }
                    c.setRoomName(roomName);
                }
            } else if (message.getType() == MessageType.C) {
                DeviceConfiguration c = null;
                for (DeviceConfiguration conf : configurations) {
                    if (conf.getSerialNumber().equalsIgnoreCase(((C_Message) message).getSerialNumber())) {
                        c = conf;
                        break;
                    }
                }

                if (c == null) {
                    configurations.add(DeviceConfiguration.create(message));
                } else {
                    c.setValues((C_Message) message);
                    Device di = getDevice(((C_Message) message).getSerialNumber());
                    if (di != null) {
                        di.setProperties(((C_Message) message).getProperties());
                        ;
                    }
                }
                if (exclusive == true) {
                    for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                        try {
                            Device di = getDevice(((C_Message) message).getSerialNumber());
                            if (di != null) {
                                deviceStatusListener.onDeviceConfigUpdate(getThing(), di);
                            }
                        } catch (NullPointerException e) {
                            // ignore
                        } catch (Exception e) {
                            logger.error("An exception occurred while calling the DeviceStatusListener", e);
                            unregisterDeviceStatusListener(deviceStatusListener);
                        }
                    }
                }
            } else if (message.getType() == MessageType.L) {
                ((L_Message) message).updateDevices(devices, configurations);
                logger.trace("{} devices found.", devices.size());
            } else if (message.getType() == MessageType.S) {
                dutyCycle = ((S_Message) message).getDutyCycle();
                freeMemorySlots = ((S_Message) message).getFreeMemorySlots();
                updateCubeState();
                if (((S_Message) message).isCommandDiscarded()) {
                    logger.info("Last Send Command discarded. Duty Cycle: {}, Free Memory Slots: {}", dutyCycle,
                            freeMemorySlots);
                } else {
                    logger.debug("S message. Duty Cycle: {}, Free Memory Slots: {}", dutyCycle, freeMemorySlots);
                }
            } else if (message.getType() == MessageType.N) {
                if (((N_Message) message).getRfAddress() != null) {
                    newInclusionDeviceFound((N_Message) message);
                }
            } else if (message.getType() == MessageType.F) {
                setProperties((F_Message) message);
            }
        }
    }

    /**
     * @param {@link: N_Message} returned from the Cube with new device information
     */
    private void newInclusionDeviceFound(N_Message message) {
        logger.info("New {} found. Serial: {}, rfaddress: {}", message.getDeviceType().toString(),
                message.getSerialNumber(), message.getRfAddress());
        // Send C command to get the configuration so it will be added to discovery
        String newSerial = message.getSerialNumber();
        queueCommand(new SendCommand(newSerial, new C_Command(message.getRfAddress()), "Refresh " + newSerial));
    }

    /**
     * Set the properties for this device
     *
     * @param H_Message
     */
    private void setProperties(H_Message message) {
        try {
            logger.debug("MAX! Cube properties update");
            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_MODEL_ID, DeviceType.Cube.toString());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, message.getFirmwareVersion());
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, message.getSerialNumber());
            properties.put(Thing.PROPERTY_VENDOR, MaxBinding.PROPERTY_VENDOR_NAME);
            updateProperties(properties);
            // TODO: Remove this once UI is displaying this info
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                logger.debug("key: {}  : {}", entry.getKey(), entry.getValue());
            }
            Configuration configuration = editConfiguration();
            configuration.put(MaxBinding.PROPERTY_RFADDRESS, message.getRFAddress());
            configuration.put(MaxBinding.PROPERTY_SERIAL_NUMBER, message.getSerialNumber());
            updateConfiguration(configuration);
            logger.debug("properties updated");
            propertiesSet = true;
        } catch (Exception e) {
            logger.debug("Exception occurred during property update: {}", e.getMessage(), e);
        }
    }

    /**
     * Set the properties for this device
     *
     * @param M_Message
     */
    private void setProperties(M_Message message) {
        Configuration configuration = editConfiguration();
        for (RoomInformation room : message.rooms) {
            configuration.put("room" + Integer.toString(room.getPosition()), room.getName());
            logger.trace("Room '{}' name='{}'", "Room" + Integer.toString(room.getPosition()), room.getName());
        }
        updateConfiguration(configuration);
        logger.debug("Room properties updated");
        roomPropertiesSet = true;
    }

    /**
     * Set the properties for this device
     *
     * @param F_Message
     */
    private void setProperties(F_Message message) {
        ntpServer1 = message.getNtpServer1();
        ntpServer2 = message.getNtpServer2();
        Configuration configuration = editConfiguration();
        configuration.put(PROPERTY_NTP_SERVER1, ntpServer1);
        configuration.put(PROPERTY_NTP_SERVER2, ntpServer2);
        updateConfiguration(configuration);
        logger.debug("NTP properties updated");
    }

    private Device getDevice(String serialNumber, ArrayList<Device> devices) {
        for (Device device : devices) {
            if (device.getSerialNumber().toUpperCase().equals(serialNumber)) {
                return device;
            }
        }
        return null;
    }

    /**
     * Returns the MAX! Device decoded during the last refreshData
     *
     * @param serialNumber
     *            the serial number of the device as String
     * @return device the {@link Device} information decoded in last refreshData
     */

    public Device getDevice(String serialNumber) {
        return getDevice(serialNumber, devices);
    }

    /**
     * Takes the device command and puts it on the command queue to be processed
     * by the MAX! Cube Lan Gateway. Note that if multiple commands for the same
     * item-channel combination are send prior that they are processed by the
     * Max! Cube, they will be removed from the queue as they would not be
     * meaningful. This will improve the behavior when using sliders in the GUI.
     *
     * @param SendCommand
     *            the SendCommand containing the serial number of the device as
     *            String the channelUID used to send the command and the the
     *            command data
     */
    public synchronized void queueCommand(SendCommand sendCommand) {

        if (commandQueue.offer(sendCommand)) {
            if (lastCommandId != null) {
                if (lastCommandId.getKey().equals(sendCommand.getKey())) {
                    if (commandQueue.remove(lastCommandId)) {
                        logger.debug("Removed Command id {} ({}) from queue. Superceeded by {}", lastCommandId.getId(),
                                lastCommandId.getKey(), sendCommand.getId());
                    }
                }
            }
            lastCommandId = sendCommand;
            logger.debug("Command queued id {} ({}:{}).", sendCommand.getId(), sendCommand.getKey(),
                    sendCommand.getCommandText());

        } else {
            logger.debug("Command queued full dropping command id {} ({}).", sendCommand.getId(), sendCommand.getKey());
        }

    }

    /**
     * Processes device command and sends it to the MAX! Cube Lan Gateway.
     *
     * @param {@link SendCommand}
     *            the SendCommand containing the serial number of the device as
     *            String the channelUID used to send the command and the the
     *            command data
     */
    private CubeCommand getCommand(SendCommand sendCommand) {

        String serialNumber = sendCommand.getDeviceSerial();
        ChannelUID channelUID = sendCommand.getChannelUID();
        Command command = sendCommand.getCommand();

        // send command to MAX! Cube LAN Gateway
        HeatingThermostat device = (HeatingThermostat) getDevice(serialNumber, devices);

        if (device == null) {
            logger.debug("Cannot send command to device with serial number {}, device not listed.", serialNumber);
            return null;
        }

        String rfAddress = device.getRFAddress();
        S_Command cmd = null;

        // Temperature setting
        if (channelUID.getId().equals(CHANNEL_SETTEMP)) {

            if (command instanceof DecimalType || command instanceof OnOffType) {
                DecimalType decimalType = DEFAULT_OFF_TEMPERATURE;
                if (command instanceof DecimalType) {
                    decimalType = (DecimalType) command;
                } else if (command instanceof OnOffType) {
                    decimalType = OnOffType.ON.equals(command) ? DEFAULT_ON_TEMPERATURE : DEFAULT_OFF_TEMPERATURE;
                }

                cmd = new S_Command(rfAddress, device.getRoomId(), device.getMode(), decimalType.doubleValue());
            }
            // Mode setting
        } else if (channelUID.getId().equals(CHANNEL_MODE)) {
            if (command instanceof StringType) {
                String commandContent = command.toString().trim().toUpperCase();
                ThermostatModeType commandThermoType = null;
                Double setTemp = Double.parseDouble(device.getTemperatureSetpoint().toString());
                if (commandContent.contentEquals(ThermostatModeType.AUTOMATIC.toString())) {
                    commandThermoType = ThermostatModeType.AUTOMATIC;
                    cmd = new S_Command(rfAddress, device.getRoomId(), commandThermoType, 0D);
                } else if (commandContent.contentEquals(ThermostatModeType.BOOST.toString())) {
                    commandThermoType = ThermostatModeType.BOOST;
                    cmd = new S_Command(rfAddress, device.getRoomId(), commandThermoType, setTemp);
                } else if (commandContent.contentEquals(ThermostatModeType.MANUAL.toString())) {
                    commandThermoType = ThermostatModeType.MANUAL;
                    cmd = new S_Command(rfAddress, device.getRoomId(), commandThermoType, setTemp);
                    logger.debug("updates to MANUAL mode with temperature '{}'", setTemp);
                } else {
                    logger.debug("Only updates to AUTOMATIC & BOOST & MANUAL supported, received value :'{}'",
                            commandContent);
                    return null;
                }
            }
        }
        return cmd;
    }

    /**
     * Updates the room information by sending M command
     *
     * @param comment
     */
    public void sendDeviceAndRoomNameUpdate(String comment) {
        if (devices.size() > 0) {
            SendCommand sendCommand = new SendCommand("Cube(" + getThing().getUID().getId() + ")",
                    new M_Command(devices, rooms), comment);
            queueCommand(sendCommand);
        } else {
            logger.debug("No devices to build room & device update message. Try later");
        }
    }

    /**
     * Delete a devices from the cube and updates the room information
     *
     * @param Device Serial
     */
    public void sendDeviceDelete(String maxDeviceSerial) {
        Device device = getDevice(maxDeviceSerial);
        if (device != null) {
            SendCommand sendCommand = new SendCommand(maxDeviceSerial, new T_Command(device.getRFAddress(), true),
                    "Delete device " + maxDeviceSerial + " from Cube!");
            queueCommand(sendCommand);
            devices.remove(device);
            sendDeviceAndRoomNameUpdate("Remove name entry for " + maxDeviceSerial);
            sendCommand = new SendCommand(maxDeviceSerial, new Q_Command(), "Reload Data");
            queueCommand(sendCommand);
        }
    }

    private void sendNtpUpdate(Map<String, Object> configurationParameters) {
        String ntpServer1 = this.ntpServer1;
        String ntpServer2 = this.ntpServer2;
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            if (configurationParameter.getKey().equals(PROPERTY_NTP_SERVER1)) {
                ntpServer1 = (String) configurationParameter.getValue();
            }
            if (configurationParameter.getKey().equals(PROPERTY_NTP_SERVER2)) {
                ntpServer2 = (String) configurationParameter.getValue();
            }
        }
        queueCommand(new SendCommand("Cube(" + getThing().getUID().getId() + ")", new F_Command(ntpServer1, ntpServer2),
                "Update NTP info"));

    }

    private boolean socketConnect() throws UnknownHostException, IOException {
        socket = new Socket(ipAddress, port);
        socket.setSoTimeout((NETWORK_TIMEOUT));
        logger.debug("Open new connection... to {} port {}", ipAddress, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new OutputStreamWriter(socket.getOutputStream());
        requestCount = 0;
        return true;
    }

    private void socketClose() {
        try {
            socket.close();
        } catch (Exception e) {
        }
        socket = null;
    }

    private void updateCubeState() {
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_FREE_MEMORY), new DecimalType(freeMemorySlots));
        updateState(new ChannelUID(getThing().getUID(), CHANNEL_DUTY_CYCLE), new DecimalType(dutyCycle));
    }
}
