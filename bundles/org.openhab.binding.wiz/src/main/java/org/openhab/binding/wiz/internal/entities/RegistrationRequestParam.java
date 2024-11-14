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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents Registration request param
 *
 * The outgoing JSON should look like this:
 *
 * {"id": 22, "method": "registration", "params": {"phoneIp": "10.0.0.xx",
 * "register": true, "homeId": xxx, "phoneMac": "xxx"}}
 *
 * NOTE: This can be sent directly to a single bulb or as a UDP broadcast. When
 * sent as a broadcast, all bulbs in the network should respond.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class RegistrationRequestParam implements Param {
    @Expose
    private String phoneIp;
    @Expose
    private boolean register;
    // NOTE: We are NOT exposing the Home id for serialization because it's not
    // necessary and it's a PITA to find it
    @Expose(serialize = false)
    private int homeId;
    @Expose
    private String phoneMac;

    public RegistrationRequestParam(String phoneIp, boolean register, int homeId, String phoneMac) {
        this.phoneIp = phoneIp;
        this.register = register;
        this.homeId = homeId;
        this.phoneMac = phoneMac;
    }

    public String getPhoneIp() {
        return phoneIp;
    }

    public void setPhoneIp(String phoneIp) {
        this.phoneIp = phoneIp;
    }

    public boolean getRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public int getHomeId() {
        return homeId;
    }

    public void setHomeId(int homeId) {
        this.homeId = homeId;
    }

    public String getPhoneMac() {
        return phoneMac;
    }

    public void setPhoneMac(String phoneMac) {
        this.phoneMac = phoneMac;
    }
}
