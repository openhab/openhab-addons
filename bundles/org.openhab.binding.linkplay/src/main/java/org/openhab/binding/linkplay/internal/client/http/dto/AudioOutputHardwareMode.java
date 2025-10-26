/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.linkplay.internal.client.http.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Audio output hardware mode.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class AudioOutputHardwareMode {
    /**
     * 1: SPDIF, 2: AUX, 3: COAX
     */
    public int hardware;
    /**
     * Bluetooth output mode: 0 disable, 1 active
     */
    public int source;
    /**
     * Audio cast output mode: 0 disable, 1 active
     */
    @SerializedName("audiocast")
    public int audioCast;
}
