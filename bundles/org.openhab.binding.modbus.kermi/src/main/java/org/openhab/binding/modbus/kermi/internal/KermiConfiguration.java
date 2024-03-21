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
package org.openhab.binding.modbus.kermi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KermiConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Kai Neuhaus - Initial contribution
 */
@NonNullByDefault
public class KermiConfiguration {

    public int refresh = 5000;
    public boolean pvEnabled = false;
}
