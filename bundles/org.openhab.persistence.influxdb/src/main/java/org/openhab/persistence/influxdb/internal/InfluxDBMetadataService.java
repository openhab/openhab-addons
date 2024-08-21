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
package org.openhab.persistence.influxdb.internal;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.persistence.influxdb.InfluxDBPersistenceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Utility service for using item metadata in InfluxDB
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(service = InfluxDBMetadataService.class)
public class InfluxDBMetadataService {
    private final MetadataRegistry metadataRegistry;

    @Activate
    public InfluxDBMetadataService(@Reference MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * get the measurement name from the item metadata or return the provided default
     *
     * @param itemName the item name
     * @param defaultName the default measurement name (
     * @return the metadata measurement name if present, defaultName otherwise
     */
    public String getMeasurementNameOrDefault(String itemName, String defaultName) {
        Optional<Metadata> metadata = getMetaData(itemName);
        if (metadata.isPresent()) {
            String metaName = metadata.get().getValue();
            if (!metaName.isBlank()) {
                return metaName;
            }
        }

        return defaultName;
    }

    /**
     * get an Optional of the metadata for an item
     *
     * @param itemName the item name
     * @return Optional with the metadata (may be empty)
     */
    public Optional<Metadata> getMetaData(String itemName) {
        MetadataKey key = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, itemName);
        return Optional.ofNullable(metadataRegistry.get(key));
    }
}
