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
package org.openhab.io.homekit.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * Different command types supported by HomekitOHItemProxy.
 *
 * @author Eugen Freiter - Initial contribution
 */

@NonNullByDefault
public enum HomekitCommandType {
    HUE_COMMAND,
    SATURATION_COMMAND,
    BRIGHTNESS_COMMAND,
    ON_COMMAND
}
