/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.smartmeter.dlms.internal;

import static org.openhab.binding.smartmeter.SmartMeterBindingConstants.*;
import static org.openhab.core.thing.DefaultSystemChannelTypeProvider.*;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.DlmsMeterConfiguration;
import org.openhab.binding.smartmeter.dlms.internal.helper.DlmsChannelInfo;
import org.openhab.binding.smartmeter.dlms.internal.helper.DlmsQuantityType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SerialConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DlmsMeterHandler} reads values from a DLMS/COSEM Smart Meter via
 * an IEC 62056-21 optical read head.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DlmsMeterHandler extends BaseThingHandler {

    private static final Duration INITIAL_REFRESH_DELAY = Duration.ofSeconds(5);
    private static final Duration CONNECTION_RETRY_INTERVAL = Duration.ofMinutes(5);

    private final Logger logger = LoggerFactory.getLogger(DlmsMeterHandler.class);

    private final Set<DlmsChannelInfo> dlmsChannelInfos = new HashSet<>();

    private @NonNullByDefault({}) DlmsMeterConfiguration config;
    private @Nullable DlmsConnection connection;
    private @Nullable ScheduledFuture<?> refreshTask;
    private @Nullable ScheduledFuture<?> reconnectTask;

    public DlmsMeterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        cancelTasks();
        dlmsChannelInfos.clear();
        DlmsConnection connection = this.connection;
        if (connection != null) {
            this.connection = null;
            try {
                connection.close();
            } catch (IOException e) {
                logger.debug("Error closing DLMS connection: {}", e.getMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels();
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        cancelTasks();
        reconnectTask = scheduler.scheduleWithFixedDelay(() -> goOnline(), 0, CONNECTION_RETRY_INTERVAL.toSeconds(),
                TimeUnit.SECONDS);
    }

    private void cancelTasks() {
        Future<?> task = reconnectTask;
        if (task != null) {
            task.cancel(false);
        }
        task = refreshTask;
        if (task != null) {
            task.cancel(false);
        }
        reconnectTask = null;
        refreshTask = null;
    }

    private void goOffline(@Nullable String cause) {
        cancelTasks();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause);
        connection = null;
        reconnectTask = scheduler.scheduleWithFixedDelay(() -> goOnline(), CONNECTION_RETRY_INTERVAL.toSeconds(),
                CONNECTION_RETRY_INTERVAL.toSeconds(), TimeUnit.SECONDS);
    }

    private void goOnline() {
        cancelTasks();
        config = getConfigAs(DlmsMeterConfiguration.class);
        // note: the connection process uses auto baudrate negotiation by default
        SerialConnectionBuilder connectionBuilder = new SerialConnectionBuilder(config.port);
        try {
            connection = connectionBuilder.build();
        } catch (IOException e) {
            goOffline(e.getMessage());
            return;
        }
        Set<DlmsChannelInfo> infos = getMeterInfos();
        if (!infos.isEmpty()) {
            dlmsChannelInfos.clear();
            dlmsChannelInfos.addAll(infos);
            if (createdChannels()) {
                refreshTask = scheduler.scheduleWithFixedDelay(() -> updateChannels(),
                        INITIAL_REFRESH_DELAY.toSeconds(), config.refresh, TimeUnit.SECONDS);
                updateStatus(ThingStatus.ONLINE);
                return;
            }
        }
        goOffline("@text/no-meter-channels-found");
    }

    /**
     * Create the OH channels from the list of meter channels.
     *
     * @return true if the meter contains any channels.
     */
    private boolean createdChannels() {
        List<Channel> channels = new ArrayList<>();
        DlmsConnection connection = this.connection;
        if (connection != null) {
            dlmsChannelInfos.forEach(info -> {
                try {
                    GetResult result = connection.get(info.getAttributeAddress());
                    if (result.getResultCode() == AccessResultCode.SUCCESS) {
                        String resultData = result.getResultData().getValue();
                        try {
                            Unit<?> unit = new DlmsQuantityType<>(resultData).getUnit();
                            ChannelTypeUID channelTypeUID;
                            if (unit.isCompatible(Units.AMPERE)) {
                                channelTypeUID = SYSTEM_CHANNEL_TYPE_UID_ELECTRIC_CURRENT;
                            } else if (unit.isCompatible(Units.VOLT)) {
                                channelTypeUID = SYSTEM_CHANNEL_TYPE_UID_ELECTRIC_VOLTAGE;
                            } else if (unit.isCompatible(Units.WATT_HOUR)) {
                                channelTypeUID = SYSTEM_CHANNEL_TYPE_UID_ELECTRIC_ENERGY;
                            } else if (unit.isCompatible(Units.WATT)) {
                                channelTypeUID = SYSTEM_CHANNEL_TYPE_UID_ELECTRIC_POWER;
                            } else if (unit.isCompatible(Units.VAR_HOUR)) {
                                channelTypeUID = SYSTEM_CHANNEL_TYPE_UID_ELECTRIC_ENERGY;
                            } else if (unit.isCompatible(Units.VAR)) {
                                channelTypeUID = SYSTEM_CHANNEL_TYPE_UID_ELECTRIC_POWER;
                            } else if (unit.isCompatible(Units.LITRE)) {
                                channelTypeUID = DLMS_CHANNEL_UID_VOLUME;
                            } else {
                                channelTypeUID = DLMS_CHANNEL_UID_GENERIC;
                            }
                            ChannelUID uid = new ChannelUID(getThing().getUID(), info.getChannelId());
                            ChannelBuilder channelBuilder = ChannelBuilder.create(uid).withType(channelTypeUID);
                            String label = info.getLabel();
                            if (label != null && !label.isBlank()) {
                                channelBuilder.withLabel(label);
                            }
                            channels.add(channelBuilder.build());
                            logger.debug("Meter channel: {}, data: {}, added OH channel: {}", info, resultData, uid);
                        } catch (IllegalArgumentException e) {
                            logger.debug("Meter channel: {}, data:{}, parse error:{}", info, resultData,
                                    e.getMessage());
                        }
                    } else {
                        logger.debug("Meter channel: {}, read error: {}", info, result);
                    }
                } catch (IOException e) {
                    logger.debug("Meter channel: {}, read error: {}", info, e.getMessage());
                }
            });
            // thing needs updating only when channels (in any order) are different
            if (!Objects.equals(new HashSet<>(thing.getChannels()), new HashSet<>(channels))) {
                updateThing(editThing().withChannels(channels).build());
            }
        }
        return !channels.isEmpty();
    }

    /**
     * Populate the list of meter channel informations.
     */
    @SuppressWarnings("unchecked")
    private Set<DlmsChannelInfo> getMeterInfos() {
        Set<DlmsChannelInfo> infos = new HashSet<>();
        DlmsConnection connection = this.connection;
        if (connection != null) {
            try {
                AttributeAddress address = new AttributeAddress(DLMS_CLASS_ID_LOGICAL_NAME,
                        new ObisCode(WellKnownInstanceIds.CURRENT_ASSOCIATION_ID), DLMS_ATTRIBUTE_ID_VALUE);
                GetResult result = connection.get(address);
                if (result.getResultCode() == AccessResultCode.SUCCESS) {
                    DataObject rootData = result.getResultData();
                    logger.trace("Channel response: {}", rootData);
                    List<DataObject> rootEntries = null;
                    if (rootData.getValue() instanceof List<?> rootList) {
                        rootEntries = (List<DataObject>) rootList;
                    } else if (rootData.getValue() instanceof DataObject[] rootArray) {
                        rootEntries = Arrays.asList(rootArray);
                    }
                    if (rootEntries != null) {
                        rootEntries.forEach(entry -> {
                            try {
                                DlmsChannelInfo info = new DlmsChannelInfo(entry);
                                infos.add(info);
                                logger.debug("Meter channel: {} added", info);
                            } catch (IllegalArgumentException e) {
                                logger.debug("Meter channel: {}, parse error: {}", entry, e.getMessage());
                            }
                        });
                    } else {
                        logger.debug("Root data error: {}", rootData);
                    }
                } else {
                    logger.debug("Root data error: {}", result);
                }
            } catch (IOException e) {
                logger.debug("Root data error: {}", e.getMessage());
                goOffline(e.getMessage());
            }
        }
        return infos;
    }

    /**
     * Update the state of the OH channels.
     */
    private void updateChannels() {
        DlmsConnection connection = this.connection;
        if (connection != null) {
            dlmsChannelInfos.forEach(info -> {
                try {
                    GetResult result = connection.get(info.getAttributeAddress());
                    if (result.getResultCode() == AccessResultCode.SUCCESS) {
                        Object rawValue = result.getResultData().getRawValue();
                        if (rawValue instanceof String value) {
                            try {
                                QuantityType<?> state = new DlmsQuantityType<>(value);
                                updateState(info.getChannelId(), state);
                                logger.trace("Meter channel: {}, data: {}, state: {}", info, value, state);
                            } catch (IllegalArgumentException e) {
                                logger.debug("Meter channel: {}, data: {}, format error: {}", info, result,
                                        e.getMessage());
                            }
                        } else {
                            logger.debug("Meter channel: {}, read unexpected data: {}", info, rawValue);
                        }
                    } else {
                        logger.debug("Meter channel: {}, read error: {}", info, result);
                    }
                } catch (IOException e) {
                    logger.debug("Meter channel: {}, read error: {}", info, e.getMessage());
                    goOffline(e.getMessage());
                    return;
                }
            });
        }
    }
}
