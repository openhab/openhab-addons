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
package org.openhab.binding.autoblind.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * Peripheral metadata from the GetAllPeripheral response (nested in room/group hierarchy).
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
public class PeripheralInfo {

    @SerializedName("PeripheralUID")
    public String peripheralUid = "";

    @SerializedName("PeripheralName")
    public String peripheralName = "";

    @SerializedName("ModuleType")
    public String moduleType = "";

    @SerializedName("ModuleDetail")
    public String moduleDetail = "";

    @SerializedName("Sorting")
    public String sorting = "";
}
