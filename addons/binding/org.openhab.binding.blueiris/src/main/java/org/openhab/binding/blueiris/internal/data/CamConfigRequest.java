/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.internal.data;

import com.google.gson.annotations.Expose;

/**
 * Requests the cam config from blue iris.
 *
 * @author David Bennett - Initial Contribution.
 */
public class CamConfigRequest extends BlueIrisCommandRequest<CamConfigReply> {
    @Expose
    private Boolean reset;
    @Expose
    private Boolean enable;
    @Expose
    private Integer pause;
    @Expose
    private Boolean motion;
    @Expose
    private String camera;
    @Expose
    private Boolean schedule;
    @Expose
    private Boolean ptzcycle;
    @Expose
    private Boolean ptzevents;

    public CamConfigRequest() {
        super(CamConfigReply.class, "camconfig");
    }

    public Boolean isReset() {
        return reset;
    }

    public void setReset(Boolean reset) {
        this.reset = reset;
    }

    public Boolean isEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Integer getPause() {
        return pause;
    }

    public void setPause(Integer pause) {
        this.pause = pause;
    }

    public Boolean isMotion() {
        return motion;
    }

    public void setMotion(Boolean motion) {
        this.motion = motion;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public Boolean isSchedule() {
        return schedule;
    }

    public Boolean isPtzcycle() {
        return ptzcycle;
    }

    public Boolean isPtzevents() {
        return ptzevents;
    }

    public void setSchedule(Boolean schedule) {
        this.schedule = schedule;
    }

    public void setPtzcycle(Boolean ptzcycle) {
        this.ptzcycle = ptzcycle;
    }

    public void setPtzevents(Boolean ptzevents) {
        this.ptzevents = ptzevents;
    }
}
