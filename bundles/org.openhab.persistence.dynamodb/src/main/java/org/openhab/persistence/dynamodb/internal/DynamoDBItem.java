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
package org.openhab.persistence.dynamodb.internal;

import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.HistoricItem;

/**
 * Represents openHAB Item serialized in a suitable format for the database
 *
 * @param <T> Type of the state as accepted by the AWS SDK.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface DynamoDBItem<T> {

    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    static final String ATTRIBUTE_NAME_TIMEUTC_LEGACY = "timeutc";
    static final String ATTRIBUTE_NAME_ITEMNAME_LEGACY = "itemname";
    static final String ATTRIBUTE_NAME_ITEMSTATE_LEGACY = "itemstate";
    static final String ATTRIBUTE_NAME_TIMEUTC = "t";
    static final String ATTRIBUTE_NAME_ITEMNAME = "i";
    static final String ATTRIBUTE_NAME_ITEMSTATE_STRING = "s";
    static final String ATTRIBUTE_NAME_ITEMSTATE_NUMBER = "n";
    static final String ATTRIBUTE_NAME_EXPIRY = "exp";

    /**
     * Convert this AbstractDynamoItem as HistoricItem, i.e. converting serialized state back to openHAB state.
     *
     * Returns null when this instance has null state.
     *
     * If item is NumberItem and has an unit, the data is converted to QuantityType with item.getUnit().
     *
     * @param item Item representing this item. Used to determine item type.
     * @return HistoricItem representing this DynamoDBItem.
     */
    @Nullable
    HistoricItem asHistoricItem(Item item);

    /**
     * Convert this AbstractDynamoItem as HistoricItem.
     *
     * Returns null when this instance has null state.
     * The implementation can deal with legacy schema as well.
     *
     * Use this method when repeated calls are expected for same item (avoids the expensive call to item.getUnit())
     *
     * @param item Item representing this item. Used to determine item type.
     * @param targetUnit unit to convert the data if item is with Dimension. Has only effect with NumberItems and with
     *            numeric DynamoDBItems.
     * @return HistoricItem representing this DynamoDBItem.
     */
    @Nullable
    HistoricItem asHistoricItem(Item item, @Nullable Unit<?> targetUnit);

    /**
     * Get item name
     *
     * @return item name
     */
    String getName();

    /**
     * Get item state, in the serialized format
     *
     * @return item state as serialized format
     */
    @Nullable
    T getState();

    /**
     * Get timestamp of this value
     *
     * @return timestamp
     */
    ZonedDateTime getTime();

    /**
     * Get expire time for the DynamoDB item in days.
     *
     * Does not have any effect with legacy schema.
     *
     * Also known as time-to-live or TTL.
     * Null means that expire is disabled
     *
     * @return expire time in days
     */
    @Nullable
    Integer getExpireDays();

    /**
     * Get expiry date for the DynamoDB item in epoch seconds
     *
     * This is used with DynamoDB Time to Live TTL feature.
     *
     * @return expiry date of the data. Equivalent to getTime() + getExpireDays() or null when expireDays is null.
     */
    @Nullable
    Long getExpiryDate();

    /**
     * Setter for item name
     *
     * @param name item name
     */
    void setName(String name);

    /**
     * Setter for serialized state
     *
     * @param state serialized state
     */
    void setState(@Nullable T state);

    /**
     * Set timestamp of the data
     *
     * @param time timestamp
     */
    void setTime(ZonedDateTime time);

    /**
     * Set expire time for the DynamoDB item in days.
     *
     * Does not have any effect with legacy schema.
     *
     * Also known as time-to-live or TTL.
     * Use null to disable expiration
     *
     * @param expireDays expire time in days. Should be positive or null.
     *
     */
    void setExpireDays(@Nullable Integer expireDays);

    <R> R accept(DynamoDBItemVisitor<R> visitor);
}
