/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpHandler} is the base class for {@link UpnpRendererHandler} and {@link UpnpServerHandler}.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public abstract class UpnpHandler extends BaseThingHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected UpnpIOService service;
    protected Set<AudioFormat> supportedFormats = new HashSet<AudioFormat>();
    private String transportState = "";
    protected int instanceId = 0;

    public UpnpHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing);

        upnpIOService.registerParticipant(this);
        this.service = upnpIOService;
    }

    @Override
    public void initialize() {
        if (service.isRegistered(this)) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not initialize communication with " + thing.getLabel());
        }
    }

    @Override
    public void dispose() {
        service.unregisterParticipant(this);
    }

    protected String getTransportState() throws IOException {
        HashMap<String, String> inputs = new HashMap<String, String>();
        inputs.put("InstanceID", Integer.toString(instanceId));
        Map<String, String> result = service.invokeAction(this, "AVTransport", "GetTransportInfo", inputs);
        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "AVTransport");
        }
        return transportState;
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        logger.debug("Upnp device {) received subscription reply {} from service {}", thing.getLabel(), succeeded,
                service);
    }

    @Override
    public void onStatusChanged(boolean status) {
        if (status) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication lost with " + thing.getLabel());
        }
    }

    @Override
    public String getUDN() {
        return getThing().getProperties().get("udn");
    }

    protected Map<String, String> invokeAction(String serviceId, String actionId, Map<String, String> inputs) {
        logger.debug("Upnp device {} invoke upnp action {} on service {} with inputs {}", thing.getLabel(), actionId,
                serviceId, inputs);
        return service.invokeAction(this, serviceId, actionId, inputs);
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (variable == null) {
            return;
        }
        switch (variable) {
            case "CurrentTransportState":
                if (!((value == null) || (value.isEmpty()))) {
                    transportState = value;
                }
                break;
            default:
                break;
        }
    }

    protected void addSubscription(String serviceId, int duration) {
        logger.debug("Upnp device {} add upnp subscription on {}", thing.getLabel(), serviceId);
        service.addSubscription(this, serviceId, duration);
    }

    protected abstract String getProtocolInfo();

    public Set<AudioFormat> getSupportedAudioFormats() {
        return supportedFormats;
    }
}
