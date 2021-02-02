/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.handler.VehicleHandler;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper;
import org.openhab.binding.bmwconnecteddrive.internal.utils.ChargeProfileWrapper.ProfileKey;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * The {@link ChargeProfileActions} provides actions for VehicleHandler
 *
 * @author Norbert Truchsess - Initial contribution
 */
@ThingActionsScope(name = "chargeprofile")
@NonNullByDefault
public class ChargeProfileActions implements ThingActions {

    private @Nullable VehicleHandler handler;

    private Optional<ChargeProfileWrapper> profile = Optional.empty();

    @RuleAction(label = "getTimer1Departure", description = "returns the departure time of timer1")
    public @Nullable @ActionOutput(name = "time", type = "DateTimeType") DateTimeType getTimer1Departure() {
        return getDateTime(TIMER1);
    }

    @RuleAction(label = "setTimer1Departure", description = "sets the timer1 departure time")
    public void setTimer1Departure(@ActionInput(name = "time", type = "DateTimeType") DateTimeType time) {
        setDateTime(TIMER1, time);
    }

    @RuleAction(label = "getTimer1Enabled", description = "returns the enabled state of timer1")
    public @Nullable @ActionOutput(name = "enabled", type = "OnOffType") OnOffType getTimer1Enabled() {
        return getEnabled(TIMER1);
    }

    @RuleAction(label = "setTimer1Enabled", description = "sets the enabled state of timer1")
    public void setTimer1Enabled(@ActionInput(name = "enabled", type = "OnOffType") OnOffType enabled) {
        setEnabled(TIMER1, enabled);
    }

    @RuleAction(label = "getTimer2Departure", description = "returns the departure time of timer2")
    public @Nullable @ActionOutput(name = "time", type = "DateTimeType") DateTimeType getTimer2Departure() {
        return getDateTime(TIMER2);
    }

    @RuleAction(label = "setTimer2Departure", description = "sets the timer2 departure time")
    public void setTimer2Departure(@ActionInput(name = "time", type = "DateTimeType") DateTimeType time) {
        setDateTime(TIMER2, time);
    }

    @RuleAction(label = "getTimer2Enabled", description = "returns the enabled state of timer2")
    public @Nullable @ActionOutput(name = "enabled", type = "OnOffType") OnOffType getTimer2Enabled() {
        return getEnabled(TIMER2);
    }

    @RuleAction(label = "setTimer2Enabled", description = "sets the enabled state of timer2")
    public void setTimer2Enabled(@ActionInput(name = "enabled", type = "OnOffType") OnOffType enabled) {
        setEnabled(TIMER2, enabled);
    }

    @RuleAction(label = "getTimer3Departure", description = "returns the departure time of timer3")
    public @Nullable @ActionOutput(name = "time", type = "DateTimeType") DateTimeType getTimer3Departure() {
        return getDateTime(TIMER3);
    }

    @RuleAction(label = "setTimer3Departure", description = "sets the timer3 departure time")
    public void setTimer3Departure(@ActionInput(name = "time", type = "DateTimeType") DateTimeType time) {
        setDateTime(TIMER3, time);
    }

    @RuleAction(label = "getTimer3Enabled", description = "returns the enabled state of timer3")
    public @Nullable @ActionOutput(name = "enabled", type = "OnOffType") OnOffType getTimer3Enabled() {
        return getEnabled(TIMER3);
    }

    @RuleAction(label = "setTimer3Enabled", description = "sets the enabled state of timer3")
    public void setTimer3Enabled(@ActionInput(name = "enabled", type = "OnOffType") OnOffType enabled) {
        setEnabled(TIMER3, enabled);
    }

    @RuleAction(label = "getOverrideTimerDeparture", description = "returns the departure time of overrideTimer")
    public @Nullable @ActionOutput(name = "time", type = "DateTimeType") DateTimeType getOverrideTimerDeparture() {
        return getDateTime(OVERRIDE);
    }

