/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The specific storage list item information - used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class StorageListItem_1_1 {
    /** The uri */
    private @Nullable String uri;

    /** The device name */
    private @Nullable String deviceName;

    /** The volume label */
    private @Nullable String volumeLabel;

    /** The permissions required */
    private @Nullable String permission;

    /** The position of the storage (internal, front, back ,etc) */
    private @Nullable String position;

    /** Whether it's formattable */
    private @Nullable String formattable;

    /** Whether the storge is mounted */
    private @Nullable String mounted;

    /** THe whole capacity (MB) */
    private @Nullable Integer wholeCapacityMB;

    /** The free capacity (MB) */
    private @Nullable Integer freeCapacityMB;

    /** The system area capacity (MB) */
    private @Nullable Integer systemAreaCapacityMB;

    /** Status of whether it's being formatted */
    private @Nullable String formatting;

    /** Whether the storage is available */
    private @Nullable String isAvailable;

    /** The logical unit number */
    private @Nullable Integer lun;

    /** The storage type */
    private @Nullable String type;

    /** The format of the storage */
    private @Nullable String format;

    /** Storage errors */
    private @Nullable String error;

    /**
     * Constructor used for deserialization only
     */
    public StorageListItem_1_1() {
    }

    /**
     * Gets the storage URI
     * 
     * @return the uri
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Gets the device name
     * 
     * @return the device name
     */
    public @Nullable String getDeviceName() {
        return deviceName;
    }

    /**
     * Gets the volume label
     * 
     * @return the volume label
     */
    public @Nullable String getVolumeLabel() {
        return volumeLabel;
    }

    /**
     * @return the permission
     */
    public @Nullable String getPermission() {
        return permission;
    }

    /**
     * Gets the position of the storage (front, back, internal, etc)
     * 
     * @return the position
     */
    public @Nullable String getPosition() {
        return position;
    }

    /**
     * Gets the formattable status
     * 
     * @return the formattable status
     */
    public @Nullable String getFormattable() {
        return formattable;
    }

    /**
     * Gets the mounted status
     * 
     * @return the mounted status
     */
    public @Nullable String getMounted() {
        return mounted;
    }

    /**
     * Gets the whole capacity (in MB)
     * 
     * @return the whole capacity
     */
    public @Nullable Integer getWholeCapacityMB() {
        return wholeCapacityMB;
    }

    /**
     * Gets the free capacity (in MB)
     * 
     * @return the free capacity
     */
    public @Nullable Integer getFreeCapacityMB() {
        return freeCapacityMB;
    }

    /**
     * Gets the system area capacity (MB)
     * 
     * @return the system area capacity
     */
    public @Nullable Integer getSystemAreaCapacityMB() {
        return systemAreaCapacityMB;
    }

    /**
     * Gets the formatting status
     * 
     * @return the formatting status
     */
    public @Nullable String getFormatting() {
        return formatting;
    }

    /**
     * Gets the storage availability status
     * 
     * @return the availability status
     */
    public @Nullable String getIsAvailable() {
        return isAvailable;
    }

    /**
     * Gets the logical unit number
     * 
     * @return the logical unit number
     */
    public @Nullable Integer getLun() {
        return lun;
    }

    /**
     * Gets the storage type
     * 
     * @return the storage type
     */
    public @Nullable String getType() {
        return type;
    }

    /**
     * Gets the storage format
     * 
     * @return the storage format
     */
    public @Nullable String getFormat() {
        return format;
    }

    /**
     * Gets the storage error
     * 
     * @return the storage error
     */
    public @Nullable String getError() {
        return error;
    }
}
