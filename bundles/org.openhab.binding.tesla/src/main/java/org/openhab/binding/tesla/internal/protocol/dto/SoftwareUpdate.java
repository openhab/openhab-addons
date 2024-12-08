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
package org.openhab.binding.tesla.internal.protocol.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SoftwareUpdate} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Hakan Tandogan - Initial contribution
 */
public class SoftwareUpdate {

    @SerializedName("download_perc")
    public int downloadPerc;
    @SerializedName("expected_duration_sec")
    public int expectedDurationSec;
    @SerializedName("install_perc")
    public int installPerc;
    public String status;
    public String version;

    SoftwareUpdate() {
    }
}
