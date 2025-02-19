/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.dto.response;

import java.util.List;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

/**
 * The {@link AutomationTO} encapsulates a single routine/automation
 *
 * @author Jan N. Klug - Initial contribution
 */
public class AutomationTO {
    @SuppressWarnings("unchecked")
    public static final TypeToken<List<AutomationTO>> LIST_TYPE_TOKEN = (TypeToken<List<AutomationTO>>) TypeToken
            .getParameterized(List.class, AutomationTO.class);
    public String automationId;
    public String name;
    public List<AutomationTriggerTO> triggers = List.of();
    public TreeMap<String, Object> sequence;
    public String status;
    public long creationTimeEpochMillis;
    public long lastUpdatedTimeEpochMillis;

    @Override
    public @NonNull String toString() {
        return "AutomationTO{automationId='" + automationId + "', name='" + name + "', triggers=" + triggers
                + ", sequence=" + sequence + ", status='" + status + "', creationTimeEpochMillis="
                + creationTimeEpochMillis + ", lastUpdatedTimeEpochMillis=" + lastUpdatedTimeEpochMillis + "}";
    }
}
