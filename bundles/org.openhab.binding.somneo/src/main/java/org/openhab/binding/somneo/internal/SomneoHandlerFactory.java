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
package org.openhab.binding.somneo.internal;

import static org.openhab.binding.somneo.internal.SomneoBindingConstants.THING_TYPE_HF367X;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomneoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.somneo", service = ThingHandlerFactory.class)
public class SomneoHandlerFactory extends BaseThingHandlerFactory implements HttpClientProvider {

    private final Logger logger = LoggerFactory.getLogger(SomneoHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HF367X);

    private final HttpClient secureClient;
    private final HttpClient insecureClient;
    private final SomneoPresetStateDescriptionProvider provider;

    @Activate
    public SomneoHandlerFactory(@Reference SomneoPresetStateDescriptionProvider provider) {
        this.provider = provider;

        this.secureClient = new HttpClient(new SslContextFactory.Client(false));
        this.insecureClient = new HttpClient(new SslContextFactory.Client(true));

        try {
            this.secureClient.start();
            this.insecureClient.start();
        } catch (Exception e) {
            logger.warn("Failed to start insecure http client: {}", e.getMessage());
            throw new IllegalStateException("Could not create insecure HttpClient");
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HF367X.equals(thingTypeUID)) {
            return new SomneoHandler(thing, this, provider);
        }

        return null;
    }

    @Override
    public HttpClient getSecureClient() {
        return secureClient;
    }

    @Override
    public HttpClient getInsecureClient() {
        return insecureClient;
    }
}
