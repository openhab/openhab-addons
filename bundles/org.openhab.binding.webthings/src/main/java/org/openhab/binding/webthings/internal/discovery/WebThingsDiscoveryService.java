/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal.discovery;

import static org.openhab.binding.webthings.internal.WebThingsBindingConstants.*;
import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.*;
import static org.openhab.binding.webthings.internal.utilities.WebThingsRestApiUtilities.getAllWebThings;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebThingsDiscoveryService} is responsible for discovering WebThings
 *
 * @author schneider_sven - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.webthings")
public class WebThingsDiscoveryService extends AbstractDiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(WebThingsDiscoveryService.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Stream.of(THING_TYPE_WEBTHING).collect(Collectors.toSet());

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;

    public WebThingsDiscoveryService() {
        // super(DISCOVER_TIMEOUT_SECONDS);
        // super(new HashSet<>(Arrays.asList(new ThingTypeUID(BINDING_ID, "-"))),
        // DISCOVER_TIMEOUT_SECONDS, true);
        super(SUPPORTED_THING_TYPES, DISCOVER_TIMEOUT_SECONDS, backgroundDiscovery);
    }

    @Override
    @Activate
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
    }

    @Override
    @Modified
    protected void modified(Map<String, Object> configProperties) {
        super.modified(configProperties);
    }

    @Override
    protected void startScan() {
        logger.debug("Starting openHAB GateWay discovery scan");
        if(serverUrl == "" || token == ""){
            logger.warn("Binding not configured -> Will not discover things from gateway");
            return;
        }

        List<JsonObject> webThingList;
        try {
            webThingList = getAllWebThings(serverUrl, "bearer", token, "things");
        } catch (IOException e) {
            logger.warn("Discovery error: Could not import WebThings from Gateway - Error: {}", e.getMessage());
            return;
        }

        for(JsonObject thing: webThingList){
            createResults(thing);
        }
    }

    public void createResults(JsonObject thing) {
        createResults(thing, "bearer", token);
    }

    public void createResults(JsonObject thing, String security, String securityToken){
        if(!thing.has("id")){
            logger.debug("Thing does not have an id. Cannot create DiscoveryResult");
            return;
        }

        if(!thing.has("title")){
            logger.debug("Thing does not have a title. Cannot create DiscoveryResult");
            return;
        }

        String title = thing.get("title").toString();
        ThingUID thingUID = new ThingUID(THING_TYPE_WEBTHING, title.replaceAll("\\W", ""));

        Map<String,Object> properties = new HashMap<String, Object>(4);
        String url = thing.get("id").getAsString();
        properties.put("link", url);
        properties.put("security", security);
        properties.put("securityToken", securityToken);
        properties.put("importToken", false);

        thingDiscovered(DiscoveryResultBuilder.create(thingUID).withThingType(THING_TYPE_WEBTHING).withLabel(title).withProperties(properties).build());
    }
}
