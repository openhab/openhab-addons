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
package org.openhab.persistence.jdbc;

/**
 * This class represents status for an {@link ItemTableCheckEntry}.
 *
 * @author Jacob Laursen - Initial contribution
 */
public enum ItemTableCheckEntryStatus {
    VALID {
        @Override
        public String toString() {
            return "Valid";
        }
    },
    ITEM_MISSING {
        @Override
        public String toString() {
            return "Item missing";
        }
    },
    TABLE_MISSING {
        @Override
        public String toString() {
            return "Table missing";
        }
    },
    ITEM_AND_TABLE_MISSING {
        @Override
        public String toString() {
            return "Item and table missing";
        }
    },
    ORPHAN_TABLE {
        @Override
        public String toString() {
            return "Orphan table";
        }
    }
}
