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
package org.openhab.binding.caddx.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caddx.internal.handler.ThingHandlerPanel;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the automation engine action handler service for the
 * caddx bridge actions.
 *
 * @author Georgios Moutsos - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = CaddxPanelActions.class)
@ThingActionsScope(name = "caddx")
@NonNullByDefault
public class CaddxPanelActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(CaddxPanelActions.class);

    private static final String HANDLER_IS_NULL = "ThingHandlerPanel is null!";
    private static final String PIN_IS_NULL = "The value for the pin is null. Action not executed.";
    private static final String PIN_IS_INVALID = "The value for the pin [{}] is invalid. Action not executed.";

    private @Nullable ThingHandlerPanel handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof ThingHandlerPanel panelHandler) {
            this.handler = panelHandler;
        }
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

    public static void turnOffAnySounderOrAlarmOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).turnOffAnySounderOrAlarmOnPanel(pin);
    }

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

    public static void disarmOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).disarmOnPanel(pin);
    }

    @RuleAction(label = "armInAwayModeOnPanel", description = "Arm in away mode on all partitions")
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

    public static void armInAwayModeOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).armInAwayModeOnPanel(pin);
    }

    @RuleAction(label = "armInStayModeOnPanel", description = "Arm in stay mode on all partitions")
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

    public static void armInStayModeOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).armInStayModeOnPanel(pin);
    }

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

    public static void cancelOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).cancelOnPanel(pin);
    }

    @RuleAction(label = "initiateAutoArmOnPanel", description = "Initiate auto arm on all partitions")
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

    public static void initiateAutoArmOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).initiateAutoArmOnPanel(pin);
    }

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

    public static void startWalkTestModeOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).startWalkTestModeOnPanel(pin);
    }

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

    public static void stopWalkTestModeOnPanel(ThingActions actions, @Nullable String pin) {
        ((CaddxPanelActions) actions).stopWalkTestModeOnPanel(pin);
    }

    @RuleAction(label = "stayOnPanel", description = "Stay (1 button arm / toggle interiors) on all partitions")
    public void stayOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.stay();
    }

    public static void stayOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).stayOnPanel();
    }

    @RuleAction(label = "chimeOnPanel", description = "Chime (toggle chime mode) on all partitions")
    public void chimeOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.chime();
    }

    public static void chimeOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).chimeOnPanel();
    }

    @RuleAction(label = "exitOnPanel", description = "Exit (1 button arm / toggle instant) on all partitions")
    public void exitOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.exit();
    }

    public static void exitOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).exitOnPanel();
    }

    @RuleAction(label = "bypassInteriorsOnPanel", description = "Bypass Interiors on all partitions")
    public void bypassInteriorsOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.bypassInteriors();
    }

    public static void bypassInteriorsOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).bypassInteriorsOnPanel();
    }

    @RuleAction(label = "firePanicOnPanel", description = "Fire Panic on all partitions")
    public void firePanicOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.firePanic();
    }

    public static void firePanicOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).firePanicOnPanel();
    }

    @RuleAction(label = "medicalPanicOnPanel", description = "Medical Panic on all partitions")
    public void medicalPanicOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.medicalPanic();
    }

    public static void medicalPanicOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).medicalPanicOnPanel();
    }

    @RuleAction(label = "policePanicOnPanel", description = "Police Panic on all partitions")
    public void policePanicOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.policePanic();
    }

    public static void policePanicOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).policePanicOnPanel();
    }

    @RuleAction(label = "smokeDetectorResetOnPanel", description = "Smoke detector reset on all partitions")
    public void smokeDetectorResetOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.smokeDetectorReset();
    }

    public static void smokeDetectorResetOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).smokeDetectorResetOnPanel();
    }

    @RuleAction(label = "autoCallbackDownloadOnPanel", description = "Auto callback download on all partitions")
    public void autoCallbackDownloadOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.autoCallbackDownload();
    }

    public static void autoCallbackDownloadOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).autoCallbackDownloadOnPanel();
    }

    @RuleAction(label = "manualPickupDownloadOnPanel", description = "Manual pickup download on all partitions")
    public void manualPickupDownloadOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.manualPickupDownload();
    }

    public static void manualPickupDownloadOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).manualPickupDownloadOnPanel();
    }

    @RuleAction(label = "enableSilentExitOnPanel", description = "Enable silent exit on all partitions")
    public void enableSilentExitOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.enableSilentExit();
    }

    public static void enableSilentExitOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).enableSilentExitOnPanel();
    }

    @RuleAction(label = "performTestOnPanel", description = "Perform test on all partitions")
    public void performTestOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.performTest();
    }

    public static void performTestOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).performTestOnPanel();
    }

    @RuleAction(label = "groupBypassOnPanel", description = "Group Bypass on all partitions")
    public void groupBypassOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.groupBypass();
    }

    public static void groupBypassOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).groupBypassOnPanel();
    }

    @RuleAction(label = "auxiliaryFunction1OnPanel", description = "Auxiliary Function 1 on all partitions")
    public void auxiliaryFunction1OnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction1();
    }

    public static void auxiliaryFunction1OnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).auxiliaryFunction1OnPanel();
    }

    @RuleAction(label = "auxiliaryFunction2OnPanel", description = "Auxiliary Function 2 on all partitions")
    public void auxiliaryFunction2OnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.auxiliaryFunction2();
    }

    public static void auxiliaryFunction2OnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).auxiliaryFunction2OnPanel();
    }

    @RuleAction(label = "startKeypadSounderOnPanel", description = "Start keypad sounder on all partitions")
    public void startKeypadSounderOnPanel() {
        ThingHandlerPanel handler = this.handler;
        if (handler == null) {
            logger.debug(HANDLER_IS_NULL);
            return;
        }

        handler.startKeypadSounder();
    }

    public static void startKeypadSounderOnPanel(ThingActions actions) {
        ((CaddxPanelActions) actions).startKeypadSounderOnPanel();
    }
}
