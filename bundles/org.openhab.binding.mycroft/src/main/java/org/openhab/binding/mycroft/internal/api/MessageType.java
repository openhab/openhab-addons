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
package org.openhab.binding.mycroft.internal.api;

import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mycroft.internal.api.dto.BaseMessage;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioNext;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioPause;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioPlay;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioPrev;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioResume;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioStop;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioTrackInfo;
import org.openhab.binding.mycroft.internal.api.dto.MessageAudioTrackInfoReply;
import org.openhab.binding.mycroft.internal.api.dto.MessageMicListen;
import org.openhab.binding.mycroft.internal.api.dto.MessageRecognizerLoopRecordBegin;
import org.openhab.binding.mycroft.internal.api.dto.MessageRecognizerLoopRecordEnd;
import org.openhab.binding.mycroft.internal.api.dto.MessageRecognizerLoopUtterance;
import org.openhab.binding.mycroft.internal.api.dto.MessageSpeak;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeDecrease;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeDuck;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeGet;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeGetResponse;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeIncrease;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeMute;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeSet;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeUnduck;
import org.openhab.binding.mycroft.internal.api.dto.MessageVolumeUnmute;

/**
 * All message type of interest, issued by Mycroft, are referenced here
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public enum MessageType {

    any("special-anymessages", BaseMessage.class),
    speak("speak", MessageSpeak.class),
    recognizer_loop__record_begin("recognizer_loop:record_begin", MessageRecognizerLoopRecordBegin.class),
    recognizer_loop__record_end("recognizer_loop:record_end", MessageRecognizerLoopRecordEnd.class),
    recognizer_loop__utterance("recognizer_loop:utterance", MessageRecognizerLoopUtterance.class),
    mycroft_mic_listen("mycroft.mic.listen", MessageMicListen.class),

    mycroft_audio_service_pause("mycroft.audio.service.pause", MessageAudioPause.class),
    mycroft_audio_service_resume("mycroft.audio.service.resume", MessageAudioResume.class),
    mycroft_audio_service_stop("mycroft.audio.service.stop", MessageAudioStop.class),
    mycroft_audio_service_play("mycroft.audio.service.play", MessageAudioPlay.class),
    mycroft_audio_service_next("mycroft.audio.service.next", MessageAudioNext.class),
    mycroft_audio_service_prev("mycroft.audio.service.prev", MessageAudioPrev.class),
    mycroft_audio_service_track_info("mycroft.audio.service.track_info", MessageAudioTrackInfo.class),
    mycroft_audio_service_track_info_reply("mycroft.audio.service.track_info_reply", MessageAudioTrackInfoReply.class),

    mycroft_volume_set("mycroft.volume.set", MessageVolumeSet.class),
    mycroft_volume_increase("mycroft.volume.increase", MessageVolumeIncrease.class),
    mycroft_volume_decrease("mycroft.volume.decrease", MessageVolumeDecrease.class),
    mycroft_volume_get("mycroft.volume.get", MessageVolumeGet.class),
    mycroft_volume_get_response("mycroft.volume.get.response", MessageVolumeGetResponse.class),
    mycroft_volume_mute("mycroft.volume.mute", MessageVolumeMute.class),
    mycroft_volume_unmute("mycroft.volume.unmute", MessageVolumeUnmute.class),
    mycroft_volume_duck("mycroft.volume.duck", MessageVolumeDuck.class),
    mycroft_volume_unduck("mycroft.volume.unduck", MessageVolumeUnduck.class),

    mycroft_reminder_mycroftai__reminder("mycroft-reminder.mycroftai:reminder", BaseMessage.class),
    mycroft_date_time_mycroftai__timeskillupdate_display("mycroft-date-time.mycroftai:TimeSkillupdate_display",
            BaseMessage.class),
    mycroft_configuration_mycroftai__configurationskillupdate_remote(
            "mycroft-configuration.mycroftai:ConfigurationSkillupdate_remote", BaseMessage.class);

    private @NotNull Class<? extends BaseMessage> messageTypeClass;
    private @NotNull String messageTypeName;

    MessageType(String messageTypeName, Class<? extends BaseMessage> messageType) {
        this.messageTypeClass = messageType;
        this.messageTypeName = messageTypeName;
    }

    /**
     * Get the expected message type for this message
     *
     * @return The message type class associated with this type
     */
    public @NotNull Class<? extends BaseMessage> getMessageTypeClass() {
        return messageTypeClass;
    }

    @NotNull
    public static MessageType fromString(String asString) {
        return Stream.of(values()).filter(messageType -> messageType.messageTypeName.equals(asString)).findFirst()
                .orElse(any);
    }

    public String getMessageTypeName() {
        return messageTypeName;
    }

    protected void setMessageTypeName(String messageTypeName) {
        this.messageTypeName = messageTypeName;
    }
}
