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

import static org.openhab.persistence.influxdb.internal.InfluxDBConstants.*;

import java.time.Instant;
import java.util.Optional;

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
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class ItemToStorePointCreator {
    private final InfluxDBConfiguration configuration;
    private final @Nullable MetadataRegistry metadataRegistry;

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

    private String calculateMeasurementName(Item item, @Nullable String storeAlias) {
        String name = storeAlias != null && !storeAlias.isBlank() ? storeAlias : item.getName();

        name = InfluxDBMetadataUtils.calculateMeasurementNameFromMetadataIfPresent(metadataRegistry, name,
                item.getName());

        if (configuration.isReplaceUnderscore()) {
            name = name.replace('_', '.');
        }

        return name;
    }

    private State getItemState(Item item) {
        final State state;
        final Optional<Class<? extends State>> desiredConversion = calculateDesiredTypeConversionToStore(item);
        if (desiredConversion.isPresent()) {
            State convertedState = item.getStateAs(desiredConversion.get());
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

    private Optional<Class<? extends State>> calculateDesiredTypeConversionToStore(Item item) {
        return item.getAcceptedCommandTypes().stream().filter(commandType -> commandType.isAssignableFrom(State.class))
                .findFirst().map(commandType -> commandType.asSubclass(State.class));
    }

    private void addPointTags(Item item, InfluxPoint.Builder point) {
        if (configuration.isAddCategoryTag()) {
            String categoryName = item.getCategory();
            if (categoryName == null) {
                categoryName = "n/a";
            }
            point.withTag(TAG_CATEGORY_NAME, categoryName);
        }

        if (configuration.isAddTypeTag()) {
            point.withTag(TAG_TYPE_NAME, item.getType());
        }

        if (configuration.isAddLabelTag()) {
            String labelName = item.getLabel();
            if (labelName == null) {
                labelName = "n/a";
            }
            point.withTag(TAG_LABEL_NAME, labelName);
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
