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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The mower information holds the main information from the majority of the available channels. This class is a POJO
 * to deserialize the JSON response from the module.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Mower identification, clock, and job status fields
 */
@NonNullByDefault
public class MowerInfo extends RobonectAnswer {

    private @Nullable String name;
    private @Nullable String id;
    private @Nullable Status status;
    private @Nullable Timer timer;
    private @Nullable Wlan wlan;
    private @Nullable Health health;
    private @Nullable Blades blades;
    private @Nullable Clock clock;
    private @Nullable ErrorEntry error;

    // not returned by the mower status API. Only used when Ajax scraping is enabled
    private @Nullable Job job;

    /**
     * Returns the name of the mower.
     *
     * @return the name of the mower
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Returns the identifier reported by Robonect.
     *
     * @return the identifier reported by Robonect, if available
     */
    public @Nullable String getId() {
        return id;
    }

    /**
     * Returns some status information of the mower. See {@link Status} for details.
     *
     * @return some status information of the mower. See {@link Status} for details.
     */
    public @Nullable Status getStatus() {
        return status;
    }

    /**
     * Returns the current timer status information.
     *
     * @return the current timer status information.
     */
    public @Nullable Timer getTimer() {
        return timer;
    }

    /**
     * Returns the WLAN signal status.
     *
     * @return the WLAN signal status.
     */
    public @Nullable Wlan getWlan() {
        return wlan;
    }

    /**
     * Returns the error information reported by the mower.
     *
     * @return the error information reported by the mower, or {@code null} if absent
     */
    public @Nullable ErrorEntry getError() {
        return error;
    }

    /**
     * Returns the job information reported by the mower. Only available when Ajax scraping is enabled.
     *
     * @return the job information reported by the mower, or {@code null} if absent
     */
    public @Nullable Job getJob() {
        return job;
    }

    /**
     * Returns the health status information.
     *
     * @return the health status information.
     */
    public @Nullable Health getHealth() {
        return health;
    }

    /**
     * Returns the blades status information.
     *
     * @return the blades status information.
     */
    public @Nullable Blades getBlades() {
        return blades;
    }

    /**
     * Returns the clock reported by Robonect.
     *
     * @return the clock reported by Robonect, if available
     */
    public @Nullable Clock getClock() {
        return clock;
    }

    /**
     * Sets the mower name.
     *
     * @param name the mower name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the mower identifier.
     *
     * @param id the mower identifier
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Sets the mower status.
     *
     * @param status the mower status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Sets the timer status.
     *
     * @param timer the timer status
     */
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    /**
     * Sets the WLAN information.
     *
     * @param wlan the WLAN information
     */
    public void setWlan(Wlan wlan) {
        this.wlan = wlan;
    }

    /**
     * Sets the mower health information.
     *
     * @param health the mower health information
     */
    public void setHealth(Health health) {
        this.health = health;
    }

    /**
     * Sets the blade information.
     *
     * @param blades the blade information
     */
    public void setBlades(Blades blades) {
        this.blades = blades;
    }

    /**
     * Sets the mower clock.
     *
     * @param clock the mower clock
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    /**
     * Sets the mower error information.
     *
     * @param error the mower error information
     */
    public void setError(@Nullable ErrorEntry error) {
        this.error = error;
    }

    /**
     * Sets the job information.
     *
     * @param job the job information
     */
    public void setJob(@Nullable Job job) {
        this.job = job;
    }
}
