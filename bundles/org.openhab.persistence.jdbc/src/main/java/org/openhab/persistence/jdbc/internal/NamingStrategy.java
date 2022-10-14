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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.persistence.jdbc.dto.ItemVO;
import org.openhab.persistence.jdbc.dto.ItemsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages strategy for table names.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class NamingStrategy {

    private static final String ITEM_NAME_PATTERN = "[^a-zA-Z_0-9\\-]";

    private final Logger logger = LoggerFactory.getLogger(NamingStrategy.class);

    private JdbcConfiguration configuration;

    public NamingStrategy(JdbcConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getTableName(int rowId, String itemName) {
        if (configuration.getTableUseRealItemNames()) {
            return (itemName.replaceAll(ITEM_NAME_PATTERN, "")).toLowerCase();
        } else {
            return configuration.getTableNamePrefix() + formatRight(rowId, configuration.getTableIdDigitCount());
        }
    }

    private static String formatRight(final Object value, final int len) {
        final String valueAsString = String.valueOf(value);
        if (valueAsString.length() < len) {
            final StringBuffer result = new StringBuffer(len);
            for (int i = len - valueAsString.length(); i > 0; i--) {
                result.append('0');
            }
            result.append(valueAsString);
            return result.toString();
        } else {
            return valueAsString;
        }
    }

    public List<ItemVO> prepareMigration(Map<Integer, String> tableIds, List<ItemsVO> itemTables) {
        String oldName = "";
        String newName = "";
        List<ItemVO> oldNewTablenames = new ArrayList<>();
        for (int i = 0; i < itemTables.size(); i++) {
            int id = -1;
            oldName = itemTables.get(i).getTable_name();
            logger.info("JDBC::formatTableNames: found Table Name= {}", oldName);

            if (oldName.startsWith(configuration.getTableNamePrefix()) && !oldName.contains("_")) {
                id = Integer.parseInt(oldName.substring(configuration.getTableNamePrefix().length()));
                logger.info("JDBC::formatTableNames: found Table with Prefix '{}' Name= {} id= {}",
                        configuration.getTableNamePrefix(), oldName, (id));
            } else if (oldName.contains("_")) {
                id = Integer.parseInt(oldName.substring(oldName.lastIndexOf("_") + 1));
                logger.info("JDBC::formatTableNames: found Table Name= {} id= {}", oldName, (id));
            }
            logger.info("JDBC::formatTableNames: found Table id= {}", id);

            newName = tableIds.get(id);
            logger.info("JDBC::formatTableNames: found Table newName= {}", newName);

            if (newName != null) {
                if (!oldName.equalsIgnoreCase(newName)) {
                    oldNewTablenames.add(new ItemVO(oldName, newName));
                    logger.info("JDBC::formatTableNames: Table '{}' will be renamed to '{}'", oldName, newName);
                } else {
                    logger.info("JDBC::formatTableNames: Table oldName='{}' newName='{}' nothing to rename", oldName,
                            newName);
                }
            } else {
                logger.error("JDBC::formatTableNames: Table '{}' could NOT be renamed to '{}'", oldName, newName);
                break;
            }
        }

        return oldNewTablenames;
    }
}
