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
package org.openhab.binding.modbus.helioseasycontrols.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.ActionOutputs;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the actions available for the Helios device
 *
 * @author Bernhard Bauer - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = HeliosEasyControlsActions.class)
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
            HeliosEasyControlsHandler handler = this.handler;
            if (handler != null) {
                handler.writeValue(variableName, "1");
            }
        } catch (HeliosException e) {
            logger.warn("Error executing action triggering switch for variable {}: {}", variableName, e.getMessage());
        } catch (InterruptedException e) {
            logger.debug(
                    "{} encountered Exception when trying to lock Semaphore for writing variable {} to the device: {}",
                    HeliosEasyControlsActions.class.getSimpleName(), variableName, e.getMessage());
        }
    }

    @RuleAction(label = "@text/action.resetFilterChangeTimer.label", description = "@text/action.resetFilterChangeTimer.description")
    public void resetFilterChangeTimer() {
        triggerSwitch(HeliosEasyControlsBindingConstants.FILTER_CHANGE_RESET);
    }

    public static void resetFilterChangeTimer(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetFilterChangeTimer();
    }

    @RuleAction(label = "@text/action.resetErrors.label", description = "@text/action.resetErrors.description")
    public void resetErrors() {
        triggerSwitch(HeliosEasyControlsBindingConstants.RESET_FLAG);
    }

    public static void resetErrors(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetErrors();
    }

    @RuleAction(label = "@text/action.resetToFactoryDefaults.label", description = "@text/action.resetToFactoryDefaults.description")
    public void resetToFactoryDefaults() {
        triggerSwitch(HeliosEasyControlsBindingConstants.FACTORY_RESET);
    }

    public static void resetToFactoryDefaults(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetToFactoryDefaults();
    }

    @RuleAction(label = "@text/action.resetSwitchingTimes.label", description = "@text/action.resetSwitchingTimes.description")
    public void resetSwitchingTimes() {
        triggerSwitch(HeliosEasyControlsBindingConstants.FACTORY_SETTING_WZU);
    }

    public static void resetSwitchingTimes(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).resetSwitchingTimes();
    }

    @RuleAction(label = "@text/action.setSysDateTime.label", description = "@text/action.setSysDateTime.description")
    public void setSysDateTime() {
        HeliosEasyControlsHandler handler = this.handler;
        if (handler != null) {
            try {
                handler.setSysDateTime();
            } catch (InterruptedException e) {
                logger.debug(
                        "{} encountered Exception when trying to lock Semaphore for setting system date and time on the device: {}",
                        HeliosEasyControlsActions.class.getSimpleName(), e.getMessage());
            }
        }
    }

    public static void setSysDateTime(ThingActions actions) {
        ((HeliosEasyControlsActions) actions).setSysDateTime();
    }

    private void setBypass(boolean from, int day, int month) {
        HeliosEasyControlsHandler handler = this.handler;
        if (handler != null) {
            try {
                handler.setBypass(from, day, month);
            } catch (InterruptedException e) {
                logger.debug(
                        "{} encountered Exception when trying to lock Semaphore for setting bypass date on the device: {}",
                        HeliosEasyControlsActions.class.getSimpleName(), e.getMessage());
            }
        }
    }

    @RuleAction(label = "@text/action.setBypassFrom.label", description = "@text/action.setBypassFrom.description")
    public void setBypassFrom(
            @ActionInput(name = "day", label = "@text/action.setBypassFrom.inputParams.day.label", description = "@text/action.setBypassFrom.inputParams.day.description") int day,
            @ActionInput(name = "month", label = "@text/action.setBypassFrom.inputParams.month.label", description = "@text/action.setBypassFrom.inputParams.month.description") int month) {
        setBypass(true, day, month);
    }

    public static void setBypassFrom(ThingActions actions, int day, int month) {
        ((HeliosEasyControlsActions) actions).setBypassFrom(day, month);
    }

    @RuleAction(label = "@text/action.setBypassTo.label", description = "@text/action.setBypassTo.description")
    public void setBypassTo(
            @ActionInput(name = "day", label = "@text/action.setBypassTo.inputParams.day.label", description = "@text/action.setBypassTo.inputParams.day.description") int day,
            @ActionInput(name = "month", label = "@text/action.setBypassTo.inputParams.day.label", description = "@text/action.setBypassTo.inputParams.day.description") int month) {
        setBypass(false, day, month);
    }

    public static void setBypassTo(ThingActions actions, int day, int month) {
        ((HeliosEasyControlsActions) actions).setBypassTo(day, month);
    }

    @RuleAction(label = "@text/action.getErrorMessages.label", description = "@text/action.getErrorMessages.description")
    public @ActionOutput(label = "Error Messages", type = "java.util.List<String>") List<String> getErrorMessages() {
        HeliosEasyControlsHandler handler = this.handler;
        return (handler != null) ? handler.getErrorMessages() : new ArrayList<>();
    }

    public static List<String> getErrorMessages(ThingActions actions) {
        return ((HeliosEasyControlsActions) actions).getErrorMessages();
    }

    @RuleAction(label = "@text/action.getWarningMessages.label", description = "@text/action.getWarningMessages.description")
    public @ActionOutput(label = "Warning Messages", type = "java.util.List<String>") List<String> getWarningMessages() {
        HeliosEasyControlsHandler handler = this.handler;
        return (handler != null) ? handler.getWarningMessages() : new ArrayList<>();
    }

    public static List<String> getWarningMessages(ThingActions actions) {
        return ((HeliosEasyControlsActions) actions).getWarningMessages();
    }

    @RuleAction(label = "@text/action.getInfoMessages.label", description = "@text/action.getInfoMessages.description")
    public @ActionOutput(label = "Info Messages", type = "java.util.List<String>") List<String> getInfoMessages() {
        HeliosEasyControlsHandler handler = this.handler;
        return (handler != null) ? handler.getInfoMessages() : new ArrayList<>();
    }

    public static List<String> getInfoMessages(ThingActions actions) {
        return ((HeliosEasyControlsActions) actions).getInfoMessages();
    }

    @RuleAction(label = "@text/action.getStatusMessages.label", description = "@text/action.getStatusMessages.description")
    public @ActionOutput(label = "Status Messages", type = "java.util.List<String>") List<String> getStatusMessages() {
        HeliosEasyControlsHandler handler = this.handler;
        return (handler != null) ? handler.getStatusMessages() : new ArrayList<>();
    }

    public static List<String> getStatusMessages(ThingActions actions) {
        return ((HeliosEasyControlsActions) actions).getStatusMessages();
    }

    @RuleAction(label = "@text/action.getMessages.label", description = "@text/action.getMessages.description")
    public @ActionOutputs({
            @ActionOutput(name = "errorMessages", label = "Error Messages", type = "java.util.List<String>"),
            @ActionOutput(name = "warningMessages", label = "Warning Messages", type = "java.util.List<String>"),
            @ActionOutput(name = "infoMessages", label = "Info Messages", type = "java.util.List<String>"),
            @ActionOutput(name = "statusMessages", label = "Status Messages", type = "java.util.List<String>") }) Map<String, Object> getMessages() {
        Map<String, Object> messages = new HashMap<>();
        HeliosEasyControlsHandler handler = this.handler;
        if (handler != null) {
            messages.put("errorMessages", handler.getErrorMessages());
            messages.put("warningMessages", handler.getWarningMessages());
            messages.put("infoMessages", handler.getInfoMessages());
            messages.put("statusMessages", handler.getStatusMessages());
        }
        return messages;
    }

    public static Map<String, Object> getMessages(ThingActions actions) {
        return ((HeliosEasyControlsActions) actions).getMessages();
    }
}
