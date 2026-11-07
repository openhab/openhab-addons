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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Object holding information from Ajax scraping.
 *
 * @author Nico Brüttner - Initial contribution
 */
public class Job {

    // Job-related information (not returned by the mower status API. Only used when Ajax scraping is enabled)
    private @Nullable Boolean jobActiv;
    private @Nullable String jobStart;
    private @Nullable String jobEnd;

    /**
     * Returns whether the job is active. Only available when Ajax scraping is enabled.
     *
     * @return whether the job is active
     */
    public @Nullable Boolean isJobActiv() {
        return jobActiv;
    }

    /**
     * Returns the job start time. Only available when Ajax scraping is enabled.
     *
     * @return the job start time
     */
    public @Nullable String getJobStart() {
        return jobStart;
    }

    /**
     * Returns the job end time. Only available when Ajax scraping is enabled.
     *
     * @return the job end time
     */
    public @Nullable String getJobEnd() {
        return jobEnd;
    }

    /**
     * Sets whether the job is active.
     *
     * @param jobActiv whether the job is active
     */
    public void setJobActiv(boolean jobActiv) {
        this.jobActiv = jobActiv;
    }

    /**
     * Sets the job start time.
     *
     * @param jobStart the job start time
     */
    public void setJobStart(String jobStart) {
        this.jobStart = jobStart;
    }

    /**
     * Sets the job end time.
     *
     * @param jobEnd the job end time
     */
    public void setJobEnd(String jobEnd) {
        this.jobEnd = jobEnd;
    }
}
