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
package org.openhab.binding.senseenergy.internal.api.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyDatagramGetRealtime } dto
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyDatagramGetRealtime {
    public float current;
    public float voltage;
    public float power;
    public int total;
    @SerializedName("err_code")
    public int errorCode;
}

/* @formatter:off
{
    "system": {
      "get_sysinfo": {
          "err_code": 0,
          "sw_ver": "1.2.5 Build 171206 Rel.085954",
          "hw_ver": "1.0",
          "type: "IOT.SMARTPLUGSWITCH",
          "model": "HS110(US)",
          "mac": "xxx",
          "deviceId": "xxx",
          "alias": "alias",
          "relay_state": 1,
          "updating": 0
      }
    },
    "emeter": {
      "get_realtime": {
          "current": x,
          "voltage": x,
          "power": x,
          "total": 0,
          "err_code": 0
      }
    }
}
 * @formatter:on
 */
