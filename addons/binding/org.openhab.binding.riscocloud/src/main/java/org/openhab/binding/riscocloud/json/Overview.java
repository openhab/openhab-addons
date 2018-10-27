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
 * Use json provided by RiscoCloud server and choose these options :
 * Package : org.openhab.binding.riscocloud.json
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

public class Overview {

    @SerializedName("partInfo")
    @Expose
    private PartInfo partInfo;
    @SerializedName("lastAlarms")
    @Expose
    private List<LastAlarm> lastAlarms = null;
    @SerializedName("bypassed")
    @Expose
    private List<Object> bypassed = null;
    @SerializedName("cameraSlides")
    @Expose
    private List<Object> cameraSlides = null;
    @SerializedName("errorIconHint")
    @Expose
    private String errorIconHint;

    public PartInfo getPartInfo() {
        return partInfo;
    }

    public void setPartInfo(PartInfo partInfo) {
        this.partInfo = partInfo;
    }

    public List<LastAlarm> getLastAlarms() {
        return lastAlarms;
    }

    public void setLastAlarms(List<LastAlarm> lastAlarms) {
        this.lastAlarms = lastAlarms;
    }

    public List<Object> getBypassed() {
        return bypassed;
    }

    public void setBypassed(List<Object> bypassed) {
        this.bypassed = bypassed;
    }

    public List<Object> getCameraSlides() {
        return cameraSlides;
    }

    public void setCameraSlides(List<Object> cameraSlides) {
        this.cameraSlides = cameraSlides;
    }

    public String getErrorIconHint() {
        return errorIconHint;
    }

    public void setErrorIconHint(String errorIconHint) {
        this.errorIconHint = errorIconHint;
    }

}
