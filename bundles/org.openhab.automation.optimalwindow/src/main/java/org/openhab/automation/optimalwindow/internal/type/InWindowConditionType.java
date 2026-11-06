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

import static org.openhab.automation.optimalwindow.internal.OptimalWindowConstants.CONDITION_TYPE_ID;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.automation.Visibility;
import org.openhab.core.automation.type.ConditionType;

/**
 * Condition type that is satisfied while the current time is within the optimal window.
 *
 * @author Thomas Leber - Initial contribution
 */
@NonNullByDefault
public class InWindowConditionType extends ConditionType {

    public static InWindowConditionType initialize() {
        return new InWindowConditionType();
    }

    public InWindowConditionType() {
        super(CONDITION_TYPE_ID, WindowConfigDescriptions.window(), "it is within the optimal window",
                "Satisfied while the current time is within the window with the lowest or highest forecast values.",
                null, Visibility.VISIBLE, List.of());
    }
}
