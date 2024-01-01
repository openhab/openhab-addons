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
package org.openhab.binding.mqtt.homie.internal.homie300;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.generic.ChannelState;

/**
 * Helper to access {@link Property} internals.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class PropertyHelper {
    public static void setChannelState(Property property, @Nullable ChannelState channelState) {
        property.channelState = channelState;
    }
}
