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
package org.openhab.automation.pidcontroller.internal.handler;

import static org.openhab.automation.pidcontroller.internal.PIDControllerConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pidcontroller.internal.PIDException;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseTriggerModuleHandler;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hilbrand Bouwkamp - Initial Contribution
 * @author Fabian Wolter - Add PID debug output values
 */
@NonNullByDefault
public class PIDControllerTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber {
    public static final String MODULE_TYPE_ID = AUTOMATION_NAME + ".trigger";
    private static final Set<String> SUBSCRIBED_EVENT_TYPES = Set.of(ItemStateEvent.TYPE, ItemStateChangedEvent.TYPE);
    private final Logger logger = LoggerFactory.getLogger(PIDControllerTriggerHandler.class);
    private final ServiceRegistration<?> eventSubscriberRegistration;
    private final PIDController controller;
    private final int loopTimeMs;
    private long previousTimeMs = System.currentTimeMillis();
    private Item inputItem;
    private Item setpointItem;
    private Optional<String> commandTopic;
    private EventFilter eventFilter;
    private EventPublisher eventPublisher;
    private @Nullable String pInspector;
    private @Nullable String iInspector;
    private @Nullable String dInspector;
    private @Nullable String eInspector;
    private ItemRegistry itemRegistry;
    private @Nullable String integralHoldItemName;
    private final Set<String> warnedItemProblems = new HashSet<>();

    public PIDControllerTriggerHandler(Trigger module, ItemRegistry itemRegistry, EventPublisher eventPublisher,
            BundleContext bundleContext) {
        super(module);
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;

        Configuration config = module.getConfiguration();

        String inputItemName = (String) requireNonNull(config.get(CONFIG_INPUT_ITEM), "Input item is not set");
        String setpointItemName = (String) requireNonNull(config.get(CONFIG_SETPOINT_ITEM), "Setpoint item is not set");

        try {
            inputItem = itemRegistry.getItem(inputItemName);
        } catch (ItemNotFoundException e) {
            throw new IllegalArgumentException("Configured input item not found: " + inputItemName, e);
        }

        try {
            setpointItem = itemRegistry.getItem(setpointItemName);
        } catch (ItemNotFoundException e) {
            throw new IllegalArgumentException("Configured setpoint item not found: " + setpointItemName, e);
        }

        String commandItemName = (String) config.get(CONFIG_COMMAND_ITEM);
        if (commandItemName != null) {
            commandTopic = Optional.of("openhab/items/" + commandItemName + "/statechanged");
        } else {
            commandTopic = Optional.empty();
        }

        double kpAdjuster = getDoubleFromConfig(config, CONFIG_KP_GAIN);
        double kiAdjuster = getDoubleFromConfig(config, CONFIG_KI_GAIN);
        double kdAdjuster = getDoubleFromConfig(config, CONFIG_KD_GAIN);
        double kdTimeConstant = getDoubleFromConfig(config, CONFIG_KD_TIMECONSTANT);
        double iMinValue = getDoubleFromConfig(config, CONFIG_I_MIN);
        double iMaxValue = getDoubleFromConfig(config, CONFIG_I_MAX);
        double integralDecayTime = getDoubleFromConfig(config, CONFIG_I_DECAY_TIME);
        integralHoldItemName = (String) config.get(CONFIG_I_HOLD_ITEM);
        boolean directionalIntegralHold = getBooleanFromConfig(config, CONFIG_I_HOLD_DIRECTIONAL);
        pInspector = (String) config.get(P_INSPECTOR);
        iInspector = (String) config.get(I_INSPECTOR);
        dInspector = (String) config.get(D_INSPECTOR);
        eInspector = (String) config.get(E_INSPECTOR);

        loopTimeMs = ((BigDecimal) requireNonNull(config.get(CONFIG_LOOP_TIME), CONFIG_LOOP_TIME + " is not set"))
                .intValue();

        double previousIntegralPart = getItemNameValueAsNumberOrZero(itemRegistry, iInspector);
        double previousDerivativePart = getItemNameValueAsNumberOrZero(itemRegistry, dInspector);
        double previousError = getItemNameValueAsNumberOrZero(itemRegistry, eInspector);

        controller = new PIDController(kpAdjuster, kiAdjuster, kdAdjuster, kdTimeConstant, iMinValue, iMaxValue,
                integralDecayTime, directionalIntegralHold, previousIntegralPart, previousDerivativePart,
                previousError);

        eventFilter = event -> {
            String topic = event.getTopic();

            return ("openhab/items/" + inputItemName + "/state").equals(topic)
                    || ("openhab/items/" + inputItemName + "/statechanged").equals(topic)
                    || ("openhab/items/" + setpointItemName + "/statechanged").equals(topic)
                    || commandTopic.map(t -> topic.equals(t)).orElse(false);
        };

        eventSubscriberRegistration = bundleContext.registerService(EventSubscriber.class.getName(), this, null);

        eventPublisher.post(ItemEventFactory.createCommandEvent(inputItemName, RefreshType.REFRESH));
    }

