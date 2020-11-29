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
import org.openhab.binding.caddx.internal.handler.ThingHandlerPartition;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
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
public class CaddxPartitionActions implements ThingActions, ICaddxPartitionActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxPartitionActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerPartition is null!";
    private static final String ACTION_CLASS_IS_WRONG = "Instance is not a CaddxPartitionActions class.";

    private @Nullable ThingHandlerPartition handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ThingHandlerPartition) {
            this.handler = (ThingHandlerPartition) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    private static ICaddxPartitionActions invokeMethodOf(@Nullable ThingActions actions) {
        if (actions == null) {
            throw new IllegalArgumentException("actions cannot be null");
        }
        if (actions.getClass().getName().equals(CaddxPartitionActions.class.getName())) {
            if (actions instanceof ICaddxPartitionActions) {
                return (ICaddxPartitionActions) actions;
            } else {
                return (ICaddxPartitionActions) Proxy.newProxyInstance(ICaddxPartitionActions.class.getClassLoader(),
                        new Class[] { ICaddxPartitionActions.class }, (Object proxy, Method method, Object[] args) -> {
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
            logger.debug("Skipping command due to null value for pin.");
            return null;
        }

        if (!pin.matches("^\\d{4,4}|\\d{6,6}$")) {
            logger.debug("Skipping command due to invalid value for pin. {}", pin);
            return null;
        }

        return (pin.length() == 4) ? pin + "00" : pin;
    }

    @Override
    @RuleAction(label = "turnOffAnySounderOrAlarm", description = "Turn off any sounder or alarm")
    public void turnOffAnySounderOrAlarm(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "turnOffAnySounderOrAlarm", description = "Turn off any sounder or alarm")
    public static void turnOffAnySounderOrAlarm(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).turnOffAnySounderOrAlarm(pin);
    }

    @Override
    @RuleAction(label = "disarm", description = "Dis-arm")
    public void disarm(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "disarm", description = "Dis-arm")
    public static void disarm(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).disarm(pin);
    }

    @Override
    @RuleAction(label = "armInAwayMode", description = "Arm in away mode")
    public void armInAwayMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "armInAwayMode", description = "Arm in away mode")
    public static void armInAwayMode(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).armInAwayMode(pin);
    }

    @Override
    @RuleAction(label = "armInStayMode", description = "Arm in stay mode")
    public void armInStayMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "armInStayMode", description = "Arm in stay mode")
    public static void armInStayMode(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).armInStayMode(pin);
    }

    @Override
    @RuleAction(label = "cancel", description = "Cancel")
    public void cancel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "cancel", description = "Cancel")
    public static void cancel(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).cancel(pin);
    }

    @Override
    @RuleAction(label = "initiateAutoArm", description = "Initiate auto arm")
    public void initiateAutoArm(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "initiateAutoArm", description = "Initiate auto arm")
    public static void initiateAutoArm(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).initiateAutoArm(pin);
    }

    @Override
    @RuleAction(label = "startWalkTestMode", description = "Start walk-test mode")
    public void startWalkTestMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "startWalkTestMode", description = "Start walk-test mode")
    public static void startWalkTestMode(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).startWalkTestMode(pin);
    }

    @Override
    @RuleAction(label = "stopWalkTestMode", description = "Stop walk-test mode")
    public void stopWalkTestMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin) {
        ThingHandlerPartition handler = this.handler;
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

    @RuleAction(label = "stopWalkTestMode", description = "Stop walk-test mode")
    public static void stopWalkTestMode(@Nullable ThingActions actions, @Nullable String pin) {
        invokeMethodOf(actions).stopWalkTestMode(pin);
    }

    @Override
    @RuleAction(label = "stay", description = "Stay (1 button arm / toggle interiors)")
    public void stay() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.stay();
    }

    @RuleAction(label = "stay", description = "Stay (1 button arm / toggle interiors)")
    public static void stay(@Nullable ThingActions actions) {
        invokeMethodOf(actions).stay();
    }

    @Override
    @RuleAction(label = "chime", description = "Chime (toggle chime mode)")
    public void chime() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.chime();
    }

    @RuleAction(label = "chime", description = "Chime (toggle chime mode)")
    public static void chime(@Nullable ThingActions actions) {
        invokeMethodOf(actions).chime();
    }

    @Override
    @RuleAction(label = "exit", description = "Exit (1 button arm / toggle instant)")
    public void exit() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.exit();
    }

    @RuleAction(label = "exit", description = "Exit (1 button arm / toggle instant)")
    public static void exit(@Nullable ThingActions actions) {
        invokeMethodOf(actions).exit();
    }

    @Override
    @RuleAction(label = "bypassInteriors", description = "Bypass Interiors")
    public void bypassInteriors() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.bypassInteriors();
    }

    @RuleAction(label = "bypassInteriors", description = "Bypass Interiors")
    public static void bypassInteriors(@Nullable ThingActions actions) {
        invokeMethodOf(actions).bypassInteriors();
    }

    @Override
    @RuleAction(label = "firePanic", description = "Fire Panic")
    public void firePanic() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.firePanic();
    }

    @RuleAction(label = "firePanic", description = "Fire Panic")
    public static void firePanic(@Nullable ThingActions actions) {
        invokeMethodOf(actions).firePanic();
    }

    @Override
    @RuleAction(label = "medicalPanic", description = "Medical Panic")
    public void medicalPanic() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.medicalPanic();
    }

    @RuleAction(label = "medicalPanic", description = "Medical Panic")
    public static void medicalPanic(@Nullable ThingActions actions) {
        invokeMethodOf(actions).medicalPanic();
    }

    @Override
    @RuleAction(label = "policePanic", description = "Police Panic")
    public void policePanic() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.policePanic();
    }

    @RuleAction(label = "policePanic", description = "Police Panic")
    public static void policePanic(@Nullable ThingActions actions) {
        invokeMethodOf(actions).policePanic();
    }

    @Override
    @RuleAction(label = "smokeDetectorReset", description = "Smoke detector reset")
    public void smokeDetectorReset() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.smokeDetectorReset();
    }

    @RuleAction(label = "smokeDetectorReset", description = "Smoke detector reset")
    public static void smokeDetectorReset(@Nullable ThingActions actions) {
        invokeMethodOf(actions).smokeDetectorReset();
    }

    @Override
    @RuleAction(label = "autoCallbackDownload", description = "Auto callback download")
    public void autoCallbackDownload() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.autoCallbackDownload();
    }

    @RuleAction(label = "autoCallbackDownload", description = "Auto callback download")
    public static void autoCallbackDownload(@Nullable ThingActions actions) {
        invokeMethodOf(actions).autoCallbackDownload();
    }

    @Override
    @RuleAction(label = "manualPickupDownload", description = "Manual pickup download")
    public void manualPickupDownload() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.manualPickupDownload();
    }

    @RuleAction(label = "manualPickupDownload", description = "Manual pickup download")
    public static void manualPickupDownload(@Nullable ThingActions actions) {
        invokeMethodOf(actions).manualPickupDownload();
    }

    @Override
    @RuleAction(label = "enableSilentExit", description = "Enable silent exit")
    public void enableSilentExit() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.enableSilentExit();
    }

    @RuleAction(label = "enableSilentExit", description = "Enable silent exit")
    public static void enableSilentExit(@Nullable ThingActions actions) {
        invokeMethodOf(actions).enableSilentExit();
    }

    @Override
    @RuleAction(label = "performTest", description = "Perform test")
    public void performTest() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.performTest();
    }

    @RuleAction(label = "performTest", description = "Perform test")
    public static void performTest(@Nullable ThingActions actions) {
        invokeMethodOf(actions).performTest();
    }

    @Override
    @RuleAction(label = "groupBypass", description = "Group Bypass")
    public void groupBypass() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.groupBypass();
    }

    @RuleAction(label = "groupBypass", description = "Group bypass")
    public static void groupBypass(@Nullable ThingActions actions) {
        invokeMethodOf(actions).groupBypass();
    }

    @Override
    @RuleAction(label = "auxiliaryFunction1", description = "Auxiliary Function 1")
    public void auxiliaryFunction1() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction1();
    }

    @RuleAction(label = "auxiliaryFunction1", description = "Auxiliary Function 1")
    public static void auxiliaryFunction1(@Nullable ThingActions actions) {
        invokeMethodOf(actions).auxiliaryFunction1();
    }

    @Override
    @RuleAction(label = "auxiliaryFunction2", description = "Auxiliary Function 2")
    public void auxiliaryFunction2() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction2();
    }

    @RuleAction(label = "auxiliaryFunction2", description = "Auxiliary Function 2")
    public static void auxiliaryFunction2(@Nullable ThingActions actions) {
        invokeMethodOf(actions).auxiliaryFunction2();
    }

    @Override
    @RuleAction(label = "startKeypadSounder", description = "Start keypad sounder")
    public void startKeypadSounder() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.startKeypadSounder();
    }

    @RuleAction(label = "startKeypadSounder", description = "Start keypad sounder")
    public static void startKeypadSounder(@Nullable ThingActions actions) {
        invokeMethodOf(actions).startKeypadSounder();
    }
}
