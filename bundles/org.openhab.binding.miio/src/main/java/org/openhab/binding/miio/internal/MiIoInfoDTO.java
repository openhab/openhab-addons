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
package org.openhab.binding.miio.internal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json for miio info response
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoInfoDTO {
    @SerializedName("life")
    @Expose
    public Long life;
    @SerializedName("cfg_time")
    @Expose
    public Long cfgTime;
    @SerializedName("token")
    @Expose
    public String token;
    @SerializedName("mac")
    @Expose
    public String mac;
    @SerializedName("fw_ver")
    @Expose
    public String fwVer;
    @SerializedName("hw_ver")
    @Expose
    public String hwVer;
    @SerializedName("uid")
    @Expose
    public Long uid;
    @SerializedName("model")
    @Expose
    public String model;
    @SerializedName("ap")
    @Expose
    public MiIoInfoApDTO ap;
    @SerializedName("wifi_fw_ver")
    @Expose
    public String wifiFwVer;
    @SerializedName("mcu_fw_ver")
    @Expose
    public String mcuFwVer;
    @SerializedName("mmfree")
    @Expose
    public Long mmfree;
}
