/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.paradoxalarm.internal;

/**
 * The {@link ParadoxPanelConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
public class ParadoxPanelConfiguration {

    private int refresh;
    private String ip150Password;
    private String pcPassword;
    private String ipAddress;
    private int port;

    public int getRefresh() {
        return refresh;
    }

    public void setRefresh(int refresh) {
        this.refresh = refresh;
    }

    public String getIp150Password() {
        return ip150Password;
    }

    public void setIp150Password(String ip150Password) {
        this.ip150Password = ip150Password;
    }

    public String getPcPassword() {
        return pcPassword;
    }

    public void setPcPassword(String pcPassword) {
        this.pcPassword = pcPassword;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
