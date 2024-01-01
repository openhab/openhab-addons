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
package org.openhab.binding.omnikinverter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OmnikInverterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Hans van den Bogert - Initial contribution
 */
@NonNullByDefault
public class OmnikInverterConfiguration {
    public String hostname = "";
    public int port;
    public int serial;
}
