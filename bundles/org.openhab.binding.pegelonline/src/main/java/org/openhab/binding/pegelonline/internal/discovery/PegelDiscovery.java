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
package org.openhab.binding.pegelonline.internal.discovery;

import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.pegelonline.internal.dto.Station;
import org.openhab.binding.pegelonline.internal.handler.PegelOnlineHandler;
import org.openhab.binding.pegelonline.internal.utils.Utils;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PegelDiscovery} Discovery of measurement stations
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.pegelonline")
public class PegelDiscovery extends AbstractDiscoveryService implements ThingHandlerService {
    private final Logger logger = LoggerFactory.getLogger(PegelDiscovery.class);
    private Optional<PegelOnlineHandler> handler = Optional.empty();
    private PointType homeLocation = UNDEF_LOCATION;
    private HttpClientFactory httpClientFactory;

    @Activate
    public PegelDiscovery(final @Reference HttpClientFactory hcf, final @Reference LocationProvider lp) {
        super(SUPPORTED_THING_TYPES_UIDS, 10, false);
        httpClientFactory = hcf;
        PointType location = lp.getLocation();
        if (location != null) {
            homeLocation = location;
        } else {
            logger.debug("No home location found");
        }
    }

    @Override
    protected void startScan() {
        double homeLat = homeLocation.getLatitude().doubleValue();
        double homeLon = homeLocation.getLongitude().doubleValue();
        try {
            ContentResponse cr = httpClientFactory.getCommonHttpClient().GET(STATIONS_URI);
            Station[] stationArray = GSON.fromJson(cr.getContentAsString(), Station[].class);
            if (stationArray != null) {
                for (Station station : stationArray) {
                    double distance = Utils.calculateDistance(homeLat, homeLon, station.latitude, station.longitude);
                    if (distance < DISCOVERY_RADIUS) {
                        logger.trace("Station in range {},{}", station.longname, station.water.shortname);
                        reportResult(station);
                    }
                }
            } else {
                logger.trace("No stations found in discovery");
            }
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            logger.trace("Exception during station discovery: {}", e.getMessage());
        }
    }

    public void reportResult(Station s) {
        String label = "Pegel Station " + Utils.toTitleCase(s.shortname) + " / " + Utils.toTitleCase(s.water.shortname);
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("agency", s.agency);
        properties.put("km", s.km);
        properties.put("river", s.water.longname);
        properties.put("station", s.longname);
        properties.put("uuid", s.uuid);
        properties.put("location", s.latitude + "," + s.longitude);
        ThingUID uid = new ThingUID(STATION_THING, s.uuid);
        thingDiscovered(DiscoveryResultBuilder.create(uid).withRepresentationProperty("uuid").withLabel(label)
                .withProperties(properties).build());
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void setThingHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof PegelOnlineHandler pegelOnlineHandler) {
            handler = Optional.of(pegelOnlineHandler);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler.orElse(null);
    }
}
