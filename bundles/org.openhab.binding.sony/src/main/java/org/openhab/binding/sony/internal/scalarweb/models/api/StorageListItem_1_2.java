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
public class StorageListItem_1_2 {
    /** The device name */
    private @Nullable String deviceName;

    /** The storage error */
    private @Nullable String error;

    /** The file system type */
    private @Nullable String fileSystem;

    /** The finalize status */
    private @Nullable String finalizeStatus;

    /** The storage format */
    private @Nullable String format;

    /** The format status */
    private @Nullable String formatStatus;

    /** The formattable status */
    private @Nullable String formattable;

    /** The free capacity (MB) */
    private @Nullable Integer freeCapacityMB;

    /** Whether it has non standard data */
    private @Nullable String hasNonStandardData;

    /** Whether it has unsupported contents */
    private @Nullable String hasUnsupportedContents;

    /** Whether it is available */
    private @Nullable String isAvailable;

    /** Whether it is locked */
    private @Nullable String isLocked;

    /** Whether the management information is full */
    private @Nullable String isManagementInfoFull;

    /** Whether it is protected */
    private @Nullable String isProtected;

    /** Whether it is registered */
    private @Nullable String isRegistered;

    /** Whether it is self recorded */
    private @Nullable String isSelfRecorded;

    /** Whether SQV (standard quality voice) is supported */
    private @Nullable String isSqvSupported;

    /** The logical unit number */
    private @Nullable Integer lun;

    /** The mount status */
    private @Nullable String mounted;

    /** The storage permission */
    private @Nullable String permission;

    /** The storage position (front, back, internal, etc) */
    private @Nullable String position;

    /** The storage protocol */
    private @Nullable String protocol;

    /** The storage registration date */
    private @Nullable String registrationDate;

    /** The system area capacity (MB) */
    private @Nullable Integer systemAreaCapacityMB;

    /** The time (in seconds) to finalize */
    private @Nullable Integer timeSecToFinalize;

    /** The time (in seconds) to get contents */
    private @Nullable Integer timeSecToGetContents;

    /** The storage type */
    private @Nullable String type;

    /** The storage URI */
    private @Nullable String uri;

    /** The USB device type */
    private @Nullable String usbDeviceType;

    /** The volume label */
    private @Nullable String volumeLabel;

    /** The whole capacity (MB) */
    private @Nullable Integer wholeCapacityMB;

    /**
     * Constructor used for deserialization only
     */
    public StorageListItem_1_2() {
    }

    /**
     * Gets the storage device name
     * 
     * @return the storage device name
     */
    public @Nullable String getDeviceName() {
        return deviceName;
    }

    /**
     * Gets the storage error
     * 
     * @return the storage error
     */
    public @Nullable String getError() {
        return error;
    }

    /**
     * Gets the file system
     * 
     * @return the file system
     */
    public @Nullable String getFileSystem() {
        return fileSystem;
    }

    /**
     * Gets the finalization status
     * 
     * @return the finalization status
     */
    public @Nullable String getFinalizeStatus() {
        return finalizeStatus;
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
     * Gets the format status
     * 
     * @return the format status
     */
    public @Nullable String getFormatStatus() {
        return formatStatus;
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
     * Gets the free capacity (MB)
     * 
     * @return the free capacity (MB)
     */
    public @Nullable Integer getFreeCapacityMB() {
        return freeCapacityMB;
    }

    /**
     * '
     * Gets whether the storage has non-standard data
     * 
     * @return whether the storage has non-standard data
     */
    public @Nullable String getHasNonStandardData() {
        return hasNonStandardData;
    }

    /**
     * Gets whether the storage has unsupported contents
     * 
     * @return whether the storage has unsupported contents
     */
    public @Nullable String getHasUnsupportedContents() {
        return hasUnsupportedContents;
    }

    /**
     * Whether the storage is available
     * 
     * @return whether the storage is available
     */
    public @Nullable String getIsAvailable() {
        return isAvailable;
    }

    /**
     * Whether the storage is locked
     * 
     * @return Whether the storage is locked
     */
    public @Nullable String getIsLocked() {
        return isLocked;
    }

    /**
     * Whether the storage management info is full
     * 
     * @return whether the storage management info is full
     */
    public @Nullable String getIsManagementInfoFull() {
        return isManagementInfoFull;
    }

    /**
     * Whether the storage is protected
     * 
     * @return whether the storage is protected
     */
    public @Nullable String getIsProtected() {
        return isProtected;
    }

    /**
     * Whether the storage is registered
     * 
     * @return whether the storage is registered
     */
    public @Nullable String getIsRegistered() {
        return isRegistered;
    }

    /**
     * Whether the storage is self recorded
     * 
     * @return whether the storage is self recorded
     */
    public @Nullable String getIsSelfRecorded() {
        return isSelfRecorded;
    }

    /**
     * Whether SQV is supported
     * 
     * @return whether SQV is supported
     */
    public @Nullable String getIsSqvSupported() {
        return isSqvSupported;
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
     * Gets the mount status
     * 
     * @return the mount status
     */
    public @Nullable String getMounted() {
        return mounted;
    }

    /**
     * Gets the storage permission
     * 
     * @return the storage permission
     */
    public @Nullable String getPermission() {
        return permission;
    }

    /**
     * Gets the storage position (front, back, internal, etc)
     * 
     * @return the storage position
     */
    public @Nullable String getPosition() {
        return position;
    }

    /**
     * Gets the storage protocol
     * 
     * @return the storage protocol
     */
    public @Nullable String getProtocol() {
        return protocol;
    }

    /**
     * Gets the registration date
     * 
     * @return the registration date
     */
    public @Nullable String getRegistrationDate() {
        return registrationDate;
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
     * Gets the time (in seconds) to finalize
     * 
     * @return the time to finalize
     */
    public @Nullable Integer getTimeSecToFinalize() {
        return timeSecToFinalize;
    }

    /**
     * Gets the time (in seconds) to get contents
     * 
     * @return the time to get contents
     */
    public @Nullable Integer getTimeSecToGetContents() {
        return timeSecToGetContents;
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
     * Gets the storage URI
     * 
     * @return the uri
     */
    public @Nullable String getUri() {
        return uri;
    }

    /**
     * Gets the USB device type
     * 
     * @return the USB device type
     */
    public @Nullable String getUsbDeviceType() {
        return usbDeviceType;
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
     * Gets the whole capacity (MB)
     * 
     * @return the whole capacity
     */
    public @Nullable Integer getWholeCapacityMB() {
        return wholeCapacityMB;
    }
}
