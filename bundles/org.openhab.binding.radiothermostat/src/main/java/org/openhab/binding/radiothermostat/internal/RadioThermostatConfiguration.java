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
package org.openhab.binding.radiothermostat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link RadioThermostatConfiguration} is the class used to match the
 * thing configuration.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RadioThermostatConfiguration {
    public @Nullable String hostName;
    public @Nullable Integer refresh;
    public @Nullable Integer logRefresh;
    public boolean isCT80 = false;
    public boolean disableLogs = false;
    public boolean clockSync = false;
    public String setpointMode = "temporary";

    public @Nullable String monMorningHeatTime;
    public @Nullable String monDayHeatTime;
    public @Nullable String monEveningHeatTime;
    public @Nullable String monNightHeatTime;
    public @Nullable String tueMorningHeatTime;
    public @Nullable String tueDayHeatTime;
    public @Nullable String tueEveningHeatTime;
    public @Nullable String tueNightHeatTime;
    public @Nullable String wedMorningHeatTime;
    public @Nullable String wedDayHeatTime;
    public @Nullable String wedEveningHeatTime;
    public @Nullable String wedNightHeatTime;
    public @Nullable String thuMorningHeatTime;
    public @Nullable String thuDayHeatTime;
    public @Nullable String thuEveningHeatTime;
    public @Nullable String thuNightHeatTime;
    public @Nullable String friMorningHeatTime;
    public @Nullable String friDayHeatTime;
    public @Nullable String friEveningHeatTime;
    public @Nullable String friNightHeatTime;
    public @Nullable String satMorningHeatTime;
    public @Nullable String satDayHeatTime;
    public @Nullable String satEveningHeatTime;
    public @Nullable String satNightHeatTime;
    public @Nullable String sunMorningHeatTime;
    public @Nullable String sunDayHeatTime;
    public @Nullable String sunEveningHeatTime;
    public @Nullable String sunNightHeatTime;

    public @Nullable String monMorningCoolTime;
    public @Nullable String monDayCoolTime;
    public @Nullable String monEveningCoolTime;
    public @Nullable String monNightCoolTime;
    public @Nullable String tueMorningCoolTime;
    public @Nullable String tueDayCoolTime;
    public @Nullable String tueEveningCoolTime;
    public @Nullable String tueNightCoolTime;
    public @Nullable String wedMorningCoolTime;
    public @Nullable String wedDayCoolTime;
    public @Nullable String wedEveningCoolTime;
    public @Nullable String wedNightCoolTime;
    public @Nullable String thuMorningCoolTime;
    public @Nullable String thuDayCoolTime;
    public @Nullable String thuEveningCoolTime;
    public @Nullable String thuNightCoolTime;
    public @Nullable String friMorningCoolTime;
    public @Nullable String friDayCoolTime;
    public @Nullable String friEveningCoolTime;
    public @Nullable String friNightCoolTime;
    public @Nullable String satMorningCoolTime;
    public @Nullable String satDayCoolTime;
    public @Nullable String satEveningCoolTime;
    public @Nullable String satNightCoolTime;
    public @Nullable String sunMorningCoolTime;
    public @Nullable String sunDayCoolTime;
    public @Nullable String sunEveningCoolTime;
    public @Nullable String sunNightCoolTime;

    public @Nullable Integer monMorningHeatTemp;
    public @Nullable Integer monDayHeatTemp;
    public @Nullable Integer monEveningHeatTemp;
    public @Nullable Integer monNightHeatTemp;
    public @Nullable Integer tueMorningHeatTemp;
    public @Nullable Integer tueDayHeatTemp;
    public @Nullable Integer tueEveningHeatTemp;
    public @Nullable Integer tueNightHeatTemp;
    public @Nullable Integer wedMorningHeatTemp;
    public @Nullable Integer wedDayHeatTemp;
    public @Nullable Integer wedEveningHeatTemp;
    public @Nullable Integer wedNightHeatTemp;
    public @Nullable Integer thuMorningHeatTemp;
    public @Nullable Integer thuDayHeatTemp;
    public @Nullable Integer thuEveningHeatTemp;
    public @Nullable Integer thuNightHeatTemp;
    public @Nullable Integer friMorningHeatTemp;
    public @Nullable Integer friDayHeatTemp;
    public @Nullable Integer friEveningHeatTemp;
    public @Nullable Integer friNightHeatTemp;
    public @Nullable Integer satMorningHeatTemp;
    public @Nullable Integer satDayHeatTemp;
    public @Nullable Integer satEveningHeatTemp;
    public @Nullable Integer satNightHeatTemp;
    public @Nullable Integer sunMorningHeatTemp;
    public @Nullable Integer sunDayHeatTemp;
    public @Nullable Integer sunEveningHeatTemp;
    public @Nullable Integer sunNightHeatTemp;

    public @Nullable Integer monMorningCoolTemp;
    public @Nullable Integer monDayCoolTemp;
    public @Nullable Integer monEveningCoolTemp;
    public @Nullable Integer monNightCoolTemp;
    public @Nullable Integer tueMorningCoolTemp;
    public @Nullable Integer tueDayCoolTemp;
    public @Nullable Integer tueEveningCoolTemp;
    public @Nullable Integer tueNightCoolTemp;
    public @Nullable Integer wedMorningCoolTemp;
    public @Nullable Integer wedDayCoolTemp;
    public @Nullable Integer wedEveningCoolTemp;
    public @Nullable Integer wedNightCoolTemp;
    public @Nullable Integer thuMorningCoolTemp;
    public @Nullable Integer thuDayCoolTemp;
    public @Nullable Integer thuEveningCoolTemp;
    public @Nullable Integer thuNightCoolTemp;
    public @Nullable Integer friMorningCoolTemp;
    public @Nullable Integer friDayCoolTemp;
    public @Nullable Integer friEveningCoolTemp;
    public @Nullable Integer friNightCoolTemp;
    public @Nullable Integer satMorningCoolTemp;
    public @Nullable Integer satDayCoolTemp;
    public @Nullable Integer satEveningCoolTemp;
    public @Nullable Integer satNightCoolTemp;
    public @Nullable Integer sunMorningCoolTemp;
    public @Nullable Integer sunDayCoolTemp;
    public @Nullable Integer sunEveningCoolTemp;
    public @Nullable Integer sunNightCoolTemp;
}
