/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bmwconnecteddrive.internal.action;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.handler.VehicleHandler;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link BMWConnectedDriveActions} provides actions for VehicleHandler
 *
 * @author Norbert Truchsess - Initial contribution
 */
@ThingActionsScope(name = "bmwconnecteddrive")
@NonNullByDefault
public class BMWConnectedDriveActions implements ThingActions {

    private Optional<VehicleHandler> handler = Optional.empty();

    private Optional<ChargeProfileWrapper> profile = Optional.empty();

    @RuleAction(label = "getTimer1Departure", description = "returns the departure time of timer1")
    public @ActionOutput(name = "time", type = "java.util.Optional<java.time.LocalTime>") Optional<LocalTime> getTimer1Departure() {
        return getTime(TIMER1);
    }

    @RuleAction(label = "setTimer1Departure", description = "sets the timer1 departure time")
    public void setTimer1Departure(@ActionInput(name = "time", type = "java.time.LocalTime") @Nullable LocalTime time) {
        setTime(TIMER1, time);
    }

    @RuleAction(label = "getTimer1Enabled", description = "returns the enabled state of timer1")
    public @ActionOutput(name = "enabled", type = "java.util.Optional<java.lang.Boolean>") Optional<Boolean> getTimer1Enabled() {
        return getEnabled(TIMER1);
    }

    @RuleAction(label = "setTimer1Enabled", description = "sets the enabled state of timer1")
    public void setTimer1Enabled(@ActionInput(name = "enabled", type = "java.lang.Boolean") @Nullable Boolean enabled) {
        setEnabled(TIMER1, enabled);
    }

    @RuleAction(label = "getTimer2Departure", description = "returns the departure time of timer2")
    public @ActionOutput(name = "time", type = "java.util.Optional<java.time.LocalTime>") Optional<LocalTime> getTimer2Departure() {
        return getTime(TIMER2);
    }

    @RuleAction(label = "setTimer2Departure", description = "sets the timer2 departure time")
    public void setTimer2Departure(@ActionInput(name = "time", type = "java.time.LocalTime") @Nullable LocalTime time) {
        setTime(TIMER2, time);
    }

    @RuleAction(label = "getTimer2Enabled", description = "returns the enabled state of timer2")
    public @ActionOutput(name = "enabled", type = "java.util.Optional<java.lang.Boolean>") Optional<Boolean> getTimer2Enabled() {
        return getEnabled(TIMER2);
    }

    @RuleAction(label = "setTimer2Enabled", description = "sets the enabled state of timer2")
    public void setTimer2Enabled(@ActionInput(name = "enabled", type = "java.lang.Boolean") @Nullable Boolean enabled) {
        setEnabled(TIMER2, enabled);
    }

    @RuleAction(label = "getTimer3Departure", description = "returns the departure time of timer3")
    public @ActionOutput(name = "time", type = "java.util.Optional<java.time.LocalTime>") Optional<LocalTime> getTimer3Departure() {
        return getTime(TIMER3);
    }

    @RuleAction(label = "setTimer3Departure", description = "sets the timer3 departure time")
    public void setTimer3Departure(@ActionInput(name = "time", type = "java.time.LocalTime") @Nullable LocalTime time) {
        setTime(TIMER3, time);
    }

    @RuleAction(label = "getTimer3Enabled", description = "returns the enabled state of timer3")
    public @ActionOutput(name = "enabled", type = "java.util.Optional<java.lang.Boolean>") Optional<Boolean> getTimer3Enabled() {
        return getEnabled(TIMER3);
    }

    @RuleAction(label = "setTimer3Enabled", description = "sets the enabled state of timer3")
    public void setTimer3Enabled(@ActionInput(name = "enabled", type = "java.lang.Boolean") @Nullable Boolean enabled) {
        setEnabled(TIMER3, enabled);
    }

    @RuleAction(label = "getOverrideTimerDeparture", description = "returns the departure time of overrideTimer")
    public @ActionOutput(name = "time", type = "java.util.Optional<java.time.LocalTime>") Optional<LocalTime> getOverrideTimerDeparture() {
        return getTime(OVERRIDE);
    }

