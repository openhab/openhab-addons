package org.openhab.binding.onecta.internal.api.dto.authentication;

import com.google.gson.annotations.SerializedName;

public class RespStep7 {
    @SerializedName("callId")
    public String callId;
    @SerializedName("errorCode")
    public Integer errorCode;
    @SerializedName("errorDetails")
    public String errorDetails;
    @SerializedName("errorMessage")
    public String errorMessage;
    @SerializedName("apiVersion")
    public Integer apiVersion;
    @SerializedName("statusCode")
    public Integer statusCode;
    @SerializedName("statusReason")
    public String statusReason;
    @SerializedName("time")
    public String time;
    @SerializedName("errorFlags")
    public String errorFlags;
    @SerializedName("registeredTimestamp")
    public Integer registeredTimestamp;
    @SerializedName("uID")
    public String uID;
    @SerializedName("uIDSignature")
    public String uIDSignature;
    @SerializedName("signatureTimestamp")
    public String signatureTimestamp;
    @SerializedName("created")
    public String created;
    @SerializedName("createdTimestamp")
    public Integer createdTimestamp;
    @SerializedName("isActive")
    public boolean isActive;
    @SerializedName("isRegistered")
    public boolean isRegistered;
    @SerializedName("isVerified")
    public boolean isVerified;
    @SerializedName("lastLogin")
    public String lastLogin;
    @SerializedName("lastLoginTimestamp")
    public Integer lastLoginTimestamp;
    @SerializedName("lastUpdated")
    public String lastUpdated;
    @SerializedName("lastUpdatedTimestamp")
    public Long lastUpdatedTimestamp;
    @SerializedName("loginProvider")
    public String loginProvider;
    @SerializedName("oldestDataUpdated")
    public String oldestDataUpdated;
    @SerializedName("oldestDataUpdatedTimestamp")
    public Long oldestDataUpdatedTimestamp;
    @SerializedName("profile")
    public RespStep7Profile profile;
    @SerializedName("registered")
    public String registered;
    @SerializedName("socialProviders")
    public String socialProviders;
    @SerializedName("verified")
    public String verified;
    @SerializedName("verifiedTimestamp")
    public Long verifiedTimestamp;
    @SerializedName("newUser")
    public boolean newUser;
    @SerializedName("sessionInfo")
    public RespStep7SessionInfo sessionInfo;

    public String getCallId() {
        return callId;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public Integer getApiVersion() {
        return apiVersion;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public String getTime() {
        return time;
    }

    public Integer getRegisteredTimestamp() {
        return registeredTimestamp;
    }

    public String getuID() {
        return uID;
    }

    public String getuIDSignature() {
        return uIDSignature;
    }

    public String getSignatureTimestamp() {
        return signatureTimestamp;
    }

    public String getCreated() {
        return created;
    }

    public Integer getCreatedTimestamp() {
        return createdTimestamp;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public Integer getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public Long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public String getLoginProvider() {
        return loginProvider;
    }

    public String getOldestDataUpdated() {
        return oldestDataUpdated;
    }

    public Long getOldestDataUpdatedTimestamp() {
        return oldestDataUpdatedTimestamp;
    }

    public RespStep7Profile getProfile() {
        return profile;
    }

    public String getRegistered() {
        return registered;
    }

    public String getSocialProviders() {
        return socialProviders;
    }

    public String getVerified() {
        return verified;
    }

    public Long getVerifiedTimestamp() {
        return verifiedTimestamp;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public RespStep7SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorFlags() {
        return errorFlags;
    }
}
