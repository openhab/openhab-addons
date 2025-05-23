/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal.handler;

import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;
import static org.openhab.binding.wemo.internal.WemoUtil.substringBefore;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wemo.internal.ApiController;
import org.openhab.binding.wemo.internal.discovery.WemoLinkDiscoveryService;
import org.openhab.binding.wemo.internal.exception.WemoException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WemoBridgeHandler} is the handler for a wemo bridge and connects it to
 * the framework.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class WemoBridgeHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private final Logger logger = LoggerFactory.getLogger(WemoBridgeHandler.class);
    private final UpnpIOService service;
    private final ApiController apiController;

    public WemoBridgeHandler(final Bridge bridge, final UpnpIOService upnpIOService, final HttpClient httpClient) {
        super(bridge);
        this.service = upnpIOService;
        this.apiController = new ApiController(httpClient);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing WemoBridgeHandler");

        Configuration configuration = getConfig();

        if (configuration.get(UDN) != null) {
            logger.trace("Initializing WemoBridgeHandler for UDN '{}'", configuration.get(UDN));
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Cannot initalize WemoBridgeHandler. UDN not set.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/config-status.error.missing-udn");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Not needed, all commands are handled in the {@link WemoLightHandler}
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(WemoLinkDiscoveryService.class);
    }

    public String getEndDevices(UpnpIOParticipant participant) throws InterruptedException, WemoException {
        String devUDN = "uuid:" + getConfig().get(UDN).toString();
        logger.trace("getEndDevices for devUDN '{}'", devUDN);

        String soapHeader = "\"urn:Belkin:service:bridge:1#GetEndDevices\"";
        String content = """
                <?xml version="1.0"?>\
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">\
                <s:Body>\
                <u:GetEndDevices xmlns:u="urn:Belkin:service:bridge:1">\
                <DevUDN>\
                """
                + devUDN + """
                        </DevUDN>\
                        <ReqListType>PAIRED_LIST</ReqListType>\
                        </u:GetEndDevices>\
                        </s:Body>\
                        </s:Envelope>\
                        """;

        URL descriptorURL = service.getDescriptorURL(participant);
        if (descriptorURL == null) {
            throw new WemoException("Descriptor URL for participant " + participant.getUDN() + " not found");
        }

        String deviceURL = substringBefore(descriptorURL.toString(), "/setup.xml");
        String wemoURL = deviceURL + "/upnp/control/bridge1";

        String endDeviceResponse = apiController.executeCall(wemoURL, soapHeader, content);

        logger.trace("endDeviceRequest answered '{}'", endDeviceResponse);

        return endDeviceResponse;
    }
}
