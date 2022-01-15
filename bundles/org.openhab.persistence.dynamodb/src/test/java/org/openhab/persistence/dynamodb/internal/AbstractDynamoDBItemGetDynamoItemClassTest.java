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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;

/**
 * Test for AbstractDynamoDBItem.getDynamoItemClass
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class AbstractDynamoDBItemGetDynamoItemClassTest {

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testCallItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBStringItem.class, AbstractDynamoDBItem.getDynamoItemClass(CallItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testContactItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBBigDecimalItem.class, AbstractDynamoDBItem.getDynamoItemClass(ContactItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testDateTimeItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBStringItem.class, AbstractDynamoDBItem.getDynamoItemClass(DateTimeItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testStringItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBStringItem.class, AbstractDynamoDBItem.getDynamoItemClass(StringItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testLocationItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBStringItem.class, AbstractDynamoDBItem.getDynamoItemClass(LocationItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testNumberItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBBigDecimalItem.class, AbstractDynamoDBItem.getDynamoItemClass(NumberItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testColorItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBStringItem.class, AbstractDynamoDBItem.getDynamoItemClass(ColorItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testDimmerItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBBigDecimalItem.class, AbstractDynamoDBItem.getDynamoItemClass(DimmerItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testPlayerItem(boolean legacy) throws IOException {
        assertEquals(legacy ? DynamoDBStringItem.class : DynamoDBBigDecimalItem.class,
                AbstractDynamoDBItem.getDynamoItemClass(PlayerItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testRollershutterItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBBigDecimalItem.class,
                AbstractDynamoDBItem.getDynamoItemClass(RollershutterItem.class, legacy));
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    public void testOnOffTypeWithSwitchItem(boolean legacy) throws IOException {
        assertEquals(DynamoDBBigDecimalItem.class, AbstractDynamoDBItem.getDynamoItemClass(SwitchItem.class, legacy));
    }
}
