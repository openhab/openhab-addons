package org.openhab.binding.enera.internal.model;

import java.util.Date;

/**
 * EneraDevice
 */
public class EneraDevice {
    private String ID;
    private String Name;
    private String Brand;
    private String Serial;
    private String MeterID;
    private String HouseholdID;
    private EneraDevicePricing Pricing;
    private Date RegisteredAt;
    private boolean IsAdapterPlug;
    private String ParentDeviceID;
    private boolean IsInstallationCompleted;
    private boolean IsProductionMeter;
    private boolean ProductionPricing;

    /**
     * @return the iD
     */
    public String getId() {
        return ID;
    }

    /**
     * @param iD the iD to set
     */
    public void setId(String id) {
        ID = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return Name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        Name = name;
    }

    /**
     * @return the brand
     */
    public String getBrand() {
        return Brand;
    }

    /**
     * @param brand the brand to set
     */
    public void setBrand(String brand) {
        Brand = brand;
    }

    /**
     * @return the serial
     */
    public String getSerial() {
        return Serial;
    }

    /**
     * @param serial the serial to set
     */
    public void setSerial(String serial) {
        Serial = serial;
    }

    /**
     * @return the meterID
     */
    public String getMeterId() {
        return MeterID;
    }

    /**
     * @param meterID the meterID to set
     */
    public void setMeterID(String meterId) {
        MeterID = meterId;
    }

    /**
     * @return the householdID
     */
    public String getHouseholdId() {
        return HouseholdID;
    }

    /**
     * @param householdID the householdID to set
     */
    public void setHouseholdID(String householdId) {
        HouseholdID = householdId;
    }

    /**
     * @return the pricing
     */
    public EneraDevicePricing getPricing() {
        return Pricing;
    }

    /**
     * @param pricing the pricing to set
     */
    public void setPricing(EneraDevicePricing pricing) {
        Pricing = pricing;
    }

    /**
     * @return the registeredAt
     */
    public Date getRegisteredAt() {
        return RegisteredAt;
    }

    /**
     * @param registeredAt the registeredAt to set
     */
    public void setRegisteredAt(Date registeredAt) {
        RegisteredAt = registeredAt;
    }

    /**
     * @return the isAdapterPlug
     */
    public boolean isIsAdapterPlug() {
        return IsAdapterPlug;
    }

    /**
     * @param isAdapterPlug the isAdapterPlug to set
     */
    public void setIsAdapterPlug(boolean isAdapterPlug) {
        IsAdapterPlug = isAdapterPlug;
    }

    /**
     * @return the parentDeviceID
     */
    public String getParentDeviceId() {
        return ParentDeviceID;
    }

    /**
     * @param parentDeviceID the parentDeviceID to set
     */
    public void setParentDeviceId(String parentDeviceId) {
        ParentDeviceID = parentDeviceId;
    }

    /**
     * @return the isInstallationCompleted
     */
    public boolean isIsInstallationCompleted() {
        return IsInstallationCompleted;
    }

    /**
     * @param isInstallationCompleted the isInstallationCompleted to set
     */
    public void setIsInstallationCompleted(boolean isInstallationCompleted) {
        IsInstallationCompleted = isInstallationCompleted;
    }

    /**
     * @return the isProductionMeter
     */
    public boolean isIsProductionMeter() {
        return IsProductionMeter;
    }

    /**
     * @param isProductionMeter the isProductionMeter to set
     */
    public void setIsProductionMeter(boolean isProductionMeter) {
        IsProductionMeter = isProductionMeter;
    }

    /**
     * @return the productionPricing
     */
    public boolean isProductionPricing() {
        return ProductionPricing;
    }

    /**
     * @param productionPricing the productionPricing to set
     */
    public void setProductionPricing(boolean productionPricing) {
        ProductionPricing = productionPricing;
    }
}
