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
package org.openhab.binding.wizlighting.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This POJO represents one Wiz Lighting Sync response
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class WizLightingSyncResponse {

    private int id;
    private String method;
    private SyncResponseParam params;

    private String bulbIpAddress;

    public WizLightingSyncResponse() {
    }

    public String getBulbMacAddress() {
        return this.getParams().mac;
    }

    public void setBulbMacAddress(final String bulbMacAddress) {
        this.getParams().mac = bulbMacAddress;
    }

    public String getBulbIpAddress() {
        return this.bulbIpAddress;
    }

    public void setBulbIpAddress(final String bulbIpAddress) {
        this.bulbIpAddress = bulbIpAddress;
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
