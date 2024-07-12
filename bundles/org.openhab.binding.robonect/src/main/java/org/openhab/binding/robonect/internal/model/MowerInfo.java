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
package org.openhab.binding.robonect.internal.model;

/**
 * The mower information holds the main information from the majority of the available channels. This class is a POJO
 * to deserialize the JSON response from the module.
 *
 * @author Marco Meyer - Initial contribution
 */
public class MowerInfo extends RobonectAnswer {

    private String name;
    private Status status;
    private Timer timer;
    private Wlan wlan;
    private Health health;
    private Blades blades;
    private ErrorEntry error;

    /**
     * @return - the name of the mower
     */
    public String getName() {
        return name;
    }

    /**
     * @return - some status information of the mower. See {@link Status} for details.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return - the current timer status information.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * @return - the WLAN signal status.
     */
    public Wlan getWlan() {
        return wlan;
    }

    /**
     * @return - if the mower is in error status {@link #getStatus()} the error information is returned, null otherwise.
     */
    public ErrorEntry getError() {
        return error;
    }

    /**
     * @return - the health status information.
     */
    public Health getHealth() {
        return health;
    }

    /**
     * @return - the blades status information.
     */
    public Blades getBlades() {
        return blades;
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

    public void setHealth(Health health) {
        this.health = health;
    }

    public void setBlades(Blades blades) {
        this.blades = blades;
    }

    public void setError(ErrorEntry error) {
        this.error = error;
    }
}
