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
package org.openhab.binding.siemenshvac.internal.metadata;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacMetadataDataPoint extends SiemensHvacMetadata {
    private String dptId = "-1";
    private String dptType = "";
    private @Nullable String dptUnit = null;
    private @Nullable String min = null;
    private @Nullable String max = null;
    private @Nullable String resolution = null;
    private @Nullable String fieldWitdh = null;
    private @Nullable String decimalDigits = null;
    private boolean detailsResolved = false;
    private @Nullable String dialogType = null;
    private @Nullable String maxLength = null;
    private @Nullable String address = null;
    private int dptSubKey = -1;
    private boolean writeAccess = false;

    private @NotNull List<SiemensHvacMetadataPointChild> child = List.of();

    public SiemensHvacMetadataDataPoint() {
        child = new ArrayList<SiemensHvacMetadataPointChild>();
    }

    public String getDptType() {
        return dptType;
    }

    public void setDptType(String dptType) {
        this.dptType = dptType;
    }

    public List<SiemensHvacMetadataPointChild> getChild() {
        return child;
    }

    public void setChild(List<SiemensHvacMetadataPointChild> child) {
        this.child = child;
    }

    public String getDptId() {
        return dptId;
    }

    public void setDptId(String dptId) {
        this.dptId = dptId;
    }

    public int getDptSubKey() {
        return dptSubKey;
    }

    public void setDptSubKey(int dptSubKey) {
        this.dptSubKey = dptSubKey;
    }

    public @Nullable String getAddress() {
        return address;
    }

    public void setWriteAccess(boolean writeAccess) {
        this.writeAccess = writeAccess;
    }

    public boolean getWriteAccess() {
        return writeAccess;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public @Nullable String getDptUnit() {
        return dptUnit;
    }

    public void setDptUnit(String dptUnit) {
        this.dptUnit = dptUnit;
    }

    public @Nullable String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public @Nullable String getDialogType() {
        return dialogType;
    }

    public void setDialogType(String dialogType) {
        this.dialogType = dialogType;
    }

    public @Nullable String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public @Nullable String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public @Nullable String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public @Nullable String getFieldWitdh() {
        return fieldWitdh;
    }

    public void setFieldWitdh(String fieldWitdh) {
        this.fieldWitdh = fieldWitdh;
    }

    public @Nullable String getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(String decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    public Boolean getDetailsResolved() {
        return detailsResolved;
    }

    public void setDetailsResolved(Boolean detailsResolved) {
        this.detailsResolved = detailsResolved;
    }

    public void resolveDptDetails(JsonObject result) {
        JsonObject subResultObj = result.getAsJsonObject("Result");
        JsonObject desc = result.getAsJsonObject("Description");

        if (subResultObj.has("Success")) {
            JsonObject error = subResultObj.getAsJsonObject("Error");
            String errorMsg = "";
            if (error != null) {
                errorMsg = error.get("Txt").getAsString();
            }

            if (("datatype not supported").equals(errorMsg)) {
                detailsResolved = true;
                return;
            }
        }

        if (desc != null) {
            this.dptType = desc.get("Type").getAsString();

            if (SiemensHvacBindingConstants.DPT_TYPE_ENUM.equals(dptType)) {
                JsonArray enums = desc.getAsJsonArray("Enums");

                for (Object obj : enums) {
                    JsonObject entry = (JsonObject) obj;

                    SiemensHvacMetadataPointChild ch = new SiemensHvacMetadataPointChild();
                    ch.setText(entry.get("Text").getAsString());
                    ch.setValue(entry.get("Value").getAsString());
                    ch.setIsActive(entry.get("IsCurrentValue").getAsString());
                    child.add(ch);
                }
            } else if (SiemensHvacBindingConstants.DPT_TYPE_NUMERIC.equals(dptType)) {
                this.dptUnit = desc.get("Unit").getAsString();
                this.min = desc.get("Min").getAsString();
                this.max = desc.get("Max").getAsString();
                this.resolution = desc.get("Resolution").getAsString();
                this.fieldWitdh = desc.get("FieldWitdh").getAsString();
                this.decimalDigits = desc.get("DecimalDigits").getAsString();
            } else if (SiemensHvacBindingConstants.DPT_TYPE_STRING.equals(dptType)) {
                this.dialogType = desc.get("DialogType").getAsString();
                this.maxLength = desc.get("MaxLength").getAsString();
            } else if (SiemensHvacBindingConstants.DPT_TYPE_RADIO.equals(dptType)) {
                JsonArray buttons = desc.getAsJsonArray("Buttons");

                child = new ArrayList<SiemensHvacMetadataPointChild>();

                for (Object obj : buttons) {
                    JsonObject button = (JsonObject) obj;

                    SiemensHvacMetadataPointChild ch = new SiemensHvacMetadataPointChild();
                    ch.setOpt0(button.get("TextOpt0").getAsString());
                    ch.setOpt1(button.get("TextOpt1").getAsString());
                    ch.setIsActive(button.get("IsActive").getAsString());
                    child.add(ch);
                }
            }

            detailsResolved = true;
        }
    }
}
