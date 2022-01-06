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
package org.openhab.binding.jablotron.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronGetEventHistoryResponse} class defines the response for the
 * getEventHistory operation
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronGetEventHistoryResponse {

    @SerializedName("http-code")
    int httpCode = -1;

    JablotronHistoryData data = new JablotronHistoryData();

    public int getHttpCode() {
        return httpCode;
    }

    public JablotronHistoryData getData() {
        return data;
    }
}