    @RuleAction(label = "setOverrideTimerDeparture", description = "sets the overrideTimer departure time")
    public void setOverrideTimerDeparture(@ActionInput(name = "time", type = "DateTimeType") DateTimeType time) {
        setDateTime(OVERRIDE, time);
    }

    @RuleAction(label = "getOverrideTimerEnabled", description = "returns the enabled state of overrideTimer")
    public @Nullable @ActionOutput(name = "enabled", type = "OnOffType") OnOffType getOverrideTimerEnabled() {
        return getEnabled(OVERRIDE);
    }

    @RuleAction(label = "setOverrideTimerEnabled", description = "sets the enabled state of overrideTimer")
    public void setOverrideTimerEnabled(@ActionInput(name = "enabled", type = "OnOffType") OnOffType enabled) {
        setEnabled(OVERRIDE, enabled);
    }

    @RuleAction(label = "getPreferredWindowStart", description = "returns the preferred charging-window start time")
    public @Nullable @ActionOutput(name = "time", type = "DateTimeType") DateTimeType getPreferredWindowStart() {
        return getDateTime(WINDOWSTART);
    }

    @RuleAction(label = "setPreferredWindowStart", description = "sets the preferred charging-window start time")
    public void setPreferredWindowStart(@ActionInput(name = "time", type = "DateTimeType") DateTimeType time) {
        setDateTime(WINDOWSTART, time);
    }

    @RuleAction(label = "getPreferredWindowEnd", description = "returns the preferred charging-window end time")
    public @Nullable @ActionOutput(name = "time", type = "DateTimeType") DateTimeType getPreferredWindowEnd() {
        return getDateTime(WINDOWEND);
    }

    @RuleAction(label = "setPreferredWindowEnd", description = "sets the preferred charging-window end time")
    public void setPreferredWindowEnd(@ActionInput(name = "time", type = "DateTimeType") DateTimeType time) {
        setDateTime(WINDOWEND, time);
    }

    @RuleAction(label = "getClimatizationEnabled", description = "returns the enabled state of climatization")
    public @Nullable @ActionOutput(name = "enabled", type = "OnOffType") OnOffType getClimatizationEnabled() {
        return getEnabled(CLIMATE);
    }

    @RuleAction(label = "setClimatizationEnabled", description = "sets the enabled state of climatization")
    public void setClimatizationEnabled(@ActionInput(name = "enabled", type = "OnOffType") OnOffType enabled) {
        setEnabled(CLIMATE, enabled);
    }

    @RuleAction(label = "getChargingMode", description = "gets the charging-mode")
    public @Nullable @ActionOutput(name = "mode", type = "StringType") StringType getChargingMode() {
        return hasProfile() ? StringType.valueOf(profile.get().getMode()) : null;
    }

    @RuleAction(label = "setChargingMode", description = "sets the charging-mode")
    public void setChargingMode(@ActionInput(name = "mode", type = "StringType") StringType mode) {
        if (hasProfile()) {
            profile.get().setMode(mode.toFullString());
        }
    }

    @RuleAction(label = "getTimer1Days", description = "returns the days of week timer1 is enabled for")
    public @Nullable @ActionOutput(name = "days", type = "StringListType") StringListType getTimer1Days() {
        return getDays(TIMER1);
    }

    @RuleAction(label = "setTimer1Days", description = "sets the days of week timer1 is enabled for")
    public void setTimer1Days(@ActionInput(name = "days", type = "StringListType") StringListType days) {
        setDays(TIMER1, days);
    }

    @RuleAction(label = "getTimer2Days", description = "returns the days of week timer2 is enabled for")
    public @Nullable @ActionOutput(name = "days", type = "StringListType") StringListType getTimer2Days() {
        return getDays(TIMER2);
    }

    @RuleAction(label = "setTimer2Days", description = "sets the days of week timer2 is enabled for")
    public void setTimer2Days(@ActionInput(name = "days", type = "StringListType") StringListType days) {
        setDays(TIMER2, days);
    }

