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
package org.openhab.binding.caddx.internal.action;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.caddx.internal.handler.ThingHandlerPanel;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the
 * caddx bridge actions.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@ThingActionsScope(name = "caddx")
@NonNullByDefault
public class CaddxPanelActions implements ThingActions, ICaddxPanelActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxPanelActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerPanel is null!";
    private static final String PIN_IS_NULL = "The value for the pin is null. Action not executed.";
    private static final String PIN_IS_INVALID = "The value for the pin [{}] is invalid. Action not executed.";
    private static final String ACTION_CLASS_IS_WRONG = "Instance is not a CaddxPanelActions class.";

    private @Nullable ThingHandlerPanel handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ThingHandlerPanel) {
            this.handler = (ThingHandlerPanel) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private static ICaddxPanelActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(CaddxPanelActions.class.getName())) {
            if (actions instanceof ICaddxPanelActions) {
                return (ICaddxPanelActions) actions;
            } else {
                return (ICaddxPanelActions) Proxy.newProxyInstance(ICaddxPanelActions.class.getClassLoader(),
                        new Class[] { ICaddxPanelActions.class }, (Object proxy, Method method, Object[] args) -> {
                            Method m = actions.getClass().getDeclaredMethod(method.getName(),
                                    method.getParameterTypes());
                            return m.invoke(actions, args);
                        });
            }
        }
        throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
    }

    // Valid are only 4 or 6 digit pins
    private @Nullable String adjustPin(@Nullable String pin) {
        if (pin == null) {
            logger.debug(PIN_IS_NULL);
            return null;
        }

        if (!pin.matches("^\\d{4,4}|\\d{6,6}$")) {
            logger.debug(PIN_IS_INVALID, pin);
            return null;
        }

        return (pin.length() == 4) ? pin + "00" : pin;
    }

    @Override
    @RuleAction(label = "turnOffAnySounderOrAlarmOnPanel", description = "Turn off any sounder or alarm on all partitions")
    public void turnOffAnySounderOrAlarmOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.turnOffAnySounderOrAlarm(adjustedPin);
    }

    @RuleAction(label = "turnOffAnySounderOrAlarmOnPanel", description = "Turn off any sounder or alarm on all partitions")
    public static void turnOffAnySounderOrAlarmOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).turnOffAnySounderOrAlarmOnPanel(pin);
    }

    @Override
    @RuleAction(label = "disarmOnPanel", description = "Disarm all partitions")
    public void disarmOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.disarm(adjustedPin);
    }

    @RuleAction(label = "disarmOnPanel", description = "Disarm all partitions")
    public static void disarmOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).disarmOnPanel(pin);
    }

    @Override
    @RuleAction(label = "armInAwayModeOnPanel", description = "Arm in away mode all partitions")
    public void armInAwayModeOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.armInAwayMode(adjustedPin);
    }

    @RuleAction(label = "armInAwayModeOnPanel", description = "Arm in away mode all partitions")
    public static void armInAwayModeOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).armInAwayModeOnPanel(pin);
    }

    @Override
    @RuleAction(label = "armInStayModeOnPanel", description = "Arm in stay mode all partitions")
    public void armInStayModeOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.armInStayMode(adjustedPin);
    }

    @RuleAction(label = "armInStayModeOnPanel", description = "Arm in stay mode all partitions")
    public static void armInStayModeOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).armInStayModeOnPanel(pin);
    }

    @Override
    @RuleAction(label = "cancelOnPanel", description = "Cancel command on all partitions")
    public void cancelOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.cancel(adjustedPin);
    }

    @RuleAction(label = "cancelOnPanel", description = "Cancel command on all partitions")
    public static void cancelOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).cancelOnPanel(pin);
    }

    @Override
    @RuleAction(label = "initiateAutoArm", description = "Initiate auto arm")
    public void initiateAutoArmOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.initiateAutoArm(adjustedPin);
    }

    @RuleAction(label = "initiateAutoArmOnPanel", description = "Initiate auto arm on all partitions")
    public static void initiateAutoArmOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).initiateAutoArmOnPanel(pin);
    }

    @Override
    @RuleAction(label = "startWalkTestModeOnPanel", description = "Start walk-test mode on all partitions")
    public void startWalkTestModeOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.startWalkTestMode(adjustedPin);
    }

    @RuleAction(label = "startWalkTestModeOnPanel", description = "Start walk-test mode on all partitions")
    public static void startWalkTestModeOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).startWalkTestModeOnPanel(pin);
    }

    @Override
    @RuleAction(label = "stopWalkTestModeOnPanel", description = "Stop walk-test mode on all partitions")
    public void stopWalkTestModeOnPanel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        String adjustedPin = adjustPin(pin);
        if (adjustedPin == null) {
            return;
        }

        handler.stopWalkTestMode(adjustedPin);
    }

    @RuleAction(label = "stopWalkTestModeOnPanel", description = "Stop walk-test mode on all partitions")
    public static void stopWalkTestModeOnPanel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).stopWalkTestModeOnPanel(pin);
    }

    @Override
    @RuleAction(label = "stayOnPanel", description = "Stay (1 button arm / toggle interiors) on all partitions")
    public void stayOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.stay();
    }

    @RuleAction(label = "stayOnPanel", description = "Stay (1 button arm / toggle interiors) on all partitions")
    public static void stayOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).stayOnPanel();
    }

    @Override
    @RuleAction(label = "chimeOnPanel", description = "Chime (toggle chime mode) on all partitions")
    public void chimeOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.chime();
    }

    @RuleAction(label = "chimeOnPanel", description = "Chime (toggle chime mode) on all partitions")
    public static void chimeOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).chimeOnPanel();
    }

    @Override
    @RuleAction(label = "exitOnPanel", description = "Exit (1 button arm / toggle instant) on all partitions")
    public void exitOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.exit();
    }

    @RuleAction(label = "exitOnPanel", description = "Exit (1 button arm / toggle instant) on all partitions")
    public static void exitOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).exitOnPanel();
    }

    @Override
    @RuleAction(label = "bypassInteriorsOnPanel", description = "Bypass Interiors on all partitions")
    public void bypassInteriorsOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.bypassInteriors();
    }

    @RuleAction(label = "bypassInteriorsOnPanel", description = "Bypass Interiors on all partitions")
    public static void bypassInteriorsOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).bypassInteriorsOnPanel();
    }

    @Override
    @RuleAction(label = "firePanicOnPanel", description = "Fire Panic on all partitions")
    public void firePanicOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.firePanic();
    }

    @RuleAction(label = "firePanicOnPanel", description = "Fire Panic on all partitions")
    public static void firePanicOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).firePanicOnPanel();
    }

    @Override
    @RuleAction(label = "medicalPanicOnPanel", description = "Medical Panic on all partitions")
    public void medicalPanicOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.medicalPanic();
    }

    @RuleAction(label = "medicalPanicOnPanel", description = "Medical Panic on all partitions")
    public static void medicalPanicOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).medicalPanicOnPanel();
    }

    @Override
    @RuleAction(label = "policePanicOnPanel", description = "Police Panic on all partitions")
    public void policePanicOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.policePanic();
    }

    @RuleAction(label = "policePanicOnPanel", description = "Police Panic on all partitions")
    public static void policePanicOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).policePanicOnPanel();
    }

    @Override
    @RuleAction(label = "smokeDetectorResetOnPanel", description = "Smoke detector reset on all partitions")
    public void smokeDetectorResetOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.smokeDetectorReset();
    }

    @RuleAction(label = "smokeDetectorResetOnPanel", description = "Smoke detector reset on all partitions")
    public static void smokeDetectorResetOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).smokeDetectorResetOnPanel();
    }

    @Override
    @RuleAction(label = "autoCallbackDownloadOnPanel", description = "Auto callback download on all partitions")
    public void autoCallbackDownloadOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.autoCallbackDownload();
    }

    @RuleAction(label = "autoCallbackDownloadOnPanel", description = "Auto callback download on all partitions")
    public static void autoCallbackDownloadOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).autoCallbackDownloadOnPanel();
    }

    @Override
    @RuleAction(label = "manualPickupDownloadOnPanel", description = "Manual pickup download on all partitions")
    public void manualPickupDownloadOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.manualPickupDownload();
    }

    @RuleAction(label = "manualPickupDownloadOnPanel", description = "Manual pickup download on all partitions")
    public static void manualPickupDownloadOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).manualPickupDownloadOnPanel();
    }

    @Override
    @RuleAction(label = "enableSilentExitOnPanel", description = "Enable silent exit on all partitions")
    public void enableSilentExitOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.enableSilentExit();
    }

    @RuleAction(label = "enableSilentExitOnPanel", description = "Enable silent exit on all partitions")
    public static void enableSilentExitOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).enableSilentExitOnPanel();
    }

    @Override
    @RuleAction(label = "performTestOnPanel", description = "Perform test on all partitions")
    public void performTestOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.performTest();
    }

    @RuleAction(label = "performTestOnPanel", description = "Perform test on all partitions")
    public static void performTestOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).performTestOnPanel();
    }

    @Override
    @RuleAction(label = "groupBypassOnPanel", description = "Group Bypass on all partitions")
    public void groupBypassOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.groupBypass();
    }

    @RuleAction(label = "groupBypassOnPanel", description = "Group Bypass on all partitions")
    public static void groupBypassOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).groupBypassOnPanel();
    }

    @Override
    @RuleAction(label = "auxiliaryFunction1OnPanel", description = "Auxiliary Function 1 on all partitions")
    public void auxiliaryFunction1OnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction1();
    }

    @RuleAction(label = "auxiliaryFunction1OnPanel", description = "Auxiliary Function 1 on all partitions")
    public static void auxiliaryFunction1OnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).auxiliaryFunction1OnPanel();
    }

    @Override
    @RuleAction(label = "auxiliaryFunction2OnPanel", description = "Auxiliary Function 2 on all partitions")
    public void auxiliaryFunction2OnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction2();
    }

    @RuleAction(label = "auxiliaryFunction2OnPanel", description = "Auxiliary Function 2 on all partitions")
    public static void auxiliaryFunction2OnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).auxiliaryFunction2OnPanel();
    }

    @Override
    @RuleAction(label = "startKeypadSounderOnPanel", description = "Start keypad sounder on all partitions")
    public void startKeypadSounderOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.startKeypadSounder();
    }

    @RuleAction(label = "startKeypadSounderOnPanel", description = "Start keypad sounder on all partitions")
    public static void startKeypadSounderOnPanel(@Nullable ThingActions actions) {
        invokeMethodOf(actions).startKeypadSounderOnPanel();
    }
}
