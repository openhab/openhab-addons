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
package org.openhab.binding.somneo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomneoConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Michael Myrcik - Initial contribution
 */
@NonNullByDefault
public class SomneoConfiguration {

    public String hostname = "";
    public int port = 443;
    public int refreshInterval = 30;
    public boolean ignoreSSLErrors = false;
}
