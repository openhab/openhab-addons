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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents a checked item/table relation.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ItemTableCheckEntry {
    private String itemName;
    private String tableName;
    private ItemTableCheckEntryStatus status;

    public ItemTableCheckEntry(String itemName, String tableName, ItemTableCheckEntryStatus status) {
        this.itemName = itemName;
        this.tableName = tableName;
        this.status = status;
    }

    public String getItemName() {
        return itemName;
    }

    public String getTableName() {
        return tableName;
    }

    public ItemTableCheckEntryStatus getStatus() {
        return status;
    }
}
