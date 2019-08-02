/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

/**
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Features {

    private List<PlanArray> planArray = null;

    private Object id;

    private String planType2;

    private String planType2Key;

    private Object sku;

    private String discount;

    private String cost;

    private String costUs;

    private String costAu;

    private String costEu;

    private String costCa;

    private String costUk;

    private String active;

    private String controllerQty;

    private String rainfall;

    private String smsQty;

    private String scheduledReports;

    private String emailAlerts;

    private String defineSensor;

    private String addUser;

    private String contractor;

    private Object description;

    private String sensorPack;

    private String filelimit;

    private String filetypeall;

    private String planType;

    private String pushNotification;

    private String weatherQty;

    private String weatherFreeQty;

    private String reportingDays;

    private String weatherHourlyUpdates;

    private String freeEnthusiastPlans;

    private String visible;

    private Object contractorPurchasable;

    private Integer boc;

    private Object expiry;

    private Object start;

    private String customerplanId;

    private Integer smsUsed;

    /**
     * @return
     */
    public List<PlanArray> getPlanArray() {
        return planArray;
    }

    /**
     * @param planArray
     */
    public void setPlanArray(List<PlanArray> planArray) {
        this.planArray = planArray;
    }

    /**
     * @return
     */
    public Object getId() {
        return id;
    }

    /**
     * @param id
     */
    public void setId(Object id) {
        this.id = id;
    }

    /**
     * @return
     */
    public String getPlanType2() {
        return planType2;
    }

    /**
     * @param planType2
     */
    public void setPlanType2(String planType2) {
        this.planType2 = planType2;
    }

    /**
     * @return
     */
    public String getPlanType2Key() {
        return planType2Key;
    }

    /**
     * @param planType2Key
     */
    public void setPlanType2Key(String planType2Key) {
        this.planType2Key = planType2Key;
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
    public Object getDescription() {
        return description;
    }

    /**
     * @param description
     */
    public void setDescription(Object description) {
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
    public Object getContractorPurchasable() {
        return contractorPurchasable;
    }

    /**
     * @param contractorPurchasable
     */
    public void setContractorPurchasable(Object contractorPurchasable) {
        this.contractorPurchasable = contractorPurchasable;
    }

    /**
     * @return
     */
    public Integer getBoc() {
        return boc;
    }

    /**
     * @param boc
     */
    public void setBoc(Integer boc) {
        this.boc = boc;
    }

    /**
     * @return
     */
    public Object getExpiry() {
        return expiry;
    }

    /**
     * @param expiry
     */
    public void setExpiry(Object expiry) {
        this.expiry = expiry;
    }

    /**
     * @return
     */
    public Object getStart() {
        return start;
    }

    /**
     * @param start
     */
    public void setStart(Object start) {
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

    /**
     * @return
     */
    public Integer getSmsUsed() {
        return smsUsed;
    }

    /**
     * @param smsUsed
     */
    public void setSmsUsed(Integer smsUsed) {
        this.smsUsed = smsUsed;
    }

}
