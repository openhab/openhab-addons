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
package org.openhab.binding.iammeter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IammeterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Yang Bo - Initial contribution
 */

@NonNullByDefault
public class IammeterConfiguration {
    public String host = "127.0.0.1";
    public int port = 80;
    public int refreshInterval = 30;
    public String username = "admin";
    public String password = "admin";
}
