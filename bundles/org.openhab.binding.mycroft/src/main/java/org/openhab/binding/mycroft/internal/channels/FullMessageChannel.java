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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mycroft.internal.MycroftBindingConstants;
import org.openhab.binding.mycroft.internal.MycroftHandler;
import org.openhab.binding.mycroft.internal.api.MessageType;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;

/**
 * The channel responsible for sending/receiving raw message
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class FullMessageChannel extends MycroftChannel<StringType> {

    private List<String> messageTypesList = new ArrayList<>();

    public FullMessageChannel(MycroftHandler handler, String messageTypesList) {
        super(handler, MycroftBindingConstants.FULL_MESSAGE_CHANNEL);
        for (String messageType : messageTypesList.split(",")) {
            this.messageTypesList.add(messageType.trim());
        }
    }

    @Override
    public List<MessageType> getMessageToListenTo() {
        return Arrays.asList(MessageType.any);
    }

    @Override
    public void messageReceived(BaseMessage message) {
        if (messageTypesList.contains(message.type.getMessageTypeName())) {
            updateMyState(new StringType(message.message));
        }
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof StringType) {
            if (handler.sendMessage(command.toFullString())) {
                updateMyState(new StringType(command.toFullString()));
            }
        }
    }
}
