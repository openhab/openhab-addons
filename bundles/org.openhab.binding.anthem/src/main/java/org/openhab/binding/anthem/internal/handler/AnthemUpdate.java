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
package org.openhab.binding.anthem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The {@link AnthemUpdate} class represents the result of parsing the response from
 * an Anthem processor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemUpdate {
    private Object updateObject;

    public AnthemUpdate(StateUpdate stateUpdate) {
        this.updateObject = stateUpdate;
    }

    public AnthemUpdate(PropertyUpdate propertyUpdate) {
        this.updateObject = propertyUpdate;
    }

    public static AnthemUpdate createStateUpdate(String groupId, String channelId, State state) {
        return new AnthemUpdate(new StateUpdate(groupId, channelId, state));
    }

    public static AnthemUpdate createPropertyUpdate(String name, String value) {
        return new AnthemUpdate(new PropertyUpdate(name, value));
    }

    public boolean isStateUpdate() {
        return updateObject instanceof StateUpdate;
    }

    public boolean isPropertyUpdate() {
        return updateObject instanceof PropertyUpdate;
    }

    public StateUpdate getStateUpdate() {
        if (updateObject instanceof StateUpdate stateUpdate) {
            return stateUpdate;
        }
        throw new IllegalStateException("Update object is not a state update");
    }

    public PropertyUpdate getPropertyUpdate() {
        if (updateObject instanceof PropertyUpdate propertyUpdate) {
            return propertyUpdate;
        }
        throw new IllegalStateException("Update object is not a property update");
    }
}
