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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * A Text State type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a text state represents a State functional block on the Miniserver
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlTextState extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlTextState(uuid);
        }

        @Override
        String getType() {
            return "textstate";
        }
    }

    /**
     * A state which will receive an update of possible Text State values)
     */
    private static final String STATE_TEXT_AND_ICON = "textandicon";

    private LxControlTextState(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        ChannelUID id = addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel, "Text state", tags, null, () -> getStateStringValue(STATE_TEXT_AND_ICON));
        addChannelStateDescriptionFragment(id, StateDescriptionFragmentBuilder.create().withReadOnly(true).build());
    }
}
