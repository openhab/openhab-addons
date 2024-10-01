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
package org.openhab.binding.onkyo.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.onkyo.internal.OnkyoBindingConstants;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OnkyoUpnpHandler} is a base class for ThingHandlers for devices which support UPnP playback.
 *
 * @author Paul Frank - Initial contribution
 * @author Laurent Garnier - Separated into OnkyoUpnpHandler and OnkyoAudioSink
 */
public abstract class OnkyoUpnpHandler extends BaseThingHandler implements UpnpIOParticipant {

    private final Logger logger = LoggerFactory.getLogger(OnkyoUpnpHandler.class);

    private UpnpIOService service;

    public OnkyoUpnpHandler(Thing thing, UpnpIOService upnpIOService) {
        super(thing);
        this.service = upnpIOService;
    }

    protected void handlePlayUri(Command command) {
        if (command instanceof StringType) {
            try {
                playMedia(command.toString());

            } catch (IllegalStateException e) {
                logger.warn("Cannot play URI ({})", e.getMessage());
            }
        }
    }

    public void playMedia(String url) {
        stop();
        removeAllTracksFromQueue();

        if (!url.startsWith("x-") && (!url.startsWith("http"))) {
            url = "x-file-cifs:" + url;
        }

        setCurrentURI(url, "");

        play();
    }

    public void stop() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Stop", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    private void play() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");
        inputs.put("Speed", "1");
        Map<String, String> result = service.invokeAction(this, "AVTransport", "Play", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    private void removeAllTracksFromQueue() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "RemoveAllTracksFromQueue", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    private void setCurrentURI(String uri, String uriMetaData) {
        if (uri != null && uriMetaData != null) {
            Map<String, String> inputs = new HashMap<>();

            try {
                inputs.put("InstanceID", "0");
                inputs.put("CurrentURI", uri);
                inputs.put("CurrentURIMetaData", uriMetaData);
            } catch (NumberFormatException ex) {
                logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            Map<String, String> result = service.invokeAction(this, "AVTransport", "SetAVTransportURI", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        }
    }

    @Override
    public String getUDN() {
        return (String) this.getConfig().get(OnkyoBindingConstants.UDN_PARAMETER);
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        logger.debug("received variable {} with value {} from service {}", variable, value, service);
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {
    }

    @Override
    public void onStatusChanged(boolean status) {
    }
}
