/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal.config;

/**
 * Config class for a Neato Robot
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class NeatoRobotConfig {

    private int refresh;
    private String secret;
    private String serial;

    public int getRefresh() {
        return refresh;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    @Override
    public String toString() {
        return "NeatoRobotConfig [refresh=" + refresh + ", secret=" + secret + ", serial=" + serial + "]";
    }

}
