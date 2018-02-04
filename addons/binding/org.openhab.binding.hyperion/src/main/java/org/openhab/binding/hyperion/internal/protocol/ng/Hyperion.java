/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Hyperion} is a POJO for a Hyperion information in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Hyperion {

    @SerializedName("config_modified")
    private Boolean configModified;

    @SerializedName("config_writeable")
    private Boolean configWriteable;

    @SerializedName("off")
    private boolean off;

    @SerializedName("sessions")
    private List<Session> sessions = null;

    public Boolean getConfigModified() {
        return configModified;
    }

    public Boolean getConfigWriteable() {
        return configWriteable;
    }

    public boolean isOff() {
        return off;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setConfigModified(Boolean configModified) {
        this.configModified = configModified;
    }

    public void setConfigWriteable(Boolean configWriteable) {
        this.configWriteable = configWriteable;
    }

    public void setOff(boolean off) {
        this.off = off;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
    }

}
