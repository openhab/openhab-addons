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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenThermGatewayConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class OpenThermGatewayConfiguration {

    public String ipaddress = "";

    public int port = 0;

    public int connectionRetryInterval = 60;
}
