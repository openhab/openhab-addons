/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.pulseaudio.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.handler.DeviceStatusListener;
import org.openhab.binding.pulseaudio.handler.PulseaudioBridgeHandler;
import org.openhab.binding.pulseaudio.handler.PulseaudioHandler;
import org.openhab.binding.pulseaudio.internal.items.AbstractAudioDeviceConfig;
import org.openhab.binding.pulseaudio.internal.items.Sink;
import org.openhab.binding.pulseaudio.internal.items.SinkInput;
import org.openhab.binding.pulseaudio.internal.items.Source;
import org.openhab.binding.pulseaudio.internal.items.SourceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PulseaudioDeviceDiscoveryService} class is used to discover Pulseaudio
 * devices on a Pulseaudio server.
 *
 * @author Tobias Bräutigam - Initial contribution
 */
public class PulseaudioDeviceDiscoveryService extends AbstractDiscoveryService implements DeviceStatusListener {

    private final Logger logger = LoggerFactory.getLogger(PulseaudioDeviceDiscoveryService.class);

    private PulseaudioBridgeHandler pulseaudioBridgeHandler;

    public PulseaudioDeviceDiscoveryService(PulseaudioBridgeHandler pulseaudioBridgeHandler) {
        super(PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS, 10, true);
        this.pulseaudioBridgeHandler = pulseaudioBridgeHandler;
    }

    public void activate() {
        pulseaudioBridgeHandler.registerDeviceStatusListener(this);
    }

    @Override
    public void deactivate() {
        pulseaudioBridgeHandler.unregisterDeviceStatusListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public void onDeviceAdded(Bridge bridge, AbstractAudioDeviceConfig device) {
        String uidName = device.getPaName();
        logger.debug("device {} found", device);
        ThingTypeUID thingType = null;
        Map<String, Object> properties = new HashMap<>();
        // All devices need this parameter
        properties.put(PulseaudioBindingConstants.DEVICE_PARAMETER_NAME, uidName);
        if (device instanceof Sink) {
            if (((Sink) device).isCombinedSink()) {
                thingType = PulseaudioBindingConstants.COMBINED_SINK_THING_TYPE;
            } else {
                thingType = PulseaudioBindingConstants.SINK_THING_TYPE;
            }
        } else if (device instanceof SinkInput) {
            thingType = PulseaudioBindingConstants.SINK_INPUT_THING_TYPE;
        } else if (device instanceof Source) {
            thingType = PulseaudioBindingConstants.SOURCE_THING_TYPE;
        } else if (device instanceof SourceOutput) {
            thingType = PulseaudioBindingConstants.SOURCE_OUTPUT_THING_TYPE;
        }

        if (thingType != null) {
            logger.trace("Adding new pulseaudio {} with name '{}' to smarthome inbox",
                    device.getClass().getSimpleName(), uidName);
            ThingUID thingUID = new ThingUID(thingType, bridge.getUID(), device.getUIDName());
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withBridge(bridge.getUID()).withLabel(device.getUIDName()).build();
            thingDiscovered(discoveryResult);
        }
    }

    @Override
    protected void startScan() {
        // this can be ignored here as we discover via the PulseaudioClient.update() mechanism
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, AbstractAudioDeviceConfig device) {
        // this can be ignored here
    }

    @Override
    public void onDeviceRemoved(PulseaudioBridgeHandler bridge, AbstractAudioDeviceConfig device) {
        // this can be ignored here
    }
}
