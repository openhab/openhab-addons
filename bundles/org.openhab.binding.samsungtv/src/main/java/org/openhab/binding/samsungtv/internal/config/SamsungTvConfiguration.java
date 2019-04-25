/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal.config;

import org.openhab.binding.samsungtv.internal.handler.SamsungTvHandler;

/**
 * Configuration class for {@link SamsungTvHandler}.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class SamsungTvConfiguration {
    public static final String HOST_NAME = "hostName";
    public static final String PORT = "port";
    public static final String REFRESH_INTERVAL = "refreshInterval";

    public String hostName;
    public int port;
    public int refreshInterval;

}
