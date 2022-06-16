/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.server.CallbackServer;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    private Optional<VehicleConfiguration> config = Optional.empty();
    private Optional<CallbackServer> srv = Optional.empty();
    private OAuthFactory oAuthFactory;
    private final HttpClientFactory httpClientFactory;
    private final String uid;

    public VehicleHandler(Thing thing, OAuthFactory oAuthFactory, HttpClientFactory hcf, String uid) {
        super(thing);
        this.oAuthFactory = oAuthFactory;
        this.httpClientFactory = hcf;
        this.uid = uid;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = Optional.of(getConfigAs(VehicleConfiguration.class));
        updateStatus(ThingStatus.UNKNOWN);
        // scheduler.execute(() -> {
        // boolean thingReachable = true; // <background task with long running initialization here>
        // // when done do:
        // if (thingReachable) {
        // } else {
        // updateStatus(ThingStatus.OFFLINE);
        // }
        // });

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleRemoval() {
        super.handleRemoval();
        if (!srv.isEmpty()) {
            srv.get().stop();
        }
        // start will be handled in next initialize
    }

    @Override
    public void dispose() {
        super.dispose();
        if (!srv.isEmpty()) {
            srv.get().stop();
        }
    }
}
