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
package org.openhab.binding.ekey.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EkeyConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class EkeyConfiguration {

    public String ipAddress = "";
    public int port;
    public String protocol = "";
    public String delimiter = "";
}
