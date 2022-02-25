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
package org.openhab.binding.sunsa.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaDeviceConfiguration {
    public static final String KEY_ID = "id";

    public String id = "";
    public int configurablePositionOpen = 0;
    public int configurablePositionClosed = -100;
}
