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
package org.openhab.binding.siemenshvac.internal.factory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.converter.ConverterFactory;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacBridgeThingHandler;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacHandlerImpl;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataRegistry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SiemensHvacHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.siemenshvac")
public class SiemensHvacHandlerFactory extends BaseThingHandlerFactory {

    private final NetworkAddressService networkAddressService;
    private final HttpClientFactory httpClientFactory;
    private final SiemensHvacMetadataRegistry metaDataRegistry;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final TranslationProvider translationProvider;

    @Activate
    public SiemensHvacHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference SiemensHvacMetadataRegistry metaDataRegistry,
            final @Reference NetworkAddressService networkAddressService,
            final @Reference ChannelTypeRegistry channelTypeRegistry,
            final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference TranslationProvider translationProvider) {
        this.httpClientFactory = httpClientFactory;
        this.metaDataRegistry = metaDataRegistry;
        this.networkAddressService = networkAddressService;
        this.channelTypeRegistry = channelTypeRegistry;
        this.translationProvider = translationProvider;

        ConverterFactory.registerConverter(timeZoneProvider);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SiemensHvacBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (SiemensHvacBindingConstants.THING_TYPE_OZW.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (SiemensHvacBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId())) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the SiemensHvac binding.");
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(SiemensHvacBindingConstants.THING_TYPE_OZW)) {
            return new SiemensHvacBridgeThingHandler((Bridge) thing, networkAddressService, httpClientFactory,
                    metaDataRegistry, translationProvider);
        } else if (SiemensHvacBindingConstants.BINDING_ID.equals(thing.getThingTypeUID().getBindingId())) {
            SiemensHvacHandlerImpl handler = new SiemensHvacHandlerImpl(thing,
                    metaDataRegistry.getSiemensHvacConnector(), metaDataRegistry, channelTypeRegistry);
            return handler;
        }
        return null;
    }
}
