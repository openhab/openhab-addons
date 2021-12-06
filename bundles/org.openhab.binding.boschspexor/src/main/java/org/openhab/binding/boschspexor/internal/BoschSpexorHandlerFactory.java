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
package org.openhab.binding.boschspexor.internal;

import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import org.connectorio.cloud.device.auth.nimbus.internal.NimbusDeviceAuthenticator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.boschspexor.internal.api.service.SpexorAPIService;
import org.openhab.binding.boschspexor.internal.api.service.SpexorBridgeHandler;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorUserGrantService;
import org.openhab.core.auth.oauth2client.internal.OAuthStoreHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BoschSpexorHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.boschspexor", service = ThingHandlerFactory.class)
public class BoschSpexorHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClient httpClient;
    private @Nullable OAuthStoreHandler oAuthStoreHandler;
    private final SpexorUserGrantService authGrantService;
    private final SpexorAuthorizationService authService;
    private final SpexorAPIService apiService;
    private @Nullable StorageService storageService;

    @Activate
    public BoschSpexorHandlerFactory(@Reference OAuthStoreHandler oAuthStoreHandler,
            @Reference StorageService storageService, @Reference final HttpClientFactory httpClientFactory,
            @Reference final NimbusDeviceAuthenticator deviceAuthenticator,
            @Reference SpexorUserGrantService authGrantService) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.authGrantService = authGrantService;
        this.storageService = storageService;
        this.authService = new SpexorAuthorizationService(oAuthStoreHandler, httpClient, storageService);
        this.apiService = new SpexorAPIService(authService);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        ThingHandler result = null;
        if (SPEXOR_BRIDGE_TYPE.equals(thingTypeUID)) {
            result = new SpexorBridgeHandler((Bridge) thing, authService, apiService);
        } else if (SPEXOR_THING_TYPE.equals(thingTypeUID)) {
            result = new BoschSpexorHandler(thing, apiService);
        }

        return result;
    }
}
