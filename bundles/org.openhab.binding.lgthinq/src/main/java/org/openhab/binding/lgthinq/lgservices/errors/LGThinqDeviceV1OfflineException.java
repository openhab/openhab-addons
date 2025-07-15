/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices.errors;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LGThinqDeviceV1OfflineException} - Normally caught by V1 API in monitoring device.
 * When the device is OFFLINE (away from internet), the API doesn't return data information and this
 * exception is thrown to indicate that this device is offline for monitoring
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinqDeviceV1OfflineException extends LGThinqApiException {
    @Serial
    private static final long serialVersionUID = 202409261450L;

    public LGThinqDeviceV1OfflineException(String message, Throwable cause) {
        super(message, cause);
    }

    public LGThinqDeviceV1OfflineException(String message) {
        super(message);
    }
}
