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
package org.openhab.binding.hue.internal;

import org.openhab.core.config.core.status.ConfigStatusMessage;

/**
 * The {@link HueConfigStatusMessage} defines
 * the keys to be used for {@link ConfigStatusMessage}s.
 *
 * @author Alexander Kostadinov - Initial contribution
 * @author Kai Kreuzer - Changed from enum to interface
 *
 */
public interface HueConfigStatusMessage {

    static final String IP_ADDRESS_MISSING = "missing-ip-address-configuration";
}
