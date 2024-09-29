/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Inverter} is the internal class for OpenHAB inverter identity information
 * from a Sun Synk Connect account.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class Inverter {
    private String uid = "";
    @SerializedName("secret_key")
    private String token = "";
    private String serialNo = "";
    private String gateSerialNo = "";
    private String id = "";
    private String alias = "";
    private int refresh;

    public void setUID(String uid) {
        this.uid = uid;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public void setGateSerialNo(String gateSerialNo) {
        this.gateSerialNo = gateSerialNo;
    }

    public void setID(String id) {
        this.id = id;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    public String getUID() {
        return this.uid;
    }

    public String getToken() {
        return this.token;
    }

    public String getSerialNo() {
        return this.serialNo;
    }

    public String getGateSerialNo() {
        return this.gateSerialNo;
    }

    public String getID() {
        return this.id;
    }

    public String getAlias() {
        return this.alias;
    }

    public int getRefresh() {
        return this.refresh;
    }
}
