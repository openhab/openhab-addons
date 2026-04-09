/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.vesync.internal.dto.responses.devices.airhumidifier;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link org.openhab.binding.vesync.internal.dto.responses.devices.airpurifier.V1Config} class is used as a DTO to
 * hold the Vesync's API's common
 * response data, with regard's to an air humidifer device's current configuration.
 *
 * @author David Goodyear - Initial contribution
 */
public class V1Config {

    @SerializedName("auto_target_humidity")
    public int autoTargetHumidity;

    @SerializedName("display")
    public boolean display;

    @SerializedName("automatic_stop")
    public boolean automaticStop;
}
