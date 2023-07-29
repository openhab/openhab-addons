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
package org.openhab.binding.remoteopenhab.internal;

import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.remoteopenhab.internal.handler.RemoteopenhabBridgeHandler;
import org.openhab.binding.remoteopenhab.internal.handler.RemoteopenhabThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link RemoteopenhabHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.remoteopenhab")
public class RemoteopenhabHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(RemoteopenhabBindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS.stream(),
                    RemoteopenhabBindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(RemoteopenhabHandlerFactory.class);

    private final HttpClient httpClient;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private final RemoteopenhabChannelTypeProvider channelTypeProvider;
    private final RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider;
    private final RemoteopenhabCommandDescriptionOptionProvider commandDescriptionProvider;
    private final Gson jsonParser;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    private HttpClient httpClientTrustingCert;

    @Activate
    public RemoteopenhabHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference ClientBuilder clientBuilder, final @Reference SseEventSourceFactory eventSourceFactory,
            final @Reference RemoteopenhabChannelTypeProvider channelTypeProvider,
            final @Reference RemoteopenhabStateDescriptionOptionProvider stateDescriptionProvider,
            final @Reference RemoteopenhabCommandDescriptionOptionProvider commandDescriptionProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
        this.channelTypeProvider = channelTypeProvider;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.jsonParser = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;

        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            TrustManager[] trustAllCerts = new TrustManager[] { new X509ExtendedTrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate @Nullable [] chain, @Nullable String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate @Nullable [] chain, @Nullable String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate @Nullable [] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable Socket socket) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable Socket socket) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable SSLEngine engine) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate @Nullable [] chain, @Nullable String authType,
                        @Nullable SSLEngine engine) throws CertificateException {
                }
            } };
            sslContext.init(null, trustAllCerts, null);
            sslContextFactory.setSslContext(sslContext);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("An exception occurred while requesting the SSL encryption algorithm : '{}'", e.getMessage(),
                    e);
        } catch (KeyManagementException e) {
            logger.warn("An exception occurred while initialising the SSL context : '{}'", e.getMessage(), e);
        }
        this.httpClientTrustingCert = httpClientFactory.createHttpClient(RemoteopenhabBindingConstants.BINDING_ID,
                sslContextFactory);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        try {
            httpClientTrustingCert.start();
        } catch (Exception e) {
            logger.warn("Unable to start Jetty HttpClient", e);
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        try {
            httpClientTrustingCert.stop();
        } catch (Exception e) {
            logger.warn("Unable to stop Jetty HttpClient", e);
        }
        super.deactivate(componentContext);
    }

    /**
     * The things this factory supports creating.
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (thingTypeUID.equals(RemoteopenhabBindingConstants.BRIDGE_TYPE_SERVER)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (RemoteopenhabBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null && thingUID != null) {
                newThingUID = new ThingUID(thingTypeUID, bridgeUID, thingUID.getId());
            } else {
                newThingUID = thingUID;
            }
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the remote openHAB binding.");
    }

    /**
     * Creates a handler for the specific thing.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(RemoteopenhabBindingConstants.BRIDGE_TYPE_SERVER)) {
            return new RemoteopenhabBridgeHandler((Bridge) thing, httpClient, httpClientTrustingCert, clientBuilder,
                    eventSourceFactory, channelTypeProvider, stateDescriptionProvider, commandDescriptionProvider,
                    jsonParser, i18nProvider, localeProvider);
        } else if (RemoteopenhabBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new RemoteopenhabThingHandler(thing);
        }
        return null;
    }
}
