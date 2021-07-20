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
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeDecrease;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeGet;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeGetResponse;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeIncrease;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeSet;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * The channel responsible for handling the volume of the Mycroft speaker
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class VolumeChannel extends MycroftChannel<State> {

    private PercentType lastVolume = new PercentType(50);
    private PercentType lastNonZeroVolume = new PercentType(50);

    public VolumeChannel(MycroftHandler handler) {
        super(handler, MycroftBindingConstants.VOLUME_CHANNEL);
    }

    @Override
    public List<MessageType> getMessageToListenTo() {
        return Arrays.asList(MessageType.mycroft_volume_get_response, MessageType.mycroft_volume_set,
                MessageType.mycroft_volume_mute, MessageType.mycroft_volume_unmute, MessageType.mycroft_volume_increase,
                MessageType.mycroft_volume_decrease);
    }

    @Override
    public void messageReceived(BaseMessage message) {

        if (message.type == MessageType.mycroft_volume_get_response) {
            float volumeGet = ((MessageVolumeGetResponse) message).data.percent;
            updateAndSaveMyState(normalizeVolume(volumeGet));
        } else if (message.type == MessageType.mycroft_volume_set) {
            float volumeSet = ((MessageVolumeSet) message).data.percent;
            updateAndSaveMyState(normalizeVolume(volumeSet));
        } else if (message.type == MessageType.mycroft_volume_mute) {
            updateAndSaveMyState(new PercentType(0));
        } else if (message.type == MessageType.mycroft_volume_unmute) {
            updateAndSaveMyState(lastNonZeroVolume);
        } else if (message.type == MessageType.mycroft_volume_increase) {
            updateAndSaveMyState(normalizeVolume(lastVolume.intValue() + 10));
        } else if (message.type == MessageType.mycroft_volume_decrease) {
            updateAndSaveMyState(normalizeVolume(lastVolume.intValue() - 10));
        }
    }

    protected final void updateAndSaveMyState(State state) {
        if (state instanceof PercentType) {
            this.lastVolume = ((PercentType) state);
            if (((PercentType) state).intValue() > 0) {
                this.lastNonZeroVolume = ((PercentType) state);
            }
        }
        super.updateMyState(state);
    }

    /**
     * Volume between 0 and 100
     *
     * @param volume
     * @return
     */
    private PercentType normalizeVolume(int volume) {
        if (volume >= 100) {
            return PercentType.HUNDRED;
        } else if (volume <= 0) {
            return PercentType.ZERO;
        } else {
            return new PercentType(volume);
        }
    }

    /**
     * Volume between 0 and 1
     *
     * @param volume
     * @return
     */
    private PercentType normalizeVolume(float volume) {
        if (volume >= 1) {
            return PercentType.HUNDRED;
        } else if (volume <= 0) {
            return PercentType.ZERO;
        } else {
            return new PercentType(Math.round(volume * 100));
        }
    }

    public float toMycroftVolume(PercentType percentType) {
        return Float.valueOf(percentType.intValue() / 100f);
    }

    public PercentType computeNewVolume(int valueAdded) {
        return new PercentType(lastVolume.intValue() + valueAdded);
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                MessageVolumeSet messageVolumeSet = new MessageVolumeSet();
                messageVolumeSet.data.percent = toMycroftVolume(lastNonZeroVolume);
                if (handler.sendMessage(messageVolumeSet)) {
                    updateAndSaveMyState(lastNonZeroVolume);
                }
            }
            if (command == OnOffType.OFF) {
                MessageVolumeSet messageVolumeSet = new MessageVolumeSet();
                messageVolumeSet.data.percent = 0;
                if (handler.sendMessage(messageVolumeSet)) {
                    updateAndSaveMyState(PercentType.ZERO);
                }
            }
        } else if (command instanceof IncreaseDecreaseType) {
            if (command == IncreaseDecreaseType.INCREASE) {
                if (handler.sendMessage(new MessageVolumeIncrease())) {
                    updateAndSaveMyState(computeNewVolume(10));
                }
            }
            if (command == IncreaseDecreaseType.DECREASE) {
                handler.sendMessage(new MessageVolumeDecrease());
                updateAndSaveMyState(computeNewVolume(-10));
            }
        } else if (command instanceof PercentType) {
            MessageVolumeSet messageVolumeSet = new MessageVolumeSet();
            messageVolumeSet.data.percent = toMycroftVolume((PercentType) command);
            handler.sendMessage(messageVolumeSet);
            updateAndSaveMyState((PercentType) command);
        } else if (command instanceof RefreshType) {
            handler.sendMessage(new MessageVolumeGet());
        }
    }
}
