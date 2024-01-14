/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * QUITE FUNCTIONAL but with workaround
 * (see https://community.mycroft.ai/t/openhab-plugin-development-audio-volume-message-types-missing/10576
 * and https://github.com/MycroftAI/skill-volume/issues/53)
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class VolumeChannel extends MycroftChannel<State> {

    /**
     * As the MessageVolumeSet is, contrary to the documentation, not listened to by Mycroft,
     * we use a workaround and send a message simulating an intent detection
     */
    public static final String VOLUME_SETTER_MESSAGE = "{\"type\": \"mycroft-volume.mycroftai:SetVolume\", \"data\": {\"intent_type\": \"mycroft-volume.mycroftai:SetVolume\", \"mycroft_volume_mycroftaiVolume\": \"volume\", \"mycroft_volume_mycroftaiLevel\": \"$$VOLUME\", \"mycroft_volume_mycroftaiTo\": \"to\", \"target\": null, \"confidence\": 0.6000000000000001, \"__tags__\": [{\"match\": \"volume\", \"key\": \"volume\", \"start_token\": 1, \"entities\": [{\"key\": \"volume\", \"match\": \"volume\", \"data\": [[\"volume\", \"mycroft_volume_mycroftaiVolume\"]], \"confidence\": 1.0}], \"end_token\": 1, \"from_context\": false}, {\"match\": \"$$VOLUME\", \"key\": \"$$VOLUME\", \"start_token\": 3, \"entities\": [{\"key\": \"$$VOLUME\", \"match\": \"$$VOLUME\", \"data\": [[\"$$VOLUME\", \"mycroft_volume_mycroftaiLevel\"]], \"confidence\": 1.0}], \"end_token\": 3, \"from_context\": false}, {\"match\": \"to\", \"key\": \"to\", \"start_token\": 2, \"entities\": [{\"key\": \"to\", \"match\": \"to\", \"data\": [[\"to\", \"mycroft_volume_mycroftaiTo\"]], \"confidence\": 1.0}], \"end_token\": 2, \"from_context\": false}], \"utterance\": \"set volume to $$VOLUME\", \"utterances\": [\"set volume to X\"]}, \"context\": {\"client_name\": \"mycroft_cli\", \"source\": [\"skills\"], \"destination\": \"debug_cli\"}}";

    private PercentType lastVolume = new PercentType(50);
    private PercentType lastNonZeroVolume = new PercentType(50);

    public VolumeChannel(MycroftHandler handler) {
        super(handler, MycroftBindingConstants.VOLUME_CHANNEL);
    }

    @Override
    public List<MessageType> getMessageToListenTo() {
        // we don't listen to mute/unmute message because duck/unduck seems sufficient
        // and we don't want to change state twice for the same event
        // but it should be tested on mark I, as volume is handled differently
        return Arrays.asList(MessageType.mycroft_volume_get_response, MessageType.mycroft_volume_set,
                MessageType.mycroft_volume_increase, MessageType.mycroft_volume_decrease,
                MessageType.mycroft_volume_duck, MessageType.mycroft_volume_unduck);
    }

    @Override
    public void messageReceived(BaseMessage message) {
        if (message.type == MessageType.mycroft_volume_get_response) {
            float volumeGet = ((MessageVolumeGetResponse) message).data.percent;
            updateAndSaveMyState(normalizeVolume(volumeGet));
        } else if (message.type == MessageType.mycroft_volume_set) {
            float volumeSet = ((MessageVolumeSet) message).data.percent;
            updateAndSaveMyState(normalizeVolume(volumeSet));
        } else if (message.type == MessageType.mycroft_volume_duck) {
            updateAndSaveMyState(new PercentType(0));
        } else if (message.type == MessageType.mycroft_volume_unduck) {
            updateAndSaveMyState(lastNonZeroVolume);
        } else if (message.type == MessageType.mycroft_volume_increase) {
            updateAndSaveMyState(normalizeVolume(lastVolume.intValue() + 10));
        } else if (message.type == MessageType.mycroft_volume_decrease) {
            updateAndSaveMyState(normalizeVolume(lastVolume.intValue() - 10));
        }
    }

    protected final void updateAndSaveMyState(State state) {
        if (state instanceof PercentType volume) {
            this.lastVolume = volume;
            if (volume.intValue() > 0) {
                this.lastNonZeroVolume = volume;
            }
        }
        super.updateMyState(state);
    }

    /**
     * Protection method for volume with
     * potentially wrong value.
     *
     * @param volume The requested volume, on a scale from 0 to 100.
     *            Could be out of bond, then it will be corrected.
     * @return A safe volume in PercentType between 0 and 100
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
     * Protection method for volume with
     * potentially wrong value.
     *
     * @param volume The requested volume, on a scale from 0 to 1.
     * @return A safe volume in PercentType between 0 and 100
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
        return Float.valueOf(percentType.intValue());
    }

    public PercentType computeNewVolume(int valueAdded) {
        return new PercentType(lastVolume.intValue() + valueAdded);
    }

    private boolean sendSetMessage(float volume) {
        String messageToSend = VOLUME_SETTER_MESSAGE.replaceAll("\\$\\$VOLUME", Float.toString(volume));
        return handler.sendMessage(messageToSend);
    }

    @Override
    public void handleCommand(Command command) {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                if (sendSetMessage(toMycroftVolume(lastNonZeroVolume))) {
                    updateAndSaveMyState(lastNonZeroVolume);
                }
            }
            if (command == OnOffType.OFF) {
                if (sendSetMessage(0)) {
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
        } else if (command instanceof PercentType volume) {
            sendSetMessage(toMycroftVolume(volume));
            updateAndSaveMyState(volume);
        } else if (command instanceof RefreshType) {
            handler.sendMessage(new MessageVolumeGet());
        }
    }
}
