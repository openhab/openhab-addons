/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        super();
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
