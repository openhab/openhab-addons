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
package org.openhab.persistence.dynamodb.internal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

/**
 * DynamoDBItem for items that can be serialized as DynamoDB number
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class DynamoDBBigDecimalItem extends AbstractDynamoDBItem<BigDecimal> {

    private static Class<@Nullable BigDecimal> NULLABLE_BIGDECIMAL = (Class<@Nullable BigDecimal>) BigDecimal.class;

    public static StaticTableSchema<DynamoDBBigDecimalItem> TABLE_SCHEMA_LEGACY = getBaseSchemaBuilder(
            DynamoDBBigDecimalItem.class, true).newItemSupplier(DynamoDBBigDecimalItem::new)
                    .addAttribute(NULLABLE_BIGDECIMAL, a -> a.name(ATTRIBUTE_NAME_ITEMSTATE_LEGACY)
                            .getter(DynamoDBBigDecimalItem::getState).setter(DynamoDBBigDecimalItem::setState))
                    .build();

    public static StaticTableSchema<DynamoDBBigDecimalItem> TABLE_SCHEMA_NEW = getBaseSchemaBuilder(
            DynamoDBBigDecimalItem.class, false)
                    .newItemSupplier(DynamoDBBigDecimalItem::new)
                    .addAttribute(NULLABLE_BIGDECIMAL,
                            a -> a.name(ATTRIBUTE_NAME_ITEMSTATE_NUMBER).getter(DynamoDBBigDecimalItem::getState)
                                    .setter(DynamoDBBigDecimalItem::setState))
                    .addAttribute(NULLABLE_LONG, a -> a.name(ATTRIBUTE_NAME_EXPIRY)
                            .getter(AbstractDynamoDBItem::getExpiryDate).setter(AbstractDynamoDBItem::setExpiry))
                    .build();

    /**
     * We get the following error if the BigDecimal has too many digits
     * "Attempting to store more than 38 significant digits in a Number"
     *
     * See "Data types" section in
     * http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Limits.html
     */
    private static final int MAX_DIGITS_SUPPORTED_BY_AMAZON = 38;

    public DynamoDBBigDecimalItem() {
        this("", null, ZonedDateTime.now(), null);
    }

    public DynamoDBBigDecimalItem(String name, @Nullable BigDecimal state, ZonedDateTime time,
            @Nullable Integer expireDays) {
        super(name, state, time, expireDays);
    }

    @Override
    public @Nullable BigDecimal getState() {
        // When serializing this to the wire, we round the number in order to ensure
        // that it is within the dynamodb limits
        BigDecimal localState = state;
        if (localState == null) {
            return null;
        }
        return loseDigits(localState);
    }

    @Override
    public void setState(@Nullable BigDecimal state) {
        this.state = state;
    }

    @Override
    public <T> T accept(DynamoDBItemVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static BigDecimal loseDigits(BigDecimal number) {
        return number.round(new MathContext(MAX_DIGITS_SUPPORTED_BY_AMAZON));
    }
}
