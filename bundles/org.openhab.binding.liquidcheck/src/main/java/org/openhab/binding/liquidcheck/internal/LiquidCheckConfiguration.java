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
package org.openhab.binding.liquidcheck.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LiquidCheckConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class LiquidCheckConfiguration {

    public String hostname = "";
    public int refreshInterval = 60;
    public int maxContent = 1;
    public byte connectionTimeout = 5;
}
