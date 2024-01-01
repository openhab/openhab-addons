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
package org.openhab.binding.resol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ResolBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class ResolBridgeConfiguration {

    public String ipAddress = "";
    public String password = "vbus";
    public Integer port = 7053;
    public String adapterSerial = "";
    public Integer refreshInterval = 300;
}
