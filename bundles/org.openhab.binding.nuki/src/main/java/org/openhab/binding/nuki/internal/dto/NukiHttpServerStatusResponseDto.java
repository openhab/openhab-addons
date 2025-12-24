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
package org.openhab.binding.nuki.internal.dto;

/**
 * The {@link NukiHttpServerStatusResponseDto} class defines the Data Transfer Object (POJO) for a status response of
 * the Nuki HTTP
 * Server.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiHttpServerStatusResponseDto {

    private String status;

    public NukiHttpServerStatusResponseDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