    @RuleAction(label = "setOverrideTimerDeparture", description = "sets the overrideTimer departure time")
    public void setOverrideTimerDeparture(
            @ActionInput(name = "time", type = "java.time.LocalTime") @Nullable LocalTime time) {
        setTime(OVERRIDE, time);
    }

    @RuleAction(label = "getOverrideTimerEnabled", description = "returns the enabled state of overrideTimer")
    public @ActionOutput(name = "enabled", type = "java.util.Optional<java.lang.Boolean>") Optional<Boolean> getOverrideTimerEnabled() {
        return getEnabled(OVERRIDE);
    }

    @RuleAction(label = "setOverrideTimerEnabled", description = "sets the enabled state of overrideTimer")
    public void setOverrideTimerEnabled(
            @ActionInput(name = "enabled", type = "java.lang.Boolean") @Nullable Boolean enabled) {
        setEnabled(OVERRIDE, enabled);
    }

    @RuleAction(label = "getPreferredWindowStart", description = "returns the preferred charging-window start time")
    public @ActionOutput(name = "time", type = "java.util.Optional<java.time.LocalTime>") Optional<LocalTime> getPreferredWindowStart() {
        return getTime(WINDOWSTART);
    }

    @RuleAction(label = "setPreferredWindowStart", description = "sets the preferred charging-window start time")
    public void setPreferredWindowStart(
            @ActionInput(name = "time", type = "java.time.LocalTime") @Nullable LocalTime time) {
        setTime(WINDOWSTART, time);
    }

    @RuleAction(label = "getPreferredWindowEnd", description = "returns the preferred charging-window end time")
    public @ActionOutput(name = "time", type = "java.util.Optional<java.time.LocalTime>") Optional<LocalTime> getPreferredWindowEnd() {
        return getTime(WINDOWEND);
    }

    @RuleAction(label = "setPreferredWindowEnd", description = "sets the preferred charging-window end time")
    public void setPreferredWindowEnd(
            @ActionInput(name = "time", type = "java.time.LocalTime") @Nullable LocalTime time) {
        setTime(WINDOWEND, time);
    }

    @RuleAction(label = "getClimatizationEnabled", description = "returns the enabled state of climatization")
    public @ActionOutput(name = "enabled", type = "java.util.Optional<java.lang.Boolean>") Optional<Boolean> getClimatizationEnabled() {
        return getEnabled(CLIMATE);
    }

    @RuleAction(label = "setClimatizationEnabled", description = "sets the enabled state of climatization")
    public void setClimatizationEnabled(
            @ActionInput(name = "enabled", type = "java.lang.Boolean") @Nullable Boolean enabled) {
        setEnabled(CLIMATE, enabled);
    }

    @RuleAction(label = "getChargingMode", description = "gets the charging-mode")
    public @ActionOutput(name = "mode", type = "java.util.Optional<java.lang.String>") Optional<String> getChargingMode() {
        return getProfile().map(profile -> profile.getMode());
    }

    @RuleAction(label = "setChargingMode", description = "sets the charging-mode")
    public void setChargingMode(@ActionInput(name = "mode", type = "java.lang.String") @Nullable String mode) {
        getProfile().ifPresent(profile -> profile.setMode(mode));
    }

    @RuleAction(label = "getTimer1Days", description = "returns the days of week timer1 is enabled for")
    public @ActionOutput(name = "days", type = "java.util.Optional<java.util.Set<java.time.DayOfWeek>>") Optional<Set<DayOfWeek>> getTimer1Days() {
        return getDays(TIMER1);
    }

    @RuleAction(label = "setTimer1Days", description = "sets the days of week timer1 is enabled for")
    public void setTimer1Days(
            @ActionInput(name = "days", type = "java.util.Set<java.time.DayOfWeek>") @Nullable Set<DayOfWeek> days) {
        setDays(TIMER1, days);
    }

    @RuleAction(label = "getTimer2Days", description = "returns the days of week timer2 is enabled for")
    public @ActionOutput(name = "days", type = "java.util.Optional<java.util.Set<java.time.DayOfWeek>>") Optional<Set<DayOfWeek>> getTimer2Days() {
        return getDays(TIMER2);
    }

