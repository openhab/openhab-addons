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
package org.openhab.automation.optimalwindow.internal.handler;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.automation.optimalwindow.internal.calc.WindowCalculator;
import org.openhab.automation.optimalwindow.internal.calc.WindowConfiguration;
import org.openhab.automation.optimalwindow.internal.calc.WindowResult;
import org.openhab.core.automation.Condition;
import org.openhab.core.automation.handler.BaseConditionModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Condition that is satisfied while the current time is within the optimal window.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class InWindowConditionHandler extends BaseConditionModuleHandler {
    private final Logger logger = LoggerFactory.getLogger(InWindowConditionHandler.class);
    private final WindowCalculator calculator;
    private final Supplier<ZoneId> zoneSupplier;
    private final Clock clock;
    private final WindowConfiguration config;

    public InWindowConditionHandler(Condition module, WindowCalculator calculator, Supplier<ZoneId> zoneSupplier,
            Clock clock) {
        super(module);
        this.calculator = calculator;
        this.zoneSupplier = zoneSupplier;
        this.clock = clock;
        config = WindowConfiguration.from(module.getConfiguration());
    }

    @Override
    public boolean isSatisfied(Map<String, Object> context) {
        ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zoneSupplier.get());
        try {
            WindowResult result = calculator.calculate(config, now);
            return result != null && result.isActive(now.toInstant());
        } catch (IllegalStateException e) {
            logger.warn("Cannot calculate optimal window for '{}': {}", config.forecastItem, e.getMessage());
            return false;
        }
    }
}
