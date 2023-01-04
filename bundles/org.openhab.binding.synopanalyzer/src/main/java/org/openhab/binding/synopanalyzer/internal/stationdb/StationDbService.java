/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.synopanalyzer.internal.stationdb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link StationDbService} makes available a list of known Synop stations.
 *
 * @author Gaël L'hopital - Initial Contribution
 */

@Component(service = StationDbService.class)
@NonNullByDefault
public class StationDbService {
    private final Logger logger = LoggerFactory.getLogger(StationDbService.class);

    private List<Station> stations;

    @Activate
    public StationDbService() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/db/stations.json");
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);) {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            stations = Arrays.asList(gson.fromJson(reader, Station[].class));
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            logger.warn("Unable to load station list : {}", e.getMessage());
            stations = List.of();
        }
    }

    public List<Station> getStations() {
        return stations;
    }
}
