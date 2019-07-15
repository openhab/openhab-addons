package org.openhab.binding.hydrawise.internal.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PlanArray {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("planType")
    @Expose
    private String planType;
    @SerializedName("planType_key")
    @Expose
    private String planTypeKey;
    @SerializedName("sku")
    @Expose
    private Object sku;
    @SerializedName("discount")
    @Expose
    private String discount;
    @SerializedName("cost")
    @Expose
    private String cost;
    @SerializedName("cost_us")
    @Expose
    private String costUs;
    @SerializedName("cost_au")
    @Expose
    private String costAu;
    @SerializedName("cost_eu")
    @Expose
    private String costEu;
    @SerializedName("cost_ca")
    @Expose
    private String costCa;
    @SerializedName("cost_uk")
    @Expose
    private String costUk;
    @SerializedName("active")
    @Expose
    private String active;
    @SerializedName("controller_qty")
    @Expose
    private String controllerQty;
    @SerializedName("rainfall")
    @Expose
    private String rainfall;
    @SerializedName("sms_qty")
    @Expose
    private String smsQty;
    @SerializedName("scheduled_reports")
    @Expose
    private String scheduledReports;
    @SerializedName("email_alerts")
    @Expose
    private String emailAlerts;
    @SerializedName("define_sensor")
    @Expose
    private String defineSensor;
    @SerializedName("add_user")
    @Expose
    private String addUser;
    @SerializedName("contractor")
    @Expose
    private String contractor;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("sensor_pack")
    @Expose
    private String sensorPack;
    @SerializedName("filelimit")
    @Expose
    private String filelimit;
    @SerializedName("filetypeall")
    @Expose
    private String filetypeall;
    @SerializedName("plan_type")
    @Expose
    private String plan_type;
    @SerializedName("push_notification")
    @Expose
    private String pushNotification;
    @SerializedName("weather_qty")
    @Expose
    private String weatherQty;
    @SerializedName("weather_free_qty")
    @Expose
    private String weatherFreeQty;
    @SerializedName("reporting_days")
    @Expose
    private String reportingDays;
    @SerializedName("weather_hourly_updates")
    @Expose
    private String weatherHourlyUpdates;
    @SerializedName("free_enthusiast_plans")
    @Expose
    private String freeEnthusiastPlans;
    @SerializedName("visible")
    @Expose
    private String visible;
    @SerializedName("contractor_purchasable")
    @Expose
    private String contractorPurchasable;
    @SerializedName("boc")
    @Expose
    private String boc;
    @SerializedName("expiry")
    @Expose
    private String expiry;
    @SerializedName("start")
    @Expose
    private String start;
    @SerializedName("customerplan_id")
    @Expose
    private String customerplanId;

    /**
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return
     */
    public String getPlanType() {
        return planType;
    }

    /**
     * @param planType
     */
    public void setPlanType(String planType) {
        this.planType = planType;
    }

    /**
     * @return
     */
    public String getPlanTypeKey() {
        return planTypeKey;
    }

    /**
     * @param planTypeKey
     */
    public void setPlanType1Key(String planTypeKey) {
        this.planTypeKey = planTypeKey;
    }

    /**
     * @return
     */
    public Object getSku() {
        return sku;
    }

    /**
     * @param sku
     */
    public void setSku(Object sku) {
        this.sku = sku;
    }

    /**
     * @return
     */
    public String getDiscount() {
        return discount;
    }

    /**
     * @param discount
     */
    public void setDiscount(String discount) {
        this.discount = discount;
    }

    /**
     * @return
     */
    public String getCost() {
        return cost;
    }

    /**
     * @param cost
     */
    public void setCost(String cost) {
        this.cost = cost;
    }

    /**
     * @return
     */
    public String getCostUs() {
        return costUs;
    }

    /**
     * @param costUs
     */
    public void setCostUs(String costUs) {
        this.costUs = costUs;
    }

    /**
     * @return
     */
    public String getCostAu() {
        return costAu;
    }

    /**
     * @param costAu
     */
    public void setCostAu(String costAu) {
        this.costAu = costAu;
    }

    /**
     * @return
     */
    public String getCostEu() {
        return costEu;
    }

    /**
     * @param costEu
     */
    public void setCostEu(String costEu) {
        this.costEu = costEu;
    }

    /**
     * @return
     */
    public String getCostCa() {
        return costCa;
    }

    /**
     * @param costCa
     */
    public void setCostCa(String costCa) {
        this.costCa = costCa;
    }

    /**
     * @return
     */
    public String getCostUk() {
        return costUk;
    }

    /**
     * @param costUk
     */
    public void setCostUk(String costUk) {
        this.costUk = costUk;
    }

    /**
     * @return
     */
    public String getActive() {
        return active;
    }

    /**
     * @param active
     */
    public void setActive(String active) {
        this.active = active;
    }

    /**
     * @return
     */
    public String getControllerQty() {
        return controllerQty;
    }

    /**
     * @param controllerQty
     */
    public void setControllerQty(String controllerQty) {
        this.controllerQty = controllerQty;
    }

    /**
     * @return
     */
    public String getRainfall() {
        return rainfall;
    }

    /**
     * @param rainfall
     */
    public void setRainfall(String rainfall) {
        this.rainfall = rainfall;
    }

    /**
     * @return
     */
    public String getSmsQty() {
        return smsQty;
    }

    /**
     * @param smsQty
     */
    public void setSmsQty(String smsQty) {
        this.smsQty = smsQty;
    }

    /**
     * @return
     */
    public String getScheduledReports() {
        return scheduledReports;
    }

    /**
     * @param scheduledReports
     */
    public void setScheduledReports(String scheduledReports) {
        this.scheduledReports = scheduledReports;
    }

    /**
     * @return
     */
    public String getEmailAlerts() {
        return emailAlerts;
    }

    /**
     * @param emailAlerts
     */
    public void setEmailAlerts(String emailAlerts) {
        this.emailAlerts = emailAlerts;
    }

    /**
     * @return
     */
    public String getDefineSensor() {
        return defineSensor;
    }

    /**
     * @param defineSensor
     */
    public void setDefineSensor(String defineSensor) {
        this.defineSensor = defineSensor;
    }

    /**
     * @return
     */
    public String getAddUser() {
        return addUser;
    }

    /**
     * @param addUser
     */
    public void setAddUser(String addUser) {
        this.addUser = addUser;
    }

    /**
     * @return
     */
    public String getContractor() {
        return contractor;
    }

    /**
     * @param contractor
     */
    public void setContractor(String contractor) {
        this.contractor = contractor;
    }

    /**
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return
     */
    public String getSensorPack() {
        return sensorPack;
    }

    /**
     * @param sensorPack
     */
    public void setSensorPack(String sensorPack) {
        this.sensorPack = sensorPack;
    }

    /**
     * @return
     */
    public String getFilelimit() {
        return filelimit;
    }

    /**
     * @param filelimit
     */
    public void setFilelimit(String filelimit) {
        this.filelimit = filelimit;
    }

    /**
     * @return
     */
    public String getFiletypeall() {
        return filetypeall;
    }

    /**
     * @param filetypeall
     */
    public void setFiletypeall(String filetypeall) {
        this.filetypeall = filetypeall;
    }

    /**
     * @return
     */
    public String getPlan_Type() {
        return plan_type;
    }

    /**
     * @param plan_type
     */
    public void setPlan_Type(String plan_type) {
        this.plan_type = plan_type;
    }

    /**
     * @return
     */
    public String getPushNotification() {
        return pushNotification;
    }

    /**
     * @param pushNotification
     */
    public void setPushNotification(String pushNotification) {
        this.pushNotification = pushNotification;
    }

    /**
     * @return
     */
    public String getWeatherQty() {
        return weatherQty;
    }

    /**
     * @param weatherQty
     */
    public void setWeatherQty(String weatherQty) {
        this.weatherQty = weatherQty;
    }

    /**
     * @return
     */
    public String getWeatherFreeQty() {
        return weatherFreeQty;
    }

    /**
     * @param weatherFreeQty
     */
    public void setWeatherFreeQty(String weatherFreeQty) {
        this.weatherFreeQty = weatherFreeQty;
    }

    /**
     * @return
     */
    public String getReportingDays() {
        return reportingDays;
    }

    /**
     * @param reportingDays
     */
    public void setReportingDays(String reportingDays) {
        this.reportingDays = reportingDays;
    }

    /**
     * @return
     */
    public String getWeatherHourlyUpdates() {
        return weatherHourlyUpdates;
    }

    /**
     * @param weatherHourlyUpdates
     */
    public void setWeatherHourlyUpdates(String weatherHourlyUpdates) {
        this.weatherHourlyUpdates = weatherHourlyUpdates;
    }

    /**
     * @return
     */
    public String getFreeEnthusiastPlans() {
        return freeEnthusiastPlans;
    }

    /**
     * @param freeEnthusiastPlans
     */
    public void setFreeEnthusiastPlans(String freeEnthusiastPlans) {
        this.freeEnthusiastPlans = freeEnthusiastPlans;
    }

    /**
     * @return
     */
    public String getVisible() {
        return visible;
    }

    /**
     * @param visible
     */
    public void setVisible(String visible) {
        this.visible = visible;
    }

    /**
     * @return
     */
    public String getContractorPurchasable() {
        return contractorPurchasable;
    }

    /**
     * @param contractorPurchasable
     */
    public void setContractorPurchasable(String contractorPurchasable) {
        this.contractorPurchasable = contractorPurchasable;
    }

    /**
     * @return
     */
    public String getBoc() {
        return boc;
    }

    /**
     * @param boc
     */
    public void setBoc(String boc) {
        this.boc = boc;
    }

    /**
     * @return
     */
    public String getExpiry() {
        return expiry;
    }

    /**
     * @param expiry
     */
    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    /**
     * @return
     */
    public String getStart() {
        return start;
    }

    /**
     * @param start
     */
    public void setStart(String start) {
        this.start = start;
    }

    /**
     * @return
     */
    public String getCustomerplanId() {
        return customerplanId;
    }

    /**
     * @param customerplanId
     */
    public void setCustomerplanId(String customerplanId) {
        this.customerplanId = customerplanId;
    }

}