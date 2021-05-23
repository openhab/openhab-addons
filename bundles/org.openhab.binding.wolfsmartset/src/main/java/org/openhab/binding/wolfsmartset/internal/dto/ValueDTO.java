
package org.openhab.binding.wolfsmartset.internal.dto;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class ValueDTO {

    @SerializedName("ValueId")
    @Expose
    private Long valueId;
    @SerializedName("Value")
    @Expose
    private String value;
    @SerializedName("State")
    @Expose
    private Integer state;

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
