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
package org.openhab.binding.electroluxappliances.internal.handler;

import static org.openhab.binding.electroluxappliances.internal.ElectroluxAppliancesBindingConstants.*;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.ProxyConfiguration;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
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

import com.google.gson.Gson;

/**
 * The {@link ElectroluxAppliancesHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.electroluxappliances", service = ThingHandlerFactory.class)
public class ElectroluxAppliancesHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ELECTROLUX_AIR_PURIFIER,
            THING_TYPE_ELECTROLUX_WASHING_MACHINE, THING_TYPE_BRIDGE);
    private final Gson gson;
    private HttpClient httpClient;
    private final Logger logger = LoggerFactory.getLogger(ElectroluxAppliancesHandlerFactory.class);
    private static final boolean DEBUG = false;

    @Activate
    public ElectroluxAppliancesHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.gson = new Gson();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ELECTROLUX_AIR_PURIFIER.equals(thingTypeUID)) {
            return new ElectroluxAirPurifierHandler(thing);
        } else if (THING_TYPE_ELECTROLUX_WASHING_MACHINE.equals(thingTypeUID)) {
            return new ElectroluxWashingMachineHandler(thing);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new ElectroluxAppliancesBridgeHandler((Bridge) thing, httpClient, gson);
        }
        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        logger.debug("setHttpClientFactory this: {}", this);
        this.httpClient = httpClientFactory.getCommonHttpClient();
        if (DEBUG) {
            try {
                // Load the mitmproxy CA certificate
                FileInputStream caInput = new FileInputStream("/Users/janne/.mitmproxy/mitmproxy-ca-cert.pem");
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                } finally {
                    caInput.close();
                }

                // Create a TrustStore with the CA certificate
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                trustStore.setCertificateEntry("mitmproxy", ca);

                // Initialize the SSLContext with the TrustStore
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(trustStore);

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

                // Create and start the HttpClient with the custom SSLContext
                this.httpClient = new HttpClient(new SslContextFactory.Client());
                ((SslContextFactory.Client) httpClient.getSslContextFactory()).setSslContext(sslContext);
                this.httpClient.start();
            } catch (Exception e) {
                logger.error("Exception: {}", e.getMessage());
            }

            logger.debug("setHttpClientFactory configure proxy!");
            ProxyConfiguration proxyConfig = httpClient.getProxyConfiguration();
            HttpProxy proxy = new HttpProxy("127.0.0.1", 8090);
            proxyConfig.getProxies().add(proxy);
        }
    }
}
