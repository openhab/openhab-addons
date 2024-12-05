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
 * This POJO represents Speed Request Param
 *
 * The outgoing JSON should look like this:
 *
 * {"id": 23, "method": "setPilot", "params": {"sceneId":3,"speed": 20}}
 *
 * NOTE: A sceneId MUST also be specified in the request or the bulb will reply
 * with an error.
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public class SpeedRequestParam extends SceneRequestParam {
    @Expose
    private int speed;

    public SpeedRequestParam(int sceneId, int speed) {
        super(sceneId);
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
