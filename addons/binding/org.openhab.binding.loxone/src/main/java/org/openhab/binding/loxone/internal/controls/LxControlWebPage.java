/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * A web page type of control on Loxone Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlWebPage extends LxControl {
    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlWebPage(uuid);
        }

        @Override
        String getType() {
            return "webpage";
        }
    }

    private StringType url;
    private StringType urlHd;

    private LxControlWebPage(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        if (details != null) {
            if (details.url != null) {
                url = new StringType(details.url);
            }
            if (details.urlHd != null) {
                urlHd = new StringType(details.urlHd);
            }
        }
        ChannelUID c1 = addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / URL", "Low resolution URL", tags, null, () -> url);
        addChannelStateDescription(c1, new StateDescription(null, null, null, null, true, null));

        ChannelUID c2 = addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_TEXT),
                defaultChannelLabel + " / URL HD", "High resolution URL", tags, null, () -> urlHd);
        addChannelStateDescription(c2, new StateDescription(null, null, null, null, true, null));
    }
}
