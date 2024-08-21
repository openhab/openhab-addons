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
package org.openhab.binding.satel.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Atd100Config} contains configuration values for ATD-100 things.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class Atd100Config {

    private int id;
    private int refresh;

    /**
     * @return zone number
     */
    public int getId() {
        return id;
    }

    /**
     * @return polling interval in minutes
     */
    public int getRefresh() {
        return refresh;
    }
}
