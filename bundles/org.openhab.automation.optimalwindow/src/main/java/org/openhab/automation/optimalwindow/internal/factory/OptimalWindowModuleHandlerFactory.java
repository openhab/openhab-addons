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
package org.openhab.automation.optimalwindow.internal.factory;

import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.*;

import java.time.Clock;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.optimalwindow.internal.calc.WindowCalculator;
import org.openhab.automation.optimalwindow.internal.handler.InWindowConditionHandler;
import org.openhab.automation.optimalwindow.internal.handler.PersistenceForecastSource;
import org.openhab.automation.optimalwindow.internal.handler.WindowTriggerHandler;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.Module;
import org.openhab.core.automation.Trigger;
import org.openhab.core.automation.handler.BaseModuleHandlerFactory;
import org.openhab.core.automation.handler.ModuleHandler;
import org.openhab.core.automation.handler.ModuleHandlerFactory;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.items.events.ItemTimeSeriesUpdatedEvent;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Creates the handlers of the Optimal Window automation, and tells the trigger handlers when their forecast item
 * received a new time series.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Thomas Leber - Adapted for the Optimal Window automation
 */
@Component(service = { ModuleHandlerFactory.class,
        EventSubscriber.class }, configurationPid = "automation.optimalwindow")
@NonNullByDefault
public class OptimalWindowModuleHandlerFactory extends BaseModuleHandlerFactory implements EventSubscriber {
    private static final Collection<String> TYPES = Set.of(TRIGGER_TYPE_ID, CONDITION_TYPE_ID);
    private static final Set<String> SUBSCRIBED_EVENT_TYPES = Set.of(ItemTimeSeriesUpdatedEvent.TYPE);

    private final EventPublisher eventPublisher;
    private final TimeZoneProvider timeZoneProvider;
    private final WindowCalculator calculator;
    private final Set<WindowTriggerHandler> triggerHandlers = new CopyOnWriteArraySet<>();

    @Activate
    public OptimalWindowModuleHandlerFactory(@Reference EventPublisher eventPublisher,
            @Reference TimeZoneProvider timeZoneProvider,
            @Reference PersistenceServiceRegistry persistenceServiceRegistry) {
        this.eventPublisher = eventPublisher;
        this.timeZoneProvider = timeZoneProvider;
        this.calculator = new WindowCalculator(new PersistenceForecastSource(persistenceServiceRegistry));
    }

    @Override
    public Set<String> getSubscribedEventTypes() {
        return SUBSCRIBED_EVENT_TYPES;
    }

    @Override
    public void receive(Event event) {
        if (event instanceof ItemTimeSeriesUpdatedEvent timeSeriesEvent) {
            String itemName = timeSeriesEvent.getItemName();
            triggerHandlers.stream().filter(h -> h.getForecastItem().equals(itemName))
                    .forEach(WindowTriggerHandler::forecastUpdated);
        }
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        return switch (module.getTypeUID()) {
            case TRIGGER_TYPE_ID -> {
                WindowTriggerHandler handler = new WindowTriggerHandler((Trigger) module, calculator, eventPublisher,
                        timeZoneProvider::getTimeZone, Clock.systemUTC());
                triggerHandlers.add(handler);
                yield handler;
            }
            case CONDITION_TYPE_ID -> new InWindowConditionHandler((Condition) module, calculator,
                    timeZoneProvider::getTimeZone, Clock.systemUTC());
            default -> null;
        };
    }

    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler handler) {
        if (handler instanceof WindowTriggerHandler triggerHandler) {
            triggerHandlers.remove(triggerHandler);
        }
        super.ungetHandler(module, ruleUID, handler);
    }
}
