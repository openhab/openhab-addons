/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link NeatoRobotInfo} is the internal class for the Neato Robot information.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoRobotInfo {

    @SerializedName("version")
    @Expose
    private Integer version;
    @SerializedName("reqId")
    @Expose
    private String reqId;
    @SerializedName("result")
    @Expose
    private String result;
    @SerializedName("error")
    @Expose
    private String error;
    @SerializedName("data")
    @Expose
    private RobotInfoData data;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public RobotInfoData getData() {
        return data;
    }

    public void setData(RobotInfoData data) {
        this.data = data;
    }
}
