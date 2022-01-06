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
package org.openhab.voice.googletts.internal.dto;

/**
 * The configuration of the synthesized audio.
 *
 * @author Wouter Born - Initial contribution
 */
public class AudioConfig {

    /**
     * Required. The format of the requested audio byte stream.
     */
    private AudioEncoding audioEncoding;

    /**
     * Optional speaking pitch, in the range [-20.0, 20.0]. 20 means increase 20 semitones from the original pitch. -20
     * means decrease 20 semitones from the original pitch.
     */
    private Double pitch;

    /**
     * The synthesis sample rate (in hertz) for this audio. Optional. If this is different from the voice's natural
     * sample rate, then the synthesizer will honor this request by converting to the desired sample rate (which might
     * result in worse audio quality), unless the specified sample rate is not supported for the encoding chosen, in
     * which case it will fail the request and return google.rpc.Code.INVALID_ARGUMENT.
     */
    private Long sampleRateHertz;

    /**
     * Optional speaking rate/speed, in the range [0.25, 4.0]. 1.0 is the normal native speed supported by the specific
     * voice. 2.0 is twice as fast, and 0.5 is half as fast. If unset(0.0), defaults to the native 1.0 speed. Any other
     * values < 0.25 or > 4.0 will return an error.
     */
    private Double speakingRate;

    /**
     * Optional volume gain (in dB) of the normal native volume supported by the specific voice, in the range [-96.0,
     * 16.0]. If unset, or set to a value of 0.0 (dB), will play at normal native signal amplitude. A value of -6.0 (dB)
     * will play at approximately half the amplitude of the normal native signal amplitude. A value of +6.0 (dB) will
     * play at approximately twice the amplitude of the normal native signal amplitude. Strongly recommend not to exceed
     * +10 (dB) as there's usually no effective increase in loudness for any value greater than that.
     */
    private Double volumeGainDb;

    public AudioConfig() {
    }

    public AudioConfig(AudioEncoding audioEncoding, Double pitch, Double speakingRate, Double volumeGainDb) {
        this(audioEncoding, pitch, null, speakingRate, volumeGainDb);
    }

    public AudioConfig(AudioEncoding audioEncoding, Double pitch, Long sampleRateHertz, Double speakingRate,
            Double volumeGainDb) {
        this.audioEncoding = audioEncoding;
        this.pitch = pitch;
        this.sampleRateHertz = sampleRateHertz;
        this.speakingRate = speakingRate;
        this.volumeGainDb = volumeGainDb;
    }

    public AudioEncoding getAudioEncoding() {
        return audioEncoding;
    }

    public Double getPitch() {
        return pitch;
    }

    public Long getSampleRateHertz() {
        return sampleRateHertz;
    }

    public Double getSpeakingRate() {
        return speakingRate;
    }

    public Double getVolumeGainDb() {
        return volumeGainDb;
    }

    public void setAudioEncoding(AudioEncoding audioEncoding) {
        this.audioEncoding = audioEncoding;
    }

    public void setPitch(Double pitch) {
        this.pitch = pitch;
    }

    public void setSampleRateHertz(Long sampleRateHertz) {
        this.sampleRateHertz = sampleRateHertz;
    }

    public void setSpeakingRate(Double speakingRate) {
        this.speakingRate = speakingRate;
    }

    public void setVolumeGainDb(Double volumeGainDb) {
        this.volumeGainDb = volumeGainDb;
    }
}
