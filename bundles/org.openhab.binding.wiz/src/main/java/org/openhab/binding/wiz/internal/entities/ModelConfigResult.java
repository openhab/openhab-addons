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
package org.openhab.binding.wiz.internal.entities;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;

/**
 * This POJO represents the "result" of one request for a bulb's model
 * configuration
 *
 * {
 * "method": "getModelConfig", "id": 1, "env":"pro",
 * "result": {
 * "ps":2, "pwmFreq":1000, "pwmRes":11, "pwmRange":[0,100],
 * "wcr":20, "nowc":1, "cctRange": [1800,2100,2100,2100],
 * "renderFactor": [120,255,255,255,0,0,20,90,255,255], "hasCctTable": 6,
 * "wizc1": {
 * "mode": [0,0,0,0,0,0,2100],
 * "opts": { "dim": 100 }
 * },
 * "wizc2": {
 * "mode": [0,0,0,0,0,0,2100],
 * "opts": { "dim": 50 }
 * },
 * "drvIface":4,
 * "i2cDrv": [
 * {
 * "chip": "BP5758D",
 * "addr": 255,
 * "freq": 200,
 * "curr": [30,30,30,36,36],
 * "output":[3,2,1,4,5]
 * }, {
 * "chip": "NONE",
 * "addr": 0,
 * "freq": 0,
 * "curr": [0,0,0,0,0],
 * "output": [0,0,0,0,0]
 * }, {
 * "chip": "NONE",
 * "addr": 0,
 * "freq": 0,
 * "curr": [0,0,0,0,0],
 * "output":[0,0,0,0,0]
 * }
 * ]
 * }
 * }
 * 
 * @author Cody Cutrer - Initial contribution
 *
 */
@NonNullByDefault
public class ModelConfigResult {
    @Expose
    public int[] cctRange = {};
}
