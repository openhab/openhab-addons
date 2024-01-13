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
package org.openhab.binding.sinope.internal.core.appdata;

/**
 * The Class SinopeLocalTimeData.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeLocalTimeData extends SinopeAppData {

    /**
     * Instantiates a new sinope local time data.
     */
    public SinopeLocalTimeData() {
        super(new byte[] { 0x00, 0x00, 0x06, 0x00 }, new byte[] { 0, 0, 0 });
    }
}
