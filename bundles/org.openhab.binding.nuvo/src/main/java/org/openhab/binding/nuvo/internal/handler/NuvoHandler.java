/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nuvo.internal.handler;

import static org.openhab.binding.nuvo.internal.NuvoBindingConstants.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateOption;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.nuvo.internal.NuvoException;
import org.openhab.binding.nuvo.internal.NuvoStateDescriptionOptionProvider;
import org.openhab.binding.nuvo.internal.communication.NuvoCommand;
import org.openhab.binding.nuvo.internal.communication.NuvoConnector;
import org.openhab.binding.nuvo.internal.communication.NuvoEnum;
import org.openhab.binding.nuvo.internal.communication.NuvoIpConnector;
import org.openhab.binding.nuvo.internal.communication.NuvoMessageEvent;
import org.openhab.binding.nuvo.internal.communication.NuvoMessageEventListener;
import org.openhab.binding.nuvo.internal.communication.NuvoSerialConnector;
import org.openhab.binding.nuvo.internal.communication.NuvoStatusCodes;
import org.openhab.binding.nuvo.internal.configuration.NuvoThingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NuvoHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * Based on the Rotel binding by Laurent Garnier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class NuvoHandler extends BaseThingHandler implements NuvoMessageEventListener {

    private final Logger logger = LoggerFactory.getLogger(NuvoHandler.class);

    private static final long RECON_POLLING_INTERVAL = TimeUnit.SECONDS.toSeconds(60);
    private static final long POLLING_INTERVAL = TimeUnit.SECONDS.toSeconds(30);
    private static final long CLOCK_SYNC_INTERVAL = TimeUnit.HOURS.toSeconds(1);
    private static final long INITIAL_POLLING_DELAY = TimeUnit.SECONDS.toSeconds(30);
    private static final long INITIAL_CLOCK_SYNC_DELAY = TimeUnit.SECONDS.toSeconds(10);
    private static final long SLEEP_BETWEEN_CMD = TimeUnit.MILLISECONDS.toMillis(70); // spec says wait 50ms, lets wait 70
    private final Unit<Time> API_SECOND_UNIT = SmartHomeUnits.SECOND;

    private static final String ZONE = "ZONE";
    private static final String CHANNEL_DELIMIT = "#";
    private static final String UNDEF = "UNDEF";

    private static final Integer ONE = 1;
    private static final Integer MAX_ZONES = 20;
    private static final Integer MAX_SRC = 6;
    private static final Integer MIN_VOLUME = 0;
    private static final Integer MAX_VOLUME = 79;
    private static final Integer MIN_EQ = -18;
    private static final Integer MAX_EQ = 18;

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> clockSyncJob;

    private NuvoStateDescriptionOptionProvider stateDescriptionProvider;
    private SerialPortManager serialPortManager;

    private @Nullable NuvoConnector connector;
    
    private Integer numZones = 1;
    List<Integer> activeZones = new ArrayList<Integer>(1); 
    
    // A state option list for the source labels
    List<StateOption> sourceLabels = new ArrayList<>();

    private Long lastEventReceived = System.currentTimeMillis();

    private Object sequenceLock = new Object();
    
    private static final String GC_STR = "NV-IG8";
    private String versionString = "";
    private boolean isGConcerto = false;

    /**
     * Constructor
     */
    public NuvoHandler(Thing thing, NuvoStateDescriptionOptionProvider stateDescriptionProvider,
            SerialPortManager serialPortManager) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing handler for thing {}", getThing().getUID());

        NuvoThingConfiguration config = getConfigAs(NuvoThingConfiguration.class);

        // Check configuration settings
        String configError = null;
        if ((config.serialPort == null || config.serialPort.isEmpty())
                && (config.host == null || config.host.isEmpty())) {
            configError = "undefined serialPort and host configuration settings; please set one of them";
        } else if (config.host == null || config.host.isEmpty()) {
            if (config.serialPort.toLowerCase().startsWith("rfc2217")) {
                configError = "use host and port configuration settings for a serial over IP connection";
            }
        } else {
            if (config.port == null) {
                configError = "undefined port configuration setting";
            } else if (config.port <= 0) {
                configError = "invalid port configuration setting";
            }
        }
        
        if (config.clockSync != null && config.clockSync == 1) {
            scheduleClockSyncJob();
        }

        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
        } else {
            if (config.serialPort != null) {
                connector = new NuvoSerialConnector(serialPortManager, config.serialPort);
            } else {
                connector = new NuvoIpConnector(config.host, config.port);
            }

            numZones = config.numZones;
            activeZones = IntStream.range((1), (numZones+1)).boxed().collect(Collectors.toList());

            // remove the channels for the zones we are not using
            if (numZones < MAX_ZONES) {
                List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

                List<Integer> zonesToRemove = 
                        IntStream.range((numZones+1), (MAX_ZONES+1)).boxed().collect(Collectors.toList());
                           
                zonesToRemove.forEach(zone -> {
                    if (channels.removeIf(c -> (c.getUID().getId().contains("zone" + zone.toString())))) {
                        logger.debug("Removed channels for zone: {}", zone);
                    } else {
                        logger.debug("Could NOT remove channels for zone: {}", zone);
                    }
                });
                updateThing(editThing().withChannels(channels).build());
            }
            
            updateStatus(ThingStatus.UNKNOWN);
            scheduleReconnectJob();
            schedulePollingJob();
        }

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for thing {}", getThing().getUID());
        cancelReconnectJob();
        cancelPollingJob();
        cancelClockSyncJob();
        closeConnection();
        super.dispose();
    }

    /**
     * Handle a command the UI
     *
     * @param channelUID the channel sending the command
     * @param command the command received
     * 
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        String[] channelSplit = channel.split(CHANNEL_DELIMIT);
        NuvoEnum target = NuvoEnum.valueOf(channelSplit[0].toUpperCase());

        String channelType = channelSplit[1];

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command {} from channel {} is ignored", command, channel);
            return;
        }

        if (!connector.isConnected()) {
            logger.debug("Command {} from channel {} is ignored: connection not established", command, channel);
            return;
        }

        synchronized (sequenceLock) {
            try {
                switch (channelType) {
                    case CHANNEL_TYPE_POWER:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand(target, NuvoCommand.ON);
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand(target, NuvoCommand.OFF);
                        }
                        break;
                    case CHANNEL_TYPE_SOURCE:
                        if (command instanceof DecimalType) {
                            Integer value = ((DecimalType) command).intValue();
                            if (value >= ONE && value <= MAX_SRC) {
                                logger.debug("Got source command {} zone {}", value, target);
                                connector.sendCommand(target, NuvoCommand.SOURCE, value.toString());
                            }
                        }
                        break;
                    case CHANNEL_TYPE_VOLUME:
                        if (command instanceof PercentType) {
                            Integer value = (MAX_VOLUME - (int) Math.round(((PercentType) command).doubleValue() / 100.0 * (MAX_VOLUME - MIN_VOLUME))
                                    + MIN_VOLUME);
                            logger.debug("Got volume command {} zone {}", value, target);
                            connector.sendCommand(target, NuvoCommand.VOLUME, value.toString());
                        }
                        break;
                    case CHANNEL_TYPE_MUTE:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand(target, NuvoCommand.MUTE_ON);
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand(target, NuvoCommand.MUTE_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_TREBLE:
                        if (command instanceof DecimalType) {
                            Integer value = ((DecimalType) command).intValue();
                            if (value >= MIN_EQ && value <= MAX_EQ && value % 2 == 0) {
                                logger.debug("Got treble command {} zone {}", value, target);
                                connector.sendCfgCommand(target, NuvoCommand.TREBLE, value.toString());
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BASS:
                        if (command instanceof DecimalType) {
                            Integer value = ((DecimalType) command).intValue();
                            if (value >= MIN_EQ && value <= MAX_EQ  && value % 2 == 0) {
                                logger.debug("Got bass command {} zone {}", value, target);
                                connector.sendCfgCommand(target, NuvoCommand.BASS, value.toString());
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BALANCE:
                        if (command instanceof DecimalType) {
                            Integer value = ((DecimalType) command).intValue();
                            if (value >= MIN_EQ && value <= MAX_EQ  && value % 2 == 0) {
                                logger.debug("Got balance command {} zone {}", value, target);
                                connector.sendCfgCommand(target, NuvoCommand.BALANCE, NuvoStatusCodes.getBalanceFromInt(value));
                            }
                        }
                        break;
                    case CHANNEL_TYPE_LOUDNESS:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCfgCommand(target, NuvoCommand.LOUDNESS, "1");
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCfgCommand(target, NuvoCommand.LOUDNESS, "0");
                        }
                        break;
                    case CHANNEL_TYPE_CONTROL:
                        handleControlCommand(target, command);
                        break;
                    case CHANNEL_TYPE_DND:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand(target, NuvoCommand.DND_ON);
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand(target, NuvoCommand.DND_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_PARTY:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand(target, NuvoCommand.PARTY_ON);
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand(target, NuvoCommand.PARTY_OFF);
                        }
                        break;
                    case CHANNEL_DISPLAY_LINE1:
                        connector.sendCommand(target, NuvoCommand.DISPLINE1, "\"" + command.toString() + "\"");
                        break;
                    case CHANNEL_DISPLAY_LINE2:
                        connector.sendCommand(target, NuvoCommand.DISPLINE2, "\"" + command.toString() + "\"");
                        break;
                    case CHANNEL_DISPLAY_LINE3:
                        connector.sendCommand(target, NuvoCommand.DISPLINE3, "\"" + command.toString() + "\"");
                        break;
                    case CHANNEL_DISPLAY_LINE4:
                        connector.sendCommand(target, NuvoCommand.DISPLINE4, "\"" + command.toString() + "\"");
                        break;
                    case CHANNEL_TYPE_ALLOFF:
                        if (command instanceof OnOffType && (command == OnOffType.ON || command == OnOffType.OFF)) {
                            connector.sendCommand(NuvoCommand.ALLOFF);
                        }
                        break;
                    case CHANNEL_TYPE_ALLMUTE:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand(NuvoCommand.ALLMUTE_ON);
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand(NuvoCommand.ALLMUTE_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_PAGE:
                        if (command instanceof OnOffType && command == OnOffType.ON) {
                            connector.sendCommand(NuvoCommand.PAGE_ON);
                        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
                            connector.sendCommand(NuvoCommand.PAGE_OFF);
                        }
                        break;
                    case CHANNEL_TYPE_SENDCMD:
                        connector.sendCommand(command.toString());
                        break;
                }
            } catch (NuvoException e) {
                logger.warn("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Sending command failed");
                closeConnection();
                scheduleReconnectJob();
            }
        }                    
    }

    /**
     * Open the connection with the Nuvo device
     *
     * @return true if the connection is opened successfully or false if not
     */
    private synchronized boolean openConnection() {
        connector.addEventListener(this);
        try {
            connector.open();
        } catch (NuvoException e) {
            logger.debug("openConnection() failed: {}", e.getMessage());
        }
        logger.debug("openConnection(): {}", connector.isConnected() ? "connected" : "disconnected");
        return connector.isConnected();
    }

    /**
     * Close the connection with the Nuvo device
     */
    private synchronized void closeConnection() {
        if (connector !=null && connector.isConnected()) {
            connector.close();
            connector.removeEventListener(this);
            logger.debug("closeConnection(): disconnected");
        }
    }

    /**
     * Handle an event received from the Nuvo device
     *
     * @param event the event to process
     */
    @Override
    public void onNewMessageEvent(EventObject event) {

        NuvoMessageEvent evt = (NuvoMessageEvent) event;
        logger.debug("onNewMessageEvent: key {} = {}", evt.getKey(), evt.getValue());
        lastEventReceived = System.currentTimeMillis();

        String type = evt.getType();
        String key = evt.getKey();
        String updateData = evt.getValue().trim();
        if (this.getThing().getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, this.versionString);
        }
        Pattern p;
        
        switch (type) {
            case NuvoConnector.TYPE_VERSION:
                this.versionString = updateData;
                // Determine if we are a Grand Concerto or not
                if (this.versionString.contains(GC_STR)) {
                    this.isGConcerto = true;
                    connector.setEssentia(false);
                }
                break;
                
            case NuvoConnector.TYPE_ALLOFF:
                activeZones.forEach(zoneNum -> {
                    updateChannelState(NuvoEnum.zoneMap.get("Z"+zoneNum.toString()), CHANNEL_TYPE_POWER, NuvoCommand.OFF.getValue());
                });
                break;
                
            case NuvoConnector.TYPE_ALLMUTE:
                updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_ALLMUTE, "1".equals(updateData) ? NuvoCommand.ON.getValue() : NuvoCommand.OFF.getValue());
                activeZones.forEach(zoneNum -> {
                    updateChannelState(NuvoEnum.zoneMap.get("Z"+zoneNum.toString()), CHANNEL_TYPE_MUTE, 
                                        "1".equals(updateData) ? NuvoCommand.ON.getValue() : NuvoCommand.OFF.getValue());
                });
                break;
                
            case NuvoConnector.TYPE_PAGE:
                updateChannelState(NuvoEnum.SYSTEM, CHANNEL_TYPE_PAGE, "1".equals(updateData) ? NuvoCommand.ON.getValue() : NuvoCommand.OFF.getValue());
                break;
                
            case NuvoConnector.TYPE_SOURCE_UPDATE:
                logger.debug("Source update: Source: {} - Value: {}", key, updateData);
                NuvoEnum targetSource = NuvoEnum.sourceMap.get(key);
                
                if (updateData.contains("DISPLINE")) {
                    // example: DISPLINE2,"Play My Song (Featuring Dee Ajayi)"
                    p = Pattern.compile("^DISPLINE(\\d{1}),\"(.*)\"$");
                    
                    try {
                        Matcher matcher=p.matcher(updateData);
                        matcher.find();
                        updateChannelState(targetSource, CHANNEL_DISPLAY_LINE + matcher.group(1), matcher.group(2));
                    } catch (IllegalStateException e){
                        logger.debug("no match on message: {}", updateData);
                    }
                } else if (updateData.contains("DISPINFO,")) {
                    // example: DISPINFO,DUR0,POS70,STATUS2 (DUR and POS are expressed in tenths of a second)
                    p = Pattern.compile("^DISPINFO,DUR(\\d{1,6}),POS(\\d{1,6}),STATUS(\\d{1,2})$"); //6 places(tenths of a second)-> max 999,999 /10/60/60/24 = 1.15 days
                    
                    try {
                        Matcher matcher=p.matcher(updateData);
                        matcher.find();
                        updateChannelState(targetSource, CHANNEL_TRACK_LENGTH, matcher.group(1));
                        updateChannelState(targetSource, CHANNEL_TRACK_POSITION, matcher.group(2));
                        updateChannelState(targetSource, CHANNEL_PLAY_MODE, matcher.group(3));
                    } catch (IllegalStateException e){
                        logger.debug("no match on message: {}", updateData);
                    }
                } else if (updateData.contains("NAME\"") && sourceLabels.size() <= MAX_SRC) {
                    // example: NAME"Ipod"
                    String name = updateData.split("\"")[1];
                    sourceLabels.add(new StateOption(key.replace("S",""), name));
                }
                
                break;
                
            case NuvoConnector.TYPE_ZONE_UPDATE:
                logger.debug("Zone update: Zone: {} - Value: {}", key, updateData);
                // example : OFF
                // or: ON,SRC3,VOL63,DND0,LOCK0
                // or: ON,SRC3,MUTE,DND0,LOCK0
                
                NuvoEnum targetZone = NuvoEnum.zoneMap.get(key);
                
                if ("OFF".equals(updateData)) {  
                    updateChannelState(targetZone, CHANNEL_TYPE_POWER, NuvoCommand.OFF.getValue());
                    updateChannelState(targetZone, CHANNEL_TYPE_SOURCE, UNDEF);
                } else {
                    p = Pattern.compile("^ON,SRC(\\d{1}),(MUTE|VOL\\d{1,2}),DND([0-1]),LOCK([0-1])$");
                    
                    try {
                        Matcher matcher=p.matcher(updateData);
                        matcher.find();
                        
                        updateChannelState(targetZone, CHANNEL_TYPE_POWER, NuvoCommand.ON.getValue());
                        updateChannelState(targetZone, CHANNEL_TYPE_SOURCE, matcher.group(1));
                        
                        if ("MUTE".equals(matcher.group(2))) {
                            updateChannelState(targetZone, CHANNEL_TYPE_MUTE, NuvoCommand.ON.getValue());
                        } else {
                            updateChannelState(targetZone, CHANNEL_TYPE_MUTE, NuvoCommand.OFF.getValue());
                            updateChannelState(targetZone, CHANNEL_TYPE_VOLUME, matcher.group(2).replace("VOL", "")); // just the number
                        }
                        
                        updateChannelState(targetZone, CHANNEL_TYPE_DND, "1".equals(matcher.group(3)) ? NuvoCommand.ON.getValue() : NuvoCommand.OFF.getValue());
                        updateChannelState(targetZone, CHANNEL_TYPE_LOCK, matcher.group(4));
                    } catch (IllegalStateException e){
                        logger.debug("no match on message: {}", updateData);
                    }
                }
                break;
                
            case NuvoConnector.TYPE_ZONE_BUTTON:
                logger.debug("Zone Button pressed: Source: {} - Button: {}", key, updateData);
                updateChannelState(NuvoEnum.sourceMap.get(key), CHANNEL_BUTTON_PRESS, updateData);
                break;
                
            case NuvoConnector.TYPE_ZONE_CONFIG:
                logger.debug("Zone Configuration: Zone: {} - Value: {}", key, updateData);
                // example: BASS1,TREB-2,BALR2,LOUDCMP1
                p = Pattern.compile("^BASS(.*),TREB(.*),BAL(.*),LOUDCMP([0-1])$");
                
                try {
                    Matcher matcher=p.matcher(updateData);
                    matcher.find();
                    updateChannelState(NuvoEnum.zoneMap.get(key), CHANNEL_TYPE_BASS, matcher.group(1));
                    updateChannelState(NuvoEnum.zoneMap.get(key), CHANNEL_TYPE_TREBLE, matcher.group(2));
                    updateChannelState(NuvoEnum.zoneMap.get(key), CHANNEL_TYPE_BALANCE, NuvoStatusCodes.getBalanceFromStr(matcher.group(3)));
                    updateChannelState(NuvoEnum.zoneMap.get(key), CHANNEL_TYPE_LOUDNESS, "1".equals(matcher.group(4)) ? NuvoCommand.ON.getValue() : NuvoCommand.OFF.getValue());
                } catch (IllegalStateException e){
                    logger.debug("no match on message: {}", updateData);
                }
                break;
                
            default:
                logger.debug("onNewMessageEvent: unhandled key {}", key);
                break;
        }
    }

    /**
     * Schedule the reconnection job
     */
    private void scheduleReconnectJob() {
        logger.debug("Schedule reconnect job");
        cancelReconnectJob();
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            if (!connector.isConnected()) {
                logger.debug("Trying to reconnect...");
                closeConnection();
                String error = null;
                if (openConnection()) {
                    synchronized (sequenceLock) {
                        try {
                            Long prevUpdateTime = lastEventReceived;

                            connector.sendCommand(NuvoCommand.GET_CONTROLLER_VERSION);
                    
                            NuvoEnum.validSources.forEach(source -> {
                                try {
                                    connector.sendQuery(NuvoEnum.sourceMap.get(source), NuvoCommand.NAME);
                                    Thread.sleep(SLEEP_BETWEEN_CMD);
                                    connector.sendQuery(NuvoEnum.sourceMap.get(source), NuvoCommand.DISPINFO);
                                    Thread.sleep(SLEEP_BETWEEN_CMD);
                                    connector.sendQuery(NuvoEnum.sourceMap.get(source), NuvoCommand.DISPLINE);
                                    Thread.sleep(SLEEP_BETWEEN_CMD);
                                } catch (NuvoException | InterruptedException e) {
                                    logger.debug("Error Querying Source data: {}", e.getMessage());
                                }
                            });
                            
                            // Query all active zones to get their current status and eq configuration
                            activeZones.forEach(zoneNum -> {
                                try {
                                    connector.sendQuery(NuvoEnum.valueOf(ZONE+zoneNum), NuvoCommand.STATUS);
                                    Thread.sleep(SLEEP_BETWEEN_CMD);
                                    connector.sendCfgCommand(NuvoEnum.valueOf(ZONE+zoneNum), NuvoCommand.EQ_QUERY, "");
                                    Thread.sleep(SLEEP_BETWEEN_CMD);
                                } catch (NuvoException | InterruptedException e) {
                                    logger.debug("Error Querying Zone data: {}", e.getMessage());
                                }
                            });
                            
                            // prevUpdateTime should have changed if a zone update was received
                            if (prevUpdateTime.equals(lastEventReceived)) {
                                error = "Controller not responding to status requests";
                            } else {                
                                // Put the source labels on all active zones
                                activeZones.forEach(zoneNum -> {
                                    stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), ZONE.toLowerCase() + zoneNum.toString() +
                                                                                CHANNEL_DELIMIT + CHANNEL_TYPE_SOURCE), sourceLabels);
                                });
                            }
                        } catch (NuvoException e) {
                            error = "First command after connection failed";
                            logger.debug("{}: {}", error, e.getMessage());
                        }
                    }
                } else {
                    error = "Reconnection failed";
                }
                if (error != null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
                    closeConnection();
                } else {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, this.versionString);
                }
            }
        }, 1, RECON_POLLING_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Cancel the reconnection job
     */
    private void cancelReconnectJob() {
        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null && !reconnectJob.isCancelled()) {
            reconnectJob.cancel(true);
            this.reconnectJob = null;
        }
    }
    
    /**
     * Schedule the polling job
     */
    private void schedulePollingJob() {
        logger.debug("Schedule polling job");
        cancelPollingJob();

        // when the Nuvo amp is off, this will keep the connection (esp Serial over IP) alive and detect if the connection goes down
        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            if (connector.isConnected()) {
                logger.debug("Polling the component for updated status...");

                synchronized (sequenceLock) {
                    try {
                        connector.sendCommand(NuvoCommand.GET_CONTROLLER_VERSION);
                    } catch (NuvoException e) {
                        logger.debug("Polling error: {}", e.getMessage());
                    }

                    // if the last event received was more than 1.25 intervals ago,
                    // the component is not responding even though the connection is still good
                    if ((System.currentTimeMillis() - lastEventReceived) > (POLLING_INTERVAL * 1.25 * 1000)) {
                        logger.debug("Component not responding to status requests");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Component not responding to status requests");
                        closeConnection();
                        scheduleReconnectJob();
                    } 
                }
            }
        }, INITIAL_POLLING_DELAY, POLLING_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Cancel the polling job
     */
    private void cancelPollingJob() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }
    
    /**
     * Schedule the clock sync job
     */
    private void scheduleClockSyncJob() {
        logger.debug("Schedule clock sync job");
        cancelClockSyncJob();
        clockSyncJob = scheduler.scheduleWithFixedDelay(() -> {
            if (this.isGConcerto) {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm");
                    connector.sendCommand(NuvoCommand.CFGTIME.getValue() + simpleDateFormat.format(new Date()));
                } catch (NuvoException e) {
                    logger.debug("Error syncing clock: {}", e.getMessage());
                }
            } else {
                this.cancelClockSyncJob();
            }
        }, INITIAL_CLOCK_SYNC_DELAY, CLOCK_SYNC_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Cancel the clock sync job
     */
    private void cancelClockSyncJob() {
        ScheduledFuture<?> clockSyncJob = this.clockSyncJob;
        if (clockSyncJob != null && !clockSyncJob.isCancelled()) {
            clockSyncJob.cancel(true);
            this.clockSyncJob = null;
        }
    }

    /**
     * Update the state of a channel
     *
     * @param target the channel group
     * @param channelType the channel group item
     * @param value the value to be updated
     */
    private void updateChannelState(NuvoEnum target, String channelType, String value) {
        String channel = target.name().toLowerCase() + CHANNEL_DELIMIT + channelType;
        
        if (!isLinked(channel)) {
            return;
        }

        State state = UnDefType.UNDEF;
        
        if (UNDEF.equals(value)) {
            updateState(channel, state);
            return;
        }
        
        switch (channelType) {
            case CHANNEL_TYPE_POWER:
            case CHANNEL_TYPE_MUTE:
            case CHANNEL_TYPE_DND:
            case CHANNEL_TYPE_PARTY:
            case CHANNEL_TYPE_ALLMUTE:
            case CHANNEL_TYPE_PAGE:
            case CHANNEL_TYPE_LOUDNESS:
                state = NuvoCommand.ON.getValue().equals(value) ? OnOffType.ON : OnOffType.OFF;
                break;
            case CHANNEL_TYPE_SOURCE:
            case CHANNEL_TYPE_TREBLE:
            case CHANNEL_TYPE_BASS:
            case CHANNEL_TYPE_BALANCE:
            case CHANNEL_TYPE_LOCK:
                state = new DecimalType(value);
                break;
            case CHANNEL_TYPE_VOLUME:
                int volume = Integer.parseInt(value);
                long volumePct = Math
                        .round((double) (MAX_VOLUME - volume) / (double) (MAX_VOLUME - MIN_VOLUME) * 100.0);
                state = new PercentType(BigDecimal.valueOf(volumePct));
                break;
            case CHANNEL_DISPLAY_LINE1:
            case CHANNEL_DISPLAY_LINE2:
            case CHANNEL_DISPLAY_LINE3:
            case CHANNEL_DISPLAY_LINE4:
            case CHANNEL_BUTTON_PRESS:
                state = new StringType(value);
                break;
            case CHANNEL_PLAY_MODE:
                state = new StringType(NuvoStatusCodes.playMode.get(value));
                break;
            case CHANNEL_TRACK_LENGTH:
            case CHANNEL_TRACK_POSITION:
                state = new QuantityType<Time>(Integer.parseInt(value)/10, this.API_SECOND_UNIT);
                break;
            default:
                break;
        }
        updateState(channel, state);
    }
    
    /**
     * Handle a button press from a UI Player item
     *
     * @param target the nuvo zone to recieve the command
     * @param command the button press command to send to the zone
     */
    private void handleControlCommand(NuvoEnum target, Command command) throws NuvoException {
        if (command instanceof PlayPauseType) {
            connector.sendCommand(target, NuvoCommand.PLAYPAUSE);
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                connector.sendCommand(target, NuvoCommand.NEXT);
            } else if (command == NextPreviousType.PREVIOUS) {
                connector.sendCommand(target, NuvoCommand.PREV);
            }
        } else {
            logger.debug("Unknown control command: {}", command);
        }
    }

}
