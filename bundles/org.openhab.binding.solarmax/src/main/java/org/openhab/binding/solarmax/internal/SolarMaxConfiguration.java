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
package org.openhab.binding.solarmax.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SolarMaxConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public class SolarMaxConfiguration {
    public String host = ""; // this will always need to be overridden
    public int portNumber = 12345; // default value is 12345
    public int deviceAddress = 1; // default value is 1

    public int refreshInterval = 15; // default value is 15
}
