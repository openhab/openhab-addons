/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.handler;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.config.ApiConfiguration;
import org.openhab.binding.freeboxos.internal.discovery.FreeboxOsDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiBridgeHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiBridgeHandler.class);

    private final ApiHandler apiHandler;

    public ApiBridgeHandler(Bridge thing, HttpClient httpClient) {
        super(thing);
        apiHandler = new ApiHandler(this, httpClient);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Freebox OS API handler for thing {}.", getThing().getUID());
        apiHandler.openConnection(getConfiguration());
    }

    public ApiHandler getApi() {
        return apiHandler;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Freebox OS API handler for thing {}", getThing().getUID());
        apiHandler.closeSession();
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(FreeboxOsDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public ApiConfiguration getConfiguration() {
        return getConfigAs(ApiConfiguration.class);
    }

    public void pushAppToken(String appToken) {
        logger.debug("Store new app token in the thing configuration");
        Configuration thingConfig = editConfiguration();
        thingConfig.put(ApiConfiguration.APP_TOKEN, appToken);
        updateConfiguration(thingConfig);
    }

    public void pushStatus(ThingStatusDetail detail, @Nullable String string) {
        if (detail == ThingStatusDetail.NONE) {
            updateStatus(ThingStatus.ONLINE);
        } else if (ThingStatusDetail.CONFIGURATION_PENDING.equals(detail)) {
            updateStatus(ThingStatus.INITIALIZING, detail, string);
        } else {
            updateStatus(ThingStatus.OFFLINE, detail, string);
        }
    }
}
