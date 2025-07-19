/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device.database;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.InsteonAddress;

/**
 * The {@link ModemDBChange} holds a link database change for a modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDBChange extends DatabaseChange<ModemDBRecord> {

    public ModemDBChange(ModemDBRecord record, ChangeType type) {
        super(record, type);
    }

    /**
     * Factory method for creating a new ModemDBChange for add
     *
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param data the record data to use
     * @return the modem db change
     */
    public static ModemDBChange forAdd(InsteonAddress address, int group, boolean isController, byte[] data) {
        return new ModemDBChange(ModemDBRecord.create(address, group, isController, data), ChangeType.ADD);
    }

    /**
     * Factory method for creating a new ModemDBChange for modify
     *
     * @param record the record to modify
     * @param data the record data to use
     * @return the modem db change
     */
    public static ModemDBChange forModify(ModemDBRecord record, byte[] data) {
        return new ModemDBChange(record.withNewData(data), ChangeType.MODIFY);
    }

    /**
     * Factory method for creating a new ModemDBChange for delete
     *
     * @param record the record to delete
     * @return the modem db change
     */
    public static ModemDBChange forDelete(ModemDBRecord record) {
        return new ModemDBChange(record, ChangeType.DELETE);
    }
}
