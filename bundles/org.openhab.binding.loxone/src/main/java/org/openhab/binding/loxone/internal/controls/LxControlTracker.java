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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * A Tracker type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a Tracker control represents a Tracker functional block on the Miniserver
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlTracker extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlTracker(uuid);
        }

        @Override
        String getType() {
            return "tracker";
        }
    }

    /**
     * A state which will receive an update of possible Text State values)
     */
    private static final String STATE_ENTRIES = "entries";

    private LxControlTracker(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        ChannelUID id = addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel, "Tracker", tags, null, () -> getStateStringValue(STATE_ENTRIES));
        addChannelStateDescriptionFragment(id, StateDescriptionFragmentBuilder.create().withReadOnly(true).build());
    }
}
