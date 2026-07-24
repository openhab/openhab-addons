/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.melcloud.internal;

import static org.openhab.binding.melcloud.internal.MelCloudBindingConstants.*;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.melcloud.internal.handler.MelCloudAccountHandler;
import org.openhab.binding.melcloud.internal.handler.MelCloudDeviceHandler;
import org.openhab.binding.melcloud.internal.handler.MelCloudHeatpumpDeviceHandler;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeApiClient;
import org.openhab.binding.melcloud.internal.home.api.MelCloudHomeAuthService;
import org.openhab.binding.melcloud.internal.home.handler.MelCloudHomeAccountHandler;
import org.openhab.binding.melcloud.internal.home.handler.MelCloudHomeAtaUnitHandler;
import org.openhab.binding.melcloud.internal.home.handler.MelCloudHomeAtwUnitHandler;
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
 * The {@link MelCloudHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Luca Calcaterra - Initial contribution
 * @author Wietse van Buitenen - Added heatpump device
 * @author Bernd Weymann - Added MELCloud Home bridge skeleton
 * @author Bernd Weymann - Wired the real MELCloud Home OAuth login flow (ADR-002)
 * @author Bernd Weymann - Wired the MELCloud Home ATA/ATW unit Things (ADR-003)
 */
@NonNullByDefault
@Component(configurationPid = "binding.melcloud", service = ThingHandlerFactory.class)
public class MelCloudHandlerFactory extends BaseThingHandlerFactory {

    private static final Duration HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private final StorageService storageService;

    @Activate
    public MelCloudHandlerFactory(@Reference StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_MELCLOUD_ACCOUNT.equals(thingTypeUID)) {
            return new MelCloudAccountHandler((Bridge) thing);
        } else if (THING_TYPE_ACDEVICE.equals(thingTypeUID)) {
            return new MelCloudDeviceHandler(thing);
        } else if (THING_TYPE_HEATPUMPDEVICE.equals(thingTypeUID)) {
            return new MelCloudHeatpumpDeviceHandler(thing);
        } else if (THING_TYPE_MELCLOUD_HOME_ACCOUNT.equals(thingTypeUID)) {
            return new MelCloudHomeAccountHandler((Bridge) thing, new MelCloudHomeAuthService(createOAuthHttpClient()),
                    new MelCloudHomeApiClient(), storageService);
        } else if (THING_TYPE_MELCLOUD_HOME_ATA_UNIT.equals(thingTypeUID)) {
            return new MelCloudHomeAtaUnitHandler(thing);
        } else if (THING_TYPE_MELCLOUD_HOME_ATW_UNIT.equals(thingTypeUID)) {
            return new MelCloudHomeAtwUnitHandler(thing);
        }

        return null;
    }

    /**
     * Builds the {@link HttpClient} used for the MELCloud Home OAuth login flow (see ADR-002).
     *
     * <p>
     * A cookie handler is required so the session cookie set by {@code auth.melcloudhome.com}/Cognito during the
     * redirect chain is preserved across requests. Redirects are intentionally not followed automatically
     * ({@link HttpClient.Redirect#NEVER}) so {@link MelCloudHomeAuthService} can intercept the final
     * {@code melcloudhome://} redirect itself instead of the client failing on the unsupported custom scheme.
     */
    private static HttpClient createOAuthHttpClient() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).connectTimeout(HTTP_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER).build();
    }
}
