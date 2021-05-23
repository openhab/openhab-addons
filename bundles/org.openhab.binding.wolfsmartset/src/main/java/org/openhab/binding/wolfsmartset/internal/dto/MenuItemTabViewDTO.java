package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MenuItemTabViewDTO {
    @SerializedName("IsExpertView")
    @Expose
    public Boolean IsExpertView;

    @SerializedName("TabName")
    @Expose
    public String TabName;

    @SerializedName("GuiId")
    @Expose
    public Long GuiId;

    @SerializedName("BundleId")
    @Expose
    public Long BundleId;

    @SerializedName("ParameterDescriptors")
    @Expose
    public List<ParameterDescriptorDTO> ParameterDescriptors;

    @SerializedName("ViewType")
    @Expose
    public Long ViewType;

    @SerializedName("SvgSchemaDeviceId")
    @Expose
    public Long SvgSchemaDeviceId;
}
