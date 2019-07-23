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
public class CustomerDetailsResponse extends Response {

    private BocTopologyDesired bocTopologyDesired;

    private BocTopologyActual bocTopologyActual;

    private List<Controller> controllers = null;

    private String currentController;

    private Boolean isBoc;

    private Integer tandc;

    private Integer controllerId;

    private Integer customerId;

    private String sessionId;

    private String hardwareVersion;

    private Integer deviceId;

    private Integer tandcVersion;

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