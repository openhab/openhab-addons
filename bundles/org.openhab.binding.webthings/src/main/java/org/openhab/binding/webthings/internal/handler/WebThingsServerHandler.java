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
package org.openhab.binding.webthings.internal.handler;

import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;

import static org.openhab.binding.webthings.internal.WebThingsBindingConstants.*;
import static org.openhab.binding.webthings.internal.utilities.WebThingsRestApiUtilities.*;

import org.openhab.binding.webthings.internal.dto.CompleteThingDTO;
import org.openhab.binding.webthings.internal.config.WebThingsServerConfiguration;
import org.openhab.binding.webthings.internal.converters.OpenhabToWebThingConverter;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonSyntaxException;

//import org.openhab.binding.webthings.internal.WebThingMaker.*;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.mozilla.iot.webthing.*;

/**
 * The {@link WebThingsServerHandler} is responsible for handling commands,
 * which are sent to one of the channels.
 *
 * @author Sven Schneider - Initial contribution
 */
public class WebThingsServerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(WebThingsServerHandler.class);
    private final OpenhabToWebThingConverter converter = new OpenhabToWebThingConverter();

    private @Nullable WebThingsServerConfiguration config;
    private @Nullable WebThingServer webThingServer;
    // private WebThingsHandler webThingsHandler = new WebThingsHandler();

    public WebThingsServerHandler(org.eclipse.smarthome.core.thing.Thing thing) {
        super(thing);
    }

    /**
     * @return the webThingServer
     */
    public WebThingServer getWebThingServer() {
        return webThingServer;
    }

    /**
     * @return the webThingsHandler
     */
    public OpenhabToWebThingConverter getWebThingsConverter() {
        return converter;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        /*if (CHANNEL_UPDATE.equals(channelUID.getId())) {

        }*/
    }

    @Override
    public void initialize() {
        config = getConfigAs(WebThingsServerConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            boolean thingReachable = true;

            try {
                // If WebThing Server already exists, stop current server
                if (webThingServer != null) {
                    webThingServer.stop();
                }

                // If "all linked items" option is checked in thing configuration import full thing definitions via API
                if (!config.linked) {
                    this.addOpenhabThings();
                } else {
                    this.addEnrichedOpenhabThings();
                }

                // Create WebThing Server
                webThingServer = new WebThingServer( new WebThingServer.MultipleThings( converter.getWebThingList(),
                                                                                        this.getThing().getUID().toString().replace(":", "-")),
                                                                                        config.port);

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        webThingServer.stop();
                        logger.info("Stopping WebThingServer - Hostname: {} - Port: {}", webThingServer.getHostname(),
                                webThingServer.getListeningPort());
                    }
                });

                // Start WebThing Server
                webThingServer.start(false);
                logger.info("Started WebThingServer - Hostname: {} - Port: {}", webThingServer.getHostname(), webThingServer.getListeningPort());

                // Save handler to access later
                SERVER_HANDLER_LIST.put(config.port, this);

                // Setup successful 
                thingReachable = true;
            } catch (BindException e) {
                logger.warn("WebThingServer already running with port: {}", webThingServer.getListeningPort());
                thingReachable = false;
            } catch (IOException e) {
                logger.error("Could not import Things from openHAB - Error: {}", e.getMessage());
                thingReachable = false;
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
                logger.error("Could not sleep thread - Error: {}", e.getMessage());
                thingReachable = false;
            }

            // Set thing status
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void dispose() {
        // Stop WebThing Server before disposing of handler
        if (webThingServer != null && webThingServer.isAlive()) {
            webThingServer.stop();
            converter.getWebThingList().clear();
            logger.info("Stopping WebThingServer - Hostname: {} - Port: {}", webThingServer.getHostname(),
                    webThingServer.getListeningPort());
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        super.dispose();
    }

    /**
     * Import all openHAB Things via REST API Call and save into WEBTHINGS_HANDLER
     * -> WebThingList
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public void addOpenhabThings() throws IOException, InterruptedException {
        List<ThingDTO> openhabThings;

        int count = 0;
        int maxTries = 4;

        // Get openHAB things via API
        while(true){
            try {
                openhabThings = getAllOpenhabThings();
                break;
            } catch (IOException | JsonSyntaxException e) {
                if(++count == maxTries){
                    throw e;
                } 
                Thread.sleep(2500);
            }
        }

        List<Thing> wotThings = new ArrayList<Thing>();
        for (ThingDTO thingDTO : openhabThings) {

            // Create a WebThing for all imported openHAB things which are not part of this binding || Property for each channel of openHAB thing
            if(config.allThings){
                if(!thingDTO.thingTypeUID.equals("webthings:connector") && !thingDTO.thingTypeUID.equals("webthings:server")){
                    wotThings.add(converter.createCustomThing(thingDTO));
                }

            // Create a WebThing for all selected openHAB things || Property for each channel of openHAB thing
            } else{
                for(Object option: config.things){
                    if(option.toString().equals(thingDTO.UID)){
                        wotThings.add(converter.createCustomThing(thingDTO));
                        continue;
                    }
                }
            }
        }

        // Add WebThings to webThingList
        if (!wotThings.isEmpty()) {
            converter.addThingList(wotThings);
        }
    }

    /**
     * Import all complete openHAB Things via REST API Call and save into WEBTHINGS_HANDLER
     * Will be done if all linked items shall be added as property
     * -> WebThingList
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public void addEnrichedOpenhabThings() throws IOException, InterruptedException {
        List<CompleteThingDTO> openhabThings;

        int count = 0;
        int maxTries = 4;

        // Get complete openHAB things via API
        while(true){
            try {
                openhabThings = getAllCompleteOpenhabThings();
                break;
            } catch (IOException | JsonSyntaxException e) {
                if(++count == maxTries){
                    throw e;
                } 
                Thread.sleep(2500);
            }
        }

        // get all openHAB items via API
        List<ItemDTO> openhabItems;
        count = 0;
        while(true){
            try {
                openhabItems = getAllOpenhabItems();
                break;
            } catch (IOException | JsonSyntaxException e) {
                if(++count == maxTries){
                    throw e;
                } 
                Thread.sleep(2500);
            }
        }

        // Save items in map to make them accessible via their name
        Map<String, ItemDTO> openhabItemsMap = new HashMap<String, ItemDTO>();
        for(ItemDTO item: openhabItems){
            openhabItemsMap.put(item.name, item);
        }

        List<Thing> wotThings = new ArrayList<Thing>();
        for(CompleteThingDTO thingDTO: openhabThings){

            // Create a WebThing for all imported openHAB things which are not part of this binding || Property for each item linked to openHAB thing
            if(config.allThings){
                if(!thingDTO.thingTypeUID.equals("webthings:connector") && !thingDTO.thingTypeUID.equals("webthings:server")){
                    wotThings.add(converter.createCustomThing(thingDTO, openhabItemsMap));
                }

            // Create a WebThing for all selected openHAB things || Property for each item linked to openHAB thing
            } else{
                for(Object option: config.things){
                    if(option.toString().equals(thingDTO.UID)){
                        wotThings.add(converter.createCustomThing(thingDTO, openhabItemsMap));
                        continue;
                    }
                }
            }
        }

        // Add WebThings to webThingList
        if(!wotThings.isEmpty()){
            converter.addThingList(wotThings);
        }
    }

    /**
     * Get Config of Server
     * @return config
     */
    @Nullable
    public WebThingsServerConfiguration getHandlerConfig(){
        return config;
    }
}
