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
package org.openhab.persistence.jdbc.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.persistence.jdbc.dto.ItemVO;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Tests the {@link NamingStrategy} class.
 *
 * @author Jacob Laursen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class NamingStrategyTest {
    private static final String ITEMS_MANAGE_TABLE_NAME = "items";

    private @Mock @NonNullByDefault({}) JdbcConfiguration configurationMock;
    private NamingStrategy namingStrategy = new NamingStrategy(configurationMock);

    @BeforeEach
    public void initialize() {
        final Logger logger = (Logger) LoggerFactory.getLogger(NamingStrategy.class);
        logger.setLevel(Level.OFF);
        namingStrategy = new NamingStrategy(configurationMock);
    }

    @Test
    public void getTableNameWhenUseRealItemNamesNameIsLowerCase() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(false).when(configurationMock).getTablePreserveCase();
        assertThat(namingStrategy.getTableName(1, "Test"), is("test"));
    }

    @Test
    public void getTableNameWhenUseRealItemNamesNameIsSameCase() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(true).when(configurationMock).getTablePreserveCase();
        assertThat(namingStrategy.getTableName(1, "Test"), is("Test"));
    }

    @Test
    public void getTableNameWhenUseRealItemNamesSpecialCharsAreRemoved() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(false).when(configurationMock).getTablePreserveCase();
        assertThat(namingStrategy.getTableName(1, "Te%st"), is("test"));
    }

    @Test
    public void getTableNameWhenNotUseRealItemNamesAndCount4NameHasLeavingZeros() {
        Mockito.doReturn(false).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(4).when(configurationMock).getTableIdDigitCount();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();
        assertThat(namingStrategy.getTableName(2, "Test"), is("Item0002"));
    }

    @Test
    public void getTableNameWhenNotUseRealItemNamesAndCount0() {
        Mockito.doReturn(false).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(0).when(configurationMock).getTableIdDigitCount();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();
        assertThat(namingStrategy.getTableName(12345, "Test"), is("Item12345"));
    }

    @Test
    public void prepareMigrationFromNumberedToRealNames() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "Item1";

        List<ItemVO> actual = prepareMigrationRealItemNames(itemId, itemName, tableName);

        assertTableName(actual, "test");
    }

    @Test
    public void prepareMigrationFromMixedNumberedToRealNames() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(false).when(configurationMock).getTablePreserveCase();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();

        Map<Integer, String> itemIdToItemNameMap = new HashMap<>(3);
        itemIdToItemNameMap.put(1, "First");
        itemIdToItemNameMap.put(2, "Second");
        itemIdToItemNameMap.put(3, "Third");

        List<String> itemTables = new ArrayList<String>(3);
        itemTables.add("Item1");
        itemTables.add("Item002");
        itemTables.add("third_0003");

        List<ItemVO> actual = namingStrategy.prepareMigration(itemTables, itemIdToItemNameMap, ITEMS_MANAGE_TABLE_NAME);

        assertThat(actual.size(), is(3));
        assertThat(actual.get(0).getNewTableName(), is("first"));
        assertThat(actual.get(1).getNewTableName(), is("second"));
        assertThat(actual.get(2).getNewTableName(), is("third"));
    }

    @Test
    public void prepareMigrationFromMixedRealNamesToNewRealNames() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "test_0001";

        List<ItemVO> actual = prepareMigrationRealItemNames(itemId, itemName, tableName);

        assertTableName(actual, "test");
    }

    @Test
    public void prepareMigrationFromRealNamesToRealNamesLowerCase() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "Test";

        List<ItemVO> actual = prepareMigrationRealItemNames(itemId, itemName, tableName);

        assertTableName(actual, "test");
    }

    @Test
    public void prepareMigrationFromRealNamesToRealNamesPreserveCase() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "test";

        List<ItemVO> actual = prepareMigrationRealItemNames(itemId, itemName, tableName, true);

        assertTableName(actual, "Test");
    }

    @Test
    public void prepareMigrationRealNamesWithTwoItemsWithDifferentCaseToNumbered() {
        Mockito.doReturn(false).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();
        Mockito.doReturn(1).when(configurationMock).getTableIdDigitCount();

        Map<Integer, String> itemIdToItemNameMap = new HashMap<>(2);
        itemIdToItemNameMap.put(1, "MyItem");
        itemIdToItemNameMap.put(2, "myItem");

        List<String> itemTables = new ArrayList<String>(2);
        itemTables.add("MyItem");
        itemTables.add("myItem");

        List<ItemVO> actual = namingStrategy.prepareMigration(itemTables, itemIdToItemNameMap, ITEMS_MANAGE_TABLE_NAME);

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getNewTableName(), is("Item1"));
        assertThat(actual.get(1).getNewTableName(), is("Item2"));
    }

    /**
     * This conflict scenario is currently handled outside {@link NamingStrategy} so test case
     * serves as documentation.
     */
    @Test
    public void prepareMigrationNumberedWithTwoItemsWithDifferentCaseToRealNamesLower() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();
        Mockito.doReturn(false).when(configurationMock).getTablePreserveCase();

        Map<Integer, String> itemIdToItemNameMap = new HashMap<>(2);
        itemIdToItemNameMap.put(1, "MyItem");
        itemIdToItemNameMap.put(2, "myItem");

        List<String> itemTables = new ArrayList<String>(2);
        itemTables.add("Item1");
        itemTables.add("Item2");

        List<ItemVO> actual = namingStrategy.prepareMigration(itemTables, itemIdToItemNameMap, ITEMS_MANAGE_TABLE_NAME);

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getNewTableName(), is("myitem"));
        assertThat(actual.get(1).getNewTableName(), is("myitem"));
    }

    @Test
    public void prepareMigrationFromMixedRealNamesToNewRealNamesWhenUnknownItemIdThenSkip() {
        final int itemId = 2;
        final String itemName = "Test";
        final String tableName = "test_0001";

        List<ItemVO> actual = prepareMigrationRealItemNames(itemId, itemName, tableName);

        assertThat(actual.size(), is(0));
    }

    @Test
    public void prepareMigrationFromMixedRealNamesToNumbered() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "test_0001";

        List<ItemVO> actual = prepareMigrationNumbered(itemId, itemName, tableName);

        assertTableName(actual, "Item0001");
    }

    @Test
    public void prepareMigrationFromNumberedToNumberedWithCorrectPadding() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "Item1";

        List<ItemVO> actual = prepareMigrationNumbered(itemId, itemName, tableName, 2);

        assertTableName(actual, "Item01");
    }

    @Test
    public void prepareMigrationFromNumberedToNumberedExceedingPadding() {
        final int itemId = 101;
        final String itemName = "Test";
        final String tableName = "Item0101";

        List<ItemVO> actual = prepareMigrationNumbered(itemId, itemName, tableName, 2);

        assertTableName(actual, "Item101");
    }

    @Test
    public void prepareMigrationFromNewRealNamesToNumbered() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "test";

        List<ItemVO> actual = prepareMigrationNumbered(itemId, itemName, tableName);

        assertTableName(actual, "Item0001");
    }

    @Test
    public void prepareMigrationFromNewRealNamesToNumberedHavingUnderscore() {
        final int itemId = 1;
        final String itemName = "My_Test";
        final String tableName = "my_test";

        List<ItemVO> actual = prepareMigrationNumbered(itemId, itemName, tableName);

        assertTableName(actual, "Item0001");
    }

    @Test
    public void prepareMigrationFromNewRealNamesToNumberedHavingUnderscoreAndNumber() {
        final int itemId = 1;
        final String itemName = "My_Test_1";
        final String tableName = "my_test_1";

        List<ItemVO> actual = prepareMigrationNumbered(itemId, itemName, tableName);

        assertTableName(actual, "Item0001");
    }

    @Test
    public void prepareMigrationWhenConflictWithItemsManageTableThenSkip() {
        final int itemId = 1;
        final String itemName = "items";
        final String tableName = "Item1";

        List<ItemVO> actual = prepareMigrationRealItemNames(itemId, itemName, tableName);

        assertThat(actual.size(), is(0));
    }

    private List<ItemVO> prepareMigrationNumbered(int itemId, String itemName, String tableName) {
        return prepareMigrationNumbered(itemId, itemName, tableName, 4);
    }

    private List<ItemVO> prepareMigrationNumbered(int itemId, String itemName, String tableName,
            int tableIdDigitCount) {
        Mockito.doReturn(tableIdDigitCount).when(configurationMock).getTableIdDigitCount();
        Mockito.doReturn(false).when(configurationMock).getTableUseRealItemNames();
        return prepareMigration(itemId, itemName, tableName);
    }

    private List<ItemVO> prepareMigrationRealItemNames(int itemId, String itemName, String tableName) {
        return prepareMigrationRealItemNames(itemId, itemName, tableName, false);
    }

    private List<ItemVO> prepareMigrationRealItemNames(int itemId, String itemName, String tableName,
            boolean preserveCase) {
        Mockito.doReturn(4).when(configurationMock).getTableIdDigitCount();
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn(preserveCase).when(configurationMock).getTablePreserveCase();
        return prepareMigration(itemId, itemName, tableName);
    }

    private List<ItemVO> prepareMigration(int itemId, String itemName, String tableName) {
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();

        Map<Integer, String> itemIdToItemNameMap = getItemIdToItemNameMap(itemId, itemName);
        List<String> itemTables = getItemTables(tableName);

        return namingStrategy.prepareMigration(itemTables, itemIdToItemNameMap, ITEMS_MANAGE_TABLE_NAME);
    }

    private Map<Integer, String> getItemIdToItemNameMap(int itemId, String itemName) {
        Map<Integer, String> itemIdToItemNameMap = new HashMap<>(1);
        itemIdToItemNameMap.put(itemId, itemName);
        return itemIdToItemNameMap;
    }

    private List<String> getItemTables(String tableName) {
        List<String> itemTables = new ArrayList<String>(1);
        itemTables.add(tableName);
        return itemTables;
    }

    private void assertTableName(List<ItemVO> actual, String expected) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getNewTableName(), is(expected));
    }
}
