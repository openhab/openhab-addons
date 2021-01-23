/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link E3DCConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Björn Brings - Initial contribution
 */
@NonNullByDefault
public class E3DCConfiguration {

    private String ip = "";
    private int updateinterval;
    private int port;
    private String webusername = "";
    private String webpassword = "";
    private String rscppassword = "";

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getUpdateinterval() {
        return updateinterval;
    }

    public void setUpdateinterval(int updateinterval) {
        this.updateinterval = updateinterval;
    }

    public String getWebusername() {
        return webusername;
    }

    public void setWebusername(String webusername) {
        this.webusername = webusername;
    }

    public String getWebpassword() {
        return webpassword;
    }

    public void setWebpassword(String webpassword) {
        this.webpassword = webpassword;
    }

    public String getRscppassword() {
        return rscppassword;
    }

    public void setRscppassword(String rscppassword) {
        this.rscppassword = rscppassword;
    }
}
