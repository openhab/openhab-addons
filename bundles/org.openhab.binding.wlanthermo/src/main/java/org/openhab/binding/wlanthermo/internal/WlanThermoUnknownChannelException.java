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
package org.openhab.binding.wlanthermo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;

/**
 * The {@link WlanThermoUnknownChannelException} is thrown if a channel or trigger is unknown
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoUnknownChannelException extends WlanThermoException {

    static final long serialVersionUID = 1L;
    public static final String UNKNOWN_CHANNEL_EXCEPTION = "Channel or Trigger unknown!";

    public WlanThermoUnknownChannelException() {
        super(UNKNOWN_CHANNEL_EXCEPTION);
    }

    public WlanThermoUnknownChannelException(ChannelUID channelUID) {
        super(UNKNOWN_CHANNEL_EXCEPTION + "ChannelUID: " + channelUID.toString());
    }

    public WlanThermoUnknownChannelException(ChannelUID channelUID, Throwable cause) {
        super(UNKNOWN_CHANNEL_EXCEPTION + "ChannelUID: " + channelUID.toString(), cause);
    }
}
