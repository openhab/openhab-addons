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
package org.openhab.automation.pwm.internal.handler;

import static org.openhab.automation.pwm.internal.PWMConstants.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pwm.internal.PWMException;
import org.openhab.automation.pwm.internal.handler.state.StateMachine;
import org.openhab.core.automation.ModuleHandlerCallback;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseTriggerModuleHandler;
import org.openhab.core.automation.handler.TriggerHandlerCallback;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemStateEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Trigger module in the rules engine.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PWMTriggerHandler extends BaseTriggerModuleHandler implements EventSubscriber {
    public static final String MODULE_TYPE_ID = AUTOMATION_NAME + ".trigger";
    private static final Set<String> SUBSCRIBED_EVENT_TYPES = Set.of(ItemStateEvent.TYPE);
    private final Logger logger = LoggerFactory.getLogger(PWMTriggerHandler.class);
    private final BundleContext bundleContext;
    private final EventFilter eventFilter;
    private final Optional<Double> minDutyCycle;
    private final Optional<Double> maxDutyCycle;
    private final boolean isEquateMinToZero;
    private final boolean isEquateMaxToHundred;
    private final Optional<Double> deadManSwitchTimeoutMs;
    private final Item dutyCycleItem;
    private @Nullable ServiceRegistration<?> eventSubscriberRegistration;
    private @Nullable ScheduledFuture<?> deadMeanSwitchTimer;
    private @Nullable StateMachine stateMachine;
    private String ruleUID;

    public PWMTriggerHandler(Trigger module, ItemRegistry itemRegistry, BundleContext bundleContext, String ruleUID) {
        super(module);
        this.bundleContext = bundleContext;
        this.ruleUID = ruleUID;

        Configuration config = module.getConfiguration();

        String dutycycleItemName = (String) Objects.requireNonNull(config.get(CONFIG_DUTY_CYCLE_ITEM),
                "DutyCycle item is not set");

        minDutyCycle = getOptionalDoubleFromConfig(config, CONFIG_MIN_DUTYCYCLE);
        isEquateMinToZero = getBooleanFromConfig(config, CONFIG_EQUATE_MIN_TO_ZERO);
        maxDutyCycle = getOptionalDoubleFromConfig(config, CONFIG_MAX_DUTYCYCLE);
        isEquateMaxToHundred = getBooleanFromConfig(config, CONFIG_EQUATE_MAX_TO_HUNDRED);
        deadManSwitchTimeoutMs = getOptionalDoubleFromConfig(config, CONFIG_DEAD_MAN_SWITCH);

        try {
            dutyCycleItem = itemRegistry.getItem(dutycycleItemName);
        } catch (ItemNotFoundException e) {
            throw new IllegalArgumentException("Dutycycle item not found: " + dutycycleItemName, e);
        }

        eventFilter = event -> event.getTopic().equals("openhab/items/" + dutycycleItemName + "/state");
    }

    @Override
    public void setCallback(ModuleHandlerCallback callback) {
        super.setCallback(callback);

        double periodSec = getDoubleFromConfig(module.getConfiguration(), CONFIG_PERIOD);
        stateMachine = new StateMachine(getCallback().getScheduler(), this::setOutput, (long) (periodSec * 1000),
                ruleUID);

        eventSubscriberRegistration = bundleContext.registerService(EventSubscriber.class.getName(), this, null);
    }

    private double getDoubleFromConfig(Configuration config, String key) {
        return ((BigDecimal) Objects.requireNonNull(config.get(key), ruleUID + ": " + key + " is not set"))
                .doubleValue();
    }

    private Optional<Double> getOptionalDoubleFromConfig(Configuration config, String key) {
        Object o = config.get(key);

        if (o instanceof BigDecimal decimal) {
            return Optional.of(decimal.doubleValue());
        }

        return Optional.empty();
    }

    private boolean getBooleanFromConfig(Configuration config, String key) {
        return ((Boolean) config.get(key)).booleanValue();
    }

    @Override
    public void receive(Event event) {
        if (!(event instanceof ItemStateEvent)) {
            return;
        }

        ItemStateEvent changedEvent = (ItemStateEvent) event;
        synchronized (this) {
            try {
                double newDutycycle = getDutyCycleValueInPercent(changedEvent.getItemState());
                double newDutycycleBeforeLimit = newDutycycle;

                restartDeadManSwitchTimer();

                // set duty cycle to 0% if it is 0% or it is smaller than min duty cycle and equateMinToZero is true
                // set duty cycle to min duty cycle if it is smaller than min duty cycle
                final double newDutyCycleFinal1 = newDutycycle;
                newDutycycle = minDutyCycle.map(minDutycycle -> {
                    long dutycycleRounded1 = Math.round(newDutyCycleFinal1);
                    if (dutycycleRounded1 <= 0 || (dutycycleRounded1 <= minDutycycle && isEquateMinToZero)) {
                        return 0d;
                    } else {
                        return Math.max(minDutycycle, newDutyCycleFinal1);
                    }
                }).orElse(newDutycycle);

                // set duty cycle to 100% if it is 100% or it is larger then max duty cycle and equateMaxToHundred is
                // true
                // set duty cycle to max duty cycle if it is larger then max duty cycle
                final double newDutyCycleFinal2 = newDutycycle;
                newDutycycle = maxDutyCycle.map(maxDutycycle -> {
                    long dutycycleRounded2 = Math.round(newDutyCycleFinal2);
                    if (dutycycleRounded2 >= 100 || (dutycycleRounded2 >= maxDutycycle && isEquateMaxToHundred)) {
                        return 100d;
                    } else {
                        return Math.min(maxDutycycle, newDutyCycleFinal2);
                    }
                }).orElse(newDutycycle);

                logger.debug("{}: Received new duty cycle: {} {}", ruleUID, newDutycycleBeforeLimit,
                        newDutycycle != newDutycycleBeforeLimit ? "Limited to: " + newDutycycle : "");

                StateMachine localStateMachine = stateMachine;
                if (localStateMachine != null) {
                    localStateMachine.setDutycycle(newDutycycle);
                } else {
                    logger.debug("{}: Initialization not finished", ruleUID);
                }
            } catch (PWMException e) {
                logger.warn("{}: {}", ruleUID, e.getMessage());
            }
        }
    }

    private void restartDeadManSwitchTimer() {
        ScheduledFuture<?> timer = deadMeanSwitchTimer;
        if (timer != null) {
            timer.cancel(true);
        }

        deadManSwitchTimeoutMs.ifPresent(timeout -> {
            deadMeanSwitchTimer = getCallback().getScheduler().schedule(this::activateDeadManSwitch,
                    timeout.longValue(), TimeUnit.MILLISECONDS);
        });
    }

    private void activateDeadManSwitch() {
        logger.warn("{}: Dead-man switch activated. Disabling output", ruleUID);

        StateMachine localStateMachine = stateMachine;
        if (localStateMachine != null) {
            localStateMachine.stop();
        }
    }

    private void setOutput(boolean enable) {
        getCallback().triggered(module, Map.of(OUTPUT, OnOffType.from(enable)));
    }

    private TriggerHandlerCallback getCallback() {
        ModuleHandlerCallback localCallback = callback;
        if (localCallback != null && localCallback instanceof TriggerHandlerCallback handlerCallback) {
            return handlerCallback;
        }

        throw new IllegalStateException();
    }

    private double getDutyCycleValueInPercent(State state) throws PWMException {
        if (state instanceof DecimalType decimal) {
            return decimal.doubleValue();
        } else if (state instanceof StringType) {
            try {
                return Integer.parseInt(state.toString());
            } catch (NumberFormatException e) {
                // nothing
            }
        } else if (state instanceof UnDefType) {
            throw new PWMException(ruleUID + ": Duty cycle item '" + dutyCycleItem.getName() + "' has no valid value");
        }
        throw new PWMException(
                ruleUID + ": Duty cycle item not of type DecimalType: " + state.getClass().getSimpleName());
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
        ServiceRegistration<?> localEventSubscriberRegistration = eventSubscriberRegistration;
        if (localEventSubscriberRegistration != null) {
            localEventSubscriberRegistration.unregister();
        }

        super.dispose();
    }
}
