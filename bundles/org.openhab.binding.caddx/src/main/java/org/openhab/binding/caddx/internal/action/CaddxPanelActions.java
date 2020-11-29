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

import java.io.IOException;

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
public class CaddxPanelActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxPanelActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerPanel is null!";
    private static final String PIN_IS_NULL = "The value for the pin is null. Action not executed.";
    private static final String PIN_IS_INVALID = "The value for the pin [{}] is invalid. Action not executed.";
    private static final String ACTION_CLASS_IS_WRONG = "Instance is not a CaddxPanelActions class.";

    private @Nullable ThingHandlerPanel handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (ThingHandlerPanel) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
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

    @RuleAction(label = "turnOffAnySounderOrAlarm", description = "Turn off any sounder or alarm")
    public void turnOffAnySounderOrAlarm(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "turnOffAnySounderOrAlarm", description = "Turn off any sounder or alarm")
    public static void turnOffAnySounderOrAlarm(@Nullable ThingActions actions, @Nullable String pin)
            throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).turnOffAnySounderOrAlarm(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "disarm", description = "Dis-arm")
    public void disarm(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "disarm", description = "Dis-arm")
    public static void disarm(@Nullable ThingActions actions, @Nullable String pin) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).disarm(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "armInAwayMode", description = "Arm in away mode")
    public void armInAwayMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "armInAwayMode", description = "Arm in away mode")
    public static void armInAwayMode(@Nullable ThingActions actions, @Nullable String pin) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).armInAwayMode(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "armInStayMode", description = "Arm in stay mode")
    public void armInStayMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "armInStayMode", description = "Arm in stay mode")
    public static void armInStayMode(@Nullable ThingActions actions, @Nullable String pin) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).armInStayMode(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "cancel", description = "Cancel")
    public void cancel(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "cancel", description = "Cancel")
    public static void cancel(@Nullable ThingActions actions, @Nullable String pin) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).cancel(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "initiateAutoArm", description = "Initiate auto arm")
    public void initiateAutoArm(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "initiateAutoArm", description = "Initiate auto arm")
    public static void initiateAutoArm(@Nullable ThingActions actions, @Nullable String pin) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).initiateAutoArm(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "startWalkTestMode", description = "Start walk-test mode")
    public void startWalkTestMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "startWalkTestMode", description = "Start walk-test mode")
    public static void startWalkTestMode(@Nullable ThingActions actions, @Nullable String pin) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).startWalkTestMode(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "stopWalkTestMode", description = "Stop walk-test mode")
    public void stopWalkTestMode(
            @ActionInput(name = "pin", label = "pin", description = "The pin 4 or 6 digit pin") @Nullable String pin)
            throws IOException {
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

    @RuleAction(label = "stopWalkTestMode", description = "Stop walk-test mode")
    public static void stopWalkTestMode(@Nullable ThingActions actions, @Nullable String pin) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).stopWalkTestMode(pin);
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "stay", description = "Stay (1 button arm / toggle interiors)")
    public void stay() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.stay();
    }

    @RuleAction(label = "stay", description = "Stay (1 button arm / toggle interiors)")
    public static void stay(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).stay();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "chime", description = "Chime (toggle chime mode)")
    public void chime() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.chime();
    }

    @RuleAction(label = "chime", description = "Chime (toggle chime mode)")
    public static void chime(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).chime();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "exit", description = "Exit (1 button arm / toggle instant)")
    public void exit() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.exit();
    }

    @RuleAction(label = "exit", description = "Exit (1 button arm / toggle instant)")
    public static void exit(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).exit();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "bypassInteriors", description = "Bypass Interiors")
    public void bypassInteriors() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.bypassInteriors();
    }

    @RuleAction(label = "bypassInteriors", description = "Bypass Interiors")
    public static void bypassInteriors(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).bypassInteriors();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "firePanic", description = "Fire Panic")
    public void firePanic() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.firePanic();
    }

    @RuleAction(label = "firePanic", description = "Fire Panic")
    public static void firePanic(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).firePanic();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "medicalPanic", description = "Medical Panic")
    public void medicalPanic() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.medicalPanic();
    }

    @RuleAction(label = "medicalPanic", description = "Medical Panic")
    public static void medicalPanic(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).medicalPanic();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "policePanic", description = "Police Panic")
    public void policePanic() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.policePanic();
    }

    @RuleAction(label = "policePanic", description = "Police Panic")
    public static void policePanic(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).policePanic();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "smokeDetectorReset", description = "Smoke detector reset")
    public void smokeDetectorReset() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.smokeDetectorReset();
    }

    @RuleAction(label = "smokeDetectorReset", description = "Smoke detector reset")
    public static void smokeDetectorReset(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).smokeDetectorReset();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "autoCallbackDownload", description = "Auto callback download")
    public void autoCallbackDownload() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.autoCallbackDownload();
    }

    @RuleAction(label = "autoCallbackDownload", description = "Auto callback download")
    public static void autoCallbackDownload(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).autoCallbackDownload();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "manualPickupDownload", description = "Manual pickup download")
    public void manualPickupDownload() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.manualPickupDownload();
    }

    @RuleAction(label = "manualPickupDownload", description = "Manual pickup download")
    public static void manualPickupDownload(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).manualPickupDownload();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "enableSilentExit", description = "Enable silent exit")
    public void enableSilentExit() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.enableSilentExit();
    }

    @RuleAction(label = "enableSilentExit", description = "Enable silent exit")
    public static void enableSilentExit(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).enableSilentExit();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "performTest", description = "Perform test")
    public void performTest() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.performTest();
    }

    @RuleAction(label = "performTest", description = "Perform test")
    public static void performTest(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).performTest();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "groupBypass", description = "Group Bypass")
    public void groupBypass() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.groupBypass();
    }

    @RuleAction(label = "groupBypass", description = "Group bypass")
    public static void groupBypass(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).groupBypass();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "auxiliaryFunction1", description = "Auxiliary Function 1")
    public void auxiliaryFunction1() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction1();
    }

    @RuleAction(label = "auxiliaryFunction1", description = "Auxiliary Function 1")
    public static void auxiliaryFunction1(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).auxiliaryFunction1();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "auxiliaryFunction2", description = "Auxiliary Function 2")
    public void auxiliaryFunction2() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction2();
    }

    @RuleAction(label = "auxiliaryFunction2", description = "Auxiliary Function 2")
    public static void auxiliaryFunction2(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).auxiliaryFunction2();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }

    @RuleAction(label = "startKeypadSounder", description = "Start keypad sounder")
    public void startKeypadSounder() throws IOException {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.startKeypadSounder();
    }

    @RuleAction(label = "startKeypadSounder", description = "Start keypad sounder")
    public static void startKeypadSounder(@Nullable ThingActions actions) throws IOException {
        if (actions instanceof CaddxPanelActions) {
            ((CaddxPanelActions) actions).startKeypadSounder();
        } else {
            throw new IllegalArgumentException(ACTION_CLASS_IS_WRONG);
        }
    }
}
