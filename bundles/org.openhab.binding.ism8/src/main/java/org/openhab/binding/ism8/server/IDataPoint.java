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
package org.openhab.binding.ism8.server;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link IDataPoint} is the interface for all data points
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public interface IDataPoint {
    int getId();

    /**
     * Gets the unit of the data-point.
     *
     */
    String getUnit();

    /**
     * Gets the type of the data-point.
     *
     */
    String getKnxDataType();

    /**
     * Gets the description of the data-point.
     *
     */
    String getDescription();

    /**
     * Gets the value as formated text.
     *
     */
    String getValueText();

    /**
     * Gets the value object.
     *
     */
    @Nullable
    Object getValueObject();

    /**
     * Processes the data received
     *
     */
    void processData(byte[] data);

    /**
     * Creates the data to be written
     *
     */
    byte[] createWriteData(Object value);
}
