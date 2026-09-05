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

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.TCP_CONNECT_INITIAL_DELAY;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.TuyaDynamicCommandDescriptionProvider;
import org.openhab.binding.tuya.internal.TuyaDynamicStateDescriptionProvider;
import org.openhab.binding.tuya.internal.local.TuyaDevice;
import org.openhab.binding.tuya.internal.local.UdpDiscoveryListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

import com.google.gson.Gson;

import io.netty.channel.EventLoopGroup;

/**
 * The {@link TuyaGatewayHandler} handles a Tuya device that also relays for sub-devices, e.g. a Bluetooth or ZigBee
 * gateway. It behaves like any other local device for its own data points and additionally routes messages tagged with
 * a sub-device node id to and from the {@link TuyaSubDeviceHandler}s of its child things.
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@NonNullByDefault
public class TuyaGatewayHandler extends TuyaDeviceHandler implements BridgeHandler {
    private final Map<String, TuyaSubDeviceHandler> subDeviceHandlers = new ConcurrentHashMap<>();

    public TuyaGatewayHandler(Bridge bridge, Gson gson,
            TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider,
            TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProvider, EventLoopGroup eventLoopGroup,
            UdpDiscoveryListener udpDiscoveryListener) {
        super(bridge, gson, dynamicCommandDescriptionProvider, dynamicStateDescriptionProvider, eventLoopGroup,
                udpDiscoveryListener);
    }

    /**
     * Mirrors {@code BaseBridgeHandler#editThing()}, which this handler cannot inherit because it extends
     * {@link TuyaDeviceHandler} to share the device implementation. {@code ThingBuilder#build()} produces a plain
     * thing, so building on it would silently turn this bridge into a non-bridge and orphan its sub-devices.
     */
    @Override
    protected BridgeBuilder editThing() {
        return BridgeBuilder.create(thing.getThingTypeUID(), thing.getUID()).withBridge(thing.getBridgeUID())
                .withChannels(thing.getChannels()).withConfiguration(thing.getConfiguration())
                .withLabel(thing.getLabel()).withLocation(thing.getLocation()).withProperties(thing.getProperties())
                .withSemanticEquipmentTag(thing.getSemanticEquipmentTag());
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (!(childHandler instanceof TuyaSubDeviceHandler subDeviceHandler)) {
            logger.debug("{}: unsupported child handler {} for thing '{}'", thing.getUID().getId(),
                    childHandler.getClass().getSimpleName(), childThing.getUID());
            return;
        }

        String subDeviceId = subDeviceHandler.getSubDeviceId();
        if (subDeviceId.isEmpty()) {
            // The sub-device handler has already reported the missing configuration.
            return;
        }

        subDeviceHandlers.put(subDeviceId, subDeviceHandler);

        // Registration has to happen before the sub-device queries its state, otherwise the response could not be
        // routed back to it.
        subDeviceHandler.gatewayConnectionStatus(isConnected(), TCP_CONNECT_INITIAL_DELAY);
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof TuyaSubDeviceHandler subDeviceHandler) {
            subDeviceHandlers.remove(subDeviceHandler.getSubDeviceId(), subDeviceHandler);
        }
    }

    @Override
    public void processDeviceStatus(@Nullable String cid, Map<Integer, Object> deviceStatus) {
        if (cid == null || cid.equals(configuration.deviceId)) {
            processDeviceStatus(deviceStatus);
            return;
        }

        TuyaSubDeviceHandler subDeviceHandler = subDeviceHandlers.get(cid);
        if (subDeviceHandler == null) {
            logger.debug("{}: received status for unknown sub-device '{}'", thing.getUID().getId(), cid);
            return;
        }

        subDeviceHandler.processDeviceStatus(deviceStatus);
    }

    @Override
    public void connectionStatus(boolean status, int initialDelay) {
        super.connectionStatus(status, initialDelay);

        subDeviceHandlers.values().forEach(handler -> handler.gatewayConnectionStatus(status, initialDelay));
    }

    @Override
    public void dispose() {
        subDeviceHandlers.clear();

        super.dispose();
    }

    /**
     * Sends data points to a sub-device connected through this gateway.
     */
    void sendCommand(String subDeviceId, Map<Integer, @Nullable Object> command) {
        TuyaDevice tuyaDevice = getTuyaDevice();
        if (tuyaDevice != null) {
            tuyaDevice.set(subDeviceId, command);
        }
    }

    /**
     * Queries the status of a sub-device connected through this gateway.
     */
    void requestStatus(String subDeviceId) {
        TuyaDevice tuyaDevice = getTuyaDevice();
        if (tuyaDevice != null) {
            tuyaDevice.requestStatus(subDeviceId);
        }
    }
}
