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
package org.openhab.binding.ism8.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Ism8Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Hans-Reiner Hoffmann - Initial contribution
 */
@NonNullByDefault
public class Ism8Configuration {
    private int portNumber;

    /**
     * Gets the port number for the ISM8.
     *
     */
    public int getPortNumber() {
        return portNumber;
    }
}
