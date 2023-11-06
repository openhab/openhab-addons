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
package org.openhab.binding.jablotron.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronDataUpdateResponse} class defines the data update call
 * response.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronDataUpdateResponse {
    boolean status = false;
    JablotronData data = new JablotronData();

    @SerializedName("error_message")
    String errorMessage = "";

    public boolean isStatus() {
        return status;
    }

    public JablotronData getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
