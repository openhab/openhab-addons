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
package org.openhab.binding.magentatv.internal;

import static org.openhab.binding.magentatv.internal.MagentaTVBindingConstants.*;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.magentatv.internal.MagentaTVDeviceManager.MagentaTVDevice;
import org.openhab.binding.magentatv.internal.handler.MagentaTVHandler;
import org.openhab.binding.magentatv.internal.network.MagentaTVNetwork;
import org.openhab.binding.magentatv.internal.network.MagentaTVPoweroffListener;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MagentaTVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, MagentaTVHandlerFactory.class }, configurationPid = "binding."
        + BINDING_ID)
public class MagentaTVHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(MagentaTVHandlerFactory.class);

    private final MagentaTVNetwork network = new MagentaTVNetwork();
    private final MagentaTVDeviceManager manager;
    private final HttpClient httpClient;
    private @Nullable MagentaTVPoweroffListener upnpListener;
    private boolean servletInitialized = false;

    /**
     * Activate the bundle: save properties
     *
     * @param componentContext
     * @param configProperties set of properties from cfg (use same names as in
     *            thing config)
     */

    @Activate
    public MagentaTVHandlerFactory(@Reference NetworkAddressService networkAddressService,
            @Reference HttpClientFactory httpClientFactory, @Reference MagentaTVDeviceManager manager,
            ComponentContext componentContext, Map<String, String> configProperties) throws IOException {
        super.activate(componentContext);
        this.manager = manager;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        try {
            logger.debug("Initialize network access");
            System.setProperty("java.net.preferIPv4Stack", "true");
            String lip = networkAddressService.getPrimaryIpv4HostAddress();
            Integer port = HttpServiceUtil.getHttpServicePort(componentContext.getBundleContext());
            if (port == -1) {
                port = 8080;
            }
            network.initLocalNet(lip != null ? lip : "", port.toString());
            upnpListener = new MagentaTVPoweroffListener(this, network.getLocalInterface());
        } catch (MagentaTVException e) {
            logger.warn("Initialization failed: {}", e.toString());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (upnpListener != null) {
            upnpListener.start();
        }

        logger.debug("Create thing type {}", thing.getThingTypeUID().getAsString());
        if (THING_TYPE_RECEIVER.equals(thingTypeUID)) {
            return new MagentaTVHandler(manager, thing, network, httpClient);
        }

        return null;
    }

    public void setNotifyServletStatus(boolean newStatus) {
        logger.debug("NotifyServlet started");
        servletInitialized = newStatus;
    }

    public boolean getNotifyServletStatus() {
        return servletInitialized;
    }

    /**
     * We received the pairing result (by the Notify servlet)
     *
     * @param notifyDeviceId The unique device id pairing was initiated for
     * @param pairingCode Pairing code computed by the receiver
     * @return true: thing handler was called, false: failed, e.g. unknown device
     */
    public boolean notifyPairingResult(String notifyDeviceId, String ipAddress, String pairingCode) {
        try {
            logger.trace("PairingResult: Check {} devices for id {}, ipAddress {}", manager.numberOfDevices(),
                    notifyDeviceId, ipAddress);
            MagentaTVDevice dev = manager.lookupDevice(ipAddress);
            if ((dev != null) && (dev.thingHandler != null)) {
                if (dev.deviceId.isEmpty()) {
                    logger.trace("deviceId {} assigned for ipAddress {}", notifyDeviceId, ipAddress);
                    dev.deviceId = notifyDeviceId;
                }
                if (dev.thingHandler != null) {
                    dev.thingHandler.onPairingResult(pairingCode);
                }
                return true;
            }

            logger.debug("Received pairingCode {} for unregistered device {}!", pairingCode, ipAddress);
        } catch (MagentaTVException e) {
            logger.debug("Unable to process pairing result for deviceID {}: {}", notifyDeviceId, e.toString());
        }
        return false;
    }

    /**
     * A programInfo or playStatus event was received from the receiver
     *
     * @param mrMac MR MAC address (used to map the device)
     * @param jsonEvent Event data in JSON format
     * @return true: thing handler was called, false: failed, e.g. unknown device
     */
    public boolean notifyMREvent(String mrMac, String jsonEvent) {
        try {
            logger.trace("Received MR event from MAC {}, JSON={}", mrMac, jsonEvent);
            MagentaTVDevice dev = manager.lookupDevice(mrMac);
            if ((dev != null) && (dev.thingHandler != null)) {
                dev.thingHandler.onMREvent(jsonEvent);
                return true;
            }
            logger.debug("Received event for unregistered MR: MAC address {}, JSON={}", mrMac, jsonEvent);
        } catch (RuntimeException e) {
            logger.debug("Unable to process MR event! {} ({}), json={}", e.getMessage(), e.getClass(), jsonEvent);
        }
        return false;
    }

    /**
     * The PowerOff Listener got a byebye message. This comes in when the receiver
     * was is going to suspend mode.
     *
     * @param ipAddress receiver IP
     */
    public void onPowerOff(String ipAddress) {
        try {
            logger.debug("ByeBye message received for IP {}", ipAddress);
            MagentaTVDevice dev = manager.lookupDevice(ipAddress);
            if ((dev != null) && (dev.thingHandler != null)) {
                dev.thingHandler.onPowerOff();
            }
        } catch (MagentaTVException e) {
            logger.debug("Unable to process SSDP message for IP {} - {}", ipAddress, e.toString());
        }
    }
}
