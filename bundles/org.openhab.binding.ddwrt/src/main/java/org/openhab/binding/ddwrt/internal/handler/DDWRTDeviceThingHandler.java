/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.handler;

import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_CPU_LOAD;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_CPU_TEMP;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_FIRMWARE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_IF_IN;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_IF_OUT;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_ONLINE;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_REBOOT;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_UPTIME;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_WAN_IN;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_WAN_IP;
import static org.openhab.binding.ddwrt.internal.DDWRTBindingConstants.CHANNEL_WAN_OUT;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ddwrt.internal.DDWRTDeviceConfiguration;
import org.openhab.binding.ddwrt.internal.api.DDWRTBaseDevice;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetwork;
import org.openhab.binding.ddwrt.internal.api.DDWRTNetworkCache;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DDWRTDeviceThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class DDWRTDeviceThingHandler extends DDWRTBaseHandler<DDWRTBaseDevice, DDWRTDeviceConfiguration> {

    private final Logger logger = LoggerFactory.getLogger(DDWRTDeviceThingHandler.class);

    private DDWRTDeviceConfiguration config = new DDWRTDeviceConfiguration();

    public DDWRTDeviceThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected boolean initialize(DDWRTDeviceConfiguration config) {
        this.config = config;
        if (isBlank(config.hostname)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-hostname");
            return false;
        }

        // Try to find or create the device in the network
        DDWRTNetwork net = getNetwork();
        if (net == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/offline.bridge-not-ready");
            return false;
        }

        DDWRTBaseDevice d = findDevice(net);
        if (d == null) {
            logger.debug("Device not found in network, attempting to add hostname: {}", config.hostname);
            d = net.addOrUpdateDevice(config);
            if (d == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-connect");
                return false;
            }
            logger.debug("Successfully added device to network: {} (MAC: {})", config.hostname, d.getMac());
        }

        d.setUpdater(this);
        updateProperties(d);
        return true;
    }

    @Override
    public void dispose() {
        DDWRTBaseDevice d = getEntity();
        if (d != null) {
            d.setUpdater(null);
        }
        super.dispose();
    }

    @Override
    protected @Nullable DDWRTBaseDevice getEntity(DDWRTNetworkCache cache) {
        // Try MAC first
        final @Nullable String mac = getThing().getProperties().get("mac");
        if (mac != null && !isBlank(mac)) {
            DDWRTBaseDevice d = cache.getDevice(mac);
            if (d != null) {
                return d;
            }
        }
        // Fallback by hostname
        for (DDWRTBaseDevice d : cache.getDevices()) {
            if (config.hostname.equalsIgnoreCase(d.getConfig().hostname)) {
                return d;
            }
        }
        return null;
    }

    @Override
    protected State getChannelState(DDWRTBaseDevice device, String channelId) {
        return switch (channelId) {
            case CHANNEL_ONLINE -> OnOffType.from(device.isOnline());
            case CHANNEL_UPTIME -> device.getUptimeSince();
            case CHANNEL_CPU_LOAD -> new DecimalType(device.getCpuLoad());
            case CHANNEL_CPU_TEMP -> new QuantityType<Temperature>(device.getCpuTemp(), SIUnits.CELSIUS);
            case CHANNEL_FIRMWARE -> StringType.valueOf(device.getFirmware());
            case CHANNEL_WAN_IP -> device.isGateway() ? StringType.valueOf(device.getWanIp()) : UnDefType.UNDEF;
            case CHANNEL_WAN_IN -> device.isGateway() ? new DecimalType(device.getWanIn()) : UnDefType.UNDEF;
            case CHANNEL_WAN_OUT -> device.isGateway() ? new DecimalType(device.getWanOut()) : UnDefType.UNDEF;
            case CHANNEL_IF_IN -> new DecimalType(device.getIfIn());
            case CHANNEL_IF_OUT -> new DecimalType(device.getIfOut());
            case CHANNEL_REBOOT -> OnOffType.OFF;
            default -> UnDefType.NULL;
        };
    }

    @Override
    protected boolean handleCommand(DDWRTNetwork network, DDWRTBaseDevice device, ChannelUID channelUID,
            Command command) {
        String channelId = channelUID.getIdWithoutGroup();
        if (CHANNEL_REBOOT.equals(channelId) && OnOffType.ON.equals(command)) {
            logger.debug("Rebooting device: {}", device.getMac());
            device.reboot();
            updateState(channelUID, OnOffType.OFF);
            return true;
        }
        return false;
    }

    @Override
    protected void updateProperties(@Nullable DDWRTBaseDevice device) {
        if (device != null) {
            updateProperty("mac", device.getMac());
            updateProperty("model", device.getModel());
            updateProperty("chipset", device.getChipset());
        }
    }

    private @Nullable DDWRTBaseDevice findDevice(DDWRTNetwork net) {
        final @Nullable String mac = getThing().getProperties().get("mac");
        if (mac != null && !isBlank(mac)) {
            DDWRTBaseDevice d = net.getDeviceByMac(mac);
            if (d != null) {
                return d;
            }
        }
        return net.getDeviceByHostname(config.hostname);
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.isBlank();
    }
}
