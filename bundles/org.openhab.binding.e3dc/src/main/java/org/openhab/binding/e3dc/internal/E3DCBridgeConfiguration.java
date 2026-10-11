/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Configuration of the bridge thing, which represents the connection to one E3/DC device.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCBridgeConfiguration {

    /** Host name or IP address of the device. */
    public String host = "";

    /** RSCP port. */
    public int port = 5033;

    /** Local user name of the device. */
    public String userName = "";

    /** Local user password of the device. */
    public String password = "";

    /** RSCP password as configured on the device; the encryption key of the connection. */
    public String rscpPassword = "";

    /** Poll interval in seconds. */
    public int refreshInterval = 60;

    /** Socket timeout in seconds. */
    public int timeout = 10;
}
