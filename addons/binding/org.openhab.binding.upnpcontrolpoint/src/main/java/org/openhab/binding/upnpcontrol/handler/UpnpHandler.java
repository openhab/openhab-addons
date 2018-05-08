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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.RefreshType;
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
    protected Set<AudioFormat> supportedFormats;
    private String transportState;
    protected int instanceId;

    public UpnpHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing);

        this.supportedFormats = new HashSet<AudioFormat>();
        this.transportState = "";
        this.instanceId = 0;

        upnpIOService.registerParticipant(this);
        this.service = upnpIOService;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for upnp media device");
        for (Channel channel : getThing().getChannels()) {
            handleCommand(channel.getUID(), RefreshType.REFRESH);
        }
        updateStatus(ThingStatus.ONLINE);
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
        logger.debug("Received subscription reply {} from service {}", succeeded, service);
    }

    @Override
    public void onStatusChanged(boolean status) {
    }

    @Override
    public String getUDN() {
        return getThing().getProperties().get("udn");
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Received variable {} with value {} from service {}", variable, value, service);
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

    public String getProtocolInfo() {
        return "";
    }

    public Set<AudioFormat> getSupportedAudioFormats() {
        return supportedFormats;
    }
}
