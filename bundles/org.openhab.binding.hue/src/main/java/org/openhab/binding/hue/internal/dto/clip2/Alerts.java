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
package org.openhab.binding.hue.internal.dto.clip2;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.enums.ActionType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for 'alert' of a light.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Alerts {
    private @Nullable @SerializedName("action_values") List<String> actionValues;
    private @Nullable String action;

    public @Nullable ActionType getAction() {
        String action = this.action;
        return Objects.nonNull(action) ? ActionType.of(action) : null;
    }

    public List<ActionType> getActionValues() {
        List<String> actionValues = this.actionValues;
        if (Objects.nonNull(actionValues)) {
            return actionValues.stream().map(ActionType::of).collect(Collectors.toList());
        }
        return List.of();
    }

    public Alerts setAction(ActionType actionType) {
        this.action = actionType.name().toLowerCase();
        return this;
    }
}
