/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.elroconnects.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ElroConnectsBridgeConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsBridgeConfiguration {

    public String connectorId = "";
    public String ipAddress = "";
    public int refreshInterval = 60;
    public boolean legacyFirmware = false;
}
