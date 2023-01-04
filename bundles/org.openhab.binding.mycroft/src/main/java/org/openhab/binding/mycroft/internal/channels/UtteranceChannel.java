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
package org.openhab.binding.mycroft.internal.channels;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mycroft.internal.MycroftBindingConstants;
import org.openhab.binding.mycroft.internal.MycroftHandler;
import org.openhab.binding.mycroft.internal.api.MessageType;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.binding.mycroft.internal.api.dto.MessageRecognizerLoopUtterance;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * This channel handle the full utterance send or received by Mycroft, before any intent recognition
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
@NonNullByDefault
public class UtteranceChannel extends MycroftChannel<StringType> {

    public UtteranceChannel(MycroftHandler handler) {
        super(handler, MycroftBindingConstants.UTTERANCE_CHANNEL);
    }

    @Override
    protected List<MessageType> getMessageToListenTo() {
        return Arrays.asList(MessageType.recognizer_loop__utterance);
    }

    @Override
    public void messageReceived(BaseMessage message) {
        if (message.type == MessageType.recognizer_loop__utterance) {
            List<String> utterances = ((MessageRecognizerLoopUtterance) message).data.utterances;
            if (!utterances.isEmpty()) {
                updateMyState(new StringType(utterances.get(0)));
            }
        }
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof StringType) {
            if (handler.sendMessage(new MessageRecognizerLoopUtterance(command.toFullString()))) {
                updateMyState(new StringType(command.toFullString()));
            }
        }
    }
}
