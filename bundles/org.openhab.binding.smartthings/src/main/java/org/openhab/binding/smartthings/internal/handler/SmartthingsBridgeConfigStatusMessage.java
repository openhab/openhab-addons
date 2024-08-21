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
package org.openhab.binding.smartthings.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Smartthings Bridge messages
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public interface SmartthingsBridgeConfigStatusMessage {

    static final String IP_MISSING = "missing-ip-configuration";
    static final String PORT_MISSING = "missing-port-configuration";
}
