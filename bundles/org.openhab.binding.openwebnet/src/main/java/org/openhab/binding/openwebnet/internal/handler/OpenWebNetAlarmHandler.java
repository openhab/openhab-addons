/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAlarm;
import org.openwebnet4j.message.Who;

/**
 * The {@link OpenWebNetAlarmHandler} is responsible for handling commands/messages for Alarm Central Unit and zones. It
 * extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetAlarmHandler extends OpenWebNetThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.ALARM_SUPPORTED_THING_TYPES;

    public OpenWebNetAlarmHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void handleChannelCommand(@NonNull ChannelUID channel, @NonNull Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void requestChannelState(@NonNull ChannelUID channel) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void refreshDevice(boolean refreshAll) {
        // TODO Auto-generated method stub

    }

    @Override
    protected @NonNull Where buildBusWhere(@NonNull String wStr) throws IllegalArgumentException {
        return new WhereAlarm(wStr);
    }

    @Override
    protected @NonNull String ownIdPrefix() {
        return Who.BURGLAR_ALARM.value().toString();
    }

}
