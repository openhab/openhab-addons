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
package org.openhab.binding.lgthinq.lgservices.model.devices.fridge;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.CapabilityDefinition;

/**
 * The {@link FridgeCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public interface FridgeCapability extends CapabilityDefinition {

    public Map<String, String> getFridgeTempCMap();

    public Map<String, String> getFridgeTempFMap();

    public Map<String, String> getFreezerTempCMap();

    public Map<String, String> getFreezerTempFMap();
}
