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
package org.openhab.binding.jablotron.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronService} class defines the service object
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronService {

    @SerializedName("data_type")
    String dataType = "";

    JablotronServiceDetail data = new JablotronServiceDetail();

    public String getDataType() {
        return dataType;
    }

    public JablotronServiceDetail getData() {
        return data;
    }
}
