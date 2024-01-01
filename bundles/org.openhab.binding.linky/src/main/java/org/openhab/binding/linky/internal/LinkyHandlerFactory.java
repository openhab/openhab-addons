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
package org.openhab.binding.linky.internal;

import static org.openhab.binding.linky.internal.LinkyBindingConstants.THING_TYPE_LINKY;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.linky.internal.handler.LinkyHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TrustAllTrustManager;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link LinkyHandlerFactory} is responsible for creating things handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.linky")
public class LinkyHandlerFactory extends BaseThingHandlerFactory {
    private static final DateTimeFormatter LINKY_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
    private static final int REQUEST_BUFFER_SIZE = 8000;

    private final Logger logger = LoggerFactory.getLogger(LinkyHandlerFactory.class);
    private final Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
            (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                    .parse(json.getAsJsonPrimitive().getAsString(), LINKY_FORMATTER))
            .create();
    private final LocaleProvider localeProvider;
    private final HttpClient httpClient;

    @Activate
    public LinkyHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference HttpClientFactory httpClientFactory) {
        this.localeProvider = localeProvider;
        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, null);
            sslContextFactory.setSslContext(sslContext);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("An exception occurred while requesting the SSL encryption algorithm : '{}'", e.getMessage(),
                    e);
        } catch (KeyManagementException e) {
            logger.warn("An exception occurred while initialising the SSL context : '{}'", e.getMessage(), e);
        }
        this.httpClient = httpClientFactory.createHttpClient(LinkyBindingConstants.BINDING_ID, sslContextFactory);
        httpClient.setFollowRedirects(false);
        httpClient.setRequestBufferSize(REQUEST_BUFFER_SIZE);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("Unable to start Jetty HttpClient {}", e.getMessage());
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Unable to stop Jetty HttpClient {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_LINKY.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        return supportsThingType(thing.getThingTypeUID()) ? new LinkyHandler(thing, localeProvider, gson, httpClient)
                : null;
    }
}
