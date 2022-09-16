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
package org.openhab.binding.easee.internal.command.charger;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.easee.internal.handler.EaseeThingHandler;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;

/**
 * implements the command api call of the charger.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SendCommandPauseResume extends SendCommand {

    public SendCommandPauseResume(EaseeThingHandler handler, String chargerId, Channel channel, Command command) {
        super(handler, channel, command);
        String value;
        if (command.equals(OnOffType.ON)) {
            value = CMD_VAL_PAUSE_CHARGING;
        } else {
            value = CMD_VAL_RESUME_CHARGING;
        }
        this.url = COMMANDS_URL.replaceAll("\\{id\\}", chargerId).replaceAll("\\{command\\}", value);
    }
}
