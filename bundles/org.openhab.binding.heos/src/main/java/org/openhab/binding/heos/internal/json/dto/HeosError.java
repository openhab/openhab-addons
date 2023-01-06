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
package org.openhab.binding.heos.internal.json.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Error object for containing information about HEOS errors
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosError {
    public final HeosErrorCode code;
    private final @Nullable Long systemErrorNumber;

    HeosError(@Nullable Long errorCode, @Nullable Long systemErrorNumber) {
        if (errorCode == null) {
            throw new IllegalArgumentException("Error code not given");
        }
        this.code = HeosErrorCode.of(errorCode);
        this.systemErrorNumber = systemErrorNumber;
    }

    @Override
    public String toString() {
        return "HeosError{" + "code=" + code + ", systemErrorNumber=" + systemErrorNumber + '}';
    }
}
