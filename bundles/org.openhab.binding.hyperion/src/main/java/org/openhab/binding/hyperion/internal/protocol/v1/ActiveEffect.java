/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.hyperion.internal.protocol.v1;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ActiveEffect} is a POJO for an active effect on the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class ActiveEffect {

    @SerializedName("priority")
    private int priority;

    @SerializedName("script")
    private String script;

    @SerializedName("timeout")
    private int timeout;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
