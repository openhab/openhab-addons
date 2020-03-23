/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.freebox.internal.api.model;

/**
 * The {@link PhoneStatus} is the Java class used to map the
 * structure used by the phone API
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PhoneStatus {
    private boolean isRinging;
    private boolean onHook;

    private String type;
    private Long id;
    private Integer gainTx;
    private String vendor;
    private Integer gainRx;
    private Boolean hardwareDefect;
    private Long typeId;

    public boolean isRinging() {
        return isRinging;
    }

    public boolean isOnHook() {
        return onHook;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public Integer getGainTx() {
        return gainTx;
    }

    protected void setGainTx(Integer gainTx) {
        this.gainTx = gainTx;
    }

    public String getVendor() {
        return vendor;
    }

    protected void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getGainRx() {
        return gainRx;
    }

    protected void setGainRx(Integer gain_rx) {
        this.gainRx = gain_rx;
    }

    public Boolean getHardwareDefect() {
        return hardwareDefect;
    }

    public Long getTypeId() {
        return typeId;
    }

    protected void setTypeId(Long typeId) {
        this.typeId = typeId;
    }
}
