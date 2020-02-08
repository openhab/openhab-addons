/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.presence.internal.tcpport;

import org.openhab.binding.presence.internal.BaseConfiguration;

/**
 * The {@link TCPPortConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Mike Dabbs - Initial contribution
 */
public class TCPPortConfiguration extends BaseConfiguration {
    // The port to try to connect to
    public int port;
}
