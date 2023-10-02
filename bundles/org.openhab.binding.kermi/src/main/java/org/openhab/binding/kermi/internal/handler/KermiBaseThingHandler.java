/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.kermi.internal.handler;

import java.util.Map;

import org.openhab.binding.kermi.internal.KermiBaseDeviceConfiguration;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.binding.kermi.internal.api.DeviceInfo;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.binding.kermi.internal.model.KermiSiteInfo;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco Descher - intial implementation
 */
public class KermiBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(KermiBaseThingHandler.class);
    private final KermiHttpUtil httpUtil;
    private final KermiSiteInfo kermiSiteInfo;
    private String busAddress;
    private String deviceId;

    public KermiBaseThingHandler(Thing thing, KermiHttpUtil httpUtil, KermiSiteInfo kermiSiteInfo) {
        super(thing);
        this.httpUtil = httpUtil;
        this.kermiSiteInfo = kermiSiteInfo;
    }

    public KermiHttpUtil getHttpUtil() {
        return httpUtil;
    }

    public KermiSiteInfo getKermiSiteInfo() {
        return kermiSiteInfo;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.info("channelLinked {}", channelUID);
        super.channelLinked(channelUID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        KermiBaseDeviceConfiguration config = getConfigAs(KermiBaseDeviceConfiguration.class);
        if (config.address != null) {
            // null for heatpump-manager
            busAddress = config.address.toString();
        }
        logger.debug("Initializing busAddress {}", busAddress);
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public String getBusAddress() {
        return busAddress;
    }

    /**
     * Update all Channels
     */
    protected void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            updateChannel(channel.getUID().getId());
        }
    }

    public void updateProperties(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return;
        }

        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceInfo.getSerial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getSoftwareVersion());
        updateProperties(properties);
    }

    /**
     * Update the channel from the last data
     *
     * @param channelId the id identifying the channel to be updated
     */
    protected void updateChannel(String channelId) {
        if (!isLinked(channelId)) {
            return;
        }

        State state = getValue(channelId);
        if (state == null) {
            state = UnDefType.NULL;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Update channel {} with state {} ({})", channelId, state.toString(),
                    state.getClass().getSimpleName());
        }
        updateState(channelId, state);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    protected State getValue(String channelId) {
        if (getDeviceId() == null) {
            return null;
        }

        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }

        final String fieldName = fields[0];
        return getKermiSiteInfo().getStateByWellKnownName(fieldName, getDeviceId());
    }

    /**
     * Called by the bridge to fetch data and update channels
     *
     * @param bridgeConfiguration the connected bridge configuration
     */
    public void refresh() {
        try {
            handleRefresh();
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (KermiCommunicationException | RuntimeException e) {
            logger.debug("Exception caught in refresh() for {}", getThing().getUID().getId(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected void handleRefresh() throws KermiCommunicationException {
        if (getDeviceId() == null) {
            DeviceInfo deviceInfo = getKermiSiteInfo().getDeviceInfoByAddress(getBusAddress());
            if (deviceInfo != null) {
                setDeviceId(deviceInfo.getDeviceId());
                updateProperties(deviceInfo);
            } else {
                throw new KermiCommunicationException("Not yet initialized");
            }
        }

        updateChannels();
    };
}
