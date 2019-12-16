/**
 * Copyright (c) 2018,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openthermgateway.internal;

/**
 * The {@link OpenThermGatewayConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Arjen Korevaar
 */
public class OpenThermGatewayConfiguration {

    public String ipaddress;

    public int port;

    public int connectionRetryInterval;
}
