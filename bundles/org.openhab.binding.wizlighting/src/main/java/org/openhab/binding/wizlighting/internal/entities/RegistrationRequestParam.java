/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wizlighting.internal.entities;

/**
 * This POJO represents Registration request param
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class RegistrationRequestParam implements Param {
    private String phoneIp;
    private boolean register;
    private int homeId;
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
