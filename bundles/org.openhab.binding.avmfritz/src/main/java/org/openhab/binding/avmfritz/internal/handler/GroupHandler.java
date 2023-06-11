/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.handler;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.GroupModel;
import org.openhab.core.thing.Thing;

/**
 * Handler for a FRITZ! group. Handles commands, which are sent to one of the channels.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GroupHandler extends AVMFritzBaseThingHandler {

    public GroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateProperties(AVMFritzBaseModel device, Map<String, String> editProperties) {
        if (device instanceof GroupModel) {
            GroupModel groupModel = (GroupModel) device;
            if (groupModel.getGroupinfo() != null) {
                editProperties.put(PROPERTY_MASTER, groupModel.getGroupinfo().getMasterdeviceid());
                editProperties.put(PROPERTY_MEMBERS, groupModel.getGroupinfo().getMembers());
            }
        }
        super.updateProperties(device, editProperties);
    }
}
