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
package org.openhab.binding.sonyprojector.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SonyProjectorEthernetConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Markus Wehrle - Initial contribution
 * @author Laurent Garnier - New model configuration setting added
 */
@NonNullByDefault
public class SonyProjectorEthernetConfiguration {
    public static final int DEFAULT_PORT = 53484;
    private static final String DEFAULT_COMMUNITY = "SONY";
    public static final String MODEL_AUTO = "AUTO";

    public String host = "";
    public int port = DEFAULT_PORT;
    public String community = DEFAULT_COMMUNITY;
    public String model = MODEL_AUTO;
}
