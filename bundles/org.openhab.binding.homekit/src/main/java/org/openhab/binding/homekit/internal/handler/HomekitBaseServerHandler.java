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
package org.openhab.binding.homekit.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.homekit.internal.PairingManager;
import org.openhab.binding.homekit.internal.SecureClient;
import org.openhab.binding.homekit.internal.SecureSession;
import org.openhab.binding.homekit.internal.SessionKeys;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles I/O with HomeKit server devices -- either simple accessories or bridge accessories that
 * contain child accessories. If the handler is for a HomeKit bridge or a stand alone HomeKit accessory
 * device it performs the pairing and secure session setup. If the handler is for a HomeKit accessory
 * that is part of a bridge, it uses the pairing and session from the bridge handler.
 * Subclasses should override the handleCommand method to handle commands for specific channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class HomekitBaseServerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HomekitBaseServerHandler.class);

    protected final HttpClient httpClient;

    protected @Nullable SecureClient client;
    protected @Nullable SessionKeys keys;
    protected @Nullable SecureSession session;
    protected @Nullable String baseUrl;
    protected @Nullable String setupCode;

    public HomekitBaseServerHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof HomekitBridgeHandler bridgeHandler) {
            // accessory is part of a bridge, so use the bridge's pairing and session
            this.keys = bridgeHandler.keys;
            this.session = bridgeHandler.session;
            this.client = bridgeHandler.client;
            if (this.client != null) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not connected");
            }
        } else {
            // standalone accessory or brige accessory, do pairing and session setup here
            this.baseUrl = "https://" + getConfig().get("ipV4Address").toString();
            this.setupCode = getConfig().get("setupCode").toString();
            try {
                this.keys = new PairingManager(httpClient, setupCode).pair(baseUrl);
                this.session = new SecureSession(keys);
                this.client = new SecureClient(httpClient, session, baseUrl);
                updateStatus(ThingStatus.ONLINE);
            } catch (Exception e) {
                logger.error("Failed to initialize HomeKit client", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // override in subclass
    }
}
