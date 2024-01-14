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
package org.openhab.binding.androidtv.internal.protocol.googletv;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GoogleTVConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class GoogleTVConfiguration {

    public String ipAddress = "";
    public int googletvPort = 6466;
    public int reconnect;
    public int heartbeat;
    public String keystoreFileName = "";
    public String keystorePassword = "";
    public int delay = 0;
    public boolean shim;
    public boolean shimNewKeys;
    public String mode = "";
    public boolean gtvEnabled;
}
