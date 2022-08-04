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

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
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
 * The {@link MercedesMeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mercedesme", service = ThingHandlerFactory.class)
public class MercedesMeHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BEV, THING_TYPE_COMB,
            THING_TYPE_HYBRID, THING_TYPE_ACCOUNT);

    private final Logger logger = LoggerFactory.getLogger(MercedesMeHandlerFactory.class);
    private final OAuthFactory oAuthFactory;
    private final HttpClient httpClient;
    private final Storage<String> tokenStorage;
    private final MercedesMeCommandOptionProvider mmcop;
    private final MercedesMeStateOptionProvider mmsop;
    private final StorageService storageService;
    private final MercedesMeTranslationProvider translationProvider;

    @Activate
    public MercedesMeHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference HttpClientFactory hcf,
            @Reference StorageService storageService, final @Reference MercedesMeCommandOptionProvider cop,
            final @Reference MercedesMeStateOptionProvider sop, final @Reference TimeZoneProvider tzp,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider) {
        this.oAuthFactory = oAuthFactory;
        this.storageService = storageService;
        Constants.zoneId = tzp.getTimeZone();
        mmcop = cop;
        mmsop = sop;
        tokenStorage = storageService.getStorage(Constants.BINDING_ID);
        translationProvider = new MercedesMeTranslationProvider(i18nProvider, localeProvider);
        httpClient = hcf.createHttpClient(Constants.BINDING_ID);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("HTTP client not started: {} - no web access possible!", e.getLocalizedMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new AccountHandler((Bridge) thing, httpClient, oAuthFactory, tokenStorage, translationProvider);
        }
        return new VehicleHandler(thing, httpClient, thingTypeUID.getId(), storageService, mmcop, mmsop,
                translationProvider);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.debug("HTTP client not stopped: {}", e.getLocalizedMessage());
        }
    }
}
