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
package org.openhab.binding.heos.internal.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.heos.internal.json.dto.HeosErrorCode;

/**
 * Exception to inform the caller that there is functional error reported by the HEOS system
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosFunctionalException extends IOException {
    private final HeosErrorCode code;

    public HeosFunctionalException(HeosErrorCode code) {
        this.code = code;
    }

    public HeosErrorCode getCode() {
        return code;
    }
}
