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

import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.PairingManager;
import org.openhab.binding.homekit.internal.SecureAccessoryClient;
import org.openhab.binding.homekit.internal.SecureSession;
import org.openhab.binding.homekit.internal.SessionKeys;
import org.openhab.binding.homekit.internal.discovery.HomekitAccessoryDiscoveryService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseHomekitServerHandler} handles I/O with HomeKit servers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class BaseHomekitServerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BaseHomekitServerHandler.class);

    protected final HttpClient httpClient;
    protected final HomekitAccessoryDiscoveryService discoveryService;

    protected @Nullable SecureAccessoryClient accessoryClient;
    protected @Nullable SessionKeys keys;
    protected @Nullable SecureSession session;
    protected @Nullable String accessoryAddress;
    protected @Nullable String setupCode;

    public BaseHomekitServerHandler(Thing thing, HomekitAccessoryDiscoveryService discoveryService) {
        super(thing);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.discoveryService = discoveryService;
    }

    @Override
    public void initialize() {
        this.accessoryAddress = getConfig().get("ipV4Address").toString();
        this.setupCode = getConfig().get("setupCode").toString();
        try {
            // pairing and session setup
            this.keys = new PairingManager(httpClient, setupCode).pair(accessoryAddress);
            this.session = new SecureSession(keys);
            this.accessoryClient = new SecureAccessoryClient(httpClient, session, accessoryAddress);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.error("Failed to initialize HomeKit client", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // override in subclass
    }
}
