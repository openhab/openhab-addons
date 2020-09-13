package org.openhab.binding.enera.internal.model;

public class EneraHousehold {
    private String ID;
    private String Name;
    private int FlatSize;
    private int AdultCount;
    private int ChildCount;
    private String Address;

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
     * @return the flatSize
     */
    public int getFlatSize() {
        return FlatSize;
    }

    /**
     * @param flatSize the flatSize to set
     */
    public void setFlatSize(int flatSize) {
        FlatSize = flatSize;
    }

    /**
     * @return the adultCount
     */
    public int getAdultCount() {
        return AdultCount;
    }

    /**
     * @param adultCount the adultCount to set
     */
    public void setAdultCount(int adultCount) {
        AdultCount = adultCount;
    }

    /**
     * @return the childCount
     */
    public int getChildCount() {
        return ChildCount;
    }

    /**
     * @param childCount the childCount to set
     */
    public void setChildCount(int childCount) {
        ChildCount = childCount;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return Address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        Address = address;
    }

    
}