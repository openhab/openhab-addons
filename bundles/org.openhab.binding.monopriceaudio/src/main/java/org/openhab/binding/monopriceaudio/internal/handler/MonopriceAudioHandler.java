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
package org.openhab.binding.monopriceaudio.internal.handler;

import static org.openhab.binding.monopriceaudio.internal.MonopriceAudioBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioException;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioStateDescriptionOptionProvider;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioCommand;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioConnector;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioDefaultConnector;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioIpConnector;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioMessageEvent;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioMessageEventListener;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioSerialConnector;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioZone;
import org.openhab.binding.monopriceaudio.internal.configuration.MonopriceAudioThingConfiguration;
import org.openhab.binding.monopriceaudio.internal.dto.MonopriceAudioZoneDTO;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MonopriceAudioHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * Based on the Rotel binding by Laurent Garnier
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class MonopriceAudioHandler extends BaseThingHandler implements MonopriceAudioMessageEventListener {
    private static final long RECON_POLLING_INTERVAL_SEC = 60;
    private static final long INITIAL_POLLING_DELAY_SEC = 5;
    private static final Pattern PATTERN = Pattern
            .compile("^(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})");

    private static final String ZONE = "ZONE";
    private static final String ALL = "all";
    private static final String CHANNEL_DELIMIT = "#";
    private static final String ON_STR = "01";
    private static final String OFF_STR = "00";

    private static final int ONE = 1;
    private static final int MAX_ZONES = 18;
    private static final int MAX_SRC = 6;
    private static final int MIN_VOLUME = 0;
    private static final int MAX_VOLUME = 38;
    private static final int MIN_TONE = -7;
    private static final int MAX_TONE = 7;
    private static final int MIN_BALANCE = -10;
    private static final int MAX_BALANCE = 10;
    private static final int BALANCE_OFFSET = 10;
    private static final int TONE_OFFSET = 7;

    // build a Map with a MonopriceAudioZoneDTO for each zoneId
    private final Map<String, MonopriceAudioZoneDTO> zoneDataMap = MonopriceAudioZone.VALID_ZONE_IDS.stream()
            .collect(Collectors.toMap(s -> s, s -> new MonopriceAudioZoneDTO()));

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioHandler.class);
    private final MonopriceAudioStateDescriptionOptionProvider stateDescriptionProvider;
    private final SerialPortManager serialPortManager;

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollingJob;

    private MonopriceAudioConnector connector = new MonopriceAudioDefaultConnector();

    private Set<String> ignoreZones = new HashSet<>();
    private long lastPollingUpdate = System.currentTimeMillis();
    private long pollingInterval = 0;
    private int numZones = 0;
    private int allVolume = 1;
    private int initialAllVolume = 0;
    private Object sequenceLock = new Object();

    public MonopriceAudioHandler(Thing thing, MonopriceAudioStateDescriptionOptionProvider stateDescriptionProvider,
            SerialPortManager serialPortManager) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        final String uid = this.getThing().getUID().getAsString();
        MonopriceAudioThingConfiguration config = getConfigAs(MonopriceAudioThingConfiguration.class);
        final String serialPort = config.serialPort;
        final String host = config.host;
        final Integer port = config.port;
        final String ignoreZonesConfig = config.ignoreZones;

        // Check configuration settings
        String configError = null;
        if ((serialPort == null || serialPort.isEmpty()) && (host == null || host.isEmpty())) {
            configError = "undefined serialPort and host configuration settings; please set one of them";
        } else if (serialPort != null && (host == null || host.isEmpty())) {
            if (serialPort.toLowerCase().startsWith("rfc2217")) {
                configError = "use host and port configuration settings for a serial over IP connection";
            }
        } else {
            if (port == null) {
                configError = "undefined port configuration setting";
            } else if (port <= 0) {
                configError = "invalid port configuration setting";
            }
        }

        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
            return;
        }

        if (serialPort != null) {
            connector = new MonopriceAudioSerialConnector(serialPortManager, serialPort, uid);
        } else if (port != null) {
            connector = new MonopriceAudioIpConnector(host, port, uid);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Either Serial port or Host & Port must be specifed");
            return;
        }

        pollingInterval = config.pollingInterval;
        numZones = config.numZones;
        initialAllVolume = config.initialAllVolume;

        // If zones were specified to be ignored by the 'all*' commands, use the specified binding
        // zone ids to get the controller's internal zone ids and save those to a list
        if (ignoreZonesConfig != null) {
            for (String zone : ignoreZonesConfig.split(",")) {
                try {
                    int zoneInt = Integer.parseInt(zone);
                    if (zoneInt >= ONE && zoneInt <= MAX_ZONES) {
                        ignoreZones.add(ZONE + zoneInt);
                    } else {
                        logger.warn("Invalid ignore zone value: {}, value must be between {} and {}", zone, ONE,
                                MAX_ZONES);
                    }
                } catch (NumberFormatException nfe) {
                    logger.warn("Invalid ignore zone value: {}", zone);
                }
            }
        }

        // Build a state option list for the source labels
        List<StateOption> sourcesLabels = new ArrayList<>();
        sourcesLabels.add(new StateOption("1", config.inputLabel1));
        sourcesLabels.add(new StateOption("2", config.inputLabel2));
        sourcesLabels.add(new StateOption("3", config.inputLabel3));
        sourcesLabels.add(new StateOption("4", config.inputLabel4));
        sourcesLabels.add(new StateOption("5", config.inputLabel5));
        sourcesLabels.add(new StateOption("6", config.inputLabel6));

        // Put the source labels on all active zones
        List<Integer> activeZones = IntStream.range(1, numZones + 1).boxed().collect(Collectors.toList());

        stateDescriptionProvider.setStateOptions(
                new ChannelUID(getThing().getUID(), ALL + CHANNEL_DELIMIT + CHANNEL_TYPE_ALLSOURCE), sourcesLabels);
        activeZones.forEach(zoneNum -> {
            stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(),
                    ZONE.toLowerCase() + zoneNum + CHANNEL_DELIMIT + CHANNEL_TYPE_SOURCE), sourcesLabels);
        });

        // remove the channels for the zones we are not using
        if (numZones < MAX_ZONES) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

            List<Integer> zonesToRemove = IntStream.range(numZones + 1, MAX_ZONES + 1).boxed()
                    .collect(Collectors.toList());

            zonesToRemove.forEach(zone -> {
                channels.removeIf(c -> (c.getUID().getId().contains(ZONE.toLowerCase() + zone)));
            });
            updateThing(editThing().withChannels(channels).build());
        }

        // initialize the all volume state
        allVolume = initialAllVolume;
        long allVolumePct = Math
                .round((double) (initialAllVolume - MIN_VOLUME) / (double) (MAX_VOLUME - MIN_VOLUME) * 100.0);
        updateState(ALL + CHANNEL_DELIMIT + CHANNEL_TYPE_ALLVOLUME, new PercentType(BigDecimal.valueOf(allVolumePct)));

        scheduleReconnectJob();
        schedulePollingJob();

        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        cancelReconnectJob();
        cancelPollingJob();
        closeConnection();
        ignoreZones.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();
        String[] channelSplit = channel.split(CHANNEL_DELIMIT);
        MonopriceAudioZone zone = MonopriceAudioZone.valueOf(channelSplit[0].toUpperCase());
        String channelType = channelSplit[1];

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command {} from channel {} is ignored", command, channel);
            return;
        }

        boolean success = true;
        synchronized (sequenceLock) {
            if (!connector.isConnected()) {
                logger.debug("Command {} from channel {} is ignored: connection not established", command, channel);
                return;
            }

            if (command instanceof RefreshType) {
                MonopriceAudioZoneDTO zoneDTO = zoneDataMap.get(zone.getZoneId());
                if (zoneDTO != null) {
                    updateChannelState(zone, channelType, zoneDTO);
                } else {
                    logger.info("Could not execute REFRESH command for zone {}: null", zone.getZoneId());
                }
                return;
            }

            Stream<String> zoneStream = MonopriceAudioZone.VALID_ZONES.stream().limit(numZones);
            try {
                switch (channelType) {
                    case CHANNEL_TYPE_POWER:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(zone, MonopriceAudioCommand.POWER, command == OnOffType.ON ? 1 : 0);
                            zoneDataMap.get(zone.getZoneId()).setPower(command == OnOffType.ON ? ON_STR : OFF_STR);
                        }
                        break;
                    case CHANNEL_TYPE_SOURCE:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            if (value >= ONE && value <= MAX_SRC) {
                                logger.debug("Got source command {} zone {}", value, zone);
                                connector.sendCommand(zone, MonopriceAudioCommand.SOURCE, value);
                                zoneDataMap.get(zone.getZoneId()).setSource(String.format("%02d", value));
                            }
                        }
                        break;
                    case CHANNEL_TYPE_VOLUME:
                        if (command instanceof PercentType) {
                            int value = (int) Math
                                    .round(((PercentType) command).doubleValue() / 100.0 * (MAX_VOLUME - MIN_VOLUME))
                                    + MIN_VOLUME;
                            logger.debug("Got volume command {} zone {}", value, zone);
                            connector.sendCommand(zone, MonopriceAudioCommand.VOLUME, value);
                            zoneDataMap.get(zone.getZoneId()).setVolume(value);
                        }
                        break;
                    case CHANNEL_TYPE_MUTE:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(zone, MonopriceAudioCommand.MUTE, command == OnOffType.ON ? 1 : 0);
                            zoneDataMap.get(zone.getZoneId()).setMute(command == OnOffType.ON ? ON_STR : OFF_STR);
                        }
                        break;
                    case CHANNEL_TYPE_TREBLE:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            if (value >= MIN_TONE && value <= MAX_TONE) {
                                logger.debug("Got treble command {} zone {}", value, zone);
                                connector.sendCommand(zone, MonopriceAudioCommand.TREBLE, value + TONE_OFFSET);
                                zoneDataMap.get(zone.getZoneId()).setTreble(value + TONE_OFFSET);
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BASS:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            if (value >= MIN_TONE && value <= MAX_TONE) {
                                logger.debug("Got bass command {} zone {}", value, zone);
                                connector.sendCommand(zone, MonopriceAudioCommand.BASS, value + TONE_OFFSET);
                                zoneDataMap.get(zone.getZoneId()).setBass(value + TONE_OFFSET);
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BALANCE:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            if (value >= MIN_BALANCE && value <= MAX_BALANCE) {
                                logger.debug("Got balance command {} zone {}", value, zone);
                                connector.sendCommand(zone, MonopriceAudioCommand.BALANCE, value + BALANCE_OFFSET);
                                zoneDataMap.get(zone.getZoneId()).setBalance(value + BALANCE_OFFSET);
                            }
                        }
                        break;
                    case CHANNEL_TYPE_DND:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(zone, MonopriceAudioCommand.DND, command == OnOffType.ON ? 1 : 0);
                            zoneDataMap.get(zone.getZoneId()).setDnd(command == OnOffType.ON ? ON_STR : OFF_STR);
                        }
                        break;
                    case CHANNEL_TYPE_ALLPOWER:
                        if (command instanceof OnOffType) {
                            zoneStream.forEach((zoneName) -> {
                                if (command == OnOffType.OFF || !ignoreZones.contains(zoneName)) {
                                    try {
                                        connector.sendCommand(MonopriceAudioZone.valueOf(zoneName),
                                                MonopriceAudioCommand.POWER, command == OnOffType.ON ? 1 : 0);
                                        if (command == OnOffType.ON) {
                                            // reset the volume of each zone to allVolume
                                            connector.sendCommand(MonopriceAudioZone.valueOf(zoneName),
                                                    MonopriceAudioCommand.VOLUME, allVolume);
                                        }
                                    } catch (MonopriceAudioException e) {
                                        logger.warn("Error Turning All Zones On: {}", e.getMessage());
                                    }
                                }

                            });
                        }
                        break;
                    case CHANNEL_TYPE_ALLSOURCE:
                        if (command instanceof DecimalType) {
                            int value = ((DecimalType) command).intValue();
                            if (value >= ONE && value <= MAX_SRC) {
                                zoneStream.forEach((zoneName) -> {
                                    if (!ignoreZones.contains(zoneName)) {
                                        try {
                                            connector.sendCommand(MonopriceAudioZone.valueOf(zoneName),
                                                    MonopriceAudioCommand.SOURCE, value);
                                        } catch (MonopriceAudioException e) {
                                            logger.warn("Error Setting Source for  All Zones: {}", e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                        break;
                    case CHANNEL_TYPE_ALLVOLUME:
                        if (command instanceof PercentType) {
                            int value = (int) Math
                                    .round(((PercentType) command).doubleValue() / 100.0 * (MAX_VOLUME - MIN_VOLUME))
                                    + MIN_VOLUME;
                            allVolume = value;
                            zoneStream.forEach((zoneName) -> {
                                if (!ignoreZones.contains(zoneName)) {
                                    try {
                                        connector.sendCommand(MonopriceAudioZone.valueOf(zoneName),
                                                MonopriceAudioCommand.VOLUME, value);
                                    } catch (MonopriceAudioException e) {
                                        logger.warn("Error Setting Volume for All Zones: {}", e.getMessage());
                                    }
                                }
                            });
                        }
                        break;
                    case CHANNEL_TYPE_ALLMUTE:
                        if (command instanceof OnOffType) {
                            int cmd = command == OnOffType.ON ? 1 : 0;
                            zoneStream.forEach((zoneName) -> {
                                if (!ignoreZones.contains(zoneName)) {
                                    try {
                                        connector.sendCommand(MonopriceAudioZone.valueOf(zoneName),
                                                MonopriceAudioCommand.MUTE, cmd);
                                    } catch (MonopriceAudioException e) {
                                        logger.warn("Error Setting Mute for All Zones: {}", e.getMessage());
                                    }
                                }
                            });
                        }
                        break;
                    default:
                        success = false;
                        logger.debug("Command {} from channel {} failed: unexpected command", command, channel);
                        break;
                }

                if (success) {
                    logger.trace("Command {} from channel {} succeeded", command, channel);
                }
            } catch (MonopriceAudioException e) {
                logger.warn("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Sending command failed");
                closeConnection();
                scheduleReconnectJob();
            }
        }
    }

    /**
     * Open the connection with the MonopriceAudio device
     *
     * @return true if the connection is opened successfully or false if not
     */
    private synchronized boolean openConnection() {
        connector.addEventListener(this);
        try {
            connector.open();
        } catch (MonopriceAudioException e) {
            logger.debug("openConnection() failed: {}", e.getMessage());
        }
        logger.debug("openConnection(): {}", connector.isConnected() ? "connected" : "disconnected");
        return connector.isConnected();
    }

    /**
     * Close the connection with the MonopriceAudio device
     */
    private synchronized void closeConnection() {
        if (connector.isConnected()) {
            connector.close();
            connector.removeEventListener(this);
            logger.debug("closeConnection(): disconnected");
        }
    }

    @Override
    public void onNewMessageEvent(MonopriceAudioMessageEvent evt) {
        String key = evt.getKey();
        String updateData = evt.getValue().trim();
        if (!MonopriceAudioConnector.KEY_ERROR.equals(key)) {
            updateStatus(ThingStatus.ONLINE);
        }
        try {
            switch (key) {
                case MonopriceAudioConnector.KEY_ERROR:
                    logger.debug("Reading feedback message failed");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Reading thread ended");
                    closeConnection();
                    break;

                case MonopriceAudioConnector.KEY_ZONE_UPDATE:
                    String zoneId = updateData.substring(0, 2);
                    MonopriceAudioZoneDTO zoneDTO = zoneDataMap.get(zoneId);
                    if (MonopriceAudioZone.VALID_ZONE_IDS.contains(zoneId) && zoneDTO != null) {
                        MonopriceAudioZone targetZone = MonopriceAudioZone.fromZoneId(zoneId);
                        processZoneUpdate(targetZone, zoneDTO, updateData);
                    } else {
                        logger.warn("invalid event: {} for key: {} or zone data null", evt.getValue(), key);
                    }
                    break;
                default:
                    logger.debug("onNewMessageEvent: unhandled key {}", key);
                    break;
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid value {} for key {}", updateData, key);
        } catch (MonopriceAudioException e) {
            logger.warn("Error processing zone update: {}", e.getMessage());
        }
    }

    /**
     * Schedule the reconnection job
     */
    private void scheduleReconnectJob() {
        logger.debug("Schedule reconnect job");
        cancelReconnectJob();
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            synchronized (sequenceLock) {
                if (!connector.isConnected()) {
                    logger.debug("Trying to reconnect...");
                    closeConnection();
                    String error = null;

                    if (openConnection()) {
                        try {
                            long prevUpdateTime = lastPollingUpdate;
                            connector.queryZone(MonopriceAudioZone.ZONE1);

                            // prevUpdateTime should have changed if a zone update was received
                            if (lastPollingUpdate == prevUpdateTime) {
                                error = "Controller not responding to status requests";
                            }

                        } catch (MonopriceAudioException e) {
                            error = "First command after connection failed";
                            logger.warn("{}: {}", error, e.getMessage());
                            closeConnection();
                        }
                    } else {
                        error = "Reconnection failed";
                    }
                    if (error != null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
                    } else {
                        updateStatus(ThingStatus.ONLINE);
                        lastPollingUpdate = System.currentTimeMillis();
                    }
                }
            }
        }, 1, RECON_POLLING_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * Cancel the reconnection job
     */
    private void cancelReconnectJob() {
        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null) {
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

        pollingJob = scheduler.scheduleWithFixedDelay(() -> {
            synchronized (sequenceLock) {
                if (connector.isConnected()) {
                    logger.debug("Polling the controller for updated status...");

                    // poll each zone up to the number of zones specified in the configuration
                    MonopriceAudioZone.VALID_ZONES.stream().limit(numZones).forEach((zoneName) -> {
                        try {
                            connector.queryZone(MonopriceAudioZone.valueOf(zoneName));
                        } catch (MonopriceAudioException e) {
                            logger.warn("Polling error: {}", e.getMessage());
                        }
                    });

                    // if the last successful polling update was more than 2.25 intervals ago, the controller
                    // is either switched off or not responding even though the connection is still good
                    if ((System.currentTimeMillis() - lastPollingUpdate) > (pollingInterval * 2.25 * 1000)) {
                        logger.warn("Controller not responding to status requests");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Controller not responding to status requests");
                        closeConnection();
                        scheduleReconnectJob();
                    }
                }
            }
        }, INITIAL_POLLING_DELAY_SEC, pollingInterval, TimeUnit.SECONDS);
    }

    /**
     * Cancel the polling job
     */
    private void cancelPollingJob() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    /**
     * Update the state of a channel
     *
     * @param channel the channel
     */
    private void updateChannelState(MonopriceAudioZone zone, String channelType, MonopriceAudioZoneDTO zoneData) {
        String channel = zone.name().toLowerCase() + CHANNEL_DELIMIT + channelType;

        if (!isLinked(channel)) {
            return;
        }

        State state = UnDefType.UNDEF;
        switch (channelType) {
            case CHANNEL_TYPE_POWER:
                state = zoneData.isPowerOn() ? OnOffType.ON : OnOffType.OFF;
                break;
            case CHANNEL_TYPE_SOURCE:
                state = new DecimalType(zoneData.getSource());
                break;
            case CHANNEL_TYPE_VOLUME:
                long volumePct = Math.round(
                        (double) (zoneData.getVolume() - MIN_VOLUME) / (double) (MAX_VOLUME - MIN_VOLUME) * 100.0);
                state = new PercentType(BigDecimal.valueOf(volumePct));
                break;
            case CHANNEL_TYPE_MUTE:
                state = zoneData.isMuted() ? OnOffType.ON : OnOffType.OFF;
                break;
            case CHANNEL_TYPE_TREBLE:
                state = new DecimalType(BigDecimal.valueOf(zoneData.getTreble() - TONE_OFFSET));
                break;
            case CHANNEL_TYPE_BASS:
                state = new DecimalType(BigDecimal.valueOf(zoneData.getBass() - TONE_OFFSET));
                break;
            case CHANNEL_TYPE_BALANCE:
                state = new DecimalType(BigDecimal.valueOf(zoneData.getBalance() - BALANCE_OFFSET));
                break;
            case CHANNEL_TYPE_DND:
                state = zoneData.isDndOn() ? OnOffType.ON : OnOffType.OFF;
                break;
            case CHANNEL_TYPE_PAGE:
                state = zoneData.isPageActive() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                break;
            case CHANNEL_TYPE_KEYPAD:
                state = zoneData.isKeypadActive() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                break;
            default:
                break;
        }
        updateState(channel, state);
    }

    private void processZoneUpdate(MonopriceAudioZone zone, MonopriceAudioZoneDTO zoneData, String newZoneData) {
        // only process the update if something actually changed in this zone since the last time through
        if (!newZoneData.equals(zoneData.toString())) {
            // example status string: 1200010000130809100601, matcher pattern from above:
            // "^(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})(\\d{2})"
            Matcher matcher = PATTERN.matcher(newZoneData);
            if (matcher.find()) {
                zoneData.setZone(matcher.group(1));

                if (!matcher.group(2).equals(zoneData.getPage())) {
                    zoneData.setPage(matcher.group(2));
                    updateChannelState(zone, CHANNEL_TYPE_PAGE, zoneData);
                }

                if (!matcher.group(3).equals(zoneData.getPower())) {
                    zoneData.setPower(matcher.group(3));
                    updateChannelState(zone, CHANNEL_TYPE_POWER, zoneData);
                }

                if (!matcher.group(4).equals(zoneData.getMute())) {
                    zoneData.setMute(matcher.group(4));
                    updateChannelState(zone, CHANNEL_TYPE_MUTE, zoneData);
                }

                if (!matcher.group(5).equals(zoneData.getDnd())) {
                    zoneData.setDnd(matcher.group(5));
                    updateChannelState(zone, CHANNEL_TYPE_DND, zoneData);
                }

                int volume = Integer.parseInt(matcher.group(6));
                if (volume != zoneData.getVolume()) {
                    zoneData.setVolume(volume);
                    updateChannelState(zone, CHANNEL_TYPE_VOLUME, zoneData);
                }

                int treble = Integer.parseInt(matcher.group(7));
                if (treble != zoneData.getTreble()) {
                    zoneData.setTreble(treble);
                    updateChannelState(zone, CHANNEL_TYPE_TREBLE, zoneData);
                }

                int bass = Integer.parseInt(matcher.group(8));
                if (bass != zoneData.getBass()) {
                    zoneData.setBass(bass);
                    updateChannelState(zone, CHANNEL_TYPE_BASS, zoneData);
                }

                int balance = Integer.parseInt(matcher.group(9));
                if (balance != zoneData.getBalance()) {
                    zoneData.setBalance(balance);
                    updateChannelState(zone, CHANNEL_TYPE_BALANCE, zoneData);
                }

                if (!matcher.group(10).equals(zoneData.getSource())) {
                    zoneData.setSource(matcher.group(10));
                    updateChannelState(zone, CHANNEL_TYPE_SOURCE, zoneData);
                }

                if (!matcher.group(11).equals(zoneData.getKeypad())) {
                    zoneData.setKeypad(matcher.group(11));
                    updateChannelState(zone, CHANNEL_TYPE_KEYPAD, zoneData);
                }
            } else {
                logger.debug("Invalid zone update message: {}", newZoneData);
            }

        }
        lastPollingUpdate = System.currentTimeMillis();
    }
}
