/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.v1;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link HyperionBuild} is a POJO for the Hyperion Build information on the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class HyperionBuild {

    @SerializedName("time")
    private String time;

    @SerializedName("version")
    private String version;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
