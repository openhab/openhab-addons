/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.elroconnects.internal;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsAccountHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsBridgeHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsCOAlarmHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsDeviceHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsEntrySensorHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsHeatAlarmHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsMotionSensorHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsPowerSocketHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsSmokeAlarmHandler;
import org.openhab.binding.elroconnects.internal.handler.ElroConnectsWaterAlarmHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.HttpClientInitializationException;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ElroConnectsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.elroconnects", service = ThingHandlerFactory.class)
public class ElroConnectsHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(ElroConnectsHandlerFactory.class);

    private final HttpClientFactory httpClientFactory;
    private final NetworkAddressService networkAddressService;
    private final ElroConnectsDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    private @Nullable HttpClient httpClient;

    @Activate
    public ElroConnectsHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference ElroConnectsDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        this.httpClientFactory = httpClientFactory;
        this.networkAddressService = networkAddressService;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Deactivate
    public void deactivate() {
        HttpClient client = httpClient;

        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                // catching exception is necessary due to the signature of HttpClient.stop()
                logger.debug("Failed to stop http client: {}", e.getMessage());
            }
            httpClient = null;
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        switch (thing.getThingTypeUID().getId()) {
            case TYPE_ACCOUNT:
                return createElroConnectsAccountHandler(thing);
            case TYPE_CONNECTOR:
                return new ElroConnectsBridgeHandler((Bridge) thing, networkAddressService,
                        dynamicStateDescriptionProvider);
            case TYPE_SMOKEALARM:
                return new ElroConnectsSmokeAlarmHandler(thing);
            case TYPE_WATERALARM:
                return new ElroConnectsWaterAlarmHandler(thing);
            case TYPE_COALARM:
                return new ElroConnectsCOAlarmHandler(thing);
            case TYPE_HEATALARM:
                return new ElroConnectsHeatAlarmHandler(thing);
            case TYPE_ENTRYSENSOR:
                return new ElroConnectsEntrySensorHandler(thing);
            case TYPE_MOTIONSENSOR:
                return new ElroConnectsMotionSensorHandler(thing);
            case TYPE_POWERSOCKET:
                return new ElroConnectsPowerSocketHandler(thing);
            case TYPE_THSENSOR:
                return new ElroConnectsDeviceHandler(thing);
            default:
                return null;
        }
    }

    private ThingHandler createElroConnectsAccountHandler(Thing thing) {
        // Create and start the httpClient for the first ElroConnectsAccountHandler that gets created. We cannot use the
        // common http client because we need to disable the authentication protocol handler.
        HttpClient client = httpClient;
        if (client == null) {
            client = httpClientFactory.createHttpClient(BINDING_ID);
            httpClient = client;

            try {
                client.start();

                // The getAccessToken call in the ElroConnectsAccountHandler returns an invalid 401 response on
                // authentication error, missing the www-authentication header. This header should be there according to
                // RFC7235. This workaround removes the protocol handler and the check.
                client.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
            } catch (Exception e) {
                // catching exception is necessary due to the signature of HttpClient.start()
                logger.debug("Failed to start http client: {}", e.getMessage());
                throw new HttpClientInitializationException("Could not initialize HttpClient", e);
            }
        }
        return new ElroConnectsAccountHandler((Bridge) thing, client);
    }
}
