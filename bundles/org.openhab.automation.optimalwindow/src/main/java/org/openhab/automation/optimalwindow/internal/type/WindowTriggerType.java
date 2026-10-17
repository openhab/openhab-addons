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
package org.openhab.automation.optimalwindow.internal.type;

import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.Output;
import org.openhab.core.automation.type.TriggerType;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.types.Command;

/**
 * Trigger type that fires when the optimal window starts and ends.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class WindowTriggerType extends TriggerType {

    public static WindowTriggerType initialize() {
        List<ConfigDescriptionParameter> parameters = new ArrayList<>(WindowConfigDescriptions.window());
        parameters.addAll(WindowConfigDescriptions.statusItems());

        List<Output> outputs = List.of(
                new Output(OUTPUT_EVENT, String.class.getName(), "Event", "START or END", null, null, null),
                new Output(OUTPUT_COMMAND, Command.class.getName(), "Command",
                        "ON when the window starts, OFF when it ends", Set.of("command"), null, null),
                new Output(OUTPUT_START, ZonedDateTime.class.getName(), "Start", "Start of the window", null, null,
                        null),
                new Output(OUTPUT_END, ZonedDateTime.class.getName(), "End", "End of the window", null, null, null),
                new Output(OUTPUT_AVERAGE, BigDecimal.class.getName(), "Average",
                        "Average forecast value within the window", null, null, null));

        return new WindowTriggerType(parameters, outputs);
    }

    public WindowTriggerType(List<ConfigDescriptionParameter> configDescriptions, List<Output> outputs) {
        super(TRIGGER_TYPE_ID, configDescriptions, "an optimal window starts or ends",
                "Fires when the window with the lowest or highest forecast values starts and when it ends.", null,
                Visibility.VISIBLE, outputs);
    }
}
