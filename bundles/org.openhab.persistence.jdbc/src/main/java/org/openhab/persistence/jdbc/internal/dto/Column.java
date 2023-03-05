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
package org.openhab.persistence.jdbc.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an INFORMATON_SCHEMA.COLUMNS table row.
 *
 * MySQL returns type as column_type
 *
 * PostgreSQL returns "data_type" (e.g. "character varying") and "udt_name" as a type alias (e.g. "varchar")
 * these should be aliased as the matching snake_case version of the attributes in this class. i.e.:
 * SELECT column_name, data_type as column_type, udt_name as column_type_alias FROM information_schema.columns
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Column {

    private @Nullable String columnName;
    private boolean isNullable;
    private @Nullable String columnType;
    private @Nullable String columnTypeAlias;

    public String getColumnName() {
        String columnName = this.columnName;
        return columnName != null ? columnName : "";
    }

    public String getColumnType() {
        String columnType = this.columnType;
        return columnType != null ? columnType : "";
    }

    public String getColumnTypeAlias() {
        String columnTypeAlias = this.columnTypeAlias;
        return columnTypeAlias != null ? columnTypeAlias : "";
    }

    public boolean getIsNullable() {
        return isNullable;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public void setColumnTypeAlias(String columnTypeAlias) {
        this.columnTypeAlias = columnTypeAlias;
    }

    public void setIsNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }
}
