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
package org.openhab.binding.nuki.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link WebApiBridgeDiscoveryDto} class defines the Data Transfer Object (POJO) for a response of
 * the https://api.nuki.io/discover/bridges Web API.
 *
 * @author Jan Vybíral - Initial contribution
 */
public class WebApiBridgeDiscoveryDto {
    private List<WebApiBridgeDto> bridges;
    private Integer errorCode;

    @NonNull
    public List<WebApiBridgeDto> getBridges() {
        if (bridges == null) {
            bridges = new ArrayList<>();
        }
        return bridges;
    }

    public void setBridges(List<WebApiBridgeDto> bridges) {
        this.bridges = bridges;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "WebApiBridgeDiscoveryDto{" + "bridges=" + bridges + ", errorCode=" + errorCode + '}';
    }
}
