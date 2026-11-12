/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.denonmarantz.internal;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * Represents the state of the handled DenonMarantz AVR
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
@NonNullByDefault
public class DenonMarantzState {

    private @Nullable State power;
    private @Nullable State mainZonePower;
    private @Nullable State mute;
    private @Nullable State mainVolume;
    private @Nullable State mainVolumeDB;
    private @Nullable State input;
    private @Nullable State surroundProgram;
    private @Nullable State allZoneStereo;
    private @Nullable State speakerPreset;

    private @Nullable State artist;
    private @Nullable State album;
    private @Nullable State track;

    // ------ Zones ------
    private @Nullable State zone2Power;
    private @Nullable State zone2Volume;
    private @Nullable State zone2VolumeDB;
    private @Nullable State zone2Mute;
    private @Nullable State zone2Input;

    private @Nullable State zone3Power;
    private @Nullable State zone3Volume;
    private @Nullable State zone3VolumeDB;
    private @Nullable State zone3Mute;
    private @Nullable State zone3Input;

    private @Nullable State zone4Power;
    private @Nullable State zone4Volume;
    private @Nullable State zone4VolumeDB;
    private @Nullable State zone4Mute;
    private @Nullable State zone4Input;

    private DenonMarantzStateChangedListener handler;
    private final Map<String, State> channelVolumes = new HashMap<>();

    public DenonMarantzState(DenonMarantzStateChangedListener handler) {
        this.handler = handler;
    }

    public void connectionError(String errorMessage) {
        handler.connectionError(errorMessage);
    }

    public @Nullable State getStateForChannelID(String channelID) {
        switch (channelID) {
            case DenonMarantzBindingConstants.CHANNEL_POWER:
                return power;
            case DenonMarantzBindingConstants.CHANNEL_MAIN_ZONE_POWER:
                return mainZonePower;
            case DenonMarantzBindingConstants.CHANNEL_MUTE:
                return mute;
            case DenonMarantzBindingConstants.CHANNEL_MAIN_VOLUME:
                return mainVolume;
            case DenonMarantzBindingConstants.CHANNEL_MAIN_VOLUME_DB:
                return mainVolumeDB;
            case DenonMarantzBindingConstants.CHANNEL_INPUT:
                return input;
            case DenonMarantzBindingConstants.CHANNEL_SURROUND_PROGRAM:
                return surroundProgram;
            case DenonMarantzBindingConstants.CHANNEL_ALL_ZONE_STEREO:
                return allZoneStereo;
            case DenonMarantzBindingConstants.CHANNEL_SPEAKER_PRESET:
                return speakerPreset;
            case DenonMarantzBindingConstants.CHANNEL_CVFL:
            case DenonMarantzBindingConstants.CHANNEL_CVFR:
            case DenonMarantzBindingConstants.CHANNEL_CVC:
            case DenonMarantzBindingConstants.CHANNEL_CVSW:
            case DenonMarantzBindingConstants.CHANNEL_CVSW2:
            case DenonMarantzBindingConstants.CHANNEL_CVSW3:
            case DenonMarantzBindingConstants.CHANNEL_CVSW4:
            case DenonMarantzBindingConstants.CHANNEL_CVSL:
            case DenonMarantzBindingConstants.CHANNEL_CVSR:
            case DenonMarantzBindingConstants.CHANNEL_CVSBL:
            case DenonMarantzBindingConstants.CHANNEL_CVSBR:
            case DenonMarantzBindingConstants.CHANNEL_CVSB:
            case DenonMarantzBindingConstants.CHANNEL_CVFHL:
            case DenonMarantzBindingConstants.CHANNEL_CVFHR:
            case DenonMarantzBindingConstants.CHANNEL_CVFWL:
            case DenonMarantzBindingConstants.CHANNEL_CVFWR:
            case DenonMarantzBindingConstants.CHANNEL_CVTFL:
            case DenonMarantzBindingConstants.CHANNEL_CVTFR:
            case DenonMarantzBindingConstants.CHANNEL_CVTML:
            case DenonMarantzBindingConstants.CHANNEL_CVTMR:
            case DenonMarantzBindingConstants.CHANNEL_CVTRL:
            case DenonMarantzBindingConstants.CHANNEL_CVTRR:
            case DenonMarantzBindingConstants.CHANNEL_CVRHL:
            case DenonMarantzBindingConstants.CHANNEL_CVRHR:
            case DenonMarantzBindingConstants.CHANNEL_CVFDL:
            case DenonMarantzBindingConstants.CHANNEL_CVFDR:
            case DenonMarantzBindingConstants.CHANNEL_CVSDL:
            case DenonMarantzBindingConstants.CHANNEL_CVSDR:
            case DenonMarantzBindingConstants.CHANNEL_CVBDL:
            case DenonMarantzBindingConstants.CHANNEL_CVBDR:
            case DenonMarantzBindingConstants.CHANNEL_CVSHL:
            case DenonMarantzBindingConstants.CHANNEL_CVSHR:
            case DenonMarantzBindingConstants.CHANNEL_CVTS:
            case DenonMarantzBindingConstants.CHANNEL_CVCH:
            case DenonMarantzBindingConstants.CHANNEL_CVTTR:
                return channelVolumes.get(channelID);
            case DenonMarantzBindingConstants.CHANNEL_NOW_PLAYING_ARTIST:
                return artist;
            case DenonMarantzBindingConstants.CHANNEL_NOW_PLAYING_ALBUM:
                return album;
            case DenonMarantzBindingConstants.CHANNEL_NOW_PLAYING_TRACK:
                return track;

            case DenonMarantzBindingConstants.CHANNEL_ZONE2_POWER:
                return zone2Power;
            case DenonMarantzBindingConstants.CHANNEL_ZONE2_VOLUME:
                return zone2Volume;
            case DenonMarantzBindingConstants.CHANNEL_ZONE2_VOLUME_DB:
                return zone2VolumeDB;
            case DenonMarantzBindingConstants.CHANNEL_ZONE2_MUTE:
                return zone2Mute;
            case DenonMarantzBindingConstants.CHANNEL_ZONE2_INPUT:
                return zone2Input;

            case DenonMarantzBindingConstants.CHANNEL_ZONE3_POWER:
                return zone3Power;
            case DenonMarantzBindingConstants.CHANNEL_ZONE3_VOLUME:
                return zone3Volume;
            case DenonMarantzBindingConstants.CHANNEL_ZONE3_VOLUME_DB:
                return zone3VolumeDB;
            case DenonMarantzBindingConstants.CHANNEL_ZONE3_MUTE:
                return zone3Mute;
            case DenonMarantzBindingConstants.CHANNEL_ZONE3_INPUT:
                return zone3Input;

            case DenonMarantzBindingConstants.CHANNEL_ZONE4_POWER:
                return zone4Power;
            case DenonMarantzBindingConstants.CHANNEL_ZONE4_VOLUME:
                return zone4Volume;
            case DenonMarantzBindingConstants.CHANNEL_ZONE4_VOLUME_DB:
                return zone4VolumeDB;
            case DenonMarantzBindingConstants.CHANNEL_ZONE4_MUTE:
                return zone4Mute;
            case DenonMarantzBindingConstants.CHANNEL_ZONE4_INPUT:
                return zone4Input;

            default:
                return null;
        }
    }

