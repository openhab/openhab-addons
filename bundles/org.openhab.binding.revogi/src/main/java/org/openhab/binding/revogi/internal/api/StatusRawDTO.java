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

/**
 * The class {@link StatusRawDTO} represents the raw data received from Revogi's SmartStrip
 *
 * @author Andi Bräu - Initial contribution
 */
public class StatusRawDTO {
    private final int response;
    private final int code;
    private final StatusDTO data;

    public StatusRawDTO(int response, int code, StatusDTO data) {
        this.response = response;
        this.code = code;
        this.data = data;
    }

    public int getResponse() {
        return response;
    }

    public int getCode() {
        return code;
    }

    public StatusDTO getData() {
        return data;
    }
}
