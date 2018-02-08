/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.internal.LightState;
import org.openhab.binding.openwebnet.internal.OpenWebNetGateway;
import org.openhab.binding.openwebnet.internal.listener.BridgeStatusListener;
import org.openhab.binding.openwebnet.internal.listener.ScanListener;
import org.openhab.binding.openwebnet.internal.listener.ThingStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Antoine Laydier - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetBridgeHandler extends BaseBridgeHandler {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(OpenWebNetBridgeHandler.class);

    private OpenWebNetGateway bridge;

    private List<BridgeStatusListener> listeners = new CopyOnWriteArrayList<BridgeStatusListener>();

    // needed for Maven
    @SuppressWarnings("null")
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(OpenWebNetBindingConstants.THING_TYPE_BRIDGE);

    public OpenWebNetBridgeHandler(Bridge bridge) {
        super(bridge);
        this.bridge = new OpenWebNetGateway();
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        logger.warn("{} --> Unexpected command {} received on channel={}", this, command, channelUID);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing OpenWebNet bridge handler.");
        String serialPortName = (String) getConfig().get(OpenWebNetBindingConstants.SERIAL_PORT);
        if (serialPortName != null) {
            if (bridge.defineAsZigbee(serialPortName)) {
                logger.debug("{} connected.", bridge);
                Map<String, String> properties = editProperties();
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION, bridge.getFirmwareVersion());
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, bridge.getHardwareVersion());
                updateProperties(properties);
                updateStatus(ThingStatus.ONLINE);
            } else {
                logger.warn("{} not available.", bridge);
                updateStatus(ThingStatus.OFFLINE);
            }
        } else {
            logger.warn("OpenWebNet Bridge port not defined ({}).", serialPortName);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-port");
        }
    }

    /**
     * Scan the network for new Thing.
     *
     * @param listener listener to call
     */
    public void scanNetwork(ScanListener listener) {
        bridge.scanNetwork(listener);
    }

    public void addThingStatusListener(int where, ThingStatusListener listener) {
        // forward request to Gateway
        bridge.addThingStatusListener(where, listener);
    }

    public void removeThingStatusListener(int where) {
        // forward request to Gateway
        bridge.removeThingStatusListener(where);
    }

    @Override
    public void dispose() {
        listeners.clear();
        try {
            bridge.close();
        } catch (Exception e) {
            // nothing can be done
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String comment) {
        super.updateStatus(status, detail, comment);
        logger.debug("Updating listeners with status {}", status);
        for (BridgeStatusListener listener : listeners) {
            listener.onBridgeStatusChange(status);
        }
    }

    public void addBridgeStatusListener(BridgeStatusListener listener) {
        listeners.add(listener);
        listener.onBridgeStatusChange(getThing().getStatus());
    }

    /**
     * Removes a HubConnectedListener
     *
     * @param listener
     */
    public void removeBridgeStatusListener(BridgeStatusListener listener) {
        listeners.remove(listener);
    }

    public void setLight(int where, Command cmd, boolean waitCompletion) {
        bridge.setLight(where, LightState.toOpenWebNet(cmd), waitCompletion, 0);
    }

    public void getLight(int where, boolean waitCompletion) {
        bridge.setLight(where, LightState.REFRESH, waitCompletion, 750);
    }

    public void setAutomation(int where, int what, boolean waitCompletion) {
        bridge.setAutomation(where, what, waitCompletion);
    }

    public void getAutomation(int where, boolean waitCompletion) {
        bridge.getAutomation(where, waitCompletion);
    }

    public void setPositionAutomation(int where, int percent, boolean waitCompletion) {
        bridge.setPositionAutomation(where, percent, waitCompletion);
    }
}
