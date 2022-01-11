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
package org.openhab.persistence.influxdb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.persistence.influxdb.InfluxDBPersistenceService;

/**
 * Logic to use items metadata from an openHAB {@link Item}
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class InfluxDBMetadataUtils {

    private InfluxDBMetadataUtils() {
    }

    public static String calculateMeasurementNameFromMetadataIfPresent(
            final @Nullable MetadataRegistry currentMetadataRegistry, String name, @Nullable String itemName) {

        if (itemName == null || currentMetadataRegistry == null) {
            return name;
        }

        MetadataKey key = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, itemName);
        Metadata metadata = currentMetadataRegistry.get(key);
        if (metadata != null) {
            String metaName = metadata.getValue();
            if (!metaName.isBlank()) {
                name = metaName;
            }
        }

        return name;
    }
}
