/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris.internal.data;

/**
 * Alertlist data from blue iris.
 *
 * @author David Bennett - Initial COntribution
 *
 */
public class AlertListRequest {
    // Short name for the camera, or group name.
    private String camera;
    // Number of seconds since Jan 1, 1970
    private Integer startdate;
    // If true erases all alerts in the alerts folder.
    private boolean reset;
    private String cmd;

    public AlertListRequest() {
        cmd = "alertlist";
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public Integer getStartdate() {
        return startdate;
    }

    public void setStartdate(Integer startdate) {
        this.startdate = startdate;
    }

    public boolean isReset() {
        return reset;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public String getCmd() {
        return cmd;
    }
}
