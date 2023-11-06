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
package org.openhab.binding.heos.internal.json.dto;

import static org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.json.HeosOption;

/**
 * Class for HEOS response objects
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosResponseObject<T> extends HeosObject {
    public final @Nullable HeosCommandTuple heosCommand;
    public final boolean result;
    public final @Nullable T payload;
    public final Map<String, HeosOption> options;

    public HeosResponseObject(@Nullable HeosCommandTuple heosCommand, String rawCommand, @Nullable String result,
            Map<String, String> attributes, @Nullable T payload,
            @Nullable List<Map<String, List<HeosOption>>> options) {
        super(rawCommand, attributes);
        this.heosCommand = heosCommand;
        this.result = "success".equals(result);
        this.payload = payload;
        this.options = processOptions(options);
    }

    private Map<String, HeosOption> processOptions(@Nullable List<Map<String, List<HeosOption>>> options) {
        if (options == null) {
            return Collections.emptyMap();
        }

        return options.stream().map(Map::entrySet).flatMap(Set::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0)));
    }

    public boolean isFinished() {
        return (result || hasAttribute(ERROR_ID)) && !hasAttribute(COMMAND_UNDER_PROCESS);
    }

    public @Nullable HeosError getError() {
        if (result || !hasAttribute(ERROR_ID)) {
            return null;
        }

        return new HeosError(getNumericAttribute(ERROR_ID), getNumericAttribute(SYSTEM_ERROR_NUMBER));
    }
}
