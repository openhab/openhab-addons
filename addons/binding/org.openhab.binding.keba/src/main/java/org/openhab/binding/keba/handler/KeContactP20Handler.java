/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.keba.handler;

import static org.openhab.binding.keba.KebaBindingConstants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link KeContactP20Handler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class KeContactP20Handler extends BaseThingHandler {

    public static final String IP_ADDRESS = "ipAddress";
    public static final String POLLING_REFRESH_INTERVAL = "refreshInterval";

    public static final int CONNECTION_REFRESH_INTERVAL = 100;
    public static final int REPORT_INTERVAL = 2000;
    public static final int PING_TIME_OUT = 3000;
    public static final int BUFFER_SIZE = 1024;
    public static final int REMOTE_PORT_NUMBER = 7090;
    public static final int LISTENER_PORT_NUMBER = 7090;

    private Logger logger = LoggerFactory.getLogger(KeContactP20Handler.class);

    private Selector selector;
    private DatagramChannel datagramChannel = null;
    protected SelectionKey datagramChannelKey = null;
    protected DatagramChannel listenerChannel = null;
    protected SelectionKey listenerKey = null;
    private final Lock lock = new ReentrantLock();
    protected JsonParser parser = new JsonParser();

    private ScheduledFuture<?> listeningJob;
    private ScheduledFuture<?> pollingJob;

    private int maxPresetCurrent = 0;
    private int maxSystemCurrent = 63000;

    public KeContactP20Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing KEBA KeContact P20 handler.");

        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error("An exception occurred while registering the selector: '{}'", e.getMessage());
        }

        configureListener(LISTENER_PORT_NUMBER);

        if (getConfig().get(IP_ADDRESS) != null && !getConfig().get(IP_ADDRESS).equals("")) {

            establishConnection();

            if (listeningJob == null || listeningJob.isCancelled()) {
                try {
                    listeningJob = scheduler.scheduleWithFixedDelay(listeningRunnable, 0, CONNECTION_REFRESH_INTERVAL,
                            TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "An exception occurred while scheduling the connection job");
                }
            }
            if (pollingJob == null || pollingJob.isCancelled()) {
                try {
                    pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0,
                            ((BigDecimal) getConfig().get(POLLING_REFRESH_INTERVAL)).intValue(), TimeUnit.SECONDS);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "An exception occurred while scheduling the polling job");
                }
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP address or port number not set");
        }

    }

    @Override
    public void dispose() {

        try {
            selector.close();
        } catch (IOException e) {
            logger.error("An exception occurred while closing the selector: '{}'", e.getMessage());
        }

        try {
            datagramChannel.close();
        } catch (IOException e) {
            logger.warn("An exception occurred while closing the channel '{}': {}", datagramChannel, e.getMessage());
        }

        try {
            listenerChannel.close();
        } catch (IOException e) {
            logger.error("An exception occurred while closing the listener channel on port number {} ({})",
                    LISTENER_PORT_NUMBER, e.getMessage());
        }

        if (listeningJob != null && !listeningJob.isCancelled()) {
            listeningJob.cancel(true);
            listeningJob = null;
        }

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        logger.debug("Handler disposed.");
    }

    protected void configureListener(int listenerPort) {

        // open the listener port
        try {
            listenerChannel = DatagramChannel.open();
            listenerChannel.socket().bind(new InetSocketAddress(listenerPort));
            listenerChannel.configureBlocking(false);

            logger.info("Listening for incoming data on {}", listenerChannel.getLocalAddress());

            synchronized (selector) {
                selector.wakeup();
                try {
                    listenerKey = listenerChannel.register(selector, listenerChannel.validOps());

                } catch (ClosedChannelException e1) {
                    logger.error("An exception occurred while registering a selector: {}", e1.getMessage());
                }
            }
        } catch (Exception e2) {
            logger.error("An exception occurred while creating the Listener Channel on port number {} ({})",
                    listenerPort, e2.getMessage());
        }
    }

    protected ByteBuffer onReadable(DatagramChannel theChannel, int bufferSize, InetAddress permittedClientAddress) {
        lock.lock();
        try {

            SelectionKey theSelectionKey = theChannel.keyFor(selector);

            if (theSelectionKey != null) {

                synchronized (selector) {
                    try {
                        selector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occurred while selecting: {}", e.getMessage());
                    }
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey selKey = it.next();
                    it.remove();
                    if (selKey.isValid() && selKey.isReadable() && selKey == theSelectionKey) {

                        ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
                        int numberBytesRead = 0;
                        boolean error = false;

                        if (selKey == listenerKey) {
                            try {
                                InetSocketAddress clientAddress = (InetSocketAddress) theChannel.receive(readBuffer);
                                if (clientAddress.getAddress().equals(permittedClientAddress)) {
                                    logger.debug("Received {} on the listener port from {}",
                                            new String(readBuffer.array()), clientAddress);
                                    numberBytesRead = readBuffer.position();
                                } else {
                                    logger.warn(
                                            "Received data from '{}' which is not the permitted remote address '{}'",
                                            clientAddress, permittedClientAddress);
                                    return null;
                                }
                            } catch (Exception e) {
                                logger.error("An exception occurred while receiving data on the listener port: '{}'",
                                        e.getMessage());
                                error = true;
                            }

                        } else {

                            try {
                                numberBytesRead = theChannel.read(readBuffer);
                            } catch (NotYetConnectedException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "The remote host is not yet connected");
                                error = true;
                            } catch (PortUnreachableException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "The remote host is probably not a KEBA EV Charging station");
                                error = true;
                            } catch (IOException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "An IO exception occurred");
                                error = true;
                            }
                        }

                        if (numberBytesRead == -1) {
                            error = true;
                        }

                        if (error) {
                            logger.debug("Disconnecting '{}' because of a socket error",
                                    getThing().getUID().toString());
                            try {
                                theChannel.close();
                            } catch (IOException e) {
                                logger.error("An exception occurred while closing the channel '{}': {}",
                                        datagramChannel, e.getMessage());
                            }

                            onConnectionLost();

                        } else {
                            readBuffer.flip();
                            return readBuffer;
                        }
                    }
                }
            }

            return null;
        } finally {
            lock.unlock();
        }
    }

    protected void onWritable(ByteBuffer buffer, DatagramChannel theChannel) {
        lock.lock();
        try {

            SelectionKey theSelectionKey = theChannel.keyFor(selector);

            if (theSelectionKey != null) {

                synchronized (selector) {
                    try {
                        selector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occurred while selecting: {}", e.getMessage());
                    }
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey selKey = it.next();
                    it.remove();
                    if (selKey.isValid() && selKey.isWritable() && selKey == theSelectionKey) {

                        boolean error = false;
                        buffer.rewind();

                        try {
                            logger.debug("Sending '{}' on the channel '{}'->'{}'",
                                    new Object[] { new String(buffer.array()), theChannel.getLocalAddress(),
                                            theChannel.getRemoteAddress() });
                            theChannel.write(buffer);
                        } catch (NotYetConnectedException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "The remote host is not yet connected");
                            error = true;
                        } catch (ClosedChannelException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "The connection to the remote host is closed");
                            error = true;
                        } catch (IOException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "An IO exception occurred");
                            error = true;
                        }

                        if (error) {
                            logger.debug("Disconnecting '{}' because of a socket error",
                                    getThing().getUID().toString());
                            try {
                                theChannel.close();
                            } catch (IOException e) {
                                logger.warn("An exception occurred while closing the channel '{}': {}", datagramChannel,
                                        e.getMessage());
                            }

                            onConnectionLost();

                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void onConnectionLost() {
        establishConnection();
    }

    public void onConnectionResumed() {
        updateStatus(ThingStatus.ONLINE);
    }

    private void establishConnection() {
        lock.lock();
        try {
            if (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR
                    && getConfig().get(IP_ADDRESS) != null && !getConfig().get(IP_ADDRESS).equals("")) {

                try {
                    datagramChannel = DatagramChannel.open();
                } catch (Exception e2) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while opening a datagram channel");
                }

                try {
                    datagramChannel.configureBlocking(false);
                } catch (IOException e2) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while configuring a datagram channel");
                }

                synchronized (selector) {
                    selector.wakeup();
                    int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                    try {
                        datagramChannelKey = datagramChannel.register(selector, interestSet);
                    } catch (ClosedChannelException e1) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "An exception occurred while registering a selector");
                    }

                    InetSocketAddress remoteAddress = new InetSocketAddress((String) getConfig().get(IP_ADDRESS),
                            REMOTE_PORT_NUMBER);

                    try {
                        logger.trace("Connecting the channel for {} ", remoteAddress);
                        datagramChannel.connect(remoteAddress);

                        onConnectionResumed();

                    } catch (Exception e) {
                        logger.error("An exception occurred while connecting connecting to '{}:{}' : {}", new Object[] {
                                (String) getConfig().get(IP_ADDRESS), REMOTE_PORT_NUMBER, e.getMessage() });
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "An exception occurred while connecting");
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        getThing().getStatusInfo().getDescription());
            }
        } finally {
            lock.unlock();
        }
    }

    private Runnable listeningRunnable = new Runnable() {

        @Override
        public void run() {
            lock.lock();
            try {
                if (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
                    if (datagramChannel == null || !datagramChannel.isConnected()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "The connection is not yet initialized");
                        onConnectionLost();
                    }

                    if (datagramChannel.isConnected()) {

                        long stamp = System.currentTimeMillis();
                        if (!InetAddress.getByName(((String) getConfig().get(IP_ADDRESS))).isReachable(PING_TIME_OUT)) {
                            logger.debug("Ping timed out after '{}' milliseconds", System.currentTimeMillis() - stamp);
                            logger.trace("Disconnecting the datagram channel '{}'", datagramChannel);
                            try {
                                datagramChannel.close();
                            } catch (IOException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "An exception occurred while closing the channel");
                            }

                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "A ping timeout occurred");
                            onConnectionLost();
                        } else {
                            ByteBuffer buffer = onReadable(datagramChannel, BUFFER_SIZE, null);
                            if (buffer != null && buffer.remaining() > 0) {
                                onRead(buffer, datagramChannel);
                            }
                        }
                    }

                    ByteBuffer buffer = onReadable(listenerChannel, BUFFER_SIZE,
                            InetAddress.getByName((String) getConfig().get(IP_ADDRESS)));
                    if (buffer != null && buffer.remaining() > 0) {
                        onRead(buffer, listenerChannel);
                    }
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while receiving event data from the charging station");
            } finally {
                lock.unlock();
            }
        }
    };

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {

                String command = "report 1";

                ByteBuffer byteBuffer = ByteBuffer.allocate(command.getBytes().length);
                try {
                    byteBuffer.put(command.getBytes("ASCII"));
                    onWritable(byteBuffer, datagramChannel);
                } catch (UnsupportedEncodingException | NumberFormatException e) {
                    logger.error("An exception occurred while polling the KEBA KeContact P20 for '{}': {}",
                            getThing().getUID(), e.getMessage());
                }

                Thread.sleep(REPORT_INTERVAL);

                command = "report 2";

                byteBuffer = ByteBuffer.allocate(command.getBytes().length);
                try {
                    byteBuffer.put(command.getBytes("ASCII"));
                    onWritable(byteBuffer, datagramChannel);
                } catch (UnsupportedEncodingException | NumberFormatException e) {
                    logger.error("An exception occurred while polling the KEBA KeContact P20 for '{}': {}",
                            getThing().getUID(), e.getMessage());
                }

                Thread.sleep(REPORT_INTERVAL);

                command = "report 3";

                byteBuffer = ByteBuffer.allocate(command.getBytes().length);
                try {
                    byteBuffer.put(command.getBytes("ASCII"));
                    onWritable(byteBuffer, datagramChannel);
                } catch (UnsupportedEncodingException | NumberFormatException e) {
                    logger.error("An exception occurred while polling the KEBA KeContact P20 for '{}': {}",
                            getThing().getUID(), e.getMessage());
                }

            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    };

    protected void onRead(ByteBuffer byteBuffer, DatagramChannel datagramChannel) {

        String response = new String(byteBuffer.array(), 0, byteBuffer.limit());
        response = StringUtils.chomp(response);

        if (response.contains("TCH-OK")) {
            // ignore confirmation messages which are not JSON
            return;
        }

        try {
            JsonObject readObject = parser.parse(response).getAsJsonObject();

            for (Entry<String, JsonElement> entry : readObject.entrySet()) {

                switch (entry.getKey()) {
                    case "Product": {
                        Map<String, String> properties = editProperties();
                        properties.put(CHANNEL_MODEL, entry.getValue().getAsString());
                        updateProperties(properties);
                        break;
                    }
                    case "Serial": {
                        Map<String, String> properties = editProperties();
                        properties.put(CHANNEL_SERIAL, entry.getValue().getAsString());
                        updateProperties(properties);
                        break;
                    }
                    case "Firmware": {
                        Map<String, String> properties = editProperties();
                        properties.put(CHANNEL_FIRMWARE, entry.getValue().getAsString());
                        updateProperties(properties);
                        break;

                    }
                    case "Plug": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 0: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.OFF);
                                break;
                            }
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.OFF);
                                break;
                            }
                            case 3: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.OFF);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.ON);
                                break;
                            }
                            case 5: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.OFF);
                                break;
                            }
                            case 7: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_WALLBOX), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_VEHICLE), OnOffType.ON);
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_PLUG_LOCKED), OnOffType.ON);
                                break;
                            }
                        }
                        break;
                    }
                    case "State": {
                        State newState = new DecimalType(entry.getValue().getAsInt());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_STATE), newState);
                        break;
                    }
                    case "Enable sys": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ENABLED), OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_ENABLED), OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Curr HW": {
                        int state = entry.getValue().getAsInt();
                        maxSystemCurrent = state;
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_SYSTEM_CURRENT), newState);
                        if (maxSystemCurrent < maxPresetCurrent) {
                            sendCommand("curr " + String.valueOf(maxSystemCurrent));
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT),
                                    new DecimalType(maxSystemCurrent));
                            updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT_RANGE),
                                    new PercentType((maxSystemCurrent - 6000) * 100 / (maxSystemCurrent - 6000)));
                        }
                        break;
                    }
                    case "Curr user": {
                        int state = entry.getValue().getAsInt();
                        maxPresetCurrent = state;
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT),
                                new DecimalType(state));
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_PRESET_CURRENT_RANGE),
                                new PercentType((state - 6000) * 100 / (maxSystemCurrent - 6000)));
                        break;
                    }
                    case "Curr FS": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_FAILSAFE_CURRENT), newState);
                        break;
                    }
                    case "Output": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTPUT), OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_OUTPUT), OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Input": {
                        int state = entry.getValue().getAsInt();
                        switch (state) {
                            case 1: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_INPUT), OnOffType.ON);
                                break;
                            }
                            default: {
                                updateState(new ChannelUID(getThing().getUID(), CHANNEL_INPUT), OnOffType.OFF);
                                break;
                            }
                        }
                        break;
                    }
                    case "Sec": {
                        long state = entry.getValue().getAsLong();

                        Calendar uptime = Calendar.getInstance();
                        uptime.setTimeZone(TimeZone.getTimeZone("GMT"));
                        uptime.setTimeInMillis(state * 1000);
                        SimpleDateFormat pFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        pFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));

                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_UPTIME),
                                new DateTimeType(pFormatter.format(uptime.getTime())));
                        break;
                    }
                    case "U1": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_U1), newState);
                        break;
                    }
                    case "U2": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_U2), newState);
                        break;
                    }
                    case "U3": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_U3), newState);
                        break;
                    }
                    case "I1": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state / 1000);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I1), newState);
                        break;
                    }
                    case "I2": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state / 1000);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I2), newState);
                        break;
                    }
                    case "I3": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state / 1000);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I3), newState);
                        break;
                    }
                    case "P": {
                        long state = entry.getValue().getAsLong();
                        State newState = new DecimalType(state / 1000);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_POWER), newState);
                        break;
                    }
                    case "PF": {
                        int state = entry.getValue().getAsInt();
                        State newState = new PercentType(state / 10);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_POWER_FACTOR), newState);
                        break;
                    }
                    case "E pres": {
                        long state = entry.getValue().getAsLong();
                        State newState = new DecimalType(state / 10);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SESSION_CONSUMPTION), newState);
                        break;
                    }
                    case "E total": {
                        long state = entry.getValue().getAsLong();
                        State newState = new DecimalType(state / 10);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TOTAL_CONSUMPTION), newState);
                        break;
                    }
                }
            }

        } catch (JsonParseException e) {
            logger.debug("Invalid JSON data will be ignored: '{}'", response);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            // Refresh all channels by scheduling a single run of the polling runnable
            scheduler.schedule(pollingRunnable, 0, TimeUnit.SECONDS);
        } else {

            switch (channelUID.getId()) {
                case CHANNEL_MAX_PRESET_CURRENT: {
                    if (command instanceof DecimalType) {
                        sendCommand("curr " + String.valueOf(
                                Math.min(Math.max(6000, ((DecimalType) command).intValue()), maxSystemCurrent)));
                    }
                    break;
                }
                case CHANNEL_MAX_PRESET_CURRENT_RANGE: {
                    if (command instanceof OnOffType || command instanceof IncreaseDecreaseType
                            || command instanceof PercentType) {

                        int newValue = 6000;
                        if (command == IncreaseDecreaseType.INCREASE) {
                            newValue = Math.min(Math.max(6000, maxPresetCurrent + 1), maxSystemCurrent);
                        } else if (command == IncreaseDecreaseType.DECREASE) {
                            newValue = Math.min(Math.max(6000, maxPresetCurrent - 1), maxSystemCurrent);
                        } else if (command == OnOffType.ON) {
                            newValue = maxSystemCurrent;
                        } else if (command == OnOffType.OFF) {
                            newValue = 6000;
                        } else if (command instanceof PercentType) {
                            newValue = 6000 + (maxSystemCurrent - 6000) * ((PercentType) command).intValue() / 100;
                        } else {
                            return;
                        }

                        sendCommand("curr " + String.valueOf(newValue));
                    }
                    break;
                }
                case CHANNEL_ENABLED: {
                    if (command instanceof OnOffType) {

                        if (command == OnOffType.ON) {
                            sendCommand("ena 1");
                        } else if (command == OnOffType.OFF) {
                            sendCommand("ena 0");
                        } else {
                            return;
                        }

                    }
                    break;
                }
                case CHANNEL_OUTPUT: {
                    if (command instanceof OnOffType) {

                        if (command == OnOffType.ON) {
                            sendCommand("output 1");
                        } else if (command == OnOffType.OFF) {
                            sendCommand("output 0");
                        } else {
                            return;
                        }

                    }
                    break;
                }
            }
        }
    }

    private void sendCommand(String command) {

        if (command != null) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(command.getBytes().length);
            try {
                byteBuffer.put(command.getBytes("ASCII"));
                onWritable(byteBuffer, datagramChannel);
            } catch (UnsupportedEncodingException | NumberFormatException e) {
                logger.error("An exception occurred while sending a command to the KeContact wallbox for '{}': {}",
                        getThing().getUID(), e.getMessage());
            }
        }

    }

}
