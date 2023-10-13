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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.kermi.internal.KermiBaseDeviceConfiguration;
import org.openhab.binding.kermi.internal.KermiBindingConstants;
import org.openhab.binding.kermi.internal.KermiCommunicationException;
import org.openhab.binding.kermi.internal.api.Config;
import org.openhab.binding.kermi.internal.api.Datapoint;
import org.openhab.binding.kermi.internal.api.DeviceInfo;
import org.openhab.binding.kermi.internal.api.KermiHttpUtil;
import org.openhab.binding.kermi.internal.model.KermiSiteInfo;
import org.openhab.binding.kermi.internal.model.KermiSiteInfoUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
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
    private final KermiBaseThingHandlerUtil kermiBaseThingHandlerUtil;
    private String busAddress;
    private String deviceId;

    public KermiBaseThingHandler(Thing thing, KermiHttpUtil httpUtil, KermiSiteInfo kermiSiteInfo) {
        super(thing);
        this.httpUtil = httpUtil;
        this.kermiSiteInfo = kermiSiteInfo;
        this.kermiBaseThingHandlerUtil = new KermiBaseThingHandlerUtil();
    }

    public KermiHttpUtil getHttpUtil() {
        return httpUtil;
    }

    public KermiSiteInfo getKermiSiteInfo() {
        return kermiSiteInfo;
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        kermiSiteInfo.putRefreshBinding(channelUID.getId(), deviceId);
        logger.trace("Thing {} linked channel {}", getThing().getUID(), channelUID);
        super.channelLinked(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        kermiSiteInfo.removeRefreshBinding(channelUID.getId(), deviceId);
        logger.trace("Thing {} unlinked channel {}", getThing().getUID(), channelUID);
        super.channelUnlinked(channelUID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannel(channelUID.getId());
        }
    }

    @Override
    public void initialize() {

        if (KermiBindingConstants.THING_TYPE_HEATPUMP_MANAGER.equals(getThing().getThingTypeUID())) {
            deviceId = KermiBindingConstants.DEVICE_ID_HEATPUMP_MANAGER;
            logger.debug("Initializing heatpump-manager with deviceId {}", deviceId);
        } else {
            KermiBaseDeviceConfiguration config = getConfigAs(KermiBaseDeviceConfiguration.class);
            busAddress = config.address.toString();
            deviceId = kermiSiteInfo.getDeviceInfoByAddress(busAddress).getDeviceId();
            logger.debug("Initializing busAddress {} with deviceId {}", busAddress, deviceId);
        }

        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        } else if (bridge.getStatus() == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }

        // determine channels for thing
        // get device info by address
        try {
            ThingBuilder thingBuilder = editThing();
            DeviceInfo deviceInfo = kermiSiteInfo.getDeviceInfoByAddress(busAddress);
            thingBuilder.withLabel(deviceInfo.getName());

            List<Datapoint> deviceDatapoints = KermiSiteInfoUtil.collectDeviceDatapoints(httpUtil, deviceInfo);
            deviceDatapoints.forEach(datapoint -> addDatapointAsChannel(getThing().getUID(), datapoint, thingBuilder));
            updateThing(thingBuilder.build());
        } catch (KermiCommunicationException e) {
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            logger.warn("Communication exception", e);
        }

        getThing().getChannels().forEach(channel -> {
            if (isLinked(channel.getUID())) {
                channelLinked(channel.getUID());
            }
        });
    }

    private void addDatapointAsChannel(@NonNull ThingUID thingUID, Datapoint datapoint, ThingBuilder thingBuilder) {
        Config datapointConfig = datapoint.getConfig();
        ChannelTypeUID channelTypeUID = kermiBaseThingHandlerUtil.determineChannelTypeUID(datapointConfig);
        if (channelTypeUID != null) {
            if (StringUtils.isNotBlank(datapointConfig.getWellKnownName())) {
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), datapointConfig.getWellKnownName());

                Channel channel = ChannelBuilder.create(channelUID).withType(channelTypeUID).withLabel(busAddress)
                        .withLabel(datapointConfig.getDisplayName()).withDescription(datapointConfig.getDescription())
                        .build();
                thingBuilder.withChannel(channel);
                logger.debug("{} added channel {}", thingUID, datapointConfig.getWellKnownName());
            }
        } else {
            logger.info("{} unsupported channel-type for datapointConfigId {}", thingUID,
                    datapointConfig.getDatapointConfigId());
        }
    }

    public String getBusAddress() {
        return busAddress;
    }

    /**
     * Update linked channels
     */
    protected void updateChannels() {
        for (Channel channel : getThing().getChannels()) {
            if (isLinked(channel.getUID())) {
                updateChannel(channel.getUID().getId());
            }
        }
    }

    public void updateProperties(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return;
        }

        Map<@NonNull String, @NonNull String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceInfo.getSerial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.getSoftwareVersion());
        properties.put("DeviceType", deviceInfo.getDeviceType());
        properties.put("DeviceId", deviceInfo.getDeviceId());
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

    protected State getValue(String channelId) {
        final String[] fields = channelId.split("#");
        if (fields.length < 1) {
            return null;
        }

        final String fieldName = fields[0];
        return getKermiSiteInfo().getStateByWellKnownName(fieldName, deviceId);
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
        DeviceInfo deviceInfo = getKermiSiteInfo().getDeviceInfoByAddress(getBusAddress());
        if (deviceInfo != null) {
            updateProperties(deviceInfo);
        } else {
            throw new KermiCommunicationException("Not yet initialized");
        }

        updateChannels();
    };
}
