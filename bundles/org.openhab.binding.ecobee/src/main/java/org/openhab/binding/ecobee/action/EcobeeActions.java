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
package org.openhab.binding.ecobee.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.ecobee.internal.dto.thermostat.EventDTO;
import org.openhab.binding.ecobee.internal.enums.AckType;
import org.openhab.binding.ecobee.internal.enums.FanMode;
import org.openhab.binding.ecobee.internal.enums.HoldType;
import org.openhab.binding.ecobee.internal.enums.PlugState;
import org.openhab.binding.ecobee.internal.enums.VentilatorMode;
import org.openhab.binding.ecobee.internal.function.AcknowledgeFunction;
import org.openhab.binding.ecobee.internal.function.ControlPlugFunction;
import org.openhab.binding.ecobee.internal.function.CreateVacationFunction;
import org.openhab.binding.ecobee.internal.function.DeleteVacationFunction;
import org.openhab.binding.ecobee.internal.function.ResetPreferencesFunction;
import org.openhab.binding.ecobee.internal.function.ResumeProgramFunction;
import org.openhab.binding.ecobee.internal.function.SendMessageFunction;
import org.openhab.binding.ecobee.internal.function.SetHoldFunction;
import org.openhab.binding.ecobee.internal.function.SetOccupiedFunction;
import org.openhab.binding.ecobee.internal.function.UpdateSensorFunction;
import org.openhab.binding.ecobee.internal.handler.EcobeeThermostatBridgeHandler;
import org.openhab.binding.ecobee.internal.handler.EcobeeUtils;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EcobeeActions} defines the thing actions for the Ecobee binding.
 * <p>
 * <b>Note:</b>The static method <b>invokeMethod</b> handles the case where
 * the test <i>actions instanceof EcobeeActions</i> fails. This test can fail
 * due to an issue in openHAB core v2.5.0 where the {@link EcobeeActions} class
 * can be loaded by a different classloader than the <i>actions</i> instance.
 *
 * @author John Cocula - Initial contribution
 * @author Mark Hilbush - Adapted for OH2/3
 * @author Connor Petty - Proxy method for invoking actions
 */
