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
package org.openhab.persistence.jdbc.internal.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the table naming data.
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
public class ItemsVO implements Serializable {

    private static final long serialVersionUID = 2871961811177601520L;

    private static final String STR_FILTER = "[^a-zA-Z0-9]";

    private String coltype = "VARCHAR(500)";
    private String colname = "ItemName";
    private String itemsManageTable;
    private int itemId;
    private String itemName;
    private String tableName;
    private String jdbcUriDatabaseName;

    public String getColtype() {
        return coltype;
    }

    public void setColtype(String coltype) {
        this.coltype = coltype.replaceAll(STR_FILTER, "");
    }

    public String getColname() {
        return colname;
    }

    public void setColname(String colname) {
        this.colname = colname.replaceAll(STR_FILTER, "");
    }

    public String getItemsManageTable() {
        return itemsManageTable;
    }

    public void setItemsManageTable(String itemsManageTable) {
        this.itemsManageTable = itemsManageTable.replaceAll(STR_FILTER, "");
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getJdbcUriDatabaseName() {
        return jdbcUriDatabaseName;
    }

    public void setJdbcUriDatabaseName(String jdbcUriDatabaseName) {
        this.jdbcUriDatabaseName = jdbcUriDatabaseName;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(itemName, itemId);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ItemsVO other = (ItemsVO) obj;
        if (itemName == null) {
            if (other.itemName != null) {
                return false;
            }
        } else if (!itemName.equals(other.itemName)) {
            return false;
        }
        return itemId == other.itemId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ItemsVO [coltype=");
        builder.append(coltype);
        builder.append(", colname=");
        builder.append(colname);
        builder.append(", itemsManageTable=");
        builder.append(itemsManageTable);
        builder.append(", itemid=");
        builder.append(itemId);
        builder.append(", itemname=");
        builder.append(itemName);
        builder.append(", table_name=");
        builder.append(tableName);
        builder.append("]");
        return builder.toString();
    }
}