    @RuleAction(label = "getTimer3Days", description = "returns the days of week timer3 is enabled for")
    public @Nullable @ActionOutput(name = "days", type = "StringListType") StringListType getTimer3Days() {
        return getDays(TIMER3);
    }

    @RuleAction(label = "setTimer3Days", description = "sets the days of week timer3 is enabled for")
    public void setTimer3Days(@ActionInput(name = "days", type = "StringListType") StringListType days) {
        setDays(TIMER3, days);
    }

    @RuleAction(label = "getOverrideTimerDays", description = "returns the days of week the overrideTimer is enabled for")
    public @Nullable @ActionOutput(name = "days", type = "StringListType") StringListType getOverrideTimerDays() {
        return getDays(OVERRIDE);
    }

    @RuleAction(label = "setOverrideTimerDays", description = "sets the days of week the overrideTimer is enabled for")
    public void setOverrideTimerDays(@ActionInput(name = "days", type = "StringListType") StringListType days) {
        setDays(OVERRIDE, days);
    }

    @RuleAction(label = "send", description = "sends the charging profile to the vehicle")
    public void send() {
        if (hasProfile() && handler != null) {
            handler.sendChargeProfile(profile.get());
        }
    }

    @RuleAction(label = "cancel", description = "cancel current edit of charging profile")
    public void cancel() {
        profile = Optional.empty();
    }

    public static @Nullable DateTimeType getTimer1Departure(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer1Departure();
    }

    public static void setTimer1Departure(ThingActions actions, DateTimeType time) {
        ((ChargeProfileActions) actions).setTimer1Departure(time);
    }

    public static @Nullable OnOffType getTimer1Enabled(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer1Enabled();
    }

    public static void setTimer1Enabled(ThingActions actions, OnOffType enabled) {
        ((ChargeProfileActions) actions).setTimer1Enabled(enabled);
    }

    public static @Nullable DateTimeType getTimer2Departure(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer2Departure();
    }

    public static void setTimer2Departure(ThingActions actions, DateTimeType time) {
        ((ChargeProfileActions) actions).setTimer2Departure(time);
    }

    public static @Nullable OnOffType getTimer2Enabled(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer2Enabled();
    }

    public static void setTimer2Enabled(ThingActions actions, OnOffType enabled) {
        ((ChargeProfileActions) actions).setTimer2Enabled(enabled);
    }

    public static @Nullable DateTimeType getTimer3Departure(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer3Departure();
    }

    public static void setTimer3Departure(ThingActions actions, DateTimeType time) {
        ((ChargeProfileActions) actions).setTimer3Departure(time);
    }

    public static @Nullable OnOffType getTimer3Enabled(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer3Enabled();
    }

    public static void setTimer3Enabled(ThingActions actions, OnOffType enabled) {
        ((ChargeProfileActions) actions).setTimer3Enabled(enabled);
    }

    public static @Nullable DateTimeType getOverrideTimerDeparture(ThingActions actions) {
        return ((ChargeProfileActions) actions).getOverrideTimerDeparture();
    }

    public static void setOverrideTimerDeparture(ThingActions actions, DateTimeType time) {
        ((ChargeProfileActions) actions).setOverrideTimerDeparture(time);
    }

    public static @Nullable OnOffType getOverrideTimerEnabled(ThingActions actions) {
        return ((ChargeProfileActions) actions).getOverrideTimerEnabled();
    }

    public static void setOverrideTimerEnabled(ThingActions actions, OnOffType enabled) {
        ((ChargeProfileActions) actions).setOverrideTimerEnabled(enabled);
    }

    public static @Nullable DateTimeType getPreferredWindowStart(ThingActions actions) {
        return ((ChargeProfileActions) actions).getPreferredWindowStart();
    }

    public static void setPreferredWindowStart(ThingActions actions, DateTimeType time) {
        ((ChargeProfileActions) actions).setPreferredWindowStart(time);
    }

    public static @Nullable DateTimeType getPreferredWindowEnd(ThingActions actions) {
        return ((ChargeProfileActions) actions).getPreferredWindowEnd();
    }