    public void setPower(boolean power) {
        OnOffType newVal = OnOffType.from(power);
        if (newVal != this.power) {
            this.power = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_POWER, newVal);
        }
    }

    public void setMainZonePower(boolean mainPower) {
        OnOffType newVal = OnOffType.from(mainPower);
        if (newVal != this.mainZonePower) {
            this.mainZonePower = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_MAIN_ZONE_POWER, newVal);
        }
    }

    public void setMute(boolean mute) {
        OnOffType newVal = OnOffType.from(mute);
        if (newVal != this.mute) {
            this.mute = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_MUTE, newVal);
        }
    }

    public void setMainVolume(BigDecimal volume) {
        PercentType newVal = new PercentType(volume);
        if (!newVal.equals(this.mainVolume)) {
            this.mainVolume = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_MAIN_VOLUME, newVal);
            // update the main volume in dB too
            State mainVolumeDB = this.mainVolumeDB = new QuantityType<>(
                    volume.subtract(DenonMarantzBindingConstants.DB_OFFSET), Units.DECIBEL);
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_MAIN_VOLUME_DB, mainVolumeDB);
        }
    }

    public void setInput(String input) {
        StringType newVal = StringType.valueOf(input);
        if (!newVal.equals(this.input)) {
            this.input = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_INPUT, newVal);
        }
    }

    public void setSurroundProgram(String surroundProgram) {
        StringType newVal = StringType.valueOf(surroundProgram);
        if (!newVal.equals(this.surroundProgram)) {
            this.surroundProgram = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_SURROUND_PROGRAM, newVal);
        }
    }

    public void setAllZoneStereo(boolean allZoneStereo) {
        OnOffType newVal = OnOffType.from(allZoneStereo);
        if (newVal != this.allZoneStereo) {
            this.allZoneStereo = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ALL_ZONE_STEREO, newVal);
        }
    }

    public void setSpeakerPreset(BigDecimal preset) {
        DecimalType newVal = new DecimalType(preset);
        if (!newVal.equals(this.speakerPreset)) {
            this.speakerPreset = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_SPEAKER_PRESET, newVal);
        }
    }

    public void setChannelVolume(String channel, BigDecimal volume) {
        QuantityType<?> newVal = new QuantityType<>(volume, Units.DECIBEL);
        State oldVal = channelVolumes.get(channel);

        if (!newVal.equals(oldVal)) {
            channelVolumes.put(channel, newVal);
            handler.stateChanged(channel, newVal);
        }
    }

    public void setNowPlayingArtist(String artist) {
        StringType newVal = artist.isBlank() ? StringType.EMPTY : StringType.valueOf(artist);
        if (!newVal.equals(this.artist)) {
            this.artist = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_NOW_PLAYING_ARTIST, newVal);
        }
    }

    public void setNowPlayingAlbum(String album) {
        StringType newVal = album.isBlank() ? StringType.EMPTY : StringType.valueOf(album);
        if (!newVal.equals(this.album)) {
            this.album = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_NOW_PLAYING_ALBUM, newVal);
        }
    }

    public void setNowPlayingTrack(String track) {
        StringType newVal = track.isBlank() ? StringType.EMPTY : StringType.valueOf(track);
        if (!newVal.equals(this.track)) {
            this.track = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_NOW_PLAYING_TRACK, newVal);
        }
    }

    public void setZone2Power(boolean power) {
        OnOffType newVal = OnOffType.from(power);
        if (newVal != this.zone2Power) {
            this.zone2Power = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE2_POWER, newVal);
        }
    }

    public void setZone2Volume(BigDecimal volume) {
        PercentType newVal = new PercentType(volume);
        if (!newVal.equals(this.zone2Volume)) {
            this.zone2Volume = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE2_VOLUME, newVal);
            // update the volume in dB too
            State zone2VolumeDB = this.zone2VolumeDB = new QuantityType<>(
                    volume.subtract(DenonMarantzBindingConstants.DB_OFFSET), Units.DECIBEL);
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE2_VOLUME_DB, zone2VolumeDB);
        }
    }

    public void setZone2Mute(boolean mute) {
        OnOffType newVal = OnOffType.from(mute);
        if (newVal != this.zone2Mute) {
            this.zone2Mute = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE2_MUTE, newVal);
        }
    }

    public void setZone2Input(String zone2Input) {
        StringType newVal = StringType.valueOf(zone2Input);
        if (!newVal.equals(this.zone2Input)) {
            this.zone2Input = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE2_INPUT, newVal);
        }
    }

    public void setZone3Power(boolean power) {
        OnOffType newVal = OnOffType.from(power);
        if (newVal != this.zone3Power) {
            this.zone3Power = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE3_POWER, newVal);
        }
    }

    public void setZone3Volume(BigDecimal volume) {
        PercentType newVal = new PercentType(volume);
        if (!newVal.equals(this.zone3Volume)) {
            this.zone3Volume = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE3_VOLUME, newVal);
            // update the volume in dB too
            State zone3VolumeDB = this.zone3VolumeDB = new QuantityType<>(
                    volume.subtract(DenonMarantzBindingConstants.DB_OFFSET), Units.DECIBEL);
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE3_VOLUME_DB, zone3VolumeDB);
        }
    }

    public void setZone3Mute(boolean mute) {
        OnOffType newVal = OnOffType.from(mute);
        if (newVal != this.zone3Mute) {
            this.zone3Mute = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE3_MUTE, newVal);
        }
    }

    public void setZone3Input(String zone3Input) {
        StringType newVal = StringType.valueOf(zone3Input);
        if (!newVal.equals(this.zone3Input)) {
            this.zone3Input = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE3_INPUT, newVal);
        }
    }

    public void setZone4Power(boolean power) {
        OnOffType newVal = OnOffType.from(power);
        if (newVal != this.zone4Power) {
            this.zone4Power = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE4_POWER, newVal);
        }
    }

    public void setZone4Volume(BigDecimal volume) {
        PercentType newVal = new PercentType(volume);
        if (!newVal.equals(this.zone4Volume)) {
            this.zone4Volume = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE4_VOLUME, newVal);
            // update the volume in dB too
            State zone4VolumeDB = this.zone4VolumeDB = new QuantityType<>(
                    volume.subtract(DenonMarantzBindingConstants.DB_OFFSET), Units.DECIBEL);
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE4_VOLUME_DB, zone4VolumeDB);
        }
    }

    public void setZone4Mute(boolean mute) {
        OnOffType newVal = OnOffType.from(mute);
        if (newVal != this.zone4Mute) {
            this.zone4Mute = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE4_MUTE, newVal);
        }
    }

    public void setZone4Input(String zone4Input) {
        StringType newVal = StringType.valueOf(zone4Input);
        if (!newVal.equals(this.zone4Input)) {
            this.zone4Input = newVal;
            handler.stateChanged(DenonMarantzBindingConstants.CHANNEL_ZONE4_INPUT, newVal);
        }
    }
}
