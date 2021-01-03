/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

    /**
     * Convert this AbstractDynamoItem as HistoricItem.
     *
     * Returns null when this instance has null state.
     *
     * @param item Item representing this item. Used to determine item type.
     * @return HistoricItem representing this DynamoDBItem.
     */
    @Nullable
    HistoricItem asHistoricItem(Item item);

    String getName();

    @Nullable
    T getState();

    ZonedDateTime getTime();

    void setName(String name);

    void setState(@Nullable T state);

    void setTime(ZonedDateTime time);

    void accept(DynamoDBItemVisitor visitor);
}
