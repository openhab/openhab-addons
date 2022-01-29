/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.revogi.internal.api;

import java.util.Objects;

/**
 * The class {@link SwitchResponseDTO} describes the response when you switch a plug
 *
 * @author Andi Bräu - Initial contribution
 */
public class SwitchResponseDTO {
    private final int response;
    private final int code;

    public SwitchResponseDTO(int response, int code) {
        this.response = response;
        this.code = code;
    }

    public int getResponse() {
        return response;
    }

    public int getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SwitchResponseDTO that = (SwitchResponseDTO) o;
        return response == that.response && code == that.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(response, code);
    }
}
