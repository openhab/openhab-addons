/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.automation.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.hueemulation.internal.dto.HueRuleEntry;

/**
 * A configuration holder class for the rule trigger handler
 * 
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueRuleTriggerConfig {
    public String address = "";
    public HueRuleEntry.Operator operator = HueRuleEntry.Operator.dx;
    public @Nullable String value;
}
