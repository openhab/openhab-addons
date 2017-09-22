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
 * This POJO represents one Wiz Lighting Sync response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public class WizLightingSyncResponse {

    private int id;
    private String method;
    private SyncResponseParam params;

    private String hostAddress;

    public WizLightingSyncResponse() {
    }

    public String getMacAddress() {
        return this.getParams().mac;
    }

    public void setMacAddress(final String macAddress) {
        this.getParams().mac = macAddress;
    }

    public String getHostAddress() {
        return this.hostAddress;
    }

    public void setHostAddress(final String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public SyncResponseParam getParams() {
        return params;
    }

    public void setParams(SyncResponseParam params) {
        this.params = params;
    }
}
