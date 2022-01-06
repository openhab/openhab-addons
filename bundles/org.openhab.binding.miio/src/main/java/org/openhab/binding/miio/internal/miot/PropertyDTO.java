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
package org.openhab.binding.miio.internal.miot;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json for miot device info
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class PropertyDTO {

    @SerializedName("iid")
    @Expose
    public Integer piid;
    @SerializedName("type")
    @Expose
    public String type;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("format")
    @Expose
    public String format;
    @SerializedName("access")
    @Expose
    public List<String> access = null;
    @SerializedName("value-list")
    @Expose
    public List<OptionsValueDescriptionsListDTO> valueList = null;
    @SerializedName("value-range")
    @Expose
    public List<Integer> valueRange = null;
    @SerializedName("unit")
    @Expose
    public String unit;
}
