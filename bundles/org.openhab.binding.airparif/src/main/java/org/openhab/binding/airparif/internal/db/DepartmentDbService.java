/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PointType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link DepartmentDbService} makes available a list of known French Metropolitan departments.
 *
 * @author Gaël L'hopital - Initial Contribution
 */

@Component(service = DepartmentDbService.class)
@NonNullByDefault
public class DepartmentDbService {
    private final Logger logger = LoggerFactory.getLogger(DepartmentDbService.class);
    private List<Department> departments = List.of();
    private volatile boolean dbLoadAttempted;

    public record Department(String id, String name, double northestLat, double southestLat, double eastestLon,
            double westestLon) {

        boolean contains(double latitude, double longitude) {
            return northestLat >= latitude && southestLat <= latitude && westestLon <= longitude
                    && eastestLon >= longitude;
        }
    }

    private void loadDB() {
        try (InputStream stream = DepartmentDbService.class.getResourceAsStream("/db/departments.json");
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            if (stream == null) {
                throw new IllegalStateException("Resource /db/departments.json not found");
            }
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            Department[] parsed = gson.fromJson(reader, Department[].class);
            departments = Arrays.asList(parsed);
            logger.debug("Loaded {} French departments", departments.size());
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            logger.warn("Unable to load departments list: {}", e.getMessage());
        }
    }

    private Stream<Department> getDeptStream() {
        if (!dbLoadAttempted) {
            synchronized (this) {
                if (!dbLoadAttempted) {
                    dbLoadAttempted = true;
                    loadDB();
                }
            }
        }
        return departments.stream();
    }

    public List<Department> getBounding(PointType location) {
        double latitude = location.getLatitude().doubleValue();
        double longitude = location.getLongitude().doubleValue();
        return getDeptStream().filter(dep -> dep.contains(latitude, longitude)).toList();
    }
}
