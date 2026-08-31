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
package org.openhab.binding.atagone.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atagone.internal.AtagOneHandler;
import org.openhab.binding.atagone.internal.dto.ControlUpdateDTO;
import org.openhab.binding.atagone.internal.dto.DeviceConfigUpdateDTO;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single-write mode activation for the ATAG ONE binding, complementing the channel-based interface.
 * <p>
 * {@code preset-mode} is the only channel that can ever trigger a mode change — writing a duration
 * channel (vacation/extend/fireplace-duration) only updates the stored value, matching the device's
 * own behavior. That means activating with a <em>custom</em> duration normally takes two writes: set
 * the duration, then set preset-mode. These actions compose the full multi-field write in one call
 * instead, for rule authors who want precise, immediate control without that two-step sequence —
 * each action reuses the same {@code composeXxxActivation}/{@code composeCancel} methods on
 * {@link AtagOneHandler} that the {@code preset-mode} channel path uses, so the two can never drift
 * apart on what they actually send.
 *
 * @author Florian Lettner - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AtagOneActions.class)
@ThingActionsScope(name = "atagone")
@NonNullByDefault
public class AtagOneActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(AtagOneActions.class);

    private @Nullable AtagOneHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof AtagOneHandler atagOneHandler) {
            this.handler = atagOneHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "activate vacation", description = "Activates vacation/holiday mode immediately for the given duration, composing the required ch_mode + start_vacation write in one call.")
    public void activateVacation(
            @ActionInput(name = "durationSeconds", label = "Duration (s)", description = "Vacation duration in seconds") long durationSeconds) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateVacation called with no handler bound");
            return;
        }
        if (durationSeconds <= 0) {
            logger.warn("activateVacation: duration must be positive, got {}", durationSeconds);
            return;
        }
        if (!AtagOneHandler.isWholeUnits(durationSeconds, AtagOneHandler.SECONDS_PER_DAY)) {
            logger.warn("activateVacation: duration must be a whole number of days, got {} s", durationSeconds);
            return;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        theHandler.composeVacationActivation(control, configUpdate, durationSeconds);
        theHandler.sendComposedUpdate("action:activateVacation", control, configUpdate);
    }

    @RuleAction(label = "activate extend", description = "Activates extend mode immediately, additive to the time remaining until the next schedule boundary.")
    public void activateExtend(
            @ActionInput(name = "durationSeconds", label = "Duration (s)", description = "Additional duration in seconds, on top of the time remaining until the next schedule boundary") long durationSeconds) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateExtend called with no handler bound");
            return;
        }
        if (durationSeconds <= 0) {
            logger.warn("activateExtend: duration must be positive, got {}", durationSeconds);
            return;
        }
        if (!AtagOneHandler.isWholeUnits(durationSeconds, AtagOneHandler.SECONDS_PER_HOUR)) {
            logger.warn("activateExtend: duration must be a whole number of hours, got {} s", durationSeconds);
            return;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        theHandler.composeExtendActivation(control, durationSeconds);
        theHandler.sendComposedUpdate("action:activateExtend", control, configUpdate);
    }

    @RuleAction(label = "activate fireplace", description = "Activates fireplace mode immediately for the given duration.")
    public void activateFireplace(
            @ActionInput(name = "durationSeconds", label = "Duration (s)", description = "Fireplace duration in seconds") long durationSeconds) {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("activateFireplace called with no handler bound");
            return;
        }
        if (durationSeconds <= 0) {
            logger.warn("activateFireplace: duration must be positive, got {}", durationSeconds);
            return;
        }
        if (!AtagOneHandler.isWholeUnits(durationSeconds, AtagOneHandler.SECONDS_PER_HOUR)) {
            logger.warn("activateFireplace: duration must be a whole number of hours, got {} s", durationSeconds);
            return;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        theHandler.composeFireplaceActivation(control, durationSeconds);
        theHandler.sendComposedUpdate("action:activateFireplace", control, configUpdate);
    }

    @RuleAction(label = "cancel mode", description = "Cancels any active timed preset and returns to auto/schedule mode.")
    public @ActionOutput(type = "java.lang.Boolean", label = "Requires Physical Confirmation", description = "true if the mode being left is fireplace — the write is accepted but has no effect until a button is pressed on the thermostat display; confirmed device behavior, no payload avoids it") boolean cancelMode() {
        AtagOneHandler theHandler = handler;
        if (theHandler == null) {
            logger.warn("cancelMode called with no handler bound");
            return false;
        }
        ControlUpdateDTO control = new ControlUpdateDTO();
        DeviceConfigUpdateDTO configUpdate = new DeviceConfigUpdateDTO();
        boolean requiresPhysicalConfirmation = theHandler.composeCancel(control, configUpdate);
        theHandler.sendComposedUpdate("action:cancelMode", control, configUpdate);
        return requiresPhysicalConfirmation;
    }

    public static void activateVacation(ThingActions actions, long durationSeconds) {
        ((AtagOneActions) actions).activateVacation(durationSeconds);
    }

    public static void activateExtend(ThingActions actions, long durationSeconds) {
        ((AtagOneActions) actions).activateExtend(durationSeconds);
    }

    public static void activateFireplace(ThingActions actions, long durationSeconds) {
        ((AtagOneActions) actions).activateFireplace(durationSeconds);
    }

    public static boolean cancelMode(ThingActions actions) {
        return ((AtagOneActions) actions).cancelMode();
    }
}
