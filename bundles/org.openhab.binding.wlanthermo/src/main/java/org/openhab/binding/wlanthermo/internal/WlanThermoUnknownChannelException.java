/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

/**
 * The {@link WlanThermoUnknownChannelException} is thrown if a channel or trigger is unknown
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
public class WlanThermoUnknownChannelException extends WlanThermoException {

    static final long serialVersionUID = 1l;

    public WlanThermoUnknownChannelException() {
        super(WlanThermoBindingConstants.UNKNOWN_CHANNEL_EXCEPTION);
    }
}
