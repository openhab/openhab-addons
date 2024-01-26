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
package org.openhab.binding.qolsysiq.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qolsysiq.internal.QolsysIQBindingConstants;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneActiveEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.event.ZoneUpdateEvent;
import org.openhab.binding.qolsysiq.internal.client.dto.model.Zone;
import org.openhab.binding.qolsysiq.internal.client.dto.model.ZoneStatus;
import org.openhab.binding.qolsysiq.internal.config.QolsysIQZoneConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QolsysIQZoneHandler} manages security zones.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class QolsysIQZoneHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(QolsysIQZoneHandler.class);

    private int zoneId;

    public QolsysIQZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("initialize");
        zoneId = getConfigAs(QolsysIQZoneConfiguration.class).id;
        initializeZone();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusChanged) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusChanged);
        initializeZone();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public int getZoneId() {
        return zoneId;
    }

    protected void updateZone(Zone zone) {
        logger.debug("updateZone {}", zone.zoneId);
        updateState(QolsysIQBindingConstants.CHANNEL_ZONE_STATE, new DecimalType(zone.state));
        updateZoneStatus(zone.status);
        Map<String, String> props = new HashMap<>();
        props.put("type", zone.type);
        props.put("name", zone.name);
        props.put("group", zone.group);
        props.put("zoneID", zone.id);
        props.put("zonePhysicalType", String.valueOf(zone.zonePhysicalType));
        props.put("zoneAlarmType", String.valueOf(zone.zoneAlarmType));
        props.put("zoneType", zone.zoneType.toString());
        props.put("partitionId", String.valueOf(zone.partitionId));
        getThing().setProperties(props);
    }

    protected void zoneActiveEvent(ZoneActiveEvent event) {
        if (event.zone.zoneId == getZoneId()) {
            updateZoneStatus(event.zone.status);
        }
    }

    protected void zoneUpdateEvent(ZoneUpdateEvent event) {
        if (event.zone.zoneId == getZoneId()) {
            updateZone(event.zone);
        }
    }

    private void initializeZone() {
        Bridge bridge = getBridge();
        BridgeHandler handler = bridge == null ? null : bridge.getHandler();
        if (bridge != null && handler instanceof QolsysIQPartitionHandler partitionHandler) {
            if (handler.getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
            Zone z = partitionHandler.getZone(getZoneId());
            if (z == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Zone not found in partition");
                return;
            }
            updateZone(z);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    private void updateZoneStatus(@Nullable ZoneStatus status) {
        if (status != null) {
            updateState(QolsysIQBindingConstants.CHANNEL_ZONE_STATUS, new StringType(status.toString()));
            updateState(QolsysIQBindingConstants.CHANNEL_ZONE_CONTACT,
                    status == ZoneStatus.CLOSED || status == ZoneStatus.IDlE ? OpenClosedType.CLOSED
                            : OpenClosedType.OPEN);
        } else {
            logger.debug("updateZoneStatus: null status");
        }
    }
}