    @RuleAction(label = "setTimer2Days", description = "sets the days of week timer2 is enabled for")
    public void setTimer2Days(
            @ActionInput(name = "days", type = "java.util.Set<java.time.DayOfWeek>") @Nullable Set<DayOfWeek> days) {
        setDays(TIMER2, days);
    }

    @RuleAction(label = "getTimer3Days", description = "returns the days of week timer3 is enabled for")
    public @ActionOutput(name = "days", type = "java.util.Optional<java.util.Set<java.time.DayOfWeek>>") Optional<Set<DayOfWeek>> getTimer3Days() {
        return getDays(TIMER3);
    }

    @RuleAction(label = "setTimer3Days", description = "sets the days of week timer3 is enabled for")
    public void setTimer3Days(
            @ActionInput(name = "days", type = "java.util.Set<java.time.DayOfWeek>") @Nullable Set<DayOfWeek> days) {
        setDays(TIMER3, days);
    }

    @RuleAction(label = "getOverrideTimerDays", description = "returns the days of week the overrideTimer is enabled for")
    public @ActionOutput(name = "days", type = "java.util.Optional<java.util.Set<java.time.DayOfWeek>>") Optional<Set<DayOfWeek>> getOverrideTimerDays() {
        return getDays(OVERRIDE);
    }

    @RuleAction(label = "setOverrideTimerDays", description = "sets the days of week the overrideTimer is enabled for")
    public void setOverrideTimerDays(
            @ActionInput(name = "days", type = "java.util.Set<java.time.DayOfWeek>") @Nullable Set<DayOfWeek> days) {
        setDays(OVERRIDE, days);
    }

    @RuleAction(label = "sendChargeProfile", description = "sends the charging profile to the vehicle")
    public void sendChargeProfile() {
        handler.ifPresent(handle -> handle.sendChargeProfile(getProfile()));
    }

    @RuleAction(label = "cancel", description = "cancel current edit of charging profile")
    public void cancelEditChargeProfile() {
        profile = Optional.empty();
    }

    public static Optional<LocalTime> getTimer1Departure(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer1Departure();
    }

    public static void setTimer1Departure(ThingActions actions, @Nullable LocalTime time) {
        ((BMWConnectedDriveActions) actions).setTimer1Departure(time);
    }

    public static Optional<Boolean> getTimer1Enabled(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer1Enabled();
    }

    public static void setTimer1Enabled(ThingActions actions, @Nullable Boolean enabled) {
        ((BMWConnectedDriveActions) actions).setTimer1Enabled(enabled);
    }

    public static Optional<LocalTime> getTimer2Departure(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer2Departure();
    }

    public static void setTimer2Departure(ThingActions actions, @Nullable LocalTime time) {
        ((BMWConnectedDriveActions) actions).setTimer2Departure(time);
    }

    public static Optional<Boolean> getTimer2Enabled(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer2Enabled();
    }

    public static void setTimer2Enabled(ThingActions actions, @Nullable Boolean enabled) {
        ((BMWConnectedDriveActions) actions).setTimer2Enabled(enabled);
    }

    public static Optional<LocalTime> getTimer3Departure(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer3Departure();
    }

    public static void setTimer3Departure(ThingActions actions, @Nullable LocalTime time) {
        ((BMWConnectedDriveActions) actions).setTimer3Departure(time);
    }

    public static Optional<Boolean> getTimer3Enabled(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer3Enabled();
    }

    public static void setTimer3Enabled(ThingActions actions, @Nullable Boolean enabled) {
        ((BMWConnectedDriveActions) actions).setTimer3Enabled(enabled);
    }

    public static Optional<LocalTime> getOverrideTimerDeparture(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getOverrideTimerDeparture();
    }

    public static void setOverrideTimerDeparture(ThingActions actions, @Nullable LocalTime time) {
        ((BMWConnectedDriveActions) actions).setOverrideTimerDeparture(time);
    }

    public static Optional<Boolean> getOverrideTimerEnabled(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getOverrideTimerEnabled();
    }

