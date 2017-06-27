/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.model;

/**
 * @author Marco Meyer - Initial contribution
 */
public class MowerInfo extends RobonectAnswer{
    
    private String name;
    private Status status;
    private Timer timer;
    private Wlan wlan;
    private ErrorEntry error;

    public String getName() {
        return name;
    }
    
    public Status getStatus() {
        return status;
    }

    public Timer getTimer() {
        return timer;
    }

    public Wlan getWlan() {
        return wlan;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public void setWlan(Wlan wlan) {
        this.wlan = wlan;
    }

    public ErrorEntry getError() {
        return error;
    }

    public void setError(ErrorEntry error) {
        this.error = error;
    }
}
