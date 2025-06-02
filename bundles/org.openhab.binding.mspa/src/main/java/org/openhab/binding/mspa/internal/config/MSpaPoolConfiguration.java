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
package org.openhab.binding.mspa.internal.config;

import static org.openhab.binding.mspa.internal.MSpaConstants.UNKNOWN;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MSpaPoolConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MSpaPoolConfiguration {

    public String deviceId = UNKNOWN;
    public String productId = UNKNOWN;
    public int refreshInterval = 15;
}
