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
import org.openhab.persistence.jdbc.dto.ItemVO;
import org.openhab.persistence.jdbc.dto.ItemsVO;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Tests the {@link NamingStrategy} class.
 *
 * @author Jacob Laursen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class NamingStrategyTest {
    private @Mock @NonNullByDefault({}) JdbcConfiguration configurationMock;
    private NamingStrategy namingStrategy = new NamingStrategy(configurationMock);

    @BeforeEach
    public void initialize() {
        final Logger logger = (Logger) LoggerFactory.getLogger(NamingStrategy.class);
        logger.setLevel(Level.OFF);
        namingStrategy = new NamingStrategy(configurationMock);
    }

    @Test
    public void getTableNameWhenUseRealItemNamesNameIsLowercase() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        assertThat(namingStrategy.getTableName(1, "Test"), is("test"));
    }

    @Test
    public void getTableNameWhenUseRealItemNamesSpecialCharsAreRemoved() {
        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
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
    public void prepareMigrationFromNumberedToRealNames() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "Item1";

        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();

        Map<Integer, String> tableIds = getTableIds(itemId, itemName);
        List<ItemsVO> itemTables = getItemTables(tableName);

        List<ItemVO> actual = namingStrategy.prepareMigration(tableIds, itemTables);

        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getNewTableName(), is("test"));
    }

    @Test
    public void prepareMigrationFromMixedRealNamesToNewRealNames() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "test_0001";

        Mockito.doReturn(true).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();

        Map<Integer, String> tableIds = getTableIds(itemId, itemName);
        List<ItemsVO> itemTables = getItemTables(tableName);

        List<ItemVO> actual = namingStrategy.prepareMigration(tableIds, itemTables);

        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getNewTableName(), is("test"));
    }

    @Test
    public void prepareMigrationFromMixedRealNamesToNumbered() {
        final int itemId = 1;
        final String itemName = "Test";
        final String tableName = "test_0001";

        Mockito.doReturn(false).when(configurationMock).getTableUseRealItemNames();
        Mockito.doReturn("Item").when(configurationMock).getTableNamePrefix();

        Map<Integer, String> tableIds = getTableIds(itemId, itemName);
        List<ItemsVO> itemTables = getItemTables(tableName);

        List<ItemVO> actual = namingStrategy.prepareMigration(tableIds, itemTables);

        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getNewTableName(), is("Item1"));
    }

    private Map<Integer, String> getTableIds(int itemId, String itemName) {
        Map<Integer, String> tableIds = new HashMap<>();
        tableIds.put(itemId, namingStrategy.getTableName(1, itemName));
        return tableIds;
    }

    private List<ItemsVO> getItemTables(String tableName) {
        List<ItemsVO> itemTables = new ArrayList<ItemsVO>();
        ItemsVO itemTable = new ItemsVO();
        itemTable.setTable_name(tableName);
        itemTables.add(itemTable);
        return itemTables;
    }
}
