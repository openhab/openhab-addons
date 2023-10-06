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
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeMute;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeSet;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeUnmute;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;

/**
 * The channel responsible for muting the Mycroft speaker
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class MuteChannel extends MycroftChannel<OnOffType> {

    private int volumeRestorationLevel;

    public MuteChannel(MycroftHandler handler, int volumeRestorationLevel) {
        super(handler, MycroftBindingConstants.VOLUME_MUTE_CHANNEL);
        this.volumeRestorationLevel = volumeRestorationLevel;
    }

    @Override
    public List<MessageType> getMessageToListenTo() {
        // we don't listen to mute/unmute message because duck/unduck seems sufficient
        // and we don't want to change state twice for the same event
        // but it should be tested on mark I, as volume is handled differently
        return Arrays.asList(MessageType.mycroft_volume_duck, MessageType.mycroft_volume_unduck,
                MessageType.mycroft_volume_set, MessageType.mycroft_volume_increase);
    }

    @Override
    public void messageReceived(BaseMessage message) {
        switch (message.type) {
            case mycroft_volume_mute:
            case mycroft_volume_duck:
                updateMyState(OnOffType.ON);
                break;
            case mycroft_volume_unmute:
            case mycroft_volume_unduck:
            case mycroft_volume_increase:
                updateMyState(OnOffType.OFF);
                break;
            case mycroft_volume_set:
                if (((MessageVolumeSet) message).data.percent > 0) {
                    updateMyState(OnOffType.OFF);
                }
                break;
            default:
        }
    }

    private boolean sendVolumeSetMessage(float volume) {
        String messageToSend = VolumeChannel.VOLUME_SETTER_MESSAGE.replaceAll("\\$\\$VOLUME", Float.toString(volume));
        return handler.sendMessage(messageToSend);
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                if (handler.sendMessage(new MessageVolumeMute())) {
                    updateMyState(OnOffType.ON);
                }
            } else if (command == OnOffType.OFF) {
                if (handler.sendMessage(new MessageVolumeUnmute())) {
                    updateMyState(OnOffType.OFF);
                    // if configured, we can restore the volume to a fixed amount
                    // usefull as a workaround for the broken Mycroft volume behavior
                    if (volumeRestorationLevel > 0) {
                        // we must wait 100ms for Mycroft to handle the message and
                        // setting old volume before forcing to our value
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                        sendVolumeSetMessage(Float.valueOf(volumeRestorationLevel));
                    }
                }
            }
        }
    }
}
