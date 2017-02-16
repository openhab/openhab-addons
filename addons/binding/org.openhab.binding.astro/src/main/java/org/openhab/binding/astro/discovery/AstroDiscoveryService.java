/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.discovery;

import static org.openhab.binding.astro.AstroBindingConstants.*;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AstroDiscoveryService} tries to automatically discover the geolocation based on the internet IP address.
 *
 * @author Gerhard Riegler
 */
public class AstroDiscoveryService extends AbstractDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(AstroDiscoveryService.class);
    private static final int DISCOVER_TIMEOUT_SECONDS = 30;

    /**
     * Creates a AstroDiscoveryService with disabled autostart.
     */
    public AstroDiscoveryService() {
        super(ImmutableSet.of(new ThingTypeUID(BINDING_ID, "-")), DISCOVER_TIMEOUT_SECONDS, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void startScan() {
        logger.debug("Starting Astro discovery scan");
        String result = null;
        try {
            result = HttpUtil.executeUrl("GET", "http://ip-api.com/json/?fields=lat,lon", 5000);
        } catch (IOException e) {
            logger.warn("Can't get latitude and longitude for the current location: {}", e);
        }

        if (result != null) {

            String lat = StringUtils.trim(StringUtils.substringBetween(result, "\"lat\":", ","));
            String lon = StringUtils.trim(StringUtils.substringBetween(result, "\"lon\":", "}"));

            try {
                Double latitude = Double.parseDouble(lat);
                Double longitude = Double.parseDouble(lon);

                logger.info("Evaluated Astro geolocation: latitude: {}, longitude: {}", latitude, longitude);

                ThingTypeUID sunType = new ThingTypeUID(BINDING_ID, SUN);
                ThingTypeUID moonType = new ThingTypeUID(BINDING_ID, MOON);

                ThingUID sunThing = new ThingUID(sunType, LOCAL);
                ThingUID moonThing = new ThingUID(moonType, LOCAL);

                String propGeolocation = String.format("%s,%s", latitude, longitude);
                thingDiscovered(DiscoveryResultBuilder.create(sunThing).withLabel("Local Sun")
                        .withProperty("geolocation", propGeolocation).build());
                thingDiscovered(DiscoveryResultBuilder.create(moonThing).withLabel("Local Moon")
                        .withProperty("geolocation", propGeolocation).build());
            } catch (Exception ex) {
                logger.warn("Can't discover Astro geolocation");
            }
        }
    }
}
