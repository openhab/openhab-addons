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
package org.openhab.binding.hapero.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Device} class contains the raw
 * data received from the heating control system and
 * conversion functions.
 *
 * @author Daniel Walter - Initial contribution
 */
@NonNullByDefault
public class Device {
    private final Logger logger = LoggerFactory.getLogger(Device.class);
    /** Raw data stream from the device */
    protected String[] dataItems = {};

    /**
     * Constructor
     *
     * @param items List of data items as Strings
     */
    public Device(String[] items) {
        dataItems = items;
    }

    /**
     * Return the data item at the given index
     *
     * @return A String containing the data item at the Index or "" if the Index is invalid
     */
    public String getDataItem(int index) {
        if (index >= 0 && index < dataItems.length) {
            return dataItems[index];
        } else {
            logger.warn("getDataItem: Invalid Index {}.", index);
            return "";
        }
    }

    /**
     * @param dataItems
     */
    public void setDataItems(String[] dataItems) {
        this.dataItems = dataItems;
    }

    /**
     * Returns the value of the dataItem at Index as a Float
     *
     * @param index Index of the dataItem to return
     * @return Float or null if the index is invalid or the dataItem can not be converted to Float
     */
    public @Nullable Float getFloat(int index) {
        Float result = null;

        if (index >= 0 && index < dataItems.length) {
            try {
                result = Float.valueOf(dataItems[index]);
            } catch (NumberFormatException e) {
                logger.warn("getFloat: {}.", e.getMessage());
            }
        } else {
            logger.warn("getFloat: Invalid Index {}.", index);
        }

        return result;
    }

    /**
     * Returns the value of the dataItem at Index as a String
     *
     * @param index Index of the dataItem to return
     * @return String or null if the index is invalid
     */
    public @Nullable String getString(int index) {
        if (index >= 0 && index >= dataItems.length) {
            logger.warn("getString: Invalid Index {}.", index);
            return null;
        }

        return dataItems[index];
    }

    /**
     * Returns the value of the dataItem at Index as a Integer
     *
     * @param index Index of the dataItem to return
     * @return Integer or null if the index is invalid or the dataItem can not be converted to Integer
     */
    public @Nullable Integer getInteger(int index) {
        Integer result = null;

        if (index >= 0 && index < dataItems.length) {
            try {
                result = Integer.valueOf(dataItems[index]);
            } catch (NumberFormatException e) {
                logger.warn("getInteger: {}.", e.getMessage());
            }
        } else {
            logger.warn("getInteger: Invalid Index {}.", index);
        }

        return result;
    }

    /**
     * Returns the value of the dataItem at Index as a Boolean
     *
     * @param index Index of the dataItem to return
     * @return true if the first char if 1, false if the first char is 0 or null if the index is invalid or the dataItem
     *         can not be converted to Boolean
     */
    public @Nullable Boolean getBoolean(int index) {
        Boolean result = null;

        if (index >= 0 && index < dataItems.length) {

            try {
                if (dataItems[index].charAt(0) == '1') {
                    result = true;
                } else if (dataItems[index].charAt(0) == '0') {
                    result = false;
                } else {
                    logger.warn("getBoolean: Invalid value for boolean {}.", dataItems[index].charAt(0));
                }
            } catch (IndexOutOfBoundsException e) {
                logger.warn("getBoolean: {}.", e.getMessage());
            }
        } else {
            logger.warn("getBoolean: Invalid Index {}.", index);
        }

        return result;
    }
}
