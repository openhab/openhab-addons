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
package org.openhab.binding.semsportal.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * POJO containing details about the inverter. Only a very small subset of the available properties is mapped.
 * 
 * @author Julio Gesser - Initial contribution
 */
public class StationInfo {

    @SerializedName("date_format")
    private String dateFormat;

    public String getDateFormat() {
        return dateFormat;
    }
}
