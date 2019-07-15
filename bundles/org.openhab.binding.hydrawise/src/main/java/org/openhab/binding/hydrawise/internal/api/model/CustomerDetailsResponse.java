package org.openhab.binding.hydrawise.internal.api.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomerDetailsResponse extends Response {

    @SerializedName("boc_topology_desired")
    @Expose
    private BocTopologyDesired bocTopologyDesired;
    @SerializedName("boc_topology_actual")
    @Expose
    private BocTopologyActual bocTopologyActual;
    @SerializedName("controllers")
    @Expose
    private List<Controller> controllers = null;
    @SerializedName("current_controller")
    @Expose
    private String currentController;
    @SerializedName("is_boc")
    @Expose
    private Boolean isBoc;
    @SerializedName("tandc")
    @Expose
    private Integer tandc;
    @SerializedName("controller_id")
    @Expose
    private Integer controllerId;
    @SerializedName("customer_id")
    @Expose
    private Integer customerId;
    @SerializedName("session_id")
    @Expose
    private String sessionId;
    @SerializedName("hardwareVersion")
    @Expose
    private String hardwareVersion;
    @SerializedName("device_id")
    @Expose
    private Integer deviceId;
    @SerializedName("tandc_version")
    @Expose
    private Integer tandcVersion;
    @SerializedName("features")
    @Expose
    private Features features;

    /**
     * @return
     */
    public BocTopologyDesired getBocTopologyDesired() {
        return bocTopologyDesired;
    }

    /**
     * @param bocTopologyDesired
     */
    public void setBocTopologyDesired(BocTopologyDesired bocTopologyDesired) {
        this.bocTopologyDesired = bocTopologyDesired;
    }

    /**
     * @return
     */
    public BocTopologyActual getBocTopologyActual() {
        return bocTopologyActual;
    }

    /**
     * @param bocTopologyActual
     */
    public void setBocTopologyActual(BocTopologyActual bocTopologyActual) {
        this.bocTopologyActual = bocTopologyActual;
    }

    /**
     * @return
     */
    public List<Controller> getControllers() {
        return controllers;
    }

    /**
     * @param controllers
     */
    public void setControllers(List<Controller> controllers) {
        this.controllers = controllers;
    }

    /**
     * @return
     */
    public String getCurrentController() {
        return currentController;
    }

    /**
     * @param currentController
     */
    public void setCurrentController(String currentController) {
        this.currentController = currentController;
    }

    /**
     * @return
     */
    public Boolean getIsBoc() {
        return isBoc;
    }

    /**
     * @param isBoc
     */
    public void setIsBoc(Boolean isBoc) {
        this.isBoc = isBoc;
    }

    /**
     * @return
     */
    public Integer getTandc() {
        return tandc;
    }

    /**
     * @param tandc
     */
    public void setTandc(Integer tandc) {
        this.tandc = tandc;
    }

    /**
     * @return
     */
    public Integer getControllerId() {
        return controllerId;
    }

    /**
     * @param controllerId
     */
    public void setControllerId(Integer controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * @return
     */
    public Integer getCustomerId() {
        return customerId;
    }

    /**
     * @param customerId
     */
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    /**
     * @return
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return
     */
    public String getHardwareVersion() {
        return hardwareVersion;
    }

    /**
     * @param hardwareVersion
     */
    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    /**
     * @return
     */
    public Integer getDeviceId() {
        return deviceId;
    }

    /**
     * @param deviceId
     */
    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * @return
     */
    public Integer getTandcVersion() {
        return tandcVersion;
    }

    /**
     * @param tandcVersion
     */
    public void setTandcVersion(Integer tandcVersion) {
        this.tandcVersion = tandcVersion;
    }

    /**
     * @return
     */
    public Features getFeatures() {
        return features;
    }

    /**
     * @param features
     */
    public void setFeatures(Features features) {
        this.features = features;
    }

}