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
package org.openhab.binding.hue.internal.discovery;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.handler.HueBridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link HueBridgeNupnpDiscovery} is responsible for discovering new Hue Bridges. It uses the 'NUPnP service
 * provided by Philips'.
 *
 * @author Awelkiyar Wehabrebi - Initial contribution
 * @author Christoph Knauf - Refactorings
 * @author Andre Fuechsel - make {@link #startScan()} asynchronous
 */
@Component(service = DiscoveryService.class, configurationPid = "discovery.hue")
@NonNullByDefault
public class HueBridgeNupnpDiscovery extends AbstractDiscoveryService {

    private static final String MODEL_NAME_PHILIPS_HUE = "\"name\":\"Philips Hue\"";
    protected static final String BRIDGE_INDICATOR = "fffe";
    private static final String DISCOVERY_URL = "https://discovery.meethue.com/";
    protected static final String LABEL_PATTERN = "Philips Hue (%s)";
    private static final String CONFIG_URL_PATTERN = "http://%s/api/0/config";
    private static final int REQUEST_TIMEOUT = 5000;
    private static final int DISCOVERY_TIMEOUT = 10;

    private final Logger logger = LoggerFactory.getLogger(HueBridgeNupnpDiscovery.class);

    public HueBridgeNupnpDiscovery() {
        super(HueBridgeHandler.SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT, false);
    }

    @Override
    protected void startScan() {
        scheduler.schedule(this::discoverHueBridges, 0, TimeUnit.SECONDS);
    }

    /**
     * Discover available Hue Bridges and then add them in the discovery inbox
     */
    private void discoverHueBridges() {
        for (BridgeJsonParameters bridge : getBridgeList()) {
            if (isReachableAndValidHueBridge(bridge)) {
                String host = bridge.getInternalIpAddress();
                String serialNumber = bridge.getId().toLowerCase();
                ThingUID uid = new ThingUID(THING_TYPE_BRIDGE, serialNumber);
                DiscoveryResult result = DiscoveryResultBuilder.create(uid) //
                        .withProperties(Map.of( //
                                HOST, host, //
                                Thing.PROPERTY_SERIAL_NUMBER, serialNumber)) //
                        .withLabel(String.format(LABEL_PATTERN, host)) //
                        .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER) //
                        .build();
                thingDiscovered(result);
            }
        }
    }

    /**
     * Checks if the Bridge is a reachable Hue Bridge with a valid id.
     *
     * @param bridge the {@link BridgeJsonParameters}s
     * @return true if Hue Bridge is a reachable Hue Bridge with an id containing
     *         BRIDGE_INDICATOR longer then 10
     */
    private boolean isReachableAndValidHueBridge(BridgeJsonParameters bridge) {
        String host = bridge.getInternalIpAddress();
        String id = bridge.getId();
        String description;
        if (host == null) {
            logger.debug("Bridge not discovered: ip is null");
            return false;
        }
        if (id == null) {
            logger.debug("Bridge not discovered: id is null");
            return false;
        }
        if (id.length() < 10) {
            logger.debug("Bridge not discovered: id {} is shorter then 10.", id);
            return false;
        }
        if (!BRIDGE_INDICATOR.equals(id.substring(6, 10))) {
            logger.debug(
                    "Bridge not discovered: id {} does not contain bridge indicator {} or its at the wrong position.",
                    id, BRIDGE_INDICATOR);
            return false;
        }
        try {
            description = doGetRequest(String.format(CONFIG_URL_PATTERN, host));
        } catch (IOException e) {
            logger.debug("Bridge not discovered: Failure accessing description file for ip: {}", host);
            return false;
        }
        if (!description.contains(MODEL_NAME_PHILIPS_HUE)) {
            logger.debug("Bridge not discovered: Description does not containing the model name: {}", description);
            return false;
        }
        return true;
    }

    /**
     * Use the Philips Hue NUPnP service to find Hue Bridges in local Network.
     *
     * @return a list of available Hue Bridges
     */
    private List<BridgeJsonParameters> getBridgeList() {
        try {
            Gson gson = new Gson();
            String json = doGetRequest(DISCOVERY_URL);
            List<BridgeJsonParameters> bridgeParameters = gson.fromJson(json,
                    new TypeToken<List<BridgeJsonParameters>>() {
                    }.getType());
            return Objects.requireNonNull(bridgeParameters);
        } catch (IOException e) {
            logger.debug("Philips Hue NUPnP service not reachable. Can't discover bridges");
        } catch (JsonParseException e) {
            logger.debug("Invalid json respone from Hue NUPnP service. Can't discover bridges");
        }
        return List.of();
    }

    /**
     * Introduced in order to enable testing.
     *
     * @param url the url
     * @return the http request result as String
     * @throws IOException if request failed
     */
    protected String doGetRequest(String url) throws IOException {
        return HttpUtil.executeUrl("GET", url, REQUEST_TIMEOUT);
    }
}
