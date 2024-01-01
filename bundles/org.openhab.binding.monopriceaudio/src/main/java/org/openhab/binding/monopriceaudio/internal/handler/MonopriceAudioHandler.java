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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioException;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioStateDescriptionOptionProvider;
import org.openhab.binding.monopriceaudio.internal.communication.AmplifierModel;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioConnector;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioDefaultConnector;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioIpConnector;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioMessageEvent;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioMessageEventListener;
import org.openhab.binding.monopriceaudio.internal.communication.MonopriceAudioSerialConnector;
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
 * @author Michael Lobstein - Add support for additional amplifier types
 */
@NonNullByDefault
public class MonopriceAudioHandler extends BaseThingHandler implements MonopriceAudioMessageEventListener {
    private static final long RECON_POLLING_INTERVAL_SEC = 60;
    private static final long INITIAL_POLLING_DELAY_SEC = 10;

    private static final String ZONE = "zone";
    private static final String ALL = "all";
    private static final String CHANNEL_DELIMIT = "#";

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int MIN_VOLUME = 0;

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioHandler.class);
    private final AmplifierModel amp;
    private final MonopriceAudioStateDescriptionOptionProvider stateDescriptionProvider;
    private final SerialPortManager serialPortManager;

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> pollingJob;

    private MonopriceAudioConnector connector = new MonopriceAudioDefaultConnector();

    private Map<String, MonopriceAudioZoneDTO> zoneDataMap = Map.of(ZONE, new MonopriceAudioZoneDTO());
    private Set<String> ignoreZones = new HashSet<>();
    private long lastPollingUpdate = System.currentTimeMillis();
    private long pollingInterval = ZERO;
    private int numZones = ZERO;
    private int allVolume = ONE;
    private int initialAllVolume = ZERO;
    private boolean disableKeypadPolling = false;
    private Object sequenceLock = new Object();

    public MonopriceAudioHandler(Thing thing, AmplifierModel amp,
            MonopriceAudioStateDescriptionOptionProvider stateDescriptionProvider,
            SerialPortManager serialPortManager) {
        super(thing);
        this.amp = amp;
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
        numZones = config.numZones;
        final String ignoreZonesConfig = config.ignoreZones;
        disableKeypadPolling = config.disableKeypadPolling || amp == AmplifierModel.MONOPRICE70;

        // build a Map with a MonopriceAudioZoneDTO for each zoneId
        zoneDataMap = amp.getZoneIds().stream().limit(numZones)
                .collect(Collectors.toMap(s -> s, s -> new MonopriceAudioZoneDTO(s)));

        // Check configuration settings
        if (serialPort != null && host == null && port == null) {
            if (serialPort.toLowerCase().startsWith("rfc2217")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error-rfc2217");
                return;
            }
        } else if (serialPort != null && (host != null || port != null)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-conflict");
            return;
        }

        if (serialPort != null) {
            connector = new MonopriceAudioSerialConnector(serialPortManager, serialPort, uid, amp);
        } else if (host != null && (port != null && port > ZERO)) {
            connector = new MonopriceAudioIpConnector(host, port, uid, amp);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-missing");
            return;
        }

        pollingInterval = config.pollingInterval;
        initialAllVolume = config.initialAllVolume;

        // If zones were specified to be ignored by the 'all*' commands, use the specified binding
        // zone ids to get the amplifier's internal zone ids and save those to a list
        if (ignoreZonesConfig != null) {
            for (String zone : ignoreZonesConfig.split(",")) {
                try {
                    int zoneInt = Integer.parseInt(zone);
                    if (zoneInt >= ONE && zoneInt <= amp.getMaxZones()) {
                        ignoreZones.add(ZONE + zoneInt);
                    } else {
                        logger.debug("Invalid ignore zone value: {}, value must be between {} and {}", zone, ONE,
                                amp.getMaxZones());
                    }
                } catch (NumberFormatException nfe) {
                    logger.debug("Invalid ignore zone value: {}", zone);
                }
            }
        }

        // Put the source labels on all active zones
        List<Integer> activeZones = IntStream.range(1, numZones + 1).boxed().collect(Collectors.toList());

        List<StateOption> sourceLabels = amp.getSourceLabels(config);
        stateDescriptionProvider.setStateOptions(
                new ChannelUID(getThing().getUID(), ALL + CHANNEL_DELIMIT + CHANNEL_TYPE_ALLSOURCE), sourceLabels);
        activeZones.forEach(zoneNum -> {
            stateDescriptionProvider.setStateOptions(
                    new ChannelUID(getThing().getUID(), ZONE + zoneNum + CHANNEL_DELIMIT + CHANNEL_TYPE_SOURCE),
                    sourceLabels);
        });

        // remove the channels for the zones we are not using
        if (numZones < amp.getMaxZones()) {
            List<Channel> channels = new ArrayList<>(this.getThing().getChannels());

            List<Integer> zonesToRemove = IntStream.range(numZones + 1, amp.getMaxZones() + 1).boxed()
                    .collect(Collectors.toList());

            zonesToRemove.forEach(zone -> {
                channels.removeIf(c -> (c.getUID().getId().contains(ZONE + zone)));
            });
            updateThing(editThing().withChannels(channels).build());
        }

        // initialize the all volume state
        allVolume = initialAllVolume;
        long allVolumePct = Math
                .round((initialAllVolume - MIN_VOLUME) / (double) (amp.getMaxVol() - MIN_VOLUME) * 100.0);
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
        String channelType = channelSplit[1];
        String zoneName = channelSplit[0];
        String zoneId = amp.getZoneIdFromZoneName(zoneName);

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
                updateChannelState(zoneId, channelType);
                return;
            }

            Stream<String> zoneStream = amp.getZoneIds().stream().limit(numZones);
            try {
                switch (channelType) {
                    case CHANNEL_TYPE_POWER:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(zoneId, amp.getPowerCmd(), command == OnOffType.ON ? ONE : ZERO);
                            zoneDataMap.get(zoneId)
                                    .setPower(command == OnOffType.ON ? amp.getOnStr() : amp.getOffStr());
                        }
                        break;
                    case CHANNEL_TYPE_SOURCE:
                        if (command instanceof DecimalType decimalCommand) {
                            final int value = decimalCommand.intValue();
                            if (value >= ONE && value <= amp.getNumSources()) {
                                logger.debug("Got source command {} zone {}", value, zoneId);
                                connector.sendCommand(zoneId, amp.getSourceCmd(), value);
                                zoneDataMap.get(zoneId).setSource(amp.getFormattedValue(value));
                            }
                        }
                        break;
                    case CHANNEL_TYPE_VOLUME:
                        if (command instanceof PercentType percentCommand) {
                            final int value = (int) Math
                                    .round(percentCommand.doubleValue() / 100.0 * (amp.getMaxVol() - MIN_VOLUME))
                                    + MIN_VOLUME;
                            logger.debug("Got volume command {} zone {}", value, zoneId);
                            connector.sendCommand(zoneId, amp.getVolumeCmd(), value);
                            zoneDataMap.get(zoneId).setVolume(value);
                        }
                        break;
                    case CHANNEL_TYPE_MUTE:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(zoneId, amp.getMuteCmd(), command == OnOffType.ON ? ONE : ZERO);
                            zoneDataMap.get(zoneId).setMute(command == OnOffType.ON ? amp.getOnStr() : amp.getOffStr());
                        }
                        break;
                    case CHANNEL_TYPE_TREBLE:
                        if (command instanceof DecimalType decimalCommand) {
                            final int value = decimalCommand.intValue();
                            if (value >= amp.getMinTone() && value <= amp.getMaxTone()) {
                                logger.debug("Got treble command {} zone {}", value, zoneId);
                                connector.sendCommand(zoneId, amp.getTrebleCmd(), value + amp.getToneOffset());
                                zoneDataMap.get(zoneId).setTreble(value + amp.getToneOffset());
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BASS:
                        if (command instanceof DecimalType decimalCommand) {
                            final int value = decimalCommand.intValue();
                            if (value >= amp.getMinTone() && value <= amp.getMaxTone()) {
                                logger.debug("Got bass command {} zone {}", value, zoneId);
                                connector.sendCommand(zoneId, amp.getBassCmd(), value + amp.getToneOffset());
                                zoneDataMap.get(zoneId).setBass(value + amp.getToneOffset());
                            }
                        }
                        break;
                    case CHANNEL_TYPE_BALANCE:
                        if (command instanceof DecimalType decimalCommand) {
                            final int value = decimalCommand.intValue();
                            if (value >= amp.getMinBal() && value <= amp.getMaxBal()) {
                                logger.debug("Got balance command {} zone {}", value, zoneId);
                                connector.sendCommand(zoneId, amp.getBalanceCmd(), value + amp.getBalOffset());
                                zoneDataMap.get(zoneId).setBalance(value + amp.getBalOffset());
                            }
                        }
                        break;
                    case CHANNEL_TYPE_DND:
                        if (command instanceof OnOffType) {
                            connector.sendCommand(zoneId, amp.getDndCmd(), command == OnOffType.ON ? ONE : ZERO);
                            zoneDataMap.get(zoneId).setDnd(command == OnOffType.ON ? amp.getOnStr() : amp.getOffStr());
                        }
                        break;
                    case CHANNEL_TYPE_ALLPOWER:
                        if (command instanceof OnOffType) {
                            final int cmd = command == OnOffType.ON ? ONE : ZERO;
                            zoneStream.forEach((streamZoneId) -> {
                                if (command == OnOffType.OFF || !ignoreZones.contains(amp.getZoneName(streamZoneId))) {
                                    try {
                                        connector.sendCommand(streamZoneId, amp.getPowerCmd(), cmd);
                                        zoneDataMap.get(streamZoneId).setPower(amp.getFormattedValue(cmd));
                                        updateChannelState(streamZoneId, CHANNEL_TYPE_POWER);

                                        if (command == OnOffType.ON) {
                                            // reset the volume of each zone to allVolume
                                            connector.sendCommand(streamZoneId, amp.getVolumeCmd(), allVolume);
                                            zoneDataMap.get(streamZoneId).setVolume(allVolume);
                                            updateChannelState(streamZoneId, CHANNEL_TYPE_VOLUME);
                                        }
                                    } catch (MonopriceAudioException e) {
                                        logger.debug("Error Turning All Zones On: {}", e.getMessage());
                                    }
                                }

                            });
                        }
                        break;
                    case CHANNEL_TYPE_ALLSOURCE:
                        if (command instanceof DecimalType decimalCommand) {
                            final int value = decimalCommand.intValue();
                            if (value >= ONE && value <= amp.getNumSources()) {
                                zoneStream.forEach((streamZoneId) -> {
                                    if (!ignoreZones.contains(amp.getZoneName(streamZoneId))) {
                                        try {
                                            connector.sendCommand(streamZoneId, amp.getSourceCmd(), value);
                                            if (zoneDataMap.get(streamZoneId).isPowerOn()
                                                    && !zoneDataMap.get(streamZoneId).isMuted()) {
                                                zoneDataMap.get(streamZoneId).setSource(amp.getFormattedValue(value));
                                                updateChannelState(streamZoneId, CHANNEL_TYPE_SOURCE);
                                            }
                                        } catch (MonopriceAudioException e) {
                                            logger.debug("Error Setting Source for All Zones: {}", e.getMessage());
                                        }
                                    }
                                });
                            }
                        }
                        break;
                    case CHANNEL_TYPE_ALLVOLUME:
                        if (command instanceof PercentType percentCommand) {
                            allVolume = (int) Math
                                    .round(percentCommand.doubleValue() / 100.0 * (amp.getMaxVol() - MIN_VOLUME))
                                    + MIN_VOLUME;
                            zoneStream.forEach((streamZoneId) -> {
                                if (!ignoreZones.contains(amp.getZoneName(streamZoneId))) {
                                    try {
                                        connector.sendCommand(streamZoneId, amp.getVolumeCmd(), allVolume);
                                        if (zoneDataMap.get(streamZoneId).isPowerOn()
                                                && !zoneDataMap.get(streamZoneId).isMuted()) {
                                            zoneDataMap.get(streamZoneId).setVolume(allVolume);
                                            updateChannelState(streamZoneId, CHANNEL_TYPE_VOLUME);
                                        }
                                    } catch (MonopriceAudioException e) {
                                        logger.debug("Error Setting Volume for All Zones: {}", e.getMessage());
                                    }
                                }
                            });
                        }
                        break;
                    case CHANNEL_TYPE_ALLMUTE:
                        if (command instanceof OnOffType) {
                            final int cmd = command == OnOffType.ON ? ONE : ZERO;
                            zoneStream.forEach((streamZoneId) -> {
                                if (!ignoreZones.contains(amp.getZoneName(streamZoneId))) {
                                    try {
                                        connector.sendCommand(streamZoneId, amp.getMuteCmd(), cmd);
                                        if (zoneDataMap.get(streamZoneId).isPowerOn()) {
                                            zoneDataMap.get(streamZoneId).setMute(amp.getFormattedValue(cmd));
                                            updateChannelState(streamZoneId, CHANNEL_TYPE_MUTE);
                                        }
                                    } catch (MonopriceAudioException e) {
                                        logger.debug("Error Setting Mute for All Zones: {}", e.getMessage());
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
                logger.debug("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error-failed");
                closeConnection();
                scheduleReconnectJob();
            }
        }
    }

    /**
     * Open the connection to the amplifier
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
     * Close the connection to the amplifier
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
        updateStatus(ThingStatus.ONLINE);
        String key = evt.getKey();

        switch (key) {
            case MonopriceAudioConnector.KEY_ZONE_UPDATE:
                MonopriceAudioZoneDTO newZoneData = amp.getZoneData(evt.getValue());
                MonopriceAudioZoneDTO zoneData = zoneDataMap.get(newZoneData.getZone());
                if (amp.getZoneIds().contains(newZoneData.getZone()) && zoneData != null) {
                    if (amp == AmplifierModel.MONOPRICE70) {
                        processMonoprice70Update(zoneData, newZoneData);
                    } else {
                        processZoneUpdate(zoneData, newZoneData);
                    }
                } else {
                    logger.debug("invalid event: {} for key: {} or zone data null", evt.getValue(), key);
                }
                break;

            case MonopriceAudioConnector.KEY_PING:
                lastPollingUpdate = System.currentTimeMillis();
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
            synchronized (sequenceLock) {
                if (!connector.isConnected()) {
                    logger.debug("Trying to reconnect...");
                    closeConnection();
                    String error = null;

                    if (openConnection()) {
                        // poll all zones on the amplifier to get current state
                        amp.getZoneIds().stream().limit(numZones).forEach((streamZoneId) -> {
                            try {
                                connector.queryZone(streamZoneId);

                                if (amp == AmplifierModel.MONOPRICE70) {
                                    connector.queryTrebBassBalance(streamZoneId);
                                }
                            } catch (MonopriceAudioException e) {
                                logger.debug("Polling error: {}", e.getMessage());
                            }
                        });

                        if (amp == AmplifierModel.XANTECH) {
                            try {
                                // for xantech send the commands to enable unsolicited updates
                                connector.sendCommand("!ZA1");
                                connector.sendCommand("!ZP10"); // Zone Periodic Auto Update set to 10 secs
                            } catch (MonopriceAudioException e) {
                                logger.debug("Error sending Xantech periodic update commands: {}", e.getMessage());
                            }
                        }
                    } else {
                        error = "@text/offline.communication-error-reconnection";
                    }
                    if (error != null) {
                        closeConnection();
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
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
                    logger.debug("Polling the amplifier for updated status...");

                    if (!disableKeypadPolling) {
                        // poll each zone up to the number of zones specified in the configuration
                        amp.getZoneIds().stream().limit(numZones).forEach((streamZoneId) -> {
                            try {
                                connector.queryZone(streamZoneId);
                            } catch (MonopriceAudioException e) {
                                logger.debug("Polling error for zone id {}: {}", streamZoneId, e.getMessage());
                            }
                        });
                    } else {
                        try {
                            // ping only (no zone updates) to verify the connection is still alive
                            connector.sendPing();
                        } catch (MonopriceAudioException e) {
                            logger.debug("Ping error: {}", e.getMessage());
                        }
                    }

                    // if the last successful polling update was more than 2.25 intervals ago, the amplifier
                    // is either switched off or not responding even though the connection is still good
                    if ((System.currentTimeMillis() - lastPollingUpdate) > (pollingInterval * 2.25 * 1000)) {
                        logger.debug("Amplifier not responding to status requests");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/offline.communication-error-polling");
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

    private void processZoneUpdate(MonopriceAudioZoneDTO zoneData, MonopriceAudioZoneDTO newZoneData) {
        // only process the update if something actually changed in this zone since the last polling update
        if (!newZoneData.toString().equals(zoneData.toString())) {
            if (!newZoneData.getPage().equals(zoneData.getPage())) {
                zoneData.setPage(newZoneData.getPage());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_PAGE);
            }

            if (!newZoneData.getPower().equals(zoneData.getPower())) {
                zoneData.setPower(newZoneData.getPower());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_POWER);
            }

            if (!newZoneData.getMute().equals(zoneData.getMute())) {
                zoneData.setMute(newZoneData.getMute());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_MUTE);
            }

            if (!newZoneData.getDnd().equals(zoneData.getDnd())) {
                zoneData.setDnd(newZoneData.getDnd());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_DND);
            }

            if (newZoneData.getVolume() != zoneData.getVolume()) {
                zoneData.setVolume(newZoneData.getVolume());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_VOLUME);
            }

            if (newZoneData.getTreble() != zoneData.getTreble()) {
                zoneData.setTreble(newZoneData.getTreble());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_TREBLE);
            }

            if (newZoneData.getBass() != zoneData.getBass()) {
                zoneData.setBass(newZoneData.getBass());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_BASS);
            }

            if (newZoneData.getBalance() != zoneData.getBalance()) {
                zoneData.setBalance(newZoneData.getBalance());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_BALANCE);
            }

            if (!newZoneData.getSource().equals(zoneData.getSource())) {
                zoneData.setSource(newZoneData.getSource());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_SOURCE);
            }

            if (!newZoneData.getKeypad().equals(zoneData.getKeypad())) {
                zoneData.setKeypad(newZoneData.getKeypad());
                updateChannelState(zoneData.getZone(), CHANNEL_TYPE_KEYPAD);
            }

        }
        lastPollingUpdate = System.currentTimeMillis();
    }

    private void processMonoprice70Update(MonopriceAudioZoneDTO zoneData, MonopriceAudioZoneDTO newZoneData) {
        if (newZoneData.getTreble() != NIL) {
            zoneData.setTreble(newZoneData.getTreble());
            updateChannelState(zoneData.getZone(), CHANNEL_TYPE_TREBLE);
        } else if (newZoneData.getBass() != NIL) {
            zoneData.setBass(newZoneData.getBass());
            updateChannelState(zoneData.getZone(), CHANNEL_TYPE_BASS);
        } else if (newZoneData.getBalance() != NIL) {
            zoneData.setBalance(newZoneData.getBalance());
            updateChannelState(zoneData.getZone(), CHANNEL_TYPE_BALANCE);
        } else {
            zoneData.setPower(newZoneData.getPower());
            zoneData.setMute(newZoneData.getMute());
            zoneData.setVolume(newZoneData.getVolume());
            zoneData.setSource(newZoneData.getSource());
            updateChannelState(zoneData.getZone(), CHANNEL_TYPE_POWER);
            updateChannelState(zoneData.getZone(), CHANNEL_TYPE_MUTE);
            updateChannelState(zoneData.getZone(), CHANNEL_TYPE_VOLUME);
            updateChannelState(zoneData.getZone(), CHANNEL_TYPE_SOURCE);

        }
        lastPollingUpdate = System.currentTimeMillis();
    }

    /**
     * Update the state of a channel
     *
     * @param zoneId the zone id used to lookup the channel to be updated
     * @param channelType the channel type to be updated
     */
    private void updateChannelState(String zoneId, String channelType) {
        MonopriceAudioZoneDTO zoneData = zoneDataMap.get(zoneId);

        if (zoneData != null) {
            String channel = amp.getZoneName(zoneId) + CHANNEL_DELIMIT + channelType;

            if (!isLinked(channel)) {
                return;
            }

            logger.debug("updating channel state for zone: {}, channel type: {}", zoneId, channelType);

            State state = UnDefType.UNDEF;
            switch (channelType) {
                case CHANNEL_TYPE_POWER:
                    state = OnOffType.from(zoneData.isPowerOn());
                    break;
                case CHANNEL_TYPE_SOURCE:
                    state = new DecimalType(zoneData.getSource());
                    break;
                case CHANNEL_TYPE_VOLUME:
                    long volumePct = Math.round(
                            (zoneData.getVolume() - MIN_VOLUME) / (double) (amp.getMaxVol() - MIN_VOLUME) * 100.0);
                    state = new PercentType(BigDecimal.valueOf(volumePct));
                    break;
                case CHANNEL_TYPE_MUTE:
                    state = OnOffType.from(zoneData.isMuted());
                    break;
                case CHANNEL_TYPE_TREBLE:
                    state = new DecimalType(BigDecimal.valueOf(zoneData.getTreble() - amp.getToneOffset()));
                    break;
                case CHANNEL_TYPE_BASS:
                    state = new DecimalType(BigDecimal.valueOf(zoneData.getBass() - amp.getToneOffset()));
                    break;
                case CHANNEL_TYPE_BALANCE:
                    state = new DecimalType(BigDecimal.valueOf(zoneData.getBalance() - amp.getBalOffset()));
                    break;
                case CHANNEL_TYPE_DND:
                    state = OnOffType.from(zoneData.isDndOn());
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
    }
}
