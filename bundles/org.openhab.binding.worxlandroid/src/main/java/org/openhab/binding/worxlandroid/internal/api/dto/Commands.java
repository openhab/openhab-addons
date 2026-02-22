/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.worxlandroid.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.worxlandroid.internal.codes.WorxLandroidActionCodes;

/**
 * The {@link Commands} class hold record definition of Commands send to API
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Commands {
    private record OTS(OTSCommand ots) {
    }

    private record OTSCommand( //
            int bc, // bordercut
            int wtm // work time minutes
    ) {
    }

    public record OneTimeCommand(OTS sc) {
        public OneTimeCommand(int bc, int wtm) {
            this(new OTS(new OTSCommand(bc, wtm)));
        }
    }

    private record ScheduleDaysP(int p, Object d, @Nullable Object dd) {
    }

    public record ScheduleDaysCommand(ScheduleDaysP sc) {
        public ScheduleDaysCommand(int p, Object[] d, Object[] dd) {
            this(new ScheduleDaysP(p, d, dd));
        }

        public ScheduleDaysCommand(int p, Object[] d) {
            this(new ScheduleDaysP(p, d, null));
        }
    }

    private record ScheduleCommandMode(int m) {
    }

    public record ScheduleCommand(ScheduleCommandMode sc) {
        public ScheduleCommand(int m) {
            this(new ScheduleCommandMode(m));
        }
    }

    public record MowerCommand(int cmd) {
        public MowerCommand(WorxLandroidActionCodes actionCode) {
            this(actionCode.code);
        }
    }

    public record ZoneMeterCommand(int[] mz) {
    }

    public record ZoneMeterAlloc(int[] mzv) {
    }

    public record SetRainDelay(int rd) {
    }
}
