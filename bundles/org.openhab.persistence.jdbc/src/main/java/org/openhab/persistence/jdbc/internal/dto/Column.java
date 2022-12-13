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
package org.openhab.persistence.jdbc.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents an INFORMATON_SCHEMA.COLUMNS table row.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Column {

    private @Nullable String columnName;
    private boolean isNullable;
    private @Nullable String columnType;

    public String getColumnName() {
        String columnName = this.columnName;
        return columnName != null ? columnName : "";
    }

    public String getColumnType() {
        String columnType = this.columnType;
        return columnType != null ? columnType : "";
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

    public void setIsNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }
}
