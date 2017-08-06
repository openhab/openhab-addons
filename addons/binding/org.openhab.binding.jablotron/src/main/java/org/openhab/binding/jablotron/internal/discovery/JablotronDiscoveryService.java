/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jablotron.internal.discovery;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryServiceCallback;
import org.eclipse.smarthome.config.discovery.ExtendedDiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.jablotron.handler.JablotronBridgeHandler;
import org.openhab.binding.jablotron.internal.Utils;
import org.openhab.binding.jablotron.internal.model.JablotronLoginResponse;
import org.openhab.binding.jablotron.internal.model.JablotronWidgetsResponse;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

/**
 * The {@link JablotronDiscoveryService} is responsible for the thing discovery
 * process.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronDiscoveryService extends AbstractDiscoveryService implements ExtendedDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(JablotronDiscoveryService.class);
    private JablotronBridgeHandler bridge;
    private DiscoveryServiceCallback discoveryServiceCallback;

    // Instantiate and configure the SslContextFactory
    SslContextFactory sslContextFactory = new SslContextFactory(true);

    HttpClient httpClient;

    ScheduledFuture<?> discoveryJob = null;

    private Gson gson = new Gson();
    private static final int DISCOVERY_TIMEOUT_SEC = 10;

    public JablotronDiscoveryService(JablotronBridgeHandler bridgeHandler) {
        super(DISCOVERY_TIMEOUT_SEC);
        logger.debug("Creating discovery service");
        this.bridge = bridgeHandler;

        sslContextFactory.setExcludeProtocols("");
        sslContextFactory.setExcludeCipherSuites("");

    }

    private void startDiscovery() {
        if (login()) {
            discoverServices();
            logout();
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Starting SomfyTahoma background discovery");

        if (discoveryJob == null || discoveryJob.isCancelled()) {
            discoveryJob = scheduler.scheduleWithFixedDelay(this::startDiscovery, 10, 3600,
                    TimeUnit.SECONDS);
        }

    }

    /**
     * Called on component activation.
     */
    @Override
    @Activate
    public void activate(@Nullable Map<String, @Nullable Object> configProperties) {
        super.activate(configProperties);
    }

    @Deactivate
    @Override
    protected void deactivate() {
        super.deactivate();
        if (discoveryJob != null) {
            discoveryJob.cancel(true);
        }
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Cannot stop http client", e);
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return new HashSet<>(Arrays.asList(THING_TYPE_OASIS, THING_TYPE_JA100));
    }

    @Override
    public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
        this.discoveryServiceCallback = discoveryServiceCallback;
    }

    @Override
    protected void startScan() {
        logger.debug("Starting scanning for items...");
        //bridge.setDiscoveryService(this);
        startDiscovery();
    }

    public void oasisDiscovered(String label, String serviceId, String url) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("serviceId", serviceId);
        properties.put("url", url);

        ThingUID thingUID = new ThingUID(THING_TYPE_OASIS, bridge.getThing().getUID(), serviceId);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.info("Detected an OASIS alarm with service id: {}", serviceId);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_OASIS).withProperties(properties)
                            .withRepresentationProperty("serviceId").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

    public void ja100Discovered(String label, String serviceId, String url) {
        Map<String, Object> properties = new HashMap<>(1);
        properties.put("serviceId", serviceId);
        properties.put("url", url);

        ThingUID thingUID = new ThingUID(THING_TYPE_JA100, bridge.getThing().getUID(), serviceId);

        if (discoveryServiceCallback.getExistingThing(thingUID) == null) {
            logger.info("Detected a JA100 alarm with service id: {}", serviceId);
            thingDiscovered(
                    DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_JA100).withProperties(properties)
                            .withRepresentationProperty("serviceId").withLabel(label)
                            .withBridge(bridge.getThing().getUID()).build());
        }
    }

    private synchronized void discoverServices() {
        try {
            String url = JABLOTRON_URL + "ajax/widget-new.php?" + Utils.getBrowserTimestamp();

            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL + "cloud")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(AGENT)
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();

            logger.debug("Response: {}", line);
            JablotronWidgetsResponse response = gson.fromJson(line, JablotronWidgetsResponse.class);

            if (!response.isOKStatus()) {
                logger.error("Invalid widgets response: {}", line);
                return;
            }

            if (response.getCntWidgets() == 0) {
                logger.error("Cannot found any Jablotron device");
                return;
            }

            for (int i = 0; i < response.getCntWidgets(); i++) {
                String serviceId = String.valueOf(response.getWidgets().get(i).getId());
                url = response.getWidgets().get(i).getUrl();
                logger.debug("Found Jablotron service: {} id: {}", response.getWidgets().get(i).getName(), serviceId);

                String device = response.getWidgets().get(i).getTemplateService();
                if (device.equals(THING_TYPE_OASIS.getId())) {
                    oasisDiscovered("Jablotron OASIS Alarm", serviceId, url);
                } else if (device.equals(THING_TYPE_JA100.getId())) {
                    ja100Discovered("Jablotron JA100 Alarm", serviceId, url);
                } else {
                    logger.error("Unsupported device type discovered: {} with serviceId: {} and url: {}", response.getWidgets().get(i).getTemplateService(), serviceId, url);
                }
            }
        } catch (TimeoutException ex) {
            logger.debug("Timeout during discovering services", ex);
        } catch (Exception ex) {
            logger.error("Cannot discover Jablotron services!", ex);
        }
    }

    protected synchronized boolean login() {
        String url;

        httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);

        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("Cannot start http client!", e);
            return false;
        }

        try {
            url = JABLOTRON_URL + "ajax/login.php";
            String urlParameters = "login=" + bridge.bridgeConfig.getLogin() + "&heslo=" + bridge.bridgeConfig.getPassword() + "&aStatus=200&loginType=Login";

            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.POST)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .agent(AGENT)
                    .content(new StringContentProvider(urlParameters), "application/x-www-form-urlencoded; charset=UTF-8")
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();

            JablotronLoginResponse response = gson.fromJson(line, JablotronLoginResponse.class);

            if (response.isOKStatus()) {
                logger.debug("Successfully logged to Jablonet cloud!");
                return true;
            } else {
                logger.debug("Received error response: {}", line);
            }
        } catch (TimeoutException e) {
            logger.debug("Timeout during getting login cookie", e);
        } catch (Exception e) {
            logger.error("Cannot get Jablotron login cookie", e);
        }
        return false;
    }

    private void logout() {

        String url = JABLOTRON_URL + "logout";
        try {
            ContentResponse resp = httpClient.newRequest(url)
                    .method(HttpMethod.GET)
                    .header(HttpHeader.ACCEPT_LANGUAGE, "cs-CZ")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip, deflate")
                    .header(HttpHeader.REFERER, JABLOTRON_URL)
                    .agent(AGENT)
                    .timeout(5, TimeUnit.SECONDS)
                    .send();

            String line = resp.getContentAsString();

            logger.debug("logout... {}", line);
            httpClient.stop();
            httpClient.destroy();
        } catch (Exception e) {
            //Silence
        }
    }
}
