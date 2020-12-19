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
package org.openhab.automation.pwm.internal.handler;

import static org.openhab.automation.pwm.internal.PWMConstants.*;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.Action;
import org.openhab.core.automation.handler.ActionHandler;
import org.openhab.core.automation.handler.BaseModuleHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class PWMActionHandler extends BaseModuleHandler<Action> implements ActionHandler {
    public static final String MODULE_TYPE_ID = AUTOMATION_NAME + ".action";
    private final Logger logger = LoggerFactory.getLogger(PWMActionHandler.class);

    private ItemRegistry itemRegistry;
    private EventPublisher eventPublisher;

    public PWMActionHandler(Action module, ItemRegistry itemRegistry, EventPublisher eventPublisher) {
        super(module);
        this.itemRegistry = itemRegistry;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public @Nullable Map<String, Object> execute(Map<String, Object> context) {
        final Configuration configuration = module.getConfiguration();

        context.forEach((k, v) -> {
            // Remove triggername from key to get raw trigger param
            String itemKey = k.substring(k.lastIndexOf('.') + 1);
            String itemName = (String) configuration.get(itemKey);

            if (itemName == null || itemName.isBlank()) {
                // try original key name (<triggername>.<trigger_param>)
                itemName = (String) configuration.get(k);
                if (itemName == null || itemName.isBlank()) {
                    return;
                }
            }
            if (v instanceof BigDecimal) {
                final BigDecimal command = (BigDecimal) v;
                final OnOffType outputValue = OnOffType.from(command.intValue() == 1);
                final ItemCommandEvent itemCommandEvent = ItemEventFactory.createCommandEvent(itemName, outputValue);

                eventPublisher.post(itemCommandEvent);
            } else {
                logger.warn(
                        "Command was not posted because either the configuration was not correct or a service was missing: ItemName: {}, Command: {}, eventPublisher: {}, ItemRegistry: {}",
                        itemName, v, eventPublisher, itemRegistry);
            }
        });
        return null;
    }
}