@ThingActionsScope(name = "ecobee")
@NonNullByDefault
public class EcobeeActions implements ThingActions, IEcobeeActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(EcobeeActions.class);

    private @Nullable EcobeeThermostatBridgeHandler handler;

    public EcobeeActions() {
        LOGGER.debug("EcobeeActions: EcobeeActions: Actions service created");
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof EcobeeThermostatBridgeHandler) {
            this.handler = (EcobeeThermostatBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private static IEcobeeActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(EcobeeActions.class.getName())) {
            if (actions instanceof IEcobeeActions) {
                return (IEcobeeActions) actions;
            } else {
                return (IEcobeeActions) Proxy.newProxyInstance(IEcobeeActions.class.getClassLoader(),
                        new Class[] { IEcobeeActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException("Actions is not an instance of EcobeeActions");
    }

    /**
     * The acknowledge function allows an alert to be acknowledged.
     *
     * @see <a
     *      href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/Acknowledge.shtml">Acknowledge
     *      </a>
     */
    @Override
    @RuleAction(label = "Acknowledge", description = "Acknowledges an alert.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean acknowledge(
            @ActionInput(name = "ackRef", description = "The acknowledge ref of alert") @Nullable String ackRef,
            @ActionInput(name = "ackType", description = "The type of acknowledgement. Valid values: accept, decline, defer, unacknowledged") @Nullable String ackType,
            @ActionInput(name = "remindMeLater", description = "(opt) Whether to remind at a later date, if this is a defer acknowledgement") @Nullable Boolean remindMeLater) {
        LOGGER.debug("EcobeeActions: Action 'Acknowledge' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        AcknowledgeFunction function = new AcknowledgeFunction(localHandler.getThermostatId(), ackRef,
                AckType.forValue(ackType), remindMeLater);
        return localHandler.actionPerformFunction(function);
    }

    public static boolean acknowledge(@Nullable ThingActions actions, @Nullable String ackRef, @Nullable String ackType,
            @Nullable Boolean remindMeLater) {
        return invokeMethodOf(actions).acknowledge(ackRef, ackType, remindMeLater);
    }

    /**
     * Control the on/off state of a plug by setting a hold on the plug.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/ControlPlug.shtml">Control
     *      Plug</a>
     */
    @Override
    @RuleAction(label = "Control Plug", description = "Control the on/off state of a plug by setting a hold on the plug.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean controlPlug(
            @ActionInput(name = "plugName", description = "The name of the plug. Ensure each plug has a unique name.") @Nullable String plugName,
            @ActionInput(name = "plugState", description = "The state to put the plug into. Valid values: on, off, resume.") @Nullable String plugState,
            @ActionInput(name = "startDateTime", description = "(opt) The start date/time in thermostat time.") @Nullable Date startDateTime,
            @ActionInput(name = "endDateTime", description = "(opt) The end date/time in thermostat time.") @Nullable Date endDateTime,
            @ActionInput(name = "holdType", description = "(opt) The hold duration type. Valid values: dateTime, nextTransition, indefinite, holdHours.") @Nullable String holdType,
            @ActionInput(name = "holdHours", description = "(opt) The number of hours to hold for, used and required if holdType='holdHours'.") @Nullable Number holdHours) {
        LOGGER.debug("EcobeeActions: Action 'Control Plug' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        ControlPlugFunction function = (new ControlPlugFunction(plugName, PlugState.forValue(plugState), startDateTime,
                endDateTime, (holdType == null) ? null : HoldType.forValue(holdType),
                (holdHours == null) ? null : Integer.valueOf(holdHours.intValue())));
        return localHandler.actionPerformFunction(function);
    }

    public static boolean controlPlug(@Nullable ThingActions actions, @Nullable String plugName,
            @Nullable String plugState, @Nullable Date startDateTime, @Nullable Date endDateTime,
            @Nullable String holdType, @Nullable Number holdHours) {
        return invokeMethodOf(actions).controlPlug(plugName, plugState, startDateTime, endDateTime, holdType,
                holdHours);
    }

    /**
     * The create vacation function creates a vacation event on the thermostat.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/CreateVacation.shtml">Create
     *      Vacation</a>
     */
    @Override
    @RuleAction(label = "Create Vacation", description = "The create vacation function creates a vacation event on the thermostat.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean createVacation(
            @ActionInput(name = "name", description = "The vacation event name. It must be unique.") @Nullable String name,
            @ActionInput(name = "coolHoldTemp", description = "The temperature at which to set the cool vacation hold.") @Nullable QuantityType<Temperature> coolHoldTemp,
            @ActionInput(name = "heatHoldTemp", description = "The temperature at which to set the heat vacation hold.") @Nullable QuantityType<Temperature> heatHoldTemp,
            @ActionInput(name = "startDateTime", description = "(opt) The start date/time in thermostat time.") @Nullable Date startDateTime,
            @ActionInput(name = "endDateTime", description = "(opt) The end date in thermostat time.") @Nullable Date endDateTime,
            @ActionInput(name = "fan", description = "(opt) The fan mode during the vacation. Values: auto, on Default: auto") @Nullable String fan,
            @ActionInput(name = "fanMinOnTime", description = "(opt) The minimum number of minutes to run the fan each hour. Range: 0-60, Default: 0") @Nullable Number fanMinOnTime) {
        LOGGER.debug("EcobeeActions: Action 'Create Vacation' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        CreateVacationFunction function = new CreateVacationFunction(name, coolHoldTemp, heatHoldTemp, startDateTime,
                endDateTime, (fan == null) ? null : FanMode.forValue(fan),
                (fanMinOnTime == null) ? null : Integer.valueOf(fanMinOnTime.intValue()));
        return localHandler.actionPerformFunction(function);
    }

    public static boolean createVacation(@Nullable ThingActions actions, @Nullable String name,
            @Nullable QuantityType<Temperature> coolHoldTemp, @Nullable QuantityType<Temperature> heatHoldTemp,
            @Nullable Date startDateTime, @Nullable Date endDateTime, @Nullable String fan,
            @Nullable Number fanMinOnTime) {
        return invokeMethodOf(actions).createVacation(name, coolHoldTemp, heatHoldTemp, startDateTime, endDateTime, fan,
                fanMinOnTime);
    }

    /**
     * The delete vacation function deletes a vacation event from a thermostat.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/DeleteVacation.shtml">Delete
     *      Vacation</a>
     */
    @Override
    @RuleAction(label = "Delete Vacation", description = "The delete vacation function deletes a vacation event from a thermostat.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean deleteVacation(
            @ActionInput(name = "name", description = "The vacation event name to delete.") @Nullable String name) {
        LOGGER.debug("EcobeeActions: Action 'Delete Vacation' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        DeleteVacationFunction function = new DeleteVacationFunction(name);
        return localHandler.actionPerformFunction(function);
    }

    public static boolean deleteVacation(@Nullable ThingActions actions, @Nullable String name) {
        return invokeMethodOf(actions).deleteVacation(name);
    }

    /**
     * The reset preferences function sets all of the user configurable settings back to the factory default values.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/ResetPreferences.shtml">Reset
     *      Preferences</a>
     */
    @Override
    @RuleAction(label = "Reset Preferences", description = "The reset preferences function sets all of the user configurable settings back to the factory default values.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean resetPreferences() {
        LOGGER.debug("EcobeeActions: Action 'Reset Preferences' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        ResetPreferencesFunction function = new ResetPreferencesFunction();
        return localHandler.actionPerformFunction(function);
    }

    public static boolean resetPreferences(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).resetPreferences();
    }

    /**
     * The resume program function removes the currently running event.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/ResumeProgram.shtml">Resume
     *      Program</a>
     */
    @Override
    @RuleAction(label = "Resume Program", description = "Removes the currently running event providing the event is not a mandatory demand response event")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean resumeProgram(
            @ActionInput(name = "resumeAll", description = "(opt) Should the thermostat be resumed to next event (false) or to its program (true)") @Nullable Boolean resumeAll) {
        LOGGER.debug("EcobeeActions: Action 'Resume Program' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        ResumeProgramFunction function = new ResumeProgramFunction(resumeAll);
        return localHandler.actionPerformFunction(function);
    }

    public static boolean resumeProgram(@Nullable ThingActions actions, @Nullable Boolean resumeAll) {
        return invokeMethodOf(actions).resumeProgram(resumeAll);
    }

    /**
     * The send message function allows an alert message to be sent to the thermostat.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/SendMessage.shtml">Send
     *      Message</a>
     */
    @Override
    @RuleAction(label = "Send Message", description = "The send message function allows an alert message to be sent to the thermostat.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean sendMessage(
            @ActionInput(name = "text", description = "The message text to send. Text will be truncated to 500 characters if longer") @Nullable String text) {
        LOGGER.debug("EcobeeActions: Action 'SendMessage' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        SendMessageFunction function = new SendMessageFunction(text);
        return localHandler.actionPerformFunction(function);
    }

    public static boolean sendMessage(@Nullable ThingActions actions, @Nullable String text) {
        return invokeMethodOf(actions).sendMessage(text);
    }

    /**
     * Set an indefinite hold using the supplied the cool and heat hold temperatures
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/SetHold.shtml">Set Hold</a>
     */
    @Override
    @RuleAction(label = "Set Hold", description = "The set hold function sets the thermostat into a hold with the specified temperatures.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean setHold(
            @ActionInput(name = "coolHoldTemp", description = "The temperature at which to set the cool hold.") @Nullable QuantityType<Temperature> coolHoldTemp,
            @ActionInput(name = "heatHoldTemp", description = "The temperature at which to set the heat hold.") @Nullable QuantityType<Temperature> heatHoldTemp) {
        if (coolHoldTemp == null || heatHoldTemp == null) {
            throw new IllegalArgumentException("hold temperatures cannot be null");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("coolHoldTemp", coolHoldTemp);
        params.put("heatHoldTemp", heatHoldTemp);
        return setHold(params, null, null, null, null);
    }

    public static boolean setHold(@Nullable ThingActions actions, @Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp) {
        return invokeMethodOf(actions).setHold(coolHoldTemp, heatHoldTemp);
    }

    /**
     * Set a hold by providing the cool and heat temperatures and the number of hours.
     */
    @Override
    @RuleAction(label = "Set Hold", description = "The set hold function sets the thermostat into a hold for the specified number of hours.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean setHold(
            @ActionInput(name = "coolHoldTemp", description = "The temperature at which to set the cool hold.") @Nullable QuantityType<Temperature> coolHoldTemp,
            @ActionInput(name = "heatHoldTemp", description = "The temperature at which to set the heat hold.") @Nullable QuantityType<Temperature> heatHoldTemp,
            @ActionInput(name = "holdHours", description = "The number of hours for the hold.") @Nullable Number holdHours) {
        if (coolHoldTemp == null || heatHoldTemp == null) {
            throw new IllegalArgumentException("hold temperatures cannot be null");
        }
        if (holdHours == null) {
            throw new IllegalArgumentException("number of hold hours is missing");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("coolHoldTemp", coolHoldTemp);
        params.put("heatHoldTemp", heatHoldTemp);
        params.put("holdType", HoldType.HOLD_HOURS);
        params.put("holdHours", Integer.valueOf(holdHours.intValue()));
        return setHold(params, null, null, null, null);
    }

    public static boolean setHold(@Nullable ThingActions actions, @Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp, @Nullable Number holdHours) {
        return invokeMethodOf(actions).setHold(coolHoldTemp, heatHoldTemp, holdHours);
    }

    /**
     * Set an indefinite hold using the supplied climateRef
     */
    @Override
    @RuleAction(label = "Set Hold", description = "The set hold function sets the thermostat into a hold with the specified climate ref.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean setHold(
            @ActionInput(name = "holdClimateRef", description = "The holdClimateRef used to set the hold.") @Nullable String holdClimateRef) {
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        if (holdClimateRef == null || !localHandler.isValidClimateRef(holdClimateRef)) {
            throw new IllegalArgumentException("hold climate ref is missing or invalid");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("holdClimateRef", holdClimateRef);
        return setHold(params, null, null, null, null);
    }

    public static boolean setHold(@Nullable ThingActions actions, @Nullable String holdClimateRef) {
        return invokeMethodOf(actions).setHold(holdClimateRef);
    }

    /**
     * Set a hold using the supplied climateRef for the supplied number of hours.
     */
    @Override
    @RuleAction(label = "Set Hold", description = "The set hold function sets the thermostat into a hold with the specified climate ref.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean setHold(
            @ActionInput(name = "holdClimateRef", description = "The holdClimateRef used to set the hold.") @Nullable String holdClimateRef,
            @ActionInput(name = "holdHours", description = "The number of hours for the hold.") @Nullable Number holdHours) {
        if (holdHours == null) {
            throw new IllegalArgumentException("number of hold hours is missing");
        }
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        if (holdClimateRef == null || !localHandler.isValidClimateRef(holdClimateRef)) {
            throw new IllegalArgumentException("hold climate ref is missing or invalid");
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("holdClimateRef", holdClimateRef);
        params.put("holdType", HoldType.HOLD_HOURS);
        params.put("holdHours", Integer.valueOf(holdHours.intValue()));
        return setHold(params, null, null, null, null);
    }

    public static boolean setHold(@Nullable ThingActions actions, @Nullable String holdClimateRef,
            @Nullable Number holdHours) {
        return invokeMethodOf(actions).setHold(holdClimateRef, holdHours);
    }

    /**
     * Set a hold
     */
    @Override
    @RuleAction(label = "Set Hold", description = "The set hold function sets the thermostat into a hold with the specified temperature or climate ref.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean setHold(
            @ActionInput(name = "coolHoldTemp", description = "(opt) The temperature at which to set the cool hold.") @Nullable QuantityType<Temperature> coolHoldTemp,
            @ActionInput(name = "heatHoldTemp", description = "(opt) The temperature at which to set the heat hold.") @Nullable QuantityType<Temperature> heatHoldTemp,
            @ActionInput(name = "holdClimateRef", description = "(opt) The Climate to use as reference for setting the coolHoldTemp, heatHoldTemp and fan settings for this hold. If this value is passed the coolHoldTemp and heatHoldTemp are not required.") @Nullable String holdClimateRef,
            @ActionInput(name = "startDateTime", description = "(opt) The start date in thermostat time.") @Nullable Date startDateTime,
            @ActionInput(name = "endDateTime", description = "(opt) The end date in thermostat time.") @Nullable Date endDateTime,
            @ActionInput(name = "holdType", description = "(opt) The hold duration type. Valid values: dateTime, nextTransition, indefinite, holdHours.") @Nullable String holdType,
            @ActionInput(name = "holdHours", description = "(opt) The number of hours to hold for, used and required if holdType='holdHours'.") @Nullable Number holdHours) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (coolHoldTemp != null) {
            params.put("coolHoldTemp", coolHoldTemp);
        }
        if (heatHoldTemp != null) {
            params.put("heatHoldTemp", heatHoldTemp);
        }
        if (holdClimateRef != null) {
            params.put("holdClimateRef", holdClimateRef);
        }
        return setHold(params, holdType, holdHours, startDateTime, endDateTime);
    }

    public static boolean setHold(@Nullable ThingActions actions, @Nullable QuantityType<Temperature> coolHoldTemp,
            @Nullable QuantityType<Temperature> heatHoldTemp, @Nullable String holdClimateRef,
            @Nullable Date startDateTime, @Nullable Date endDateTime, @Nullable String holdType,
            @Nullable Number holdHours) {
        return invokeMethodOf(actions).setHold(coolHoldTemp, heatHoldTemp, holdClimateRef, startDateTime, endDateTime,
                holdType, holdHours);
    }

    /**
     * Set a hold by providing a parameter map
     */
    @Override
    @RuleAction(label = "Set Hold", description = "The set hold function sets the thermostat into a hold with the specified event parameters.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean setHold(
            @ActionInput(name = "params", description = "The map of hold parameters.") @Nullable Map<String, Object> params,
            @ActionInput(name = "holdType", description = "(opt) The hold duration type. Valid values: dateTime, nextTransition, indefinite, holdHours.") @Nullable String holdType,
            @ActionInput(name = "holdHours", description = "(opt) The number of hours to hold for, used and required if holdType='holdHours'.") @Nullable Number holdHours,
            @ActionInput(name = "startDateTime", description = "(opt) The start date in thermostat time.") @Nullable Date startDateTime,
            @ActionInput(name = "endDateTime", description = "(opt) The end date in thermostat time.") @Nullable Date endDateTime) {
        LOGGER.debug("EcobeeActions: Action 'SetHold' called");
        if (params == null) {
            throw new IllegalArgumentException("params cannot be null");
        }
        EventDTO event = new EventDTO();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            switch (key) {
                case "isOccupied":
                    event.isOccupied = ((Boolean) value);
                    break;
                case "isCoolOff":
                    event.isCoolOff = ((Boolean) value);
                    break;
                case "isHeatOff":
                    event.isHeatOff = ((Boolean) value);
                    break;
                case "coolHoldTemp":
                    event.coolHoldTemp = EcobeeUtils.convertQuantityTypeToEcobeeTemp(value);
                    break;
                case "heatHoldTemp":
                    event.heatHoldTemp = EcobeeUtils.convertQuantityTypeToEcobeeTemp(value);
                    break;
                case "fan":
                    event.fan = FanMode.forValue((String) value).toString();
                    break;
                case "vent":
                    event.vent = VentilatorMode.forValue((String) value).toString();
                    break;
                case "ventilatorMinOnTime":
                    event.ventilatorMinOnTime = ((Integer) value);
                    break;
                case "isOptional":
                    event.isOptional = ((Boolean) value);
                    break;
                case "isTemperatureRelative":
                    event.isTemperatureRelative = ((Boolean) value);
                    break;
                case "coolRelativeTemp":
                    event.coolRelativeTemp = EcobeeUtils.convertQuantityTypeToEcobeeTemp(value);
                    break;
                case "heatRelativeTemp":
                    event.heatRelativeTemp = EcobeeUtils.convertQuantityTypeToEcobeeTemp(value);
                    break;
                case "isTemperatureAbsolute":
                    event.isTemperatureAbsolute = ((Boolean) value);
                    break;
                case "fanMinOnTime":
                    event.fanMinOnTime = ((Integer) value);
                    break;
                case "holdClimateRef":
                    event.holdClimateRef = ((String) value);
                    break;
                default:
                    LOGGER.warn("Unrecognized event field '{}' with value '{}' ignored.", key, value);
                    break;
            }
        }
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        SetHoldFunction function = new SetHoldFunction(event, (holdType == null) ? null : HoldType.forValue(holdType),
                (holdHours == null) ? null : holdHours.intValue(), startDateTime, endDateTime);
        return localHandler.actionPerformFunction(function);
    }

    public static boolean setHold(@Nullable ThingActions actions, @Nullable Map<String, Object> params,
            @Nullable String holdType, @Nullable Number holdHours, @Nullable Date startDateTime,
            @Nullable Date endDateTime) {
        return invokeMethodOf(actions).setHold(params, holdType, holdHours, startDateTime, endDateTime);
    }

    /**
     * The set occupied function may only be used by EMS thermostats. The function switches a thermostat from occupied
     * mode to unoccupied, or vice versa.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/SetOccupied.shtml">Set
     *      Occupied</a>
     */
    @Override
    @RuleAction(label = "Set Occupied", description = "The function switches a thermostat from occupied mode to unoccupied, or vice versa (EMS MODELS ONLY).")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean setOccupied(
            @ActionInput(name = "occupied", description = "The climate to use for the temperature, occupied (true) or unoccupied (false).") @Nullable Boolean occupied,
            @ActionInput(name = "startDateTime", description = "(opt) The start date in thermostat time.") @Nullable Date startDateTime,
            @ActionInput(name = "endDateTime", description = "(opt) The end date in thermostat time.") @Nullable Date endDateTime,
            @ActionInput(name = "holdType", description = "(opt) The hold duration type. Valid values: dateTime, nextTransition, indefinite, holdHours.") @Nullable String holdType,
            @ActionInput(name = "holdHours", description = "(opt) The number of hours to hold for, used and required if holdType='holdHours'.") @Nullable Number holdHours) {
        LOGGER.debug("EcobeeActions: Action 'Set Occupied' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        SetOccupiedFunction function = new SetOccupiedFunction(occupied, startDateTime, endDateTime,
                (holdType == null) ? null : HoldType.forValue(holdType),
                (holdHours == null) ? null : Integer.valueOf(holdHours.intValue()));
        return localHandler.actionPerformFunction(function);
    }

    public static boolean setOccupied(@Nullable ThingActions actions, @Nullable Boolean occupied,
            @Nullable Date startDateTime, @Nullable Date endDateTime, @Nullable String holdType,
            @Nullable Number holdHours) {
        return invokeMethodOf(actions).setOccupied(occupied, startDateTime, endDateTime, holdType, holdHours);
    }

    /**
     * The update sensor function allows the caller to update the name of an ecobee3 remote sensor.
     *
     * @see <a href="https://www.ecobee.com/home/developer/api/documentation/v1/functions/UpdateSensor.shtml">Update
     *      Sensor</a>
     */
    @Override
    @RuleAction(label = "Update Sensor", description = "The update sensor function allows the caller to update the name of an ecobee3 remote sensor.")
    public @ActionOutput(name = "success", type = "java.lang.Boolean") Boolean updateSensor(
            @ActionInput(name = "name", description = "The updated name to give the sensor. Has a max length of 32, but shorter is recommended.") @Nullable String name,
            @ActionInput(name = "deviceId", description = "The deviceId for the sensor, typically this indicates the enclosure and corresponds to the ThermostatRemoteSensor.id field. For example: rs:100") @Nullable String deviceId,
            @ActionInput(name = "sensorId", description = "The identifier for the sensor within the enclosure. Corresponds to the RemoteSensorCapability.id. For example: 1") @Nullable String sensorId) {
        LOGGER.debug("EcobeeActions: Action 'UpdateSensor' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return false;
        }
        UpdateSensorFunction function = new UpdateSensorFunction(name, deviceId, sensorId);
        return localHandler.actionPerformFunction(function);
    }

    public static boolean updateSensor(@Nullable ThingActions actions, @Nullable String name, @Nullable String deviceId,
            @Nullable String sensorId) {
        return invokeMethodOf(actions).updateSensor(name, deviceId, sensorId);
    }

    /**
     * Get the alerts list. Returns a JSON string containing all the alerts.
     */
    @Override
    @RuleAction(label = "Get Alerts", description = "Get the alerts list")
    public @ActionOutput(name = "alerts", type = "java.lang.String") @Nullable String getAlerts() {
        LOGGER.debug("EcobeeActions: Action 'Get Alerts' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return null;
        }
        return localHandler.getAlerts();
    }

    public static @Nullable String getAlerts(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).getAlerts();
    }

    /**
     * Get the events list. Returns a JSON string contains all events.
     */
    @Override
    @RuleAction(label = "Get Events", description = "Get the events list")
    public @ActionOutput(name = "events", type = "java.lang.String") @Nullable String getEvents() {
        LOGGER.debug("EcobeeActions: Action 'Get Events' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return null;
        }
        return localHandler.getEvents();
    }

    public static @Nullable String getEvents(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).getEvents();
    }

    /**
     * Get a list of climates. Returns a JSON string contains all climates.
     */
    @Override
    @RuleAction(label = "Get Climates", description = "Get a list of climates")
    public @ActionOutput(name = "climates", type = "java.lang.String") @Nullable String getClimates() {
        LOGGER.debug("EcobeeActions: Action 'Get Climates' called");
        EcobeeThermostatBridgeHandler localHandler = handler;
        if (localHandler == null) {
            LOGGER.info("EcobeeActions: Action service ThingHandler is null!");
            return null;
        }
        return localHandler.getClimates();
    }

    public static @Nullable String getClimates(@Nullable ThingActions actions) {
        return invokeMethodOf(actions).getClimates();
    }
}
