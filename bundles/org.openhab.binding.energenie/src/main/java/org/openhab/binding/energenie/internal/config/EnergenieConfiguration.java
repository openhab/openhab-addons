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
package org.openhab.binding.energenie.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EnergenieConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class EnergenieConfiguration {
    /**
     * The default refresh interval in Seconds.
     */
    public static final int DEFAULT_REFRESH_INTERVAL = 60;

    public String host = "";
    public String password = "";
}
