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
package org.openhab.binding.network.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains the handler configuration and default values. The field names represent the configuration names,
 * do not rename them if you don't intend to break the configuration interface.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class NetworkHandlerConfiguration {
    public String hostname = "";
    public String macAddress = "";
    public @Nullable Integer port;
    public Integer retry = 1;
    public Integer refreshInterval = 60000;
    public Integer timeout = 5000;
}
