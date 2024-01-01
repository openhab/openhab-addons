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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class PortalIotCommandXmlResponse extends AbstractPortalIotCommandResponse {
    @SerializedName("resp")
    private final String responseXml;

    public PortalIotCommandXmlResponse(String result, String responseXml, int errorCode, String errorMessage) {
        super(result, errorCode, errorMessage);
        this.responseXml = responseXml;
    }

    public String getResponsePayloadXml() {
        return responseXml != null ? responseXml.replaceAll("\n|\r", "") : null;
    }
}
