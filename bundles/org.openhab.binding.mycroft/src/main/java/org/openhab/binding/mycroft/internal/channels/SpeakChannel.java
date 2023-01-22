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
package org.openhab.binding.mycroft.internal.channels;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mycroft.internal.MycroftBindingConstants;
import org.openhab.binding.mycroft.internal.MycroftHandler;
import org.openhab.binding.mycroft.internal.api.MessageType;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.binding.mycroft.internal.api.dto.MessageSpeak;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * The channel responsible for TSS
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class SpeakChannel extends MycroftChannel<StringType> {

    public SpeakChannel(MycroftHandler handler) {
        super(handler, MycroftBindingConstants.SPEAK_CHANNEL);
    }

    @Override
    public List<MessageType> getMessageToListenTo() {
        return Arrays.asList(MessageType.speak);
    }

    @Override
    public void messageReceived(BaseMessage message) {
        if (message.type == MessageType.speak) {
            MessageSpeak messageSpeak = (MessageSpeak) message;
            updateMyState(new StringType(messageSpeak.data.utterance));
        }
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof StringType) {
            if (handler.sendMessage(new MessageSpeak(command.toFullString()))) {
                updateMyState(new StringType(command.toFullString()));
            }
        }
    }
}
