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
package org.openhab.binding.http.internal;

import static org.openhab.binding.http.internal.HttpBindingConstants.THING_TYPE_URL;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
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
 * The {@link HttpHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.http", service = ThingHandlerFactory.class)
public class HttpHandlerFactory extends BaseThingHandlerFactory implements HttpClientProvider {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_URL);
    private final Logger logger = LoggerFactory.getLogger(HttpHandlerFactory.class);

    private final HttpClient secureClient;
    private final HttpClient insecureClient;

    private final HttpDynamicStateDescriptionProvider httpDynamicStateDescriptionProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public HttpHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference HttpDynamicStateDescriptionProvider httpDynamicStateDescriptionProvider,
            @Reference TimeZoneProvider timeZoneProvider) {
        this.secureClient = new HttpClient(new SslContextFactory.Client());
        this.insecureClient = new HttpClient(new SslContextFactory.Client(true));
        // clear user agent, this needs to be set later in the thing configuration as additional header
        this.secureClient.setUserAgentField(null);
        this.insecureClient.setUserAgentField(null);
        try {
            this.secureClient.start();
            this.insecureClient.start();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            logger.warn("Failed to start http client: {}", e.getMessage());
            throw new IllegalStateException("Could not create HttpClient", e);
        }
        this.httpDynamicStateDescriptionProvider = httpDynamicStateDescriptionProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Deactivate
    public void deactivate() {
        try {
            secureClient.stop();
            insecureClient.stop();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.stop()
            logger.warn("Failed to stop insecure http client: {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_URL.equals(thingTypeUID)) {
            return new HttpThingHandler(thing, this, httpDynamicStateDescriptionProvider, timeZoneProvider);
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
