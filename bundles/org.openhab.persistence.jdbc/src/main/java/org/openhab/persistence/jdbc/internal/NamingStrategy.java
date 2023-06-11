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
package org.openhab.persistence.jdbc.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.ItemUtil;
import org.openhab.persistence.jdbc.internal.dto.ItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages strategy for table names.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class NamingStrategy {

    private final Logger logger = LoggerFactory.getLogger(NamingStrategy.class);

    private JdbcConfiguration configuration;

    public NamingStrategy(JdbcConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getTableName(int itemId, String itemName) {
        if (!ItemUtil.isValidItemName(itemName)) {
            throw new IllegalArgumentException(itemName + " is not a valid item name");
        }
        if (configuration.getTableUseRealItemNames()) {
            return formatTableName(itemName, itemId);
        } else {
            return configuration.getTableNamePrefix() + getSuffix(itemId);
        }
    }

    private String formatTableName(String itemName, int itemId) {
        if (configuration.getTableCaseSensitiveItemNames()) {
            return itemName;
        } else {
            return itemName.toLowerCase() + "_" + getSuffix(itemId);
        }
    }

    private String getSuffix(int itemId) {
        int digits = configuration.getTableIdDigitCount();
        if (digits > 0) {
            return String.format("%0" + configuration.getTableIdDigitCount() + "d", itemId);
        } else {
            return String.valueOf(itemId);
        }
    }

    public List<ItemVO> prepareMigration(List<String> itemTables, Map<Integer, String> itemIdToItemNameMap,
            String itemsManageTable) {
        List<ItemVO> oldNewTableNames = new ArrayList<>();
        Map<String, Integer> tableNameToItemIdMap = new HashMap<>();

        for (Entry<Integer, String> entry : itemIdToItemNameMap.entrySet()) {
            String itemName = entry.getValue();
            tableNameToItemIdMap.put(itemName, entry.getKey());
        }

        for (String oldName : itemTables) {
            Integer itemIdBoxed = tableNameToItemIdMap.get(oldName);
            int itemId = -1;

            if (Objects.nonNull(itemIdBoxed)) {
                itemId = itemIdBoxed;
                logger.info("JDBC::formatTableNames: found by name; table name= {} id= {}", oldName, itemId);
            } else {
                try {
                    itemId = Integer.parseInt(oldName.replaceFirst("^.*\\D", ""));
                    logger.info("JDBC::formatTableNames: found by id; table name= {} id= {}", oldName, itemId);
                } catch (NumberFormatException e) {
                    // Fall through.
                }
            }

            String itemName = itemIdToItemNameMap.get(itemId);

            if (!Objects.isNull(itemName)) {
                String newName = getTableName(itemId, itemName);
                if (newName.equalsIgnoreCase(itemsManageTable)) {
                    logger.error(
                            "JDBC::formatTableNames: Table '{}' could NOT be renamed to '{}' since it conflicts with manage table",
                            oldName, newName);
                } else if (!oldName.equals(newName)) {
                    oldNewTableNames.add(new ItemVO(oldName, newName));
                    logger.info("JDBC::formatTableNames: Table '{}' will be renamed to '{}'", oldName, newName);
                } else {
                    logger.info("JDBC::formatTableNames: Table oldName='{}' newName='{}' nothing to rename", oldName,
                            newName);
                }
            } else {
                logger.error("JDBC::formatTableNames: Table '{}' could NOT be renamed for id '{}'", oldName, itemId);
            }
        }

        return oldNewTableNames;
    }
}
