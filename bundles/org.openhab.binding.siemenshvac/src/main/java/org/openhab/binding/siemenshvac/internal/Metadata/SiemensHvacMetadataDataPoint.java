package org.openhab.binding.siemenshvac.internal.Metadata;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SiemensHvacMetadataDataPoint extends SiemensHvacMetadata {
    private int dptId = -1;
    private String dptType = null;
    private String dptUnit = null;
    private String min = null;
    private String max = null;
    private String resolution = null;
    private String fieldWitdh = null;
    private String decimalDigits = null;
    private Boolean detailsResolved = false;
    private String dialogType = null;
    private String maxLength = null;
    private String address = null;
    private int dptSubKey = -1;
    private boolean writeAccess = false;
    private List<SiemensHvacMetadataPointChild> child = null;

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

    public int getDptId() {
        return dptId;
    }

    public void setDptId(int dptId) {
        this.dptId = dptId;
    }

    public int getDptSubKey() {
        return dptSubKey;
    }

    public void setDptSubKey(int dptSubKey) {
        this.dptSubKey = dptSubKey;
    }

    public String getAddress() {
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

    public String getDptUnit() {
        return dptUnit;
    }

    public void setDptUnit(String dptUnit) {
        this.dptUnit = dptUnit;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }

    public String getDialogType() {
        return dialogType;
    }

    public void setDialogType(String dialogType) {
        this.dialogType = dialogType;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getFieldWitdh() {
        return fieldWitdh;
    }

    public void setFieldWitdh(String fieldWitdh) {
        this.fieldWitdh = fieldWitdh;
    }

    public String getDecimalDigits() {
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
        if (result == null) {
            return;
        }

        JsonObject desc = result.getAsJsonObject("Description");

        if (desc != null) {
            this.dptType = desc.get("Type").getAsString();

            if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_ENUM)) {

                JsonArray enums = desc.getAsJsonArray("Enums");
                child = new ArrayList<SiemensHvacMetadataPointChild>();

                for (Object obj : enums) {
                    JsonObject entry = (JsonObject) obj;

                    SiemensHvacMetadataPointChild ch = new SiemensHvacMetadataPointChild();
                    ch.setText(entry.get("Text").getAsString());
                    ch.setValue(entry.get("Value").getAsString());
                    ch.setIsActive(entry.get("IsCurrentValue").getAsString());
                    child.add(ch);
                }
            } else if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_NUMERIC)) {
                this.dptUnit = desc.get("Unit").getAsString();
                this.min = desc.get("Min").getAsString();
                this.max = desc.get("Max").getAsString();
                this.resolution = desc.get("Resolution").getAsString();
                this.fieldWitdh = desc.get("FieldWitdh").getAsString();
                this.decimalDigits = desc.get("DecimalDigits").getAsString();
            } else if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_STRING)) {
                this.dialogType = desc.get("DialogType").getAsString();
                this.maxLength = desc.get("MaxLength").getAsString();
            } else if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_RADIO)) {
                JsonArray buttons = desc.getAsJsonArray("Buttons");

                child = new ArrayList<SiemensHvacMetadataPointChild>();

                for (Object obj : buttons) {
                    JsonObject button = (JsonObject) obj;

                    SiemensHvacMetadataPointChild ch = new SiemensHvacMetadataPointChild();
                    ch.setOpt0(button.get("TextOpt0").getAsString());
                    ch.setOpt1(button.get("TextOpt1").getAsString());
                    ch.setIsActive(button.get("IsActive").getAsString());
                    child.add(ch);

                    String signifiance = button.get("Significance").getAsString();
                }
            } else if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_DATE)) {
                System.out.println("");
            } else if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_TIME)) {
                System.out.println("");
            } else if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_SCHEDULER)) {
                System.out.println("");
            } else if (dptType.equals(SiemensHvacBindingConstants.DPT_TYPE_CALENDAR)) {
                System.out.println("");
            } else {
                System.out.println("");
            }
            detailsResolved = true;
        }

    }

}
