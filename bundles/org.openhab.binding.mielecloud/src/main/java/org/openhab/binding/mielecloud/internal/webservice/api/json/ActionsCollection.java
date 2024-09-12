/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * Immutable POJO representing a collection of actions queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class ActionsCollection {
    private static final java.lang.reflect.Type STRING_ACTIONS_MAP_TYPE = new TypeToken<Map<String, Actions>>() {
    }.getType();

    private final Map<String, Actions> actions;

    ActionsCollection(Map<String, Actions> actions) {
        this.actions = actions;
    }

    /**
     * Creates a new {@link ActionsCollection} from the given Json text.
     *
     * @param json The Json text.
     * @return The created {@link ActionsCollection}.
     * @throws MieleSyntaxException if parsing the data from {@code json} fails.
     */
    public static ActionsCollection fromJson(String json) {
        try {
            Map<String, Actions> actions = new Gson().fromJson(json, STRING_ACTIONS_MAP_TYPE);
            if (actions == null) {
                throw new MieleSyntaxException("Failed to parse Json.");
            }
            return new ActionsCollection(actions);
        } catch (JsonSyntaxException e) {
            throw new MieleSyntaxException("Failed to parse Json.", e);
        }
    }

    public Set<String> getDeviceIdentifiers() {
        return actions.keySet();
    }

    public Actions getActions(String identifier) {
        Actions actions = this.actions.get(identifier);
        if (actions == null) {
            throw new IllegalArgumentException("There are no actions for identifier " + identifier);
        }
        return actions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(actions);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActionsCollection other = (ActionsCollection) obj;
        return Objects.equals(actions, other.actions);
    }

    @Override
    public String toString() {
        return "ActionsCollection [actions=" + actions + "]";
    }
}