    public static void setPreferredWindowEnd(ThingActions actions, DateTimeType time) {
        ((ChargeProfileActions) actions).setPreferredWindowEnd(time);
    }

    public static @Nullable OnOffType getClimatizationEnabled(ThingActions actions) {
        return ((ChargeProfileActions) actions).getClimatizationEnabled();
    }

    public static void setClimatizationEnabled(ThingActions actions, OnOffType enabled) {
        ((ChargeProfileActions) actions).setClimatizationEnabled(enabled);
    }

    public static @Nullable StringType getChargingMode(ThingActions actions) {
        return ((ChargeProfileActions) actions).getChargingMode();
    }

    public static void setChargingMode(ThingActions actions, StringType mode) {
        ((ChargeProfileActions) actions).setChargingMode(mode);
    }

    public static @Nullable StringListType getTimer1Days(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer1Days();
    }

    public static void setTimer1Days(ThingActions actions, StringListType days) {
        ((ChargeProfileActions) actions).setTimer1Days(days);
    }

    public static @Nullable StringListType getTimer2Days(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer2Days();
    }

    public static void setTimer2Days(ThingActions actions, StringListType days) {
        ((ChargeProfileActions) actions).setTimer2Days(days);
    }

    public static @Nullable StringListType getTimer3Days(ThingActions actions) {
        return ((ChargeProfileActions) actions).getTimer3Days();
    }

    public static void setTimer3Days(ThingActions actions, StringListType days) {
        ((ChargeProfileActions) actions).setTimer3Days(days);
    }

    public static @Nullable StringListType getOverrideTimerDays(ThingActions actions) {
        return ((ChargeProfileActions) actions).getOverrideTimerDays();
    }

    public static void setOverrideTimerDays(ThingActions actions, StringListType days) {
        ((ChargeProfileActions) actions).setOverrideTimerDays(days);
    }

    public static void send(ThingActions actions) {
        ((ChargeProfileActions) actions).send();
    }

    public static void cancel(ThingActions actions) {
        ((ChargeProfileActions) actions).cancel();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof VehicleHandler) {
            this.handler = (VehicleHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    private boolean hasProfile() {
        if (profile.isEmpty() && handler != null) {
            final ChargeProfileWrapper wrapper = handler.getChargeProfileWrapper();
            if (wrapper != null) {
                profile = Optional.of(wrapper);
            }
        }
        return profile.isPresent();
    }

    private @Nullable DateTimeType getDateTime(ProfileKey key) {
        if (hasProfile()) {
            final LocalTime time = profile.get().getTime(key);
            if (time != null) {
                return new DateTimeType(ZonedDateTime.of(Constants.EPOCHDAY, time, ZoneId.systemDefault()));
            }
        }
        return null;
    }

    private void setDateTime(ProfileKey key, DateTimeType time) {
        if (hasProfile()) {
            profile.get().setTime(key, time.getZonedDateTime().toLocalTime());
        }
    }

    private @Nullable OnOffType getEnabled(ProfileKey key) {
        if (hasProfile()) {
            Boolean enabled = profile.get().isEnabled(key);
            if (enabled != null) {
                return OnOffType.from(enabled);
            }
        }
        return null;
    }

    private void setEnabled(ProfileKey key, OnOffType enabled) {
        if (hasProfile()) {
            profile.get().setEnabled(key, OnOffType.ON.equals(enabled));
        }
    }

    private @Nullable StringListType getDays(ProfileKey key) {
        if (hasProfile()) {
            final List<String> days = profile.get().getDays(key);
            if (days != null) {
                return new StringListType(days);
            }
        }
        return null;
    }

    private void setDays(ProfileKey key, StringListType days) {
        if (hasProfile()) {
            List<String> dayList = new ArrayList<>();
            try {
                for (int i = 0;; i++) {
                    dayList.add(days.getValue(i));
                }
            } catch (IllegalArgumentException iae) {
            }
            profile.get().setDays(key, dayList);
        }
    }
}
