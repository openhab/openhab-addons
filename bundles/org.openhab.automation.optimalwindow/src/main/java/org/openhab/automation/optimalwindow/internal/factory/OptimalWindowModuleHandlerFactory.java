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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.optimalwindow.internal.calc.WindowCalculator;
import org.openhab.automation.optimalwindow.internal.calc.WindowConfiguration;
import org.openhab.automation.optimalwindow.internal.handler.InWindowConditionHandler;
import org.openhab.automation.optimalwindow.internal.handler.PersistenceForecastSource;
import org.openhab.automation.optimalwindow.internal.handler.WindowTracker;
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
 * Creates the handlers of the Optimal Window automation, and tells their {@link WindowTracker} when the forecast item
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
    private final Map<ModuleHandler, WindowTracker> trackers = new ConcurrentHashMap<>();

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
            trackers.values().stream().filter(tracker -> tracker.getForecastItem().equals(itemName))
                    .forEach(WindowTracker::forecastUpdated);
        }
    }

    @Override
    public Collection<String> getTypes() {
        return TYPES;
    }

    @Override
    protected @Nullable ModuleHandler internalCreate(Module module, String ruleUID) {
        String type = module.getTypeUID();
        if (!TYPES.contains(type)) {
            return null;
        }
        WindowTracker tracker = new WindowTracker(calculator, WindowConfiguration.from(module.getConfiguration()));
        ModuleHandler handler = TRIGGER_TYPE_ID.equals(type)
                ? new WindowTriggerHandler((Trigger) module, tracker, eventPublisher, timeZoneProvider::getTimeZone,
                        Clock.systemUTC())
                : new InWindowConditionHandler((Condition) module, tracker, timeZoneProvider::getTimeZone,
                        Clock.systemUTC());
        trackers.put(handler, tracker);
        return handler;
    }

    @Override
    public void ungetHandler(Module module, String ruleUID, ModuleHandler handler) {
        trackers.remove(handler);
        super.ungetHandler(module, ruleUID, handler);
    }
}
