package org.openhab.binding.enera.internal.model;

public class EneraElectricConsumer {
    private String ID;
    private String Name;
    private String Type;
    private String Brand;
    private int UsageFrequency;
    private int UsageFrequencyUnit;
    private int PurchaseYear;

    /**
     * @return the iD
     */
    public String getID() {
        return ID;
    }

    /**
     * @param iD the iD to set
     */
    public void setID(String iD) {
        ID = iD;
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
     * @return the type
     */
    public String getType() {
        return Type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        Type = type;
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
     * @return the usageFrequency
     */
    public int getUsageFrequency() {
        return UsageFrequency;
    }

    /**
     * @param usageFrequency the usageFrequency to set
     */
    public void setUsageFrequency(int usageFrequency) {
        UsageFrequency = usageFrequency;
    }

    /**
     * @return the usageFrequencyUnit
     */
    public int getUsageFrequencyUnit() {
        return UsageFrequencyUnit;
    }

    /**
     * @param usageFrequencyUnit the usageFrequencyUnit to set
     */
    public void setUsageFrequencyUnit(int usageFrequencyUnit) {
        UsageFrequencyUnit = usageFrequencyUnit;
    }

    /**
     * @return the purchaseYear
     */
    public int getPurchaseYear() {
        return PurchaseYear;
    }

    /**
     * @param purchaseYear the purchaseYear to set
     */
    public void setPurchaseYear(int purchaseYear) {
        PurchaseYear = purchaseYear;
    }

    
}