    @Override
    public void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);
        getCallback().getScheduler().scheduleWithFixedDelay(this::calculate, 0, loopTimeMs, TimeUnit.MILLISECONDS);
    }

    private <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }

    /**
     * Whether this is the first time the given problem has been seen, and records it.
     *
     * <p>
     * Every one of these checks runs on each calculation, so logging unconditionally turns a
     * single misconfigured Item name into one warning per {@code loopTime}: at the documented
     * 1s loop that is 86400 a day, and {@link #updateItem} multiplies it by the four inspector
     * Items. The key is the failure kind plus the Item name, so each Item reports
     * independently and two different problems with the same Item do not mask one another.
     * Clearing the key on success lets an Item recreated at runtime recover silently while a
     * later failure is still reported.
     *
     * <p>
     * The caller logs its own constant format string rather than passing one in, which
     * Checkstyle requires so that the placeholders can be verified.
     */
    private boolean firstReportOf(String key) {
        return warnedItemProblems.add(key);
    }

    private void clearWarning(String key) {
        warnedItemProblems.remove(key);
    }

    /**
     * Whether the caller is currently reporting that the actuator cannot act on the process, in which case the I-part
     * must not keep accumulating. Anything that is not a definite "on" leaves the controller integrating, so a missing
     * or uninitialised item cannot silently freeze the loop.
     */
    private boolean isIntegralHeld() {
        String itemName = integralHoldItemName;
        if (itemName == null || itemName.isBlank()) {
            return false;
        }
        try {
            State state = itemRegistry.getItem(itemName).getState();
            clearWarning("hold:" + itemName);
            if (state instanceof OnOffType onOff) {
                return onOff == OnOffType.ON;
            }
            if (state instanceof OpenClosedType openClosed) {
                return openClosed == OpenClosedType.CLOSED;
            }
            return false;
        } catch (ItemNotFoundException e) {
            if (firstReportOf("hold:" + itemName)) {
                logger.warn("Integral hold Item '{}' not found, continuing to integrate", itemName);
            }
            return false;
        }
    }

    private double getDoubleFromConfig(Configuration config, String key) {
        Object rawValue = config.get(key);

        if (rawValue == null) {
            return Double.NaN;
        }

        return ((BigDecimal) rawValue).doubleValue();
    }

    private boolean getBooleanFromConfig(Configuration config, String key) {
        Object rawValue = config.get(key);

        if (rawValue instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (rawValue instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }

        return false;
    }

    private void calculate() {
        double input;
        double setpoint;

        try {
            input = getItemValueAsNumber(inputItem);
            clearWarning("value:" + inputItem.getName());
        } catch (PIDException e) {
            if (firstReportOf("value:" + inputItem.getName())) {
                logger.warn("Input item: {}: {}", inputItem.getName(), e.getMessage());
            }
            return;
        }

        try {
            setpoint = getItemValueAsNumber(setpointItem);
            clearWarning("value:" + setpointItem.getName());
        } catch (PIDException e) {
            if (firstReportOf("value:" + setpointItem.getName())) {
                logger.warn("Setpoint item: {}: {}", setpointItem.getName(), e.getMessage());
            }
            return;
        }

        long now = System.currentTimeMillis();

        PIDOutputDTO output = controller.calculate(input, setpoint, now - previousTimeMs, loopTimeMs, isIntegralHeld());
        previousTimeMs = now;

        updateItem(pInspector, output.getProportionalPart());
        updateItem(iInspector, output.getIntegralPart());
        updateItem(dInspector, output.getDerivativePart());
        updateItem(eInspector, output.getError());

        getCallback().triggered(module, Map.of(COMMAND, new DecimalType(output.getOutput())));
    }

    private void updateItem(@Nullable String itemName, double value) {
        if (itemName != null) {
            try {
                itemRegistry.getItem(itemName);
                clearWarning("missing:" + itemName);
                eventPublisher.post(ItemEventFactory.createStateEvent(itemName,
                        Double.isFinite(value) ? new DecimalType(value) : UnDefType.UNDEF));
            } catch (ItemNotFoundException e) {
                if (firstReportOf("missing:" + itemName)) {
                    logger.warn("Item doesn't exist: {}", itemName);
                }
            }
        }
    }

    private TriggerHandlerCallback getCallback() {
        ModuleHandlerCallback localCallback = callback;
        if (localCallback != null && localCallback instanceof TriggerHandlerCallback handlerCallback) {
            return handlerCallback;
        }

        throw new IllegalStateException("The module callback is not set");
    }

    private double getItemNameValueAsNumberOrZero(ItemRegistry itemRegistry, @Nullable String itemName)
            throws IllegalArgumentException {
        double value = 0.0;

        if (itemName == null) {
            return value;
        }

        try {
            value = getItemValueAsNumber(itemRegistry.getItem(itemName));
            logger.debug("Item '{}' value {} recovered by PID controller", itemName, value);
        } catch (ItemNotFoundException e) {
            throw new IllegalArgumentException("Configured item not found: " + itemName, e);
        } catch (PIDException e) {
            logger.warn("Item '{}' value recovery errored: {}", itemName, e.getMessage());
        }

        return value;
    }

    private double getItemValueAsNumber(Item item) throws PIDException {
        State setpointState = item.getState();

        if (setpointState instanceof Number number) {
            double doubleValue = number.doubleValue();

            if (Double.isFinite(doubleValue) && !Double.isNaN(doubleValue)) {
                return doubleValue;
            }
        } else if (setpointState instanceof StringType) {
            try {
                return Double.parseDouble(setpointState.toString());
            } catch (NumberFormatException e) {
                // nothing
            }
        }
        throw new PIDException("Not a number: " + setpointState.getClass().getSimpleName() + ": " + setpointState);
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ItemStateChangedEvent changedEvent) {
            if (commandTopic.isPresent() && event.getTopic().equals(commandTopic.get())) {
                if ("RESET".equals(changedEvent.getItemState().toString())) {
                    controller.setIntegralResult(0);
                    controller.setDerivativeResult(0);
                    eventPublisher.post(ItemEventFactory.createStateEvent(changedEvent.getItemName(), UnDefType.NULL));
                } else if (changedEvent.getItemState() != UnDefType.NULL) {
                    logger.warn("Unknown command: {}", changedEvent.getItemState());
                }
            } else {
                calculate();
            }
        }
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return SUBSCRIBED_EVENT_TYPES;
    }

    @Override
    public @Nullable EventFilter getEventFilter() {
        return eventFilter;
    }

    @Override
    public void dispose() {
        eventSubscriberRegistration.unregister();

        super.dispose();
    }
}
