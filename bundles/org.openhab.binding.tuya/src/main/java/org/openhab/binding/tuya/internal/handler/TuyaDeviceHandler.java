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
package org.openhab.binding.tuya.internal.handler;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_IP;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.CONFIG_PROTOCOL;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.TuyaDynamicCommandDescriptionProvider;
import org.openhab.binding.tuya.internal.TuyaDynamicStateDescriptionProvider;
import org.openhab.binding.tuya.internal.local.DeviceInfoSubscriber;
import org.openhab.binding.tuya.internal.local.DeviceStatusListener;
import org.openhab.binding.tuya.internal.local.TuyaDevice;
import org.openhab.binding.tuya.internal.local.UdpDiscoveryListener;
import org.openhab.binding.tuya.internal.local.dto.DeviceInfo;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

import com.google.gson.Gson;

import io.netty.channel.EventLoopGroup;

/**
 * The {@link TuyaDeviceHandler} handles commands and state updates for a device that is reached directly over the
 * local network
 *
 * @author Jan N. Klug - Initial contribution
 * @author Maciej Jarzebowski - Extract common handling into BaseTuyaDeviceHandler
 */
@NonNullByDefault
public class TuyaDeviceHandler extends BaseTuyaDeviceHandler implements DeviceInfoSubscriber, DeviceStatusListener {
    private final Gson gson;
    private final UdpDiscoveryListener udpDiscoveryListener;
    private final EventLoopGroup eventLoopGroup;

    private @Nullable TuyaDevice tuyaDevice;
    private volatile boolean connected = false;

    public TuyaDeviceHandler(Thing thing, Gson gson,
            TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider,
            TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProvider, EventLoopGroup eventLoopGroup,
            UdpDiscoveryListener udpDiscoveryListener) {
        super(thing, gson, dynamicCommandDescriptionProvider, dynamicStateDescriptionProvider);
        this.gson = gson;
        this.udpDiscoveryListener = udpDiscoveryListener;
        this.eventLoopGroup = eventLoopGroup;
    }

    /**
     * @return whether the connection to the device is currently established
     */
    protected boolean isConnected() {
        return connected;
    }

    protected @Nullable TuyaDevice getTuyaDevice() {
        return tuyaDevice;
    }

    @Override
    protected boolean canCommunicate() {
        return tuyaDevice != null;
    }

    @Override
    protected void sendCommand(Map<Integer, @Nullable Object> command) {
        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice != null) {
            tuyaDevice.set(command);
        }
    }

    @Override
    protected void requestStatus() {
        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice != null) {
            tuyaDevice.requestStatus();
        }
    }

    @Override
    protected void refreshStatus() {
        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice != null) {
            tuyaDevice.refreshStatus();
        }
    }

    @Override
    public void processDeviceStatus(@Nullable String cid, Map<Integer, Object> deviceStatus) {
        if (cid != null) {
            logger.debug("{}: ignoring status for sub-device '{}', this thing is not a gateway", thing.getUID().getId(),
                    cid);
            return;
        }

        processDeviceStatus(deviceStatus);
    }

    @Override
    public void connectionStatus(boolean status, int initialDelay) {
        connected = status;

        if (status) {
            logger.debug("{}: connected", thing.getUID().getId());

            // Tuya devices are never offline (if they are battery devices they are expected
            // to be unreachable practically all the time) so really we're just clearing the
            // status message here rather than actually setting the Thing online.
            updateStatus(ThingStatus.ONLINE);

            onCommunicationEstablished(initialDelay);
        } else {
            logger.debug("{}: disconnected", thing.getUID().getId());

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.wait-for-device");

            onCommunicationLost();
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        connected = false;

        udpDiscoveryListener.unregisterListener(this);

        TuyaDevice tuyaDevice = this.tuyaDevice;
        if (tuyaDevice != null) {
            this.tuyaDevice = null;
            tuyaDevice.dispose();
        }
    }

    @Override
    protected void initializeTransport() {
        if (!configuration.ip.isBlank()) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.wait-for-device");

            this.tuyaDevice = new TuyaDevice(gson, this, eventLoopGroup, configuration.deviceId,
                    configuration.localKey.getBytes(StandardCharsets.UTF_8), configuration.ip, configuration.port,
                    configuration.protocol, getAllDpIds());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "@text/offline.wait-for-ip");
        }

        udpDiscoveryListener.registerListener(configuration.deviceId, this);
    }

    @Override
    public void deviceInfoChanged(DeviceInfo deviceInfo) {
        if (!configuration.ip.equals(deviceInfo.ip) || !configuration.protocol.equals(deviceInfo.protocolVersion)) {
            logger.info("Configuring IP address '{}' for thing '{}'.", deviceInfo, thing.getUID());

            TuyaDevice tuyaDevice = this.tuyaDevice;
            if (tuyaDevice != null) {
                this.tuyaDevice = null;
                tuyaDevice.dispose();
            }

            try {
                Configuration newConfig = editConfiguration();
                newConfig.put(CONFIG_IP, deviceInfo.ip);
                newConfig.put(CONFIG_PROTOCOL, deviceInfo.protocolVersion);
                updateConfiguration(newConfig);

                configuration.ip = deviceInfo.ip;
                configuration.protocol = deviceInfo.protocolVersion;

                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.wait-for-device");

                this.tuyaDevice = new TuyaDevice(gson, this, eventLoopGroup, configuration.deviceId,
                        configuration.localKey.getBytes(StandardCharsets.UTF_8), configuration.ip, configuration.port,
                        configuration.protocol, getAllDpIds());
            } catch (IllegalArgumentException e) {
                logger.warn("{}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }
}
