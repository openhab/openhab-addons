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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link JablotronServiceData} class defines the service data object
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronServiceData {

    @SerializedName("service_id")
    String serviceId = "";

    List<JablotronService> data = new ArrayList<>();

    public String getServiceId() {
        return serviceId;
    }

    public List<JablotronService> getData() {
        return data;
    }
}
