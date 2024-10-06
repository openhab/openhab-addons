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
package org.openhab.binding.linktap.protocol.http;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linktap.protocol.frames.GatewayDeviceResponse;

/**
 * The {@link InvalidParameterException} should be thrown when the endpoint being communicated with
 * does not appear to be a Tap Link Gateway device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class InvalidParameterException extends I18Exception {
    @Serial
    private static final long serialVersionUID = -7784829499604153947L;

    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(final String message) {
        super(message);
    }

    public InvalidParameterException(final Throwable cause) {
        super(cause);
    }

    public InvalidParameterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public InvalidParameterException(final GatewayDeviceResponse.ResultStatus rs) {
        super(rs.getDesc());
        this.i18Key = rs.getI18Key();
    }

    public String getI18Key() {
        return getI18Key("exception.invalid-parameter-exception");
    }
}
