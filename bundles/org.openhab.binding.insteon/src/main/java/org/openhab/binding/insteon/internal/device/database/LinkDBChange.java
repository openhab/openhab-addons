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
 * The {@link LinkDBChange} holds a link database change for a device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDBChange extends DatabaseChange<LinkDBRecord> {

    public LinkDBChange(LinkDBRecord record, ChangeType type) {
        super(record, type);
    }

    public int getLocation() {
        return record.getLocation();
    }

    @Override
    public LinkDBRecord getRecord() {
        return type == ChangeType.DELETE ? record.asInactive() : record;
    }

    /**
     * Factory method for creating a new LinkDBChange for add
     *
     * @param location the record location to use
     * @param address the record address to use
     * @param group the record group to use
     * @param isController if is controller record
     * @param data the record data to use
     * @return the link db change
     */
    public static LinkDBChange forAdd(int location, InsteonAddress address, int group, boolean isController,
            byte[] data) {
        return new LinkDBChange(LinkDBRecord.create(location, address, group, isController, data), ChangeType.ADD);
    }

    /**
     * Factory method for creating a new LinkDBChange for modify
     *
     * @param record the record to modify
     * @param data the data record to use
     * @return the link db change
     */
    public static LinkDBChange forModify(LinkDBRecord record, byte[] data) {
        return new LinkDBChange(record.withNewData(data), ChangeType.MODIFY);
    }

    /**
     * Factory method for creating a new LinkDBChange for delete
     *
     * @param record the record to delete
     * @return the link db change
     */
    public static LinkDBChange forDelete(LinkDBRecord record) {
        return new LinkDBChange(record, ChangeType.DELETE);
    }
}
