/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb2.internal;

import static org.openhab.persistence.influxdb2.internal.InfluxDBConstants.*;

import java.time.Instant;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

/**
 * Logic to create an InfluxDB {@link Point} from an openHAB {@link Item}
 *
 * @author Joan Pujol Espinar - Initial contribution
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

    public @Nullable Point convert(Item item, @Nullable String storeAlias) {
        if (item.getState() instanceof UnDefType) {
            return null;
        }

        String measurementName = calculateMeasurementName(item, storeAlias);
        String itemName = item.getName();
        State state = getItemState(item);

        Object value = InfluxDBStateConvertUtils.stateToObject(state);

        Point point = Point.measurement(measurementName).time(Instant.now(), WritePrecision.MS);

        setPointValue(value, point);

        point.addTag(TAG_ITEM_NAME, itemName);

        addPointTags(item, point);

        return point;

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
        final @NonNull State state;
        final Class<? extends State> desiredConversion = calculateDesiredTypeConversionToStore(item);
        if (desiredConversion != null) {
            State convertedState = item.getStateAs(desiredConversion);
            if (convertedState != null)
                state = convertedState;
            else
                state = item.getState();
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

    private void setPointValue(@Nullable Object value, Point point) {
        if (value instanceof String)
            point.addField(COLUMN_VALUE_NAME, (String) value);
        else if (value instanceof Number)
            point.addField(COLUMN_VALUE_NAME, (Number) value);
        else if (value instanceof Boolean)
            point.addField(COLUMN_VALUE_NAME, (Boolean) value);
        else if (value == null)
            point.addField(COLUMN_VALUE_NAME, (String) null);
        else
            throw new RuntimeException("Not expected value type");
    }

    private void addPointTags(Item item, Point point) {
        if (configuration.isAddCategoryTag()) {
            point.addTag(TAG_CATEGORY_NAME, Optional.ofNullable(item.getCategory()).orElse("n/a"));
        }

        if (configuration.isAddTypeTag()) {
            point.addTag(TAG_TYPE_NAME, item.getType());
        }

        if (configuration.isAddLabelTag()) {
            point.addTag(TAG_LABEL_NAME, Optional.ofNullable(item.getLabel()).orElse("n/a"));
        }

        final MetadataRegistry currentMetadataRegistry = metadataRegistry;
        if (currentMetadataRegistry != null) {
            MetadataKey key = new MetadataKey(InfluxDB2PersistenceService.SERVICE_NAME, item.getName());
            Metadata metadata = currentMetadataRegistry.get(key);
            if (metadata != null) {
                metadata.getConfiguration().forEach((tagName, tagValue) -> {
                    point.addTag(tagName, tagValue.toString());
                });
            }
        }
    }

}