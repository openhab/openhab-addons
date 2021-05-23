package org.openhab.binding.wolfsmartset.internal.dto;

import javax.annotation.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class CreateSession2DTO {

    @SerializedName("CultureInfoCode")
    @Expose
    private String cultureInfoCode;
    @SerializedName("IsPasswordReset")
    @Expose
    private Boolean isPasswordReset;
    @SerializedName("IsProfessional")
    @Expose
    private Boolean isProfessional;
    @SerializedName("IsProfessionalPasswordReset")
    @Expose
    private Boolean isProfessionalPasswordReset;
    @SerializedName("BrowserSessionId")
    @Expose
    private Integer browserSessionId;

    public String getCultureInfoCode() {
        return cultureInfoCode;
    }

    public void setCultureInfoCode(String cultureInfoCode) {
        this.cultureInfoCode = cultureInfoCode;
    }

    public Boolean getIsPasswordReset() {
        return isPasswordReset;
    }

    public void setIsPasswordReset(Boolean isPasswordReset) {
        this.isPasswordReset = isPasswordReset;
    }

    public Boolean getIsProfessional() {
        return isProfessional;
    }

    public void setIsProfessional(Boolean isProfessional) {
        this.isProfessional = isProfessional;
    }

    public Boolean getIsProfessionalPasswordReset() {
        return isProfessionalPasswordReset;
    }

    public void setIsProfessionalPasswordReset(Boolean isProfessionalPasswordReset) {
        this.isProfessionalPasswordReset = isProfessionalPasswordReset;
    }

    public Integer getBrowserSessionId() {
        return browserSessionId;
    }

    public void setBrowserSessionId(Integer browserSessionId) {
        this.browserSessionId = browserSessionId;
    }
}
