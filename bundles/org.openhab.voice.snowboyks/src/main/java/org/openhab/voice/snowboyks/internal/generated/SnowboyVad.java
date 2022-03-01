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

package org.openhab.voice.snowboyks.internal.generated;

/**
 * The {@link SnowboyVad} auto generated with
 *
 * @author Miguel Álvarez - Initial contribution
 */
public class SnowboyVad {
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected SnowboyVad(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(SnowboyVad obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    @SuppressWarnings("deprecation")
    protected void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (swigCPtr != 0) {
            if (swigCMemOwn) {
                swigCMemOwn = false;
                snowboyJNI.delete_SnowboyVad(swigCPtr);
            }
            swigCPtr = 0;
        }
    }

    public SnowboyVad(String resource_filename) {
        this(snowboyJNI.new_SnowboyVad(resource_filename), true);
    }

    public boolean Reset() {
        return snowboyJNI.SnowboyVad_Reset(swigCPtr, this);
    }

    public int RunVad(String data, boolean is_end) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_0(swigCPtr, this, data, is_end);
    }

    public int RunVad(String data) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_1(swigCPtr, this, data);
    }

    public int RunVad(float[] data, int array_length, boolean is_end) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_2(swigCPtr, this, data, array_length, is_end);
    }

    public int RunVad(float[] data, int array_length) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_3(swigCPtr, this, data, array_length);
    }

    public int RunVad(short[] data, int array_length, boolean is_end) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_4(swigCPtr, this, data, array_length, is_end);
    }

    public int RunVad(short[] data, int array_length) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_5(swigCPtr, this, data, array_length);
    }

    public int RunVad(int[] data, int array_length, boolean is_end) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_6(swigCPtr, this, data, array_length, is_end);
    }

    public int RunVad(int[] data, int array_length) {
        return snowboyJNI.SnowboyVad_RunVad__SWIG_7(swigCPtr, this, data, array_length);
    }

    public void SetAudioGain(float audio_gain) {
        snowboyJNI.SnowboyVad_SetAudioGain(swigCPtr, this, audio_gain);
    }

    public void ApplyFrontend(boolean apply_frontend) {
        snowboyJNI.SnowboyVad_ApplyFrontend(swigCPtr, this, apply_frontend);
    }

    public int SampleRate() {
        return snowboyJNI.SnowboyVad_SampleRate(swigCPtr, this);
    }

    public int NumChannels() {
        return snowboyJNI.SnowboyVad_NumChannels(swigCPtr, this);
    }

    public int BitsPerSample() {
        return snowboyJNI.SnowboyVad_BitsPerSample(swigCPtr, this);
    }
}
