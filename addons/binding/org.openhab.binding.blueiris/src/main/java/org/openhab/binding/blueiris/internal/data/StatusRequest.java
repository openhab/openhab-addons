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
 * Sends a status request to the blue iris system.
 *
 * @author David Bennett - Initial Contribution
 */
public class StatusRequest extends BlueIrisCommandRequest<StatusReply> {
    @Expose
    private Integer signal;
    @Expose
    private Integer profile;
    @Expose
    private Integer dio;
    @Expose
    private String camera;

    public StatusRequest() {
        super(StatusReply.class, "status");
    }

    public Integer getSignal() {
        return signal;
    }

    public void setSignal(Integer signal) {
        this.signal = signal;
    }

    public Integer getProfile() {
        return profile;
    }

    public void setProfile(Integer profile) {
        this.profile = profile;
    }

    public Integer getDio() {
        return dio;
    }

    public void setDio(Integer dio) {
        this.dio = dio;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }
}
