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
package org.openhab.binding.novafinedust.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link NovaFineDustConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
public class NovaFineDustConfiguration {

    /**
     * USB port of the device
     */
    public String port = "";
    public boolean reporting = true;
    public int reportingInterval = 1;
    public int pollingInterval = 10;
}
