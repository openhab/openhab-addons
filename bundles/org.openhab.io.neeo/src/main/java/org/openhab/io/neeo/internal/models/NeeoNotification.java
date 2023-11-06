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
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoUtil;

/**
 * The model representing a NEEO notification (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoNotification {

    /** The type of notification */
    private final String type;

    /** The value of the notification */
    private final Object data;

    /**
     * Instantiates a new neeo notification from the key, item name and data.
     *
     * @param deviceKey the non-null, non-empty device key
     * @param itemName the non-null, non-empty item name
     * @param data the possibly null, possibly empty (if a string) data
     */
    public NeeoNotification(String deviceKey, String itemName, @Nullable Object data) {
        NeeoUtil.requireNotEmpty(deviceKey, "deviceKey cannot be empty");
        NeeoUtil.requireNotEmpty(itemName, "itemName cannot be empty");

        this.type = deviceKey + ":" + itemName;
        this.data = data == null || (data instanceof String && data.toString().isEmpty()) ? "-" : data;
    }

    /**
     * Gets the notification type.
     *
     * @return the notification type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the data.
     *
     * @return the data
     */
    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "NeeoNotification [type=" + type + ", data=" + data + "]";
    }
}
