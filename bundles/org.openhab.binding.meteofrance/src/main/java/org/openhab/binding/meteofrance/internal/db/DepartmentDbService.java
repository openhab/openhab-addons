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
package org.openhab.binding.meteofrance.internal.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PointType;
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
 * The {@link DepartmentDbService} makes available a list of known French Metropolitan departments.
 *
 * @author Gaël L'hopital - Initial Contribution
 */

@Component(service = DepartmentDbService.class)
@NonNullByDefault
public class DepartmentDbService {
    private final Logger logger = LoggerFactory.getLogger(DepartmentDbService.class);
    private final List<Department> departments = new ArrayList<>();

    public record Department(String id, String name, double northestLat, double southestLat, double eastestLon,
            double westestLon) {

        boolean contains(double latitude, double longitude) {
            return northestLat >= latitude && southestLat <= latitude && //
                    westestLon <= longitude && eastestLon >= longitude;
        }
    }

    @Activate
    public DepartmentDbService() {
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("/db/departments.json");
                Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            departments.addAll(Arrays.asList(gson.fromJson(reader, Department[].class)));
            logger.debug("Successfully loaded {} French departments", departments.size());
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            logger.warn("Unable to load departments list: {}", e.getMessage());
        }
    }

    public List<Department> getBounding(PointType location) {
        double latitude = location.getLatitude().doubleValue();
        double longitude = location.getLongitude().doubleValue();
        return departments.stream().filter(dep -> dep.contains(latitude, longitude)).toList();
    }
}
