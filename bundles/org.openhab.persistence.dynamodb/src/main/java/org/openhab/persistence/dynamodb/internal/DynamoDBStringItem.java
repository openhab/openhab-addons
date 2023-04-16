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
package org.openhab.persistence.dynamodb.internal;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

/**
 * DynamoDBItem for items that can be serialized as DynamoDB string
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class DynamoDBStringItem extends AbstractDynamoDBItem<String> {

    private static Class<@Nullable String> NULLABLE_STRING = (Class<@Nullable String>) String.class;

    public static final StaticTableSchema<DynamoDBStringItem> TABLE_SCHEMA_LEGACY = getBaseSchemaBuilder(
            DynamoDBStringItem.class, true).newItemSupplier(DynamoDBStringItem::new)
            .addAttribute(NULLABLE_STRING, a -> a.name(DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_LEGACY)
                    .getter(DynamoDBStringItem::getState).setter(DynamoDBStringItem::setState))
            .build();

    public static final StaticTableSchema<DynamoDBStringItem> TABLE_SCHEMA_NEW = getBaseSchemaBuilder(
            DynamoDBStringItem.class, false)
            .newItemSupplier(DynamoDBStringItem::new)
            .addAttribute(NULLABLE_STRING,
                    a -> a.name(DynamoDBItem.ATTRIBUTE_NAME_ITEMSTATE_STRING).getter(DynamoDBStringItem::getState)
                            .setter(DynamoDBStringItem::setState))
            .addAttribute(NULLABLE_LONG, a -> a.name(ATTRIBUTE_NAME_EXPIRY).getter(AbstractDynamoDBItem::getExpiryDate)
                    .setter(AbstractDynamoDBItem::setExpiry))
            .build();

    public DynamoDBStringItem() {
        this("", null, ZonedDateTime.now(), null);
    }

    public DynamoDBStringItem(String name, @Nullable String state, ZonedDateTime time, @Nullable Integer expireDays) {
        super(name, state, time, expireDays);
    }

    @Override
    public @Nullable String getState() {
        return state;
    }

    @Override
    public void setState(@Nullable String state) {
        this.state = state;
    }

    @Override
    public <T> T accept(DynamoDBItemVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
