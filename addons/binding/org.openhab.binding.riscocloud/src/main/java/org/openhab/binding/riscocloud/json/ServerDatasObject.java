/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.riscocloud.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This class is generated with http://www.jsonschema2pojo.org/
 * Use json provided by MyElas server and choose these options :
 * Package : org.openhab.binding.myelas.server.handler
 * Class Name : ServerDatasObject
 * Target language : Java
 * Source type : JSON
 * Annotation style : Gson
 * Tick :
 * - Use double numbers
 * - Include getters and setters
 * - Allow additional properties
 *
 * @author Sébastien Cantineau - Initial contribution
 */

public class ServerDatasObject {

    @SerializedName("error")
    @Expose
    private Integer error;
    @SerializedName("strResult")
    @Expose
    private String strResult;
    @SerializedName("eh")
    @Expose
    private Object eh;
    @SerializedName("overview")
    @Expose
    private Overview overview;
    @SerializedName("haSwitch")
    @Expose
    private List<Object> haSwitch = null;
    @SerializedName("detectors")
    @Expose
    private Detectors detectors;
    @SerializedName("allGrpState")
    @Expose
    private AllGrpState allGrpState;
    @SerializedName("IsOffline")
    @Expose
    private Boolean isOffline;
    @SerializedName("OfflineSince")
    @Expose
    private String offlineSince;
    @SerializedName("ShowRearmButton")
    @Expose
    private Boolean showRearmButton;
    @SerializedName("OngoingAlarm")
    @Expose
    private Boolean ongoingAlarm;
    @SerializedName("MemoryAlarm")
    @Expose
    private Boolean memoryAlarm;
    @SerializedName("HideDisarmOption")
    @Expose
    private Boolean hideDisarmOption;
    @SerializedName("unix_time")
    @Expose
    private Double unixTime;
    @SerializedName("PartArmString")
    @Expose
    private String partArmString;
    @SerializedName("FullArmString")
    @Expose
    private String fullArmString;
    @SerializedName("ExitDelayTimeout")
    @Expose
    private List<Integer> exitDelayTimeout = null;

    public Integer getError() {
        return error;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public String getStrResult() {
        return strResult;
    }

    public void setStrResult(String strResult) {
        this.strResult = strResult;
    }

    public Object getEh() {
        return eh;
    }

    public void setEh(Object eh) {
        this.eh = eh;
    }

    public Overview getOverview() {
        return overview;
    }

    public void setOverview(Overview overview) {
        this.overview = overview;
    }

    public List<Object> getHaSwitch() {
        return haSwitch;
    }

    public void setHaSwitch(List<Object> haSwitch) {
        this.haSwitch = haSwitch;
    }

    public Detectors getDetectors() {
        return detectors;
    }

    public void setDetectors(Detectors detectors) {
        this.detectors = detectors;
    }

    public AllGrpState getAllGrpState() {
        return allGrpState;
    }

    public void setAllGrpState(AllGrpState allGrpState) {
        this.allGrpState = allGrpState;
    }

    public Boolean getIsOffline() {
        return isOffline;
    }

    public void setIsOffline(Boolean isOffline) {
        this.isOffline = isOffline;
    }

    public String getOfflineSince() {
        return offlineSince;
    }

    public void setOfflineSince(String offlineSince) {
        this.offlineSince = offlineSince;
    }

    public Boolean getShowRearmButton() {
        return showRearmButton;
    }

    public void setShowRearmButton(Boolean showRearmButton) {
        this.showRearmButton = showRearmButton;
    }

    public Boolean getOngoingAlarm() {
        return ongoingAlarm;
    }

    public void setOngoingAlarm(Boolean ongoingAlarm) {
        this.ongoingAlarm = ongoingAlarm;
    }

    public Boolean getMemoryAlarm() {
        return memoryAlarm;
    }

    public void setMemoryAlarm(Boolean memoryAlarm) {
        this.memoryAlarm = memoryAlarm;
    }

    public Boolean getHideDisarmOption() {
        return hideDisarmOption;
    }

    public void setHideDisarmOption(Boolean hideDisarmOption) {
        this.hideDisarmOption = hideDisarmOption;
    }

    public Double getUnixTime() {
        return unixTime;
    }

    public void setUnixTime(Double unixTime) {
        this.unixTime = unixTime;
    }

    public String getPartArmString() {
        return partArmString;
    }

    public void setPartArmString(String partArmString) {
        this.partArmString = partArmString;
    }

    public String getFullArmString() {
        return fullArmString;
    }

    public void setFullArmString(String fullArmString) {
        this.fullArmString = fullArmString;
    }

    public List<Integer> getExitDelayTimeout() {
        return exitDelayTimeout;
    }

    public void setExitDelayTimeout(List<Integer> exitDelayTimeout) {
        this.exitDelayTimeout = exitDelayTimeout;
    }

}
