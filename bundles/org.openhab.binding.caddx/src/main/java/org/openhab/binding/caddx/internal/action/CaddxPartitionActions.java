/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
public class CaddxPartitionActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxPartitionActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerPartition is null!";

    private @Nullable ThingHandlerPartition handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ThingHandlerPartition partitionHandler) {
            this.handler = partitionHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
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

    public static void turnOffAnySounderOrAlarm(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).turnOffAnySounderOrAlarm(pin);
    }

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

    public static void disarm(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).disarm(pin);
    }

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

    public static void armInAwayMode(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).armInAwayMode(pin);
    }

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

    public static void armInStayMode(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).armInStayMode(pin);
    }

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

    public static void cancel(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).cancel(pin);
    }

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

    public static void initiateAutoArm(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).initiateAutoArm(pin);
    }

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

    public static void startWalkTestMode(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).startWalkTestMode(pin);
    }

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

    public static void stopWalkTestMode(ThingActions actions, @Nullable String pin) {
        ((CaddxPartitionActions) actions).stopWalkTestMode(pin);
    }

    @RuleAction(label = "stay", description = "Stay (1 button arm / toggle interiors)")
    public void stay() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.stay();
    }

    public static void stay(ThingActions actions) {
        ((CaddxPartitionActions) actions).stay();
    }

    @RuleAction(label = "chime", description = "Chime (toggle chime mode)")
    public void chime() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.chime();
    }

    public static void chime(ThingActions actions) {
        ((CaddxPartitionActions) actions).chime();
    }

    @RuleAction(label = "exit", description = "Exit (1 button arm / toggle instant)")
    public void exit() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.exit();
    }

    public static void exit(ThingActions actions) {
        ((CaddxPartitionActions) actions).exit();
    }

    @RuleAction(label = "bypassInteriors", description = "Bypass Interiors")
    public void bypassInteriors() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.bypassInteriors();
    }

    public static void bypassInteriors(ThingActions actions) {
        ((CaddxPartitionActions) actions).bypassInteriors();
    }

    @RuleAction(label = "firePanic", description = "Fire Panic")
    public void firePanic() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.firePanic();
    }

    public static void firePanic(ThingActions actions) {
        ((CaddxPartitionActions) actions).firePanic();
    }

    @RuleAction(label = "medicalPanic", description = "Medical Panic")
    public void medicalPanic() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.medicalPanic();
    }

    public static void medicalPanic(ThingActions actions) {
        ((CaddxPartitionActions) actions).medicalPanic();
    }

    @RuleAction(label = "policePanic", description = "Police Panic")
    public void policePanic() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.policePanic();
    }

    public static void policePanic(ThingActions actions) {
        ((CaddxPartitionActions) actions).policePanic();
    }

    @RuleAction(label = "smokeDetectorReset", description = "Smoke detector reset")
    public void smokeDetectorReset() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.smokeDetectorReset();
    }

    public static void smokeDetectorReset(ThingActions actions) {
        ((CaddxPartitionActions) actions).smokeDetectorReset();
    }

    @RuleAction(label = "autoCallbackDownload", description = "Auto callback download")
    public void autoCallbackDownload() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.autoCallbackDownload();
    }

    public static void autoCallbackDownload(ThingActions actions) {
        ((CaddxPartitionActions) actions).autoCallbackDownload();
    }

    @RuleAction(label = "manualPickupDownload", description = "Manual pickup download")
    public void manualPickupDownload() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.manualPickupDownload();
    }

    public static void manualPickupDownload(ThingActions actions) {
        ((CaddxPartitionActions) actions).manualPickupDownload();
    }

    @RuleAction(label = "enableSilentExit", description = "Enable silent exit")
    public void enableSilentExit() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.enableSilentExit();
    }

    public static void enableSilentExit(ThingActions actions) {
        ((CaddxPartitionActions) actions).enableSilentExit();
    }

    @RuleAction(label = "performTest", description = "Perform test")
    public void performTest() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.performTest();
    }

    public static void performTest(ThingActions actions) {
        ((CaddxPartitionActions) actions).performTest();
    }

    @RuleAction(label = "groupBypass", description = "Group Bypass")
    public void groupBypass() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.groupBypass();
    }

    public static void groupBypass(ThingActions actions) {
        ((CaddxPartitionActions) actions).groupBypass();
    }

    @RuleAction(label = "auxiliaryFunction1", description = "Auxiliary Function 1")
    public void auxiliaryFunction1() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction1();
    }

    public static void auxiliaryFunction1(ThingActions actions) {
        ((CaddxPartitionActions) actions).auxiliaryFunction1();
    }

    @RuleAction(label = "auxiliaryFunction2", description = "Auxiliary Function 2")
    public void auxiliaryFunction2() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction2();
    }

    public static void auxiliaryFunction2(ThingActions actions) {
        ((CaddxPartitionActions) actions).auxiliaryFunction2();
    }

    @RuleAction(label = "startKeypadSounder", description = "Start keypad sounder")
    public void startKeypadSounder() {
        ThingHandlerPartition handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.startKeypadSounder();
    }

    public static void startKeypadSounder(ThingActions actions) {
        ((CaddxPartitionActions) actions).startKeypadSounder();
    }
}
