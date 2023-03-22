/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Logic to create an InfluxDB {@link InfluxPoint} from an openHAB {@link Item}
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class ItemToStorePointCreator {
    private final InfluxDBConfiguration configuration;
    private final InfluxDBMetadataService influxDBMetadataService;

    public ItemToStorePointCreator(InfluxDBConfiguration configuration,
            InfluxDBMetadataService influxDBMetadataService) {
        this.configuration = configuration;
        this.influxDBMetadataService = influxDBMetadataService;
    }

    public @Nullable InfluxPoint convert(Item item, @Nullable String storeAlias) {
        if (item.getState() instanceof UnDefType) {
            return null;
        }

        String measurementName = calculateMeasurementName(item, storeAlias);
        String itemName = item.getName();
        State state = getItemState(item);

        Object value = InfluxDBStateConvertUtils.stateToObject(state);

        InfluxPoint.Builder pointBuilder = InfluxPoint.newBuilder(measurementName).withTime(Instant.now())
                .withValue(value).withTag(TAG_ITEM_NAME, itemName);

        addPointTags(item, pointBuilder);

        return pointBuilder.build();
    }

    private String calculateMeasurementName(Item item, @Nullable String storeAlias) {
        String name = storeAlias != null && !storeAlias.isBlank() ? storeAlias : item.getName();
        name = influxDBMetadataService.getMeasurementNameOrDefault(item.getName(), name);

        if (configuration.isReplaceUnderscore()) {
            name = name.replace('_', '.');
        }

        return name;
    }

    private State getItemState(Item item) {
        return calculateDesiredTypeConversionToStore(item)
                .map(desiredClass -> Objects.requireNonNullElseGet(item.getStateAs(desiredClass), item::getState))
                .orElseGet(item::getState);
    }

    private Optional<Class<? extends State>> calculateDesiredTypeConversionToStore(Item item) {
        return item.getAcceptedCommandTypes().stream().filter(commandType -> commandType.isAssignableFrom(State.class))
                .findFirst().map(commandType -> commandType.asSubclass(State.class));
    }

    private void addPointTags(Item item, InfluxPoint.Builder pointBuilder) {
        if (configuration.isAddCategoryTag()) {
            String categoryName = Objects.requireNonNullElse(item.getCategory(), "n/a");
            pointBuilder.withTag(TAG_CATEGORY_NAME, categoryName);
        }

        if (configuration.isAddTypeTag()) {
            pointBuilder.withTag(TAG_TYPE_NAME, item.getType());
        }

        if (configuration.isAddLabelTag()) {
            String labelName = Objects.requireNonNullElse(item.getLabel(), "n/a");
            pointBuilder.withTag(TAG_LABEL_NAME, labelName);
        }

        influxDBMetadataService.getMetaData(item.getName())
                .ifPresent(metadata -> metadata.getConfiguration().forEach(pointBuilder::withTag));
    }
}
