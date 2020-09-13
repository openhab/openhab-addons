package org.openhab.binding.enera.internal.model;

import java.util.List;

public class EneraAccount {
    private String ID;
    private String Email;
    private List<EneraDevice> Devices;
    private List<EneraHousehold> Households;
    private List<EneraElectricConsumer> ElectricConsumers;
    private EneraDataProtection DataProtection;
    private boolean ShowAdapterPlugs;
    private boolean IsProsumer;
    private String Name;
    private String FirstName;
    private String LastName;
    private String ZipCode;
    private String Street;
    private String City;
    private String Salutation;
    private String MobilePhone;
    private String BirthDate;
    private String Phone;
    private String id;
    private String LiveURI;

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
     * @return the email
     */
    public String getEmail() {
        return Email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        Email = email;
    }

    /**
     * @return the devices
     */
    public List<EneraDevice> getDevices() {
        return Devices;
    }

    /**
     * @param devices the devices to set
     */
    public void setDevices(List<EneraDevice> devices) {
        Devices = devices;
    }

    /**
     * @return the households
     */
    public List<EneraHousehold> getHouseholds() {
        return Households;
    }

    /**
     * @param households the households to set
     */
    public void setHouseholds(List<EneraHousehold> households) {
        Households = households;
    }

    /**
     * @return the electricConsumers
     */
    public List<EneraElectricConsumer> getElectricConsumers() {
        return ElectricConsumers;
    }

    /**
     * @param electricConsumers the electricConsumers to set
     */
    public void setElectricConsumers(List<EneraElectricConsumer> electricConsumers) {
        ElectricConsumers = electricConsumers;
    }

    /**
     * @return the dataProtection
     */
    public EneraDataProtection getDataProtection() {
        return DataProtection;
    }

    /**
     * @param dataProtection the dataProtection to set
     */
    public void setDataProtection(EneraDataProtection dataProtection) {
        DataProtection = dataProtection;
    }

    /**
     * @return the showAdapterPlugs
     */
    public boolean isShowAdapterPlugs() {
        return ShowAdapterPlugs;
    }

    /**
     * @param showAdapterPlugs the showAdapterPlugs to set
     */
    public void setShowAdapterPlugs(boolean showAdapterPlugs) {
        ShowAdapterPlugs = showAdapterPlugs;
    }

    /**
     * @return the isProsumer
     */
    public boolean isIsProsumer() {
        return IsProsumer;
    }

    /**
     * @param isProsumer the isProsumer to set
     */
    public void setIsProsumer(boolean isProsumer) {
        IsProsumer = isProsumer;
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
     * @return the firstName
     */
    public String getFirstName() {
        return FirstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return LastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        LastName = lastName;
    }

    /**
     * @return the zipCode
     */
    public String getZipCode() {
        return ZipCode;
    }

    /**
     * @param zipCode the zipCode to set
     */
    public void setZipCode(String zipCode) {
        ZipCode = zipCode;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return Street;
    }

    /**
     * @param street the street to set
     */
    public void setStreet(String street) {
        Street = street;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return City;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        City = city;
    }

    /**
     * @return the salutation
     */
    public String getSalutation() {
        return Salutation;
    }

    /**
     * @param salutation the salutation to set
     */
    public void setSalutation(String salutation) {
        Salutation = salutation;
    }

    /**
     * @return the mobilePhone
     */
    public String getMobilePhone() {
        return MobilePhone;
    }

    /**
     * @param mobilePhone the mobilePhone to set
     */
    public void setMobilePhone(String mobilePhone) {
        MobilePhone = mobilePhone;
    }

    /**
     * @return the birthDate
     */
    public String getBirthDate() {
        return BirthDate;
    }

    /**
     * @param birthDate the birthDate to set
     */
    public void setBirthDate(String birthDate) {
        BirthDate = birthDate;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return Phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        Phone = phone;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the liveURI
     */
    public String getLiveURI() {
        return LiveURI;
    }

    /**
     * @param liveURI the liveURI to set
     */
    public void setLiveURI(String liveURI) {
        LiveURI = liveURI;
    }

    

    
}