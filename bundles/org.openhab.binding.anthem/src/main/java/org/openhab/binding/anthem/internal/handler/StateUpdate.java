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
import org.openhab.core.types.State;

/**
 * The {@link StateUpdate} class represents a state that needs to be updated
 * on an Anthem thing channel.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class StateUpdate {
    private String groupId;
    private String channelId;
    private State state;

    public StateUpdate(String groupId, String channelId, State state) {
        this.groupId = groupId;
        this.channelId = channelId;
        this.state = state;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getChannelId() {
        return channelId;
    }

    public State getState() {
        return state;
    }
}
