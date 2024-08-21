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
import java.util.Date;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the Item-data on the part of MyBatis/database.
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
public class ItemVO implements Serializable {
    private final Logger logger = LoggerFactory.getLogger(ItemVO.class);

    private static final long serialVersionUID = 1871441039821454890L;

    private String tableName;
    private @Nullable String newTableName;
    private String dbType;
    private String jdbcType;
    private String itemType;
    private Class<?> javaType;
    private Date time;
    private Object value;

    public ItemVO(String tableName, @Nullable String newTableName) {
        logger.debug("JDBC:ItemVO tableName={}; newTableName={}; ", tableName, newTableName);
        this.tableName = tableName;
        this.newTableName = newTableName;
    }

    public ItemVO() {
    }

    public void setValueTypes(String dbType, Class<?> javaType) {
        logger.debug("JDBC:ItemVO setValueTypes dbType={}; javaType={};", dbType, javaType);
        this.dbType = dbType;
        this.javaType = javaType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public @Nullable String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getJavaType() {
        return javaType.getName();
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

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
        ItemVO other = (ItemVO) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return Objects.equals(time, other.time);
    }

    @Override
    public String toString() {
        return new StringBuilder("ItemVO [tableName=").append(tableName).append(", newTableName=").append(newTableName)
                .append(", dbType=").append(dbType).append(", javaType=").append(javaType).append(", time=")
                .append(time).append(", value=").append(value).append("]").toString();
    }
}
