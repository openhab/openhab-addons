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
package org.openhab.binding.webthings.internal.components;

import static org.openhab.binding.webthings.internal.WebThingsBindingConstants.*;
import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.*;
import static org.openhab.binding.webthings.internal.utilities.WebThingsRestApiUtilities.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WebThingsConfigOptionProvider} is responsible to provide config options dynamically
 *
 * @author schneider_sven - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.webthings", service = ConfigOptionProvider.class)
public class WebThingsConfigOptionProvider implements ConfigOptionProvider {
    private final Logger logger = LoggerFactory.getLogger(WebThingsConfigOptionProvider.class);

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable Locale locale) {
        if(!"thing-type".equals(uri.getScheme())){
            return null;
        }
        if(uri.getSchemeSpecificPart().equals(THING_TYPE_SERVER.getAsString()) && param.equals(CHANNEL_THINGS)){
            List<ParameterOption> options = new ArrayList<ParameterOption>();
            List<ThingDTO> openhabThings = new ArrayList<ThingDTO>();

            int count = 0;
            int maxTries = 4;
            // Import openHAB things via API 
            while(true){
                try {
                    openhabThings = getAllOpenhabThings();
                    break;
                } catch (IOException | JsonSyntaxException e) {
                    if(++count == maxTries){
                        logger.error("Could not import openHAB Things to provide config options - {}", e.getMessage());
                        return null;
                    } 
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e1) {
                        logger.error("Could not import openHAB Things to provide config options - {}", e.getMessage());
                    }
                }
            }

            if(!openhabThings.isEmpty()){
                // Add ParameterOption for every openHAB thing
                for(ThingDTO thing: openhabThings){
                    options.add(new ParameterOption(thing.UID,thing.UID));
                }
                return options;
            }
            // If import via API was unsuccessful try directly from database
            else{
                Map<String,Object> backupImport = null;
                String file = null;

                // Check if custom path was set and build db string
                if(system != null){
                    file = system + "/jsondb/org.eclipse.smarthome.core.thing.Thing.json";
                }else{
                    file = userdataPath + "/jsondb/org.eclipse.smarthome.core.thing.Thing.json";
                }

                // Import things from jsondb
                try{
                    backupImport = new Gson().fromJson(new FileReader(file), new TypeToken<Map<String,Object>>(){}.getType());
                }catch (FileNotFoundException e){
                    logger.error("File not found");
                    return null;
                }

                // Add ParameterOption for every openHAB thing
                for(String s : backupImport.keySet()){
                    options.add(new ParameterOption(s, s));
                }
                return options;
            }
        }
        return null;
    }
}
