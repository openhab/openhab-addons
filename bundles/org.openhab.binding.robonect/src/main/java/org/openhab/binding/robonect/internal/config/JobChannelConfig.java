/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.config;

/**
 * The {@link JobChannelConfig} class holds the channel configuration for the Job channel.
 *
 * @author Marco Meyer - Initial contribution
 */
public class JobChannelConfig {

    private String remoteStart;

    private String afterMode;

    private int duration;

    public String getRemoteStart() {
        return remoteStart;
    }

    public void setRemoteStart(String remoteStart) {
        this.remoteStart = remoteStart;
    }

    public String getAfterMode() {
        return afterMode;
    }

    public void setAfterMode(String afterMode) {
        this.afterMode = afterMode;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
