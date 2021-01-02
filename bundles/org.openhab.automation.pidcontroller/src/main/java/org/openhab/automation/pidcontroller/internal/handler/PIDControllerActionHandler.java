/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.ActionHandler;
import org.openhab.core.automation.handler.BaseModuleHandler;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.DecimalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hilbrand Bouwkamp - Initial Contribution
 * @author Fabian Wolter - Add PID debugging items
 */
@NonNullByDefault
public class PIDControllerActionHandler extends BaseModuleHandler<Action> implements ActionHandler {
    public static final String MODULE_TYPE_ID = AUTOMATION_NAME + ".action";

    private final Logger logger = LoggerFactory.getLogger(PIDControllerActionHandler.class);

    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;

    public PIDControllerActionHandler(Action module, ItemRegistry itemRegistry, EventPublisher eventPublisher) {
        super(module);
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        Stream.of(OUTPUT, P_INSPECTOR, I_INSPECTOR, D_INSPECTOR, E_INSPECTOR).forEach(arg -> {
            final String itemName = (String) module.getConfiguration().get(arg);

            if (itemName == null || itemName.isBlank()) {
                return;
            }

            final BigDecimal command = (BigDecimal) context.get("1." + arg);

            if (command != null) {
                final DecimalType outputValue = new DecimalType(command);
                final ItemCommandEvent itemCommandEvent = ItemEventFactory.createCommandEvent(itemName, outputValue);

                eventPublisher.post(itemCommandEvent);
            } else {
                logger.warn(
                        "Command was not posted because either the configuration was not correct or a service was missing: ItemName: {}, Command: {}, eventPublisher: {}, ItemRegistry: {}",
                        itemName, command, eventPublisher, itemRegistry);
            }
        });
        return null;
    }
}
