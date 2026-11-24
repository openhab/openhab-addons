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
package org.openhab.binding.verisure.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of the check that tells if the alarm is ready to be armed.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class VerisureArmCheckDTO {

    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_STATE_CHANGE_FAILED = "STATE_CHANGE_FAILED";

    private @Nullable Data data;

    public @Nullable String getStatus() {
        DryRunStatus dryRunStatus = getDryRunStatus();
        Status status = dryRunStatus != null ? dryRunStatus.status : null;
        return status != null ? status.status : null;
    }

    /**
     * @return the violations, or null if the response has no result
     */
    public @Nullable List<Violation> getViolations() {
        DryRunStatus dryRunStatus = getDryRunStatus();
        Result result = dryRunStatus != null ? dryRunStatus.result : null;
        return result != null ? result.deviceViolations : null;
    }

    private @Nullable DryRunStatus getDryRunStatus() {
        Data localData = data;
        Installation installation = localData != null ? localData.installation : null;
        ArmState armState = installation != null ? installation.armState : null;
        return armState != null ? armState.dryRunStatus : null;
    }

    private static class Data {
        private @Nullable Installation installation;
    }

    private static class Installation {
        private @Nullable ArmState armState;
    }

    private static class ArmState {
        private @Nullable DryRunStatus dryRunStatus;
    }

    private static class DryRunStatus {
        private @Nullable Status status;
        private @Nullable Result result;
    }

    private static class Status {
        private @Nullable String status;
    }

    private static class Result {
        private @Nullable List<Violation> deviceViolations;
    }

    public static class Violation {
        private @Nullable String deviceLabel;
        private @Nullable String violation;

        public @Nullable String getDeviceLabel() {
            return deviceLabel;
        }

        public @Nullable String getViolation() {
            return violation;
        }
    }
}
