/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.internal.model;

/**
 * @author Andrew Schofield - Initial contribution
 */
public class DetectedAccessPointDTO {

    private String sSID;
    private Integer channel;
    private String securityMode;
    private Integer rSSI;

    public String getSSID() {
        return sSID;
    }

    public void setSSID(final String sSID) {
        this.sSID = sSID;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(final Integer channel) {
        this.channel = channel;
    }

    public String getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(final String securityMode) {
        this.securityMode = securityMode;
    }

    public Integer getRSSI() {
        return rSSI;
    }
}
