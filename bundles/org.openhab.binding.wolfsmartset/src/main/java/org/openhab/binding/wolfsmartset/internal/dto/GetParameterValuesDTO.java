
package org.openhab.binding.wolfsmartset.internal.dto;

import java.util.List;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class GetParameterValuesDTO {

    @SerializedName("LastAccess")
    @Expose
    private String lastAccess;
    @SerializedName("Values")
    @Expose
    private List<ValueDTO> values = null;
    @SerializedName("IsNewJobCreated")
    @Expose
    private Boolean isNewJobCreated;

    public String getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(String lastAccess) {
        this.lastAccess = lastAccess;
    }

    public List<ValueDTO> getValues() {
        return values;
    }

    public void setValues(List<ValueDTO> values) {
        this.values = values;
    }

    public Boolean getIsNewJobCreated() {
        return isNewJobCreated;
    }

    public void setIsNewJobCreated(Boolean isNewJobCreated) {
        this.isNewJobCreated = isNewJobCreated;
    }
}
