/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal.api;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.api.model.AuthParams;
import org.openhab.binding.cloudrain.internal.api.model.Controller;
import org.openhab.binding.cloudrain.internal.api.model.Irrigation;
import org.openhab.binding.cloudrain.internal.api.model.Zone;

/**
 * A mockup implementation for the {@link CloudrainAPI} in order to test different behaviors or rules without an actual
 * Cloudrain account or real API interactions.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainAPIMockup implements CloudrainAPI {

    private LocalTime startTime1 = LocalTime.now();
    private LocalTime endTime1 = LocalTime.now();
    private LocalTime startTime2 = LocalTime.now();
    private LocalTime endTime2 = LocalTime.now();
    private LocalTime startTime3 = LocalTime.now();
    private LocalTime endTime3 = LocalTime.now();

    private static final String ZONE1_ID = "1234";
    private static final String ZONE1_NAME = "Frontyard";
    private static final String ZONE2_ID = "1235";
    private static final String ZONE2_NAME = "Flowers";
    private static final String ZONE3_ID = "1236";
    private static final String ZONE3_NAME = "Backyard";

    private static final String CONTROLLER_ID = "XY191140030006";
    private static final String CONTROLLER_NAME = "My Controller";

    @Override
    public void initialize(CloudrainAPIConfig config) throws CloudrainAPIException {
        // Validate that testMode is on
        if (!config.getTestMode()) {
            throw new CloudrainAPIException("Something went wrong. API Mockup is used, but TestMode is false.");
        }
        // Reset all running test irrigations
        reset();
    }

    @Override
    public void authenticate(AuthParams params) throws CloudrainAPIException {
        // Nothing to be done
    }

    @Override
    public List<Controller> getControllers() throws CloudrainAPIException {
        List<Controller> result = new ArrayList<Controller>();
        result.add(new Controller(CONTROLLER_ID, CONTROLLER_NAME));
        return result;
    }

    @Override
    public @Nullable Zone getZone(String zoneId) throws CloudrainAPIException {
        Zone result = null;
        if (ZONE1_ID.equals(zoneId)) {
            result = new Zone(ZONE1_ID, ZONE1_NAME, CONTROLLER_ID, CONTROLLER_NAME);
        }
        if (ZONE2_ID.equals(zoneId)) {
            result = new Zone(ZONE2_ID, ZONE2_NAME, CONTROLLER_ID, CONTROLLER_NAME);
        }
        if (ZONE3_ID.equals(zoneId)) {
            result = new Zone(ZONE3_ID, ZONE3_NAME, CONTROLLER_ID, CONTROLLER_NAME);
        }
        return result;
    }

    @Override
    public List<Zone> getZones() throws CloudrainAPIException {
        List<Zone> result = new ArrayList<Zone>();
        result.add(new Zone(ZONE1_ID, ZONE1_NAME, CONTROLLER_ID, CONTROLLER_NAME));
        result.add(new Zone(ZONE2_ID, ZONE2_NAME, CONTROLLER_ID, CONTROLLER_NAME));
        result.add(new Zone(ZONE3_ID, ZONE3_NAME, CONTROLLER_ID, CONTROLLER_NAME));
        return result;
    }

    @Override
    public List<Irrigation> getIrrigations() throws CloudrainAPIException {
        List<Irrigation> result = new ArrayList<Irrigation>();
        if (startTime1.isBefore(LocalTime.now()) && endTime1.isAfter(LocalTime.now())) {
            int remainingSecs = (int) Duration.between(LocalTime.now(), endTime1).toSeconds();
            int duration = (int) Duration.between(startTime1, endTime1).toSeconds();
            result.add(new Irrigation(ZONE1_ID, remainingSecs, duration, startTime1, endTime1, CONTROLLER_ID,
                    CONTROLLER_NAME));
        }
        if (startTime2.isBefore(LocalTime.now()) && endTime2.isAfter(LocalTime.now())) {
            int remainingSecs = (int) Duration.between(LocalTime.now(), endTime2).toSeconds();
            int duration = (int) Duration.between(startTime2, endTime2).toSeconds();
            result.add(new Irrigation(ZONE2_ID, remainingSecs, duration, startTime2, endTime2, CONTROLLER_ID,
                    CONTROLLER_NAME));
        }
        if (startTime3.isBefore(LocalTime.now()) && endTime3.isAfter(LocalTime.now())) {
            int remainingSecs = (int) Duration.between(LocalTime.now(), endTime3).toSeconds();
            int duration = (int) Duration.between(startTime3, endTime3).toSeconds();
            result.add(new Irrigation(ZONE3_ID, remainingSecs, duration, startTime3, endTime3, CONTROLLER_ID,
                    CONTROLLER_NAME));
        }
        return result;
    }

    @Override
    public @Nullable Irrigation getIrrigation(String zoneId) throws CloudrainAPIException {
        Irrigation result = null;
        if (ZONE1_ID.equals(zoneId)) {
            if (startTime1.isBefore(LocalTime.now()) && endTime1.isAfter(LocalTime.now())) {
                int remainingSecs = (int) Duration.between(LocalTime.now(), endTime1).toSeconds();
                int duration = (int) Duration.between(startTime1, endTime1).toSeconds();
                result = new Irrigation(ZONE1_ID, remainingSecs, duration, startTime1, endTime1, CONTROLLER_ID,
                        CONTROLLER_NAME);
            }
        }
        if (ZONE2_ID.equals(zoneId)) {
            if (startTime2.isBefore(LocalTime.now()) && endTime2.isAfter(LocalTime.now())) {
                int remainingSecs = (int) Duration.between(LocalTime.now(), endTime2).toSeconds();
                int duration = (int) Duration.between(startTime2, endTime2).toSeconds();
                result = new Irrigation(ZONE2_ID, remainingSecs, duration, startTime2, endTime2, CONTROLLER_ID,
                        CONTROLLER_NAME);
            }
        }
        if (ZONE3_ID.equals(zoneId)) {
            if (startTime3.isBefore(LocalTime.now()) && endTime3.isAfter(LocalTime.now())) {
                int remainingSecs = (int) Duration.between(LocalTime.now(), endTime3).toSeconds();
                int duration = (int) Duration.between(startTime3, endTime3).toSeconds();
                result = new Irrigation(ZONE3_ID, remainingSecs, duration, startTime3, endTime3, CONTROLLER_ID,
                        CONTROLLER_NAME);
            }
        }
        return result;
    }

    @Override
    public void startIrrigation(String zoneId, int duration) throws CloudrainAPIException {
        if (ZONE1_ID.equals(zoneId)) {
            startTime1 = LocalTime.now();
            endTime1 = startTime1.plus(duration, ChronoUnit.SECONDS);
        }
        if (ZONE2_ID.equals(zoneId)) {
            startTime2 = LocalTime.now();
            endTime2 = startTime2.plus(duration, ChronoUnit.SECONDS);
        }
        if (ZONE3_ID.equals(zoneId)) {
            startTime3 = LocalTime.now();
            endTime3 = startTime3.plus(duration, ChronoUnit.SECONDS);
        }
    }

    @Override
    public void adjustIrrigation(String zoneId, int duration) throws CloudrainAPIException {
        if (ZONE1_ID.equals(zoneId) && startTime1.isBefore(LocalTime.now()) && endTime1.isAfter(LocalTime.now())) {
            startTime1 = LocalTime.now();
            endTime1 = startTime1.plus(duration, ChronoUnit.SECONDS);
        }
        if (ZONE2_ID.equals(zoneId) && startTime2.isBefore(LocalTime.now()) && endTime2.isAfter(LocalTime.now())) {
            startTime2 = LocalTime.now();
            endTime2 = startTime2.plus(duration, ChronoUnit.SECONDS);
        }
        if (ZONE3_ID.equals(zoneId) && startTime3.isBefore(LocalTime.now()) && endTime3.isAfter(LocalTime.now())) {
            startTime3 = LocalTime.now();
            endTime3 = startTime3.plus(duration, ChronoUnit.SECONDS);
        }
    }

    @Override
    public void stopIrrigation(String zoneId) throws CloudrainAPIException {
        if (ZONE1_ID.equals(zoneId) && startTime1.isBefore(LocalTime.now()) && endTime1.isAfter(LocalTime.now())) {
            endTime1 = LocalTime.now();
        }
        if (ZONE2_ID.equals(zoneId) && startTime2.isBefore(LocalTime.now()) && endTime2.isAfter(LocalTime.now())) {
            endTime2 = LocalTime.now();
        }
        if (ZONE3_ID.equals(zoneId) && startTime3.isBefore(LocalTime.now()) && endTime3.isAfter(LocalTime.now())) {
            endTime3 = LocalTime.now();
        }
    }

    /**
     * Resets currently running irrigations in the test zones.
     */
    public void reset() {
        this.startTime1 = LocalTime.now();
        this.endTime1 = LocalTime.now();
        this.startTime2 = LocalTime.now();
        this.endTime2 = LocalTime.now();
        this.startTime3 = LocalTime.now();
        this.endTime3 = LocalTime.now();
    }
}
