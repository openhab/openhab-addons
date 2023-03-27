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
package org.openhab.binding.anthem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;

/**
 * The {@link AnthemUpdate} class represents the result of parsing the response from
 * an Anthem processor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class AnthemUpdate {
    private static final int TYPE_STATE_UPDATE = 1;
    private static final int TYPE_PROPERTY_UPDATE = 2;

    private int updateType;
    private @Nullable StateUpdate stateUpdate;
    private @Nullable PropertyUpdate propertyUpdate;

    public AnthemUpdate(StateUpdate stateUpdate) {
        updateType = TYPE_STATE_UPDATE;
        this.stateUpdate = stateUpdate;
    }

    public AnthemUpdate(PropertyUpdate propertyUpdate) {
        updateType = TYPE_PROPERTY_UPDATE;
        this.propertyUpdate = propertyUpdate;
    }

    public static AnthemUpdate createStateUpdate(String groupId, String channelId, State state) {
        return new AnthemUpdate(new StateUpdate(groupId, channelId, state));
    }

    public static AnthemUpdate createPropertyUpdate(String name, String value) {
        return new AnthemUpdate(new PropertyUpdate(name, value));
    }

    public boolean isStateUpdate() {
        return updateType == TYPE_STATE_UPDATE;
    }

    public boolean isPropertyUpdate() {
        return updateType == TYPE_PROPERTY_UPDATE;
    }

    public StateUpdate getStateUpdate() {
        StateUpdate localStateUpdate = stateUpdate;
        if (updateType == TYPE_STATE_UPDATE && localStateUpdate != null) {
            return localStateUpdate;
        }
        throw new IllegalStateException("Update type is state update but stateUpdate object is null");
    }

    public PropertyUpdate getPropertyUpdate() {
        PropertyUpdate localPropertyUpdate = propertyUpdate;
        if (updateType == TYPE_PROPERTY_UPDATE && localPropertyUpdate != null) {
            return localPropertyUpdate;
        }
        throw new IllegalStateException("Update type is property update but propertyUpdate object is null");
    }
}
