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
package org.openhab.binding.modbus.hager.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The value of one channel, decoded from a Modbus register.
 *
 * @param groupId channel group the channel belongs to
 * @param channelId channel id within the channel group
 * @param state the decoded state
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public record ChannelUpdate(String groupId, String channelId, State state) {
}
