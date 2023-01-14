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

import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxTags;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

/**
 * An InfoOnlyAnalog type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, this control covers analog virtual states only. This control does not send any
 * commands to the Miniserver. It can be used to read a formatted representation of an analog virtual state.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlInfoOnlyAnalog extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlInfoOnlyAnalog(uuid);
        }

        @Override
        String getType() {
            return "infoonlyanalog";
        }
    }

    /**
     * InfoOnlyAnalog state with current value
     */
    private static final String STATE_VALUE = "value";

    private LxControlInfoOnlyAnalog(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        LxCategory category = getCategory();
        if (category != null && category.getType() == LxCategory.CategoryType.TEMPERATURE) {
            tags.addAll(LxTags.TEMPERATURE);
        }
        ChannelUID cid = addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_ANALOG),
                defaultChannelLabel, "Analog virtual state", tags, null, () -> getStateDecimalValue(STATE_VALUE));
        String format;
        if (details != null && details.format != null) {
            format = details.format;
        } else {
            format = "%.1f";
        }
        addChannelStateDescriptionFragment(cid,
                StateDescriptionFragmentBuilder.create().withPattern(format).withReadOnly(true).build());
    }
}
