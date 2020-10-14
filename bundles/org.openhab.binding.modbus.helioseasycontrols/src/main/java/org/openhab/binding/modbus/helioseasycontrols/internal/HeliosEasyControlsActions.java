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
package org.openhab.binding.modbus.helioseasycontrols.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the actions available for the Helios device
 *
 * @author Bernhard Bauer - Initial contribution
 */
@ThingActionsScope(name = "modbus.helioseasycontrols")
@NonNullByDefault
public class HeliosEasyControlsActions implements ThingActions {

    private @Nullable HeliosEasyControlsHandler handler;

    private final Logger logger = LoggerFactory.getLogger(HeliosEasyControlsActions.class);

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (HeliosEasyControlsHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    private void triggerSwitch(String variableName) {
        try {
            if (handler != null) {
                handler.writeValue(variableName, "1");
            }
        } catch (HeliosException e) {
            logger.warn("Error executing action 'resetFilterChangeTimer': {}", e.getMessage());
        }
    }

    @RuleAction(label = "reset filter change timer", description = "Sets the filter change timer back to the configured interval.")
    public void resetFilterChangeTimer() {
        triggerSwitch(HeliosEasyControlsBindingConstants.FILTER_CHANGE_RESET);
    }

    public static void resetFilterChangeTimer(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetFilterChangeTimer();
    }

    @RuleAction(label = "reset error messages", description = "Reset error/warning/info messages.")
    public void resetErrors() {
        triggerSwitch(HeliosEasyControlsBindingConstants.RESET_FLAG);
    }

    public static void resetErrors(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetErrors();
    }

    @RuleAction(label = "reset to factory defaults", description = "Reset device to factory defaults.")
    public void resetToFactoryDefaults() {
        triggerSwitch(HeliosEasyControlsBindingConstants.FACTORY_RESET);
    }

    public static void resetToFactoryDefaults(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetToFactoryDefaults();
    }

    @RuleAction(label = "reset individual switching times", description = "Reset individual switching times.")
    public void resetSwitchingTimes() {
        triggerSwitch(HeliosEasyControlsBindingConstants.FACTORY_SETTING_WZU);
    }

    public static void resetSwitchingTimes(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetSwitchingTimes();
    }

    @RuleAction(label = "set system date and time", description = "Sets the device's system date and time based on OH's system date and time.")
    public void setSysDateTime() {
        HeliosEasyControlsHandler handler = this.handler;
        if (handler != null) {
            handler.setSysDateTime();
        }
    }

    public static void setSysDateTime(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).setSysDateTime();
    }

    private void setBypass(boolean from, int day, int month) {
        HeliosEasyControlsHandler handler = this.handler;
        if (handler != null) {
            handler.setBypass(from, day, month);
        }
    }

    @RuleAction(label = "set the bypass from day and month", description = "Sets the day and month from when the bypass should be active.")
    public void setBypassFrom(
            @ActionInput(name = "day", label = "bypass from day", description = "The day from when the bypass should be active") int day,
            @ActionInput(name = "month", label = "bypass from month", description = "The month from when the bypass should be active") int month) {
        setBypass(true, day, month);
    }

    public static void setBypassFrom(ThingActions actions, int day, int month) {
        ((HeliosEasyControlsActions) actions).setBypassFrom(day, month);
    }

    @RuleAction(label = "set the bypass to day and month", description = "Sets the day and month until when the bypass should be active.")
    public void setBypassTo(
            @ActionInput(name = "day", label = "bypass to day", description = "The day until when the bypass should be active") int day,
            @ActionInput(name = "month", label = "bypass to month", description = "The month until when the bypass should be active") int month) {
        setBypass(false, day, month);
    }

    public static void setBypassTo(ThingActions actions, int day, int month) {
        ((HeliosEasyControlsActions) actions).setBypassTo(day, month);
    }
}
