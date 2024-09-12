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
package org.openhab.binding.tr064.internal;

import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.tr064.internal.phonebook.PhonebookProfileFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
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
 * The {@link Tr064HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.tr064")
public class Tr064HandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(Tr064RootHandler.SUPPORTED_THING_TYPES, Tr064SubHandler.SUPPORTED_THING_TYPES).flatMap(Set::stream)
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(Tr064HandlerFactory.class);
    private final HttpClient httpClient;
    private final PhonebookProfileFactory phonebookProfileFactory;

    // the Tr064ChannelTypeProvider is needed for creating the channels and
    // referenced here to make sure it is available before things are
    // initialized
    @SuppressWarnings("unused")
    private final Tr064ChannelTypeProvider channelTypeProvider;

    @Activate
    public Tr064HandlerFactory(@Reference Tr064ChannelTypeProvider channelTypeProvider,
            @Reference PhonebookProfileFactory phonebookProfileFactory,
            final @Reference HttpClientFactory httpClientFactory) {
        this.channelTypeProvider = channelTypeProvider;
        this.phonebookProfileFactory = phonebookProfileFactory;
        // use an insecure client (i.e. without verifying the certificate)
        this.httpClient = httpClientFactory.createHttpClient(BINDING_ID, new SslContextFactory.Client(true));
        try {
            this.httpClient.start();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            logger.warn("Failed to start http client: {}", e.getMessage());
            throw new IllegalStateException("Could not create HttpClient instance.", e);
        }
    }

    @Deactivate
    public void deactivate() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.stop()
            logger.warn("Failed to stop http client: {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Tr064RootHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            Tr064RootHandler handler = new Tr064RootHandler((Bridge) thing, httpClient);
            if (THING_TYPE_FRITZBOX.equals(thingTypeUID)) {
                phonebookProfileFactory.registerPhonebookProvider(handler);
            }
            return handler;
        } else if (Tr064SubHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new Tr064SubHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof Tr064RootHandler tr064RootHandler) {
            phonebookProfileFactory.unregisterPhonebookProvider(tr064RootHandler);
        }
    }
}
