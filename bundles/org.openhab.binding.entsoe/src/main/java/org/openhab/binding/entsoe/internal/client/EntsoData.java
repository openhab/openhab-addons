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
package org.openhab.binding.entsoe.internal.client;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Miika Jukka - Initial contribution
 *
 */
@NonNullByDefault
public class EntsoData {

    private ZonedDateTime dateTime;
    private Double price;

    public EntsoData(ZonedDateTime dateTime, Double price) {
        this.dateTime = dateTime;
        this.price = price;
    }

    /**
     * @return the dateTime
     *
     */
    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    /**
     * @return the price
     *
     */
    public Double getPrice() {
        return price;
    }

    public boolean equals(EntsoData data) {
        if (this.dateTime.isEqual(data.getDateTime())) {
            return true;
        }
        return false;
    }
}
