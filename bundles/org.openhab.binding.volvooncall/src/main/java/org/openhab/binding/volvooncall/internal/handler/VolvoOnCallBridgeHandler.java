/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.volvooncall.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.volvooncall.internal.VolvoOnCallException;
import org.openhab.binding.volvooncall.internal.api.VocHttpApi;
import org.openhab.binding.volvooncall.internal.config.VolvoOnCallBridgeConfiguration;
import org.openhab.binding.volvooncall.internal.discovery.VolvoVehicleDiscoveryService;
import org.openhab.binding.volvooncall.internal.dto.CustomerAccounts;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link VolvoOnCallBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VolvoOnCallBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(VolvoOnCallBridgeHandler.class);
    private final Gson gson;

    private Optional<VocHttpApi> api = Optional.empty();

    public VolvoOnCallBridgeHandler(Bridge bridge, Gson gson) {
        super(bridge);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing VolvoOnCall API bridge handler.");
        VolvoOnCallBridgeConfiguration configuration = getConfigAs(VolvoOnCallBridgeConfiguration.class);
        api = Optional.of(new VocHttpApi(configuration, gson));
        api.ifPresent(service -> {
            try {
                CustomerAccounts account = service.getURL("customeraccounts/", CustomerAccounts.class);
                if (account.username != null) {
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, account.username);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Incorrect username or password");
                }
            } catch (VolvoOnCallException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        api = Optional.empty();
    }

    public Optional<VocHttpApi> getApi() {
        return api;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(VolvoVehicleDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Do nothing
    }
}
