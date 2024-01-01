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
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * generated with https://www.jsonschema2pojo.org/
 * 
 * @author Bo Biene - Initial contribution
 */
public class MenuItemTabViewDTO {
    @SerializedName("IsExpertView")
    @Expose
    public Boolean isExpertView;

    @SerializedName("TabName")
    @Expose
    public String tabName;

    @SerializedName("GuiId")
    @Expose
    public Long guiId;

    @SerializedName("BundleId")
    @Expose
    public Long bundleId;

    @SerializedName("ParameterDescriptors")
    @Expose
    public List<ParameterDescriptorDTO> parameterDescriptors;

    @SerializedName("ViewType")
    @Expose
    public Long viewType;

    @SerializedName("SvgSchemaDeviceId")
    @Expose
    public Long svgSchemaDeviceId;
}
