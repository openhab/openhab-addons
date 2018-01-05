package org.openhab.binding.draytonwiser.internal.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("ProductType")
    @Expose
    private String productType;
    @SerializedName("ProductIdentifier")
    @Expose
    private String productIdentifier;
    @SerializedName("ActiveFirmwareVersion")
    @Expose
    private String activeFirmwareVersion;
    @SerializedName("Manufacturer")
    @Expose
    private String manufacturer;
    @SerializedName("ModelIdentifier")
    @Expose
    private String modelIdentifier;
    @SerializedName("HardwareVersion")
    @Expose
    private String hardwareVersion;
    @SerializedName("SerialNumber")
    @Expose
    private String serialNumber;
    @SerializedName("ProductRange")
    @Expose
    private String productRange;
    @SerializedName("ProductModel")
    @Expose
    private String productModel;
    @SerializedName("ProductFamily")
    @Expose
    private String productFamily;
    @SerializedName("DisplayedSignalStrength")
    @Expose
    private String displayedSignalStrength;
    @SerializedName("BatteryVoltage")
    @Expose
    private Integer batteryVoltage;
    @SerializedName("BatteryLevel")
    @Expose
    private String batteryLevel;
    @SerializedName("Rssi")
    @Expose
    private Integer rssi;
    @SerializedName("Lqi")
    @Expose
    private Integer lqi;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }

    public void setProductIdentifier(String productIdentifier) {
        this.productIdentifier = productIdentifier;
    }

    public String getActiveFirmwareVersion() {
        return activeFirmwareVersion;
    }

    public void setActiveFirmwareVersion(String activeFirmwareVersion) {
        this.activeFirmwareVersion = activeFirmwareVersion;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelIdentifier() {
        return modelIdentifier;
    }

    public void setModelIdentifier(String modelIdentifier) {
        this.modelIdentifier = modelIdentifier;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getProductRange() {
        return productRange;
    }

    public void setProductRange(String productRange) {
        this.productRange = productRange;
    }

    public String getProductModel() {
        return productModel;
    }

    public void setProductModel(String productModel) {
        this.productModel = productModel;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(String productFamily) {
        this.productFamily = productFamily;
    }

    public String getDisplayedSignalStrength() {
        return displayedSignalStrength;
    }

    public void setDisplayedSignalStrength(String displayedSignalStrength) {
        this.displayedSignalStrength = displayedSignalStrength;
    }

    public Integer getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(Integer batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Integer getLqi() {
        return lqi;
    }

    public void setLqi(Integer lqi) {
        this.lqi = lqi;
    }

}
