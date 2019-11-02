/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

/**
 * The {@link IDataPoint} is the interface for all data points
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public interface IDataPoint {
    int getId();

    String getUnit() throws Exception;

    String getKnxDataType() throws Exception;

    String getDescription() throws Exception;

    String getValueText() throws Exception;

    Object getValueObject() throws Exception;

    void processData(byte[] data) throws Exception;

    byte[] createWriteData(Object value) throws Exception;
}