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
package org.openhab.binding.linktap.protocol.frames;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TimeDataResp} defines a response that defines the time, date and weekday
 * from the gateway.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class TimeDataResp extends HandshakeResp {

    public TimeDataResp() {
    }
}
