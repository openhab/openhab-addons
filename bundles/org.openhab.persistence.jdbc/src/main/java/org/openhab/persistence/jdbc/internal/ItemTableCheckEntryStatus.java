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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents status for an {@link ItemTableCheckEntry}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum ItemTableCheckEntryStatus {
    /**
     * Table is consistent.
     */
    VALID {
        @Override
        public String toString() {
            return "Valid";
        }
    },
    /**
     * Table has no corresponding item.
     */
    ITEM_MISSING {
        @Override
        public String toString() {
            return "Item missing";
        }
    },
    /**
     * Referenced table does not exist.
     */
    TABLE_MISSING {
        @Override
        public String toString() {
            return "Table missing";
        }
    },
    /**
     * Referenced table does not exist nor has corresponding item.
     */
    ITEM_AND_TABLE_MISSING {
        @Override
        public String toString() {
            return "Item and table missing";
        }
    },
    /**
     * Mapping for table does not exist in index.
     */
    ORPHAN_TABLE {
        @Override
        public String toString() {
            return "Orphan table";
        }
    }
}
