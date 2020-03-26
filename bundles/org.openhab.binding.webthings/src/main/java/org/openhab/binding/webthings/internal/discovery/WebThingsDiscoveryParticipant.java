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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebThingsDiscoveryParticipant} is responsible for discovering
 * WebThings
 *
 * @author schneider_sven - Initial contribution
 */
@Component(immediate = true, configurationPid = "discovery.webthingsMdns")
public class WebThingsDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(WebThingsDiscoveryParticipant.class);
    //private final WebThingsDiscoveryService webThingsDiscoveryService = new WebThingsDiscoveryService();

    private static final String SERVICE_TYPE = "_webthing._tcp.local.";

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType(){
        return SERVICE_TYPE;
    }

    @Override
    public DiscoveryResult createResult(ServiceInfo service) {
        final ThingUID uid = getThingUID(service);
        if(uid == null) {
            return null;
        }

        if(service.getName().contains("webthings-server")){
            return null;
        }

        String host; 
        if(service.getHostAddresses().length > 0){
             host = service.getHostAddresses()[0];
        } else {
            logger.warn("Cannot auto detect thing. Service does not provide host address. Server name: {}", service.getName());
            return null;
        }

        int port = service.getPort();
        String url = "http://" + host + ":" + port;
        Boolean reachable = pingURL(url, 1000);
        Boolean multipleThings = pingURL(url + "/0", 1000);
        logger.debug("WebThingServer found: {}:{} - reachable: {}", host, port, reachable);

        if(!reachable){
            return null;
        }else if(reachable && multipleThings){
            url += "/0";
        }

        final Map<String, Object> properties = new HashMap<>(2);
        properties.put("link", url);
        if(!multipleThings){
            properties.put("security", "none");
        }

        String name = uid.toString().substring(uid.toString().lastIndexOf(":") +1, uid.toString().length());
        final DiscoveryResult result = DiscoveryResultBuilder.create(uid).withThingType(THING_TYPE_WEBTHING).withProperties(properties).withLabel(url + ": " + name).build();

        return result;

        /*String base = service.getPropertyString("path");

        List<JsonObject> webThingList;
        try {
            if(multipleThings){
                webThingList = getAllWebThings(url, "none", "", base);
            } else{
                webThingList = new ArrayList<JsonObject>();
                webThingList.add(getWebThing(url, "none", "", base));
            }
        } catch (IOException e) {
            logger.warn("Discovery error: Could not import WebThings from Gateway - Error: ", e.getMessage());
            return null;
        }

        for(JsonObject webThing: webThingList){
            webThingsDiscoveryService.createResults(webThing, "none", "");
        }

        return null;*/
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String name = service.getName().replaceAll("\\W", "");
        //String name = UUID.randomUUID().toString()
        ThingUID thingUID = new ThingUID(THING_TYPE_WEBTHING, name);
        return thingUID;
    }  

    public boolean pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            InetSocketAddress url = new InetSocketAddress(host, port);
            socket.connect(url, timeout);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    /**
     * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in 
     * the 200-399 range.
     * @param url The HTTP URL to be pinged.
     * @param timeout The timeout in millis for both the connection timeout and the response read timeout. Note that
     * the total timeout is effectively two times the given timeout.
     * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a HEAD request within the
     * given timeout, otherwise <code>false</code>.
     */
    public boolean pingURL(String url, int timeout) {
        url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            return (200 <= responseCode && responseCode <= 399);
        } catch (IOException exception) {
            return false;
        }
    }
}
