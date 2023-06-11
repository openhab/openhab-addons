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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria;
import org.openhab.core.persistence.FilterCriteria.Ordering;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class TestStoreMixedTypesTest extends BaseIntegrationTest {

    public static final boolean LEGACY_MODE = false;

    private static final AtomicInteger testCounter = new AtomicInteger();
    private int uniqueId;

    private String getItemName() {
        return "localItem" + uniqueId;
    }

    @BeforeEach
    public void generateUniqueItemId() {
        uniqueId = testCounter.getAndIncrement();
    }

    @AfterEach
    public void tearDownLocalItems() {
        ITEMS.remove(getItemName());
    }

    @SuppressWarnings("null")
    public void storeItemWithDifferentTypes() {

        try {
            // First writing two values with string item
            {
                StringItem item = new StringItem(getItemName());
                ITEMS.put(getItemName(), item);
                item.setState(StringType.valueOf("a1"));
                service.store(item);
                Thread.sleep(10);

                item.setState(StringType.valueOf("b1"));
                service.store(item);
                Thread.sleep(10);
            }
            // then writing with same item but numbers
            {
                NumberItem item = new NumberItem(getItemName());
                assert item != null;
                ITEMS.put(getItemName(), item);
                item.setState(DecimalType.valueOf("33.14"));
                service.store(item);
                Thread.sleep(10);
                item.setState(DecimalType.valueOf("66.28"));
                service.store(item);
                Thread.sleep(10);
            }
            // finally some switch values
            {
                SwitchItem item = new SwitchItem(getItemName());
                assert item != null;
                ITEMS.put(getItemName(), item);
                item.setState(OnOffType.ON);
                service.store(item);
                Thread.sleep(10);
                item.setState(OnOffType.OFF);
                service.store(item);
                Thread.sleep(10);
            }
            // Player
            {
                PlayerItem item = new PlayerItem(getItemName());
                assert item != null;
                ITEMS.put(getItemName(), item);
                item.setState(PlayPauseType.PAUSE);
                service.store(item);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    /**
     * Test where first data is stored with various item types, some serialized as numbers and some as strings.
     *
     * - Querying with NumberItem returns data that have been persisted using DynamoDBBigDecimalItem DTO, that is,
     * NumberItem and SwitchItem data
     * - Querying with StringItem returns data that have been persisted using DynamoDBStringItem DTO (StringItem and
     * PlayerItem)
     * - Querying with SwitchItem returns data that have been persisted using DynamoDBBigDecimalItem DTO. All numbers
     * are converted to OnOff (ON if nonzero)
     * - Querying with PlayerItem returns data that have been persisted using DynamoDBStringItem DTO. However, some
     * values are not convertible to PlayPauseType are ignored (warning logged).
     *
     */
    @Test
    public void testQueryAllItemTypeChanged() {
        storeItemWithDifferentTypes();
        {
            NumberItem item = new NumberItem(getItemName());
            ITEMS.put(getItemName(), item);
            waitForAssert(() -> {
                assertQueryAll(getItemName(), expectedNumberItem());
            });
        }
        {
            SwitchItem item = new SwitchItem(getItemName());
            ITEMS.put(getItemName(), item);
            waitForAssert(() -> {
                assertQueryAll(getItemName(), expectedSwitchItem());
            });
        }
        {
            StringItem item = new StringItem(getItemName());
            ITEMS.put(getItemName(), item);
            waitForAssert(() -> {
                assertQueryAll(getItemName(), expectedStringItem());
            });
        }
        {
            PlayerItem item = new PlayerItem(getItemName());
            assert item != null;
            ITEMS.put(getItemName(), item);
            waitForAssert(() -> {
                assertQueryAll(getItemName(), expectedPlayerItem());
            });
        }
    }

    protected PlayPauseType[] expectedPlayerItem() {
        return new PlayPauseType[] { /* ON=1=PLAY */PlayPauseType.PLAY, /* OFF=0=PAUSE */PlayPauseType.PAUSE,
                PlayPauseType.PAUSE };
    }

    protected StringType[] expectedStringItem() {
        return new StringType[] { StringType.valueOf("a1"), StringType.valueOf("b1") };
    }

    protected OnOffType[] expectedSwitchItem() {
        return new OnOffType[] { /* 33.14 */OnOffType.ON, /* 66.28 */ OnOffType.ON, OnOffType.ON, OnOffType.OFF,
                /* pause */ OnOffType.OFF };
    }

    protected DecimalType[] expectedNumberItem() {
        return new DecimalType[] { DecimalType.valueOf("33.14"), DecimalType.valueOf("66.28"),
                /* on */DecimalType.valueOf("1"), /* off */DecimalType.valueOf("0"),
                /* pause */DecimalType.valueOf("0") };
    }

    private void assertQueryAll(String item, State[] expectedStates) {
        FilterCriteria criteria = new FilterCriteria();
        criteria.setOrdering(Ordering.ASCENDING);
        criteria.setItemName(item);
        @SuppressWarnings("null")
        Iterable<HistoricItem> iterable = BaseIntegrationTest.service.query(criteria);
        List<State> actualStatesList = new ArrayList<>();
        iterable.forEach(i -> actualStatesList.add(i.getState()));
        State[] actualStates = actualStatesList.toArray(new State[0]);
        assertArrayEquals(expectedStates, actualStates, Arrays.toString(actualStates));
    }
}
