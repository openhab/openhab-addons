/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.classes;

/**
 * The {@link NeatoGeneralInfo} is the internal class for Neato general information.
 *
 * @author Patrik Wimnell - Initial contribution
 * @author Holger Eisold - fixes to get GeneralInfo working
 */
public class NeatoGeneralInfo {

    private Integer version;
    private String reqId;
    private String result;
    private NeatoGeneralInfoData data;

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

    public NeatoGeneralInfoData getData() {
        return data;
    }

    public void setData(NeatoGeneralInfoData data) {
        this.data = data;
    }
}
