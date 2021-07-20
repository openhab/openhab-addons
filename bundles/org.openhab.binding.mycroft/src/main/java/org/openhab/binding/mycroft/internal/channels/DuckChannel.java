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
package org.openhab.binding.mycroft.internal.channels;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mycroft.internal.MycroftBindingConstants;
import org.openhab.binding.mycroft.internal.MycroftHandler;
import org.openhab.binding.mycroft.internal.api.MessageType;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeDuck;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeUnduck;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * The channel responsible for triggering STT recognition
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class DuckChannel extends MycroftChannel<OnOffType> {

    public DuckChannel(MycroftHandler handler) {
        super(handler, MycroftBindingConstants.VOLUME_DUCK_CHANNEL);
    }

    @Override
    public List<MessageType> getMessageToListenTo() {
        return Arrays.asList(MessageType.mycroft_volume_duck, MessageType.mycroft_volume_unduck);
    }

    @Override
    public void messageReceived(BaseMessage message) {
        if (message.type == MessageType.mycroft_volume_duck) {
            updateMyState(OnOffType.ON);
        } else if (message.type == MessageType.mycroft_volume_unduck) {
            updateMyState(OnOffType.OFF);
        }
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                if (handler.sendMessage(new MessageVolumeDuck())) {
                    updateMyState(OnOffType.ON);
                }
            } else if (command == OnOffType.OFF) {
                if (handler.sendMessage(new MessageVolumeUnduck())) {
                    updateMyState(OnOffType.OFF);
                }
            }
        }
    }
}
