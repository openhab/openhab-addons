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
package org.openhab.binding.insteon.internal.device.database;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link DatabaseChange} holds a link database change
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class DatabaseChange<@NonNull T extends DatabaseRecord> {

    protected static enum ChangeType {
        ADD,
        MODIFY,
        DELETE
    }

    protected T record;
    protected ChangeType type;

    public DatabaseChange(T record, ChangeType type) {
        this.record = record;
        this.type = type;
    }

    public T getRecord() {
        return record;
    }

    public boolean isDelete() {
        return type == ChangeType.DELETE;
    }

    @Override
    public String toString() {
        return record + " (" + type + ")";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DatabaseChange<?> other = (DatabaseChange<?>) obj;
        return record.equals(other.record) && type == other.type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + record.hashCode();
        result = prime * result + type.hashCode();
        return result;
    }
}
