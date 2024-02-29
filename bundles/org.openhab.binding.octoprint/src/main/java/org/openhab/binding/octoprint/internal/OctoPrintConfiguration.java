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
package org.openhab.binding.octoprint.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OctoPrintConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Tim-Niclas Ruppert - Initial contribution
 */
@NonNullByDefault
public class OctoPrintConfiguration {

    /**
     * Configuration parameters.
     */
    public String ip = "";
    public String username = "";
    public String password = "";
    public String apikey = "";
    public String serialPort = "";
    public int baudRate = 115200;
    public String printerProfile = "";
    public int refreshInterval = 10;
}
