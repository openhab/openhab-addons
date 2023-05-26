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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link FridgeCanonicalCapability}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class FridgeCanonicalCapability extends AbstractCapability<FridgeCanonicalCapability>
        implements FridgeCapability {

    private static final Logger logger = LoggerFactory.getLogger(FridgeCanonicalCapability.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, String> fridgeTempCMap = new LinkedHashMap<String, String>();
    private final Map<String, String> fridgeTempFMap = new LinkedHashMap<String, String>();
    private final Map<String, String> freezerTempCMap = new LinkedHashMap<String, String>();
    private final Map<String, String> freezerTempFMap = new LinkedHashMap<String, String>();

    public Map<String, String> getFridgeTempCMap() {
        return fridgeTempCMap;
    }

    public Map<String, String> getFridgeTempFMap() {
        return fridgeTempFMap;
    }

    public Map<String, String> getFreezerTempCMap() {
        return freezerTempCMap;
    }

    public Map<String, String> getFreezerTempFMap() {
        return freezerTempFMap;
    }
}
