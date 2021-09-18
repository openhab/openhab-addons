package org.openhab.binding.freeboxos.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class FbxDevice extends ApiVersion {

    private int id;
    @SerializedName(value = "mac", alternate = { "main_mac" })
    private @NonNullByDefault({}) String mac;

    @SerializedName(value = "device_name", alternate = { "name" })
    private @Nullable String name;

    @SerializedName(value = "device_model", alternate = { "model" })
    private String model = "";

    public int getId() {
        return id;
    }

    public String getMac() {
        return mac.toLowerCase();
    }

    public @Nullable String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    /**
     * @return a string like eg : '17/api/v8'
     */
    @Override
    public String baseUrl() {
        return String.format("%d%s/", id, super.baseUrl());
    }
}
