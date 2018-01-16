/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.keba.KebaBindingConstants.KebaFirmware;
import org.openhab.binding.keba.KebaBindingConstants.KebaSeries;
import org.openhab.binding.keba.KebaBindingConstants.KebaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link KeContactHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Karel Goderis - Initial contribution
 */
public class KeContactHandler extends BaseThingHandler {

    public static final String IP_ADDRESS = "ipAddress";
    public static final String POLLING_REFRESH_INTERVAL = "refreshInterval";
    public static final int LISTENING_INTERVAL = 100;
    public static final int REPORT_INTERVAL = 2000;
    public static final int PING_TIME_OUT = 3000;
    public static final int BUFFER_SIZE = 1024;
    public static final int REMOTE_PORT_NUMBER = 7090;
    private static final String KEBA_HANDLER_THREADPOOL_NAME = "Keba";

    private final Logger logger = LoggerFactory.getLogger(KeContactHandler.class);

    private Selector selector;
    private DatagramChannel datagramChannel = null;
    protected SelectionKey datagramChannelKey = null;
    private final Lock lock = new ReentrantLock();
    protected JsonParser parser = new JsonParser();

    private ScheduledFuture<?> pollingJob;
    private ScheduledFuture<?> listeningJob;
    private static KeContactBroadcastListener broadcastListener = new KeContactBroadcastListener();

    private int maxPresetCurrent = 0;
    private int maxSystemCurrent = 63000;
    private KebaType type;
    private KebaFirmware firmware;
    private KebaSeries series;

    public KeContactHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        if (getConfig().get(IP_ADDRESS) != null && !getConfig().get(IP_ADDRESS).equals("")) {

            broadcastListener.registerHandler(this);

            try {
                selector = Selector.open();
            } catch (IOException e) {
                logger.error("An exception occurred while registering the selector: '{}'", e.getMessage());
            }

            establishConnection();

            if (listeningJob == null || listeningJob.isCancelled()) {
                try {
                    listeningJob = scheduler.scheduleWithFixedDelay(listeningRunnable, 0, LISTENING_INTERVAL,
                            TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "An exception occurred while scheduling the listening job");
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

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (listeningJob != null && !listeningJob.isCancelled()) {
            listeningJob.cancel(true);
            listeningJob = null;
        }

        broadcastListener.unRegisterHandler(this);
    }

    public String getIPAddress() {
        return getConfig().get(IP_ADDRESS) != null ? (String) getConfig().get(IP_ADDRESS) : "";
    }

    protected ByteBuffer onReadable(DatagramChannel theChannel, int bufferSize) {
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

                        if (selKey == datagramChannelKey) {
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
            try {
                if (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
                    if (datagramChannel == null || !datagramChannel.isConnected()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "The connection is not yet initialized");
                        onConnectionLost();
                    }

                    if (datagramChannel.isConnected()) {
                        ByteBuffer buffer = onReadable(datagramChannel, BUFFER_SIZE);
                        if (buffer != null && buffer.remaining() > 0) {
                            onRead(buffer);
                        }
                    }
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while receiving event data from the charging station");
            }
        }
    };

    private Runnable pollingRunnable = new Runnable() {

        @Override
        public void run() {
            try {
                long stamp = System.currentTimeMillis();
                if (!InetAddress.getByName(((String) getConfig().get(IP_ADDRESS))).isReachable(PING_TIME_OUT)) {
                    logger.debug("Ping timed out after '{}' milliseconds", System.currentTimeMillis() - stamp);
                    logger.trace("Disconnecting the datagram channel '{}'", datagramChannel);
                    datagramChannel.close();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "A ping timeout occurred");
                    onConnectionLost();
                } else {
                    if (getThing().getStatus() == ThingStatus.ONLINE) {
                        String command = "report 1";
                        ByteBuffer byteBuffer = ByteBuffer.allocate(command.getBytes().length);
                        byteBuffer.put(command.getBytes("ASCII"));
                        onWritable(byteBuffer, datagramChannel);

                        Thread.sleep(REPORT_INTERVAL);

                        command = "report 2";
                        byteBuffer = ByteBuffer.allocate(command.getBytes().length);
                        byteBuffer.put(command.getBytes("ASCII"));
                        onWritable(byteBuffer, datagramChannel);

                        Thread.sleep(REPORT_INTERVAL);

                        command = "report 3";
                        byteBuffer = ByteBuffer.allocate(command.getBytes().length);
                        byteBuffer.put(command.getBytes("ASCII"));
                        onWritable(byteBuffer, datagramChannel);
                    }

                }
            } catch (InterruptedException | NumberFormatException | IOException e) {
                logger.debug("An exception occurred while polling the KEBA KeContact for '{}': {}", getThing().getUID(),
                        e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while while polling the charging station");
            }
        }
    };

    protected void onRead(ByteBuffer byteBuffer) {
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
                        String product = entry.getValue().getAsString().trim();
                        properties.put(CHANNEL_MODEL, product);
                        updateProperties(properties);
                        if (product.contains("P20")) {
                            type = KebaType.P20;
                        } else if (product.contains("P30")) {
                            type = KebaType.P30;
                        }
                        series = KebaSeries.getSeries(product.substring(13, 14).charAt(0));
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
                        firmware = KebaFirmware.getFirmware(entry.getValue().getAsString());
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
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I1), newState);
                        break;
                    }
                    case "I2": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_I2), newState);
                        break;
                    }
                    case "I3": {
                        int state = entry.getValue().getAsInt();
                        State newState = new DecimalType(state);
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
                case CHANNEL_DISPLAY: {
                    if (command instanceof StringType) {
                        if (type == KebaType.P30 && (series == KebaSeries.C || series == KebaSeries.X)) {
                            String cmd = command.toString();
                            int maxLength = (cmd.length() < 23) ? cmd.length() : 23;
                            sendCommand("display 0 0 0 0 " + cmd.substring(0, maxLength));
                        } else {
                            logger.warn("'Display' is not supported on a Keba {}:{}", type, series);
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

    public void setBroadcastListenerStatus(boolean status) {
        if (status) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "The broadcast listener is offline");
        }
    }
}
