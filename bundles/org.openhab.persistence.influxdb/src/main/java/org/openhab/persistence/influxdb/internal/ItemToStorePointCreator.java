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
package org.openhab.persistence.influxdb.internal;

import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.TAG_CATEGORY_NAME;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.TAG_ITEM_NAME;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.TAG_LABEL_NAME;
import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.TAG_TYPE_NAME;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.persistence.influxdb.InfluxDBPersistenceService;

/**
 * Logic to create an InfluxDB {@link InfluxPoint} from an openHAB {@link Item}
 *
 * @author Joan Pujol Espinar - Addon rewrite refactoring code and adding support for InfluxDB 2.0
 */
@NonNullByDefault
public class ItemToStorePointCreator {
    private final InfluxDBConfiguration configuration;
    @Nullable
    private final MetadataRegistry metadataRegistry;

    public ItemToStorePointCreator(InfluxDBConfiguration configuration, @Nullable MetadataRegistry metadataRegistry) {
        this.configuration = configuration;
        this.metadataRegistry = metadataRegistry;
    }

    public @Nullable InfluxPoint convert(Item item, @Nullable String storeAlias) {
        if (item.getState() instanceof UnDefType) {
            return null;
        }

        String measurementName = calculateMeasurementName(item, storeAlias);
        String itemName = item.getName();
        State state = getItemState(item);

        Object value = InfluxDBStateConvertUtils.stateToObject(state);

        InfluxPoint.Builder point = InfluxPoint.newBuilder(measurementName).withTime(Instant.now()).withValue(value)
                .withTag(TAG_ITEM_NAME, itemName);

        addPointTags(item, point);

        return point.build();
    }

    @SuppressWarnings("null")
    private String calculateMeasurementName(Item item, @Nullable String storeAlias) {
        String name;
        if (StringUtils.isNotBlank(storeAlias)) {
            name = storeAlias;
        } else {
            name = item.getName();
        }

        if (configuration.isReplaceUnderscore()) {
            name = name.replace('_', '.');
        }

        return name;
    }

    private State getItemState(Item item) {
        final State state;
        final Class<? extends State> desiredConversion = calculateDesiredTypeConversionToStore(item);
        if (desiredConversion != null) {
            State convertedState = item.getStateAs(desiredConversion);
            if (convertedState != null) {
                state = convertedState;
            } else {
                state = item.getState();
            }
        } else {
            state = item.getState();
        }
        return state;
    }

    private @Nullable Class<? extends State> calculateDesiredTypeConversionToStore(Item item) {
        Class<? extends State> conversion = null;
        if (item.getAcceptedCommandTypes().size() > 1) {
            if (item.getAcceptedCommandTypes().get(0).isAssignableFrom(State.class)) {
                conversion = item.getAcceptedCommandTypes().get(0).asSubclass(State.class);
            }
        }
        return conversion;
    }

    private void addPointTags(Item item, InfluxPoint.Builder point) {
        if (configuration.isAddCategoryTag()) {
            point.withTag(TAG_CATEGORY_NAME, Optional.ofNullable(item.getCategory()).orElse("n/a"));
        }

        if (configuration.isAddTypeTag()) {
            point.withTag(TAG_TYPE_NAME, item.getType());
        }

        if (configuration.isAddLabelTag()) {
            point.withTag(TAG_LABEL_NAME, Optional.ofNullable(item.getLabel()).orElse("n/a"));
        }

        final MetadataRegistry currentMetadataRegistry = metadataRegistry;
        if (currentMetadataRegistry != null) {
            MetadataKey key = new MetadataKey(InfluxDBPersistenceService.SERVICE_NAME, item.getName());
            Metadata metadata = currentMetadataRegistry.get(key);
            if (metadata != null) {
                metadata.getConfiguration().forEach((tagName, tagValue) -> {
                    point.withTag(tagName, tagValue.toString());
                });
            }
        }
    }
}