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
package org.openhab.binding.tuya.internal.local.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RequestRefusal} is the reply of a device to a request it does not handle, such as a DP_QUERY sent to a
 * device that only reports its status in reply to a CONTROL
 *
 * @param reason the text the device replied with instead of JSON
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@NonNullByDefault
public record RequestRefusal(String reason) {
}