    public static void setOverrideTimerEnabled(ThingActions actions, @Nullable Boolean enabled) {
        ((BMWConnectedDriveActions) actions).setOverrideTimerEnabled(enabled);
    }

    public static Optional<LocalTime> getPreferredWindowStart(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getPreferredWindowStart();
    }

    public static void setPreferredWindowStart(ThingActions actions, @Nullable LocalTime time) {
        ((BMWConnectedDriveActions) actions).setPreferredWindowStart(time);
    }

    public static Optional<LocalTime> getPreferredWindowEnd(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getPreferredWindowEnd();
    }

    public static void setPreferredWindowEnd(ThingActions actions, @Nullable LocalTime time) {
        ((BMWConnectedDriveActions) actions).setPreferredWindowEnd(time);
    }

    public static Optional<Boolean> getClimatizationEnabled(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getClimatizationEnabled();
    }

    public static void setClimatizationEnabled(ThingActions actions, @Nullable Boolean enabled) {
        ((BMWConnectedDriveActions) actions).setClimatizationEnabled(enabled);
    }

    public static Optional<String> getChargingMode(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getChargingMode();
    }

    public static void setChargingMode(ThingActions actions, @Nullable String mode) {
        ((BMWConnectedDriveActions) actions).setChargingMode(mode);
    }

    public static Optional<Set<DayOfWeek>> getTimer1Days(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer1Days();
    }

    public static void setTimer1Days(ThingActions actions, @Nullable Set<DayOfWeek> days) {
        ((BMWConnectedDriveActions) actions).setTimer1Days(days);
    }

    public static Optional<Set<DayOfWeek>> getTimer2Days(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer2Days();
    }

    public static void setTimer2Days(ThingActions actions, @Nullable Set<DayOfWeek> days) {
        ((BMWConnectedDriveActions) actions).setTimer2Days(days);
    }

    public static Optional<Set<DayOfWeek>> getTimer3Days(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getTimer3Days();
    }

    public static void setTimer3Days(ThingActions actions, @Nullable Set<DayOfWeek> days) {
        ((BMWConnectedDriveActions) actions).setTimer3Days(days);
    }

    public static Optional<Set<DayOfWeek>> getOverrideTimerDays(ThingActions actions) {
        return ((BMWConnectedDriveActions) actions).getOverrideTimerDays();
    }

    public static void setOverrideTimerDays(ThingActions actions, @Nullable Set<DayOfWeek> days) {
        ((BMWConnectedDriveActions) actions).setOverrideTimerDays(days);
    }

    public static void sendChargeProfile(ThingActions actions) {
        ((BMWConnectedDriveActions) actions).sendChargeProfile();
    }

    public static void cancelEditChargeProfile(ThingActions actions) {
        ((BMWConnectedDriveActions) actions).cancelEditChargeProfile();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VehicleHandler) {
            this.handler = Optional.of((VehicleHandler) handler);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler.get();
    }

    private Optional<ChargeProfileWrapper> getProfile() {
        if (profile.isEmpty()) {
            profile = handler.flatMap(handle -> handle.getChargeProfileWrapper());
        }
        return profile;
    }

    private Optional<LocalTime> getTime(ProfileKey key) {
        return getProfile().map(profile -> profile.getTime(key));
    }

    private void setTime(ProfileKey key, @Nullable LocalTime time) {
        getProfile().ifPresent(profile -> profile.setTime(key, time));
    }

    private Optional<Boolean> getEnabled(ProfileKey key) {
        return getProfile().map(profile -> profile.isEnabled(key));
    }

    private void setEnabled(ProfileKey key, @Nullable Boolean enabled) {
        getProfile().ifPresent(profile -> profile.setEnabled(key, enabled));
    }

    private Optional<Set<DayOfWeek>> getDays(ProfileKey key) {
        return getProfile().map(profile -> profile.getDays(key));
    }

    private void setDays(ProfileKey key, @Nullable Set<DayOfWeek> days) {
        getProfile().ifPresent(profile -> {
            profile.setDays(key, days);
        });
    }
}
