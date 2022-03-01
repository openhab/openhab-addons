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
 * The {@link SnowboyDetect} auto generated with
 *
 * @author Miguel Álvarez - Initial contribution
 */
public class SnowboyDetect {
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected SnowboyDetect(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(SnowboyDetect obj) {
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
                snowboyJNI.delete_SnowboyDetect(swigCPtr);
            }
            swigCPtr = 0;
        }
    }

    public SnowboyDetect(String resource_filename, String model_str) {
        this(snowboyJNI.new_SnowboyDetect(resource_filename, model_str), true);
    }

    public boolean Reset() {
        return snowboyJNI.SnowboyDetect_Reset(swigCPtr, this);
    }

    public int RunDetection(String data, boolean is_end) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_0(swigCPtr, this, data, is_end);
    }

    public int RunDetection(String data) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_1(swigCPtr, this, data);
    }

    public int RunDetection(float[] data, int array_length, boolean is_end) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_2(swigCPtr, this, data, array_length, is_end);
    }

    public int RunDetection(float[] data, int array_length) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_3(swigCPtr, this, data, array_length);
    }

    public int RunDetection(short[] data, int array_length, boolean is_end) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_4(swigCPtr, this, data, array_length, is_end);
    }

    public int RunDetection(short[] data, int array_length) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_5(swigCPtr, this, data, array_length);
    }

    public int RunDetection(int[] data, int array_length, boolean is_end) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_6(swigCPtr, this, data, array_length, is_end);
    }

    public int RunDetection(int[] data, int array_length) {
        return snowboyJNI.SnowboyDetect_RunDetection__SWIG_7(swigCPtr, this, data, array_length);
    }

    public void SetSensitivity(String sensitivity_str) {
        snowboyJNI.SnowboyDetect_SetSensitivity(swigCPtr, this, sensitivity_str);
    }

    public void SetHighSensitivity(String high_sensitivity_str) {
        snowboyJNI.SnowboyDetect_SetHighSensitivity(swigCPtr, this, high_sensitivity_str);
    }

    public String GetSensitivity() {
        return snowboyJNI.SnowboyDetect_GetSensitivity(swigCPtr, this);
    }

    public void SetAudioGain(float audio_gain) {
        snowboyJNI.SnowboyDetect_SetAudioGain(swigCPtr, this, audio_gain);
    }

    public void UpdateModel() {
        snowboyJNI.SnowboyDetect_UpdateModel(swigCPtr, this);
    }

    public int NumHotwords() {
        return snowboyJNI.SnowboyDetect_NumHotwords(swigCPtr, this);
    }

    public void ApplyFrontend(boolean apply_frontend) {
        snowboyJNI.SnowboyDetect_ApplyFrontend(swigCPtr, this, apply_frontend);
    }

    public int SampleRate() {
        return snowboyJNI.SnowboyDetect_SampleRate(swigCPtr, this);
    }

    public int NumChannels() {
        return snowboyJNI.SnowboyDetect_NumChannels(swigCPtr, this);
    }

    public int BitsPerSample() {
        return snowboyJNI.SnowboyDetect_BitsPerSample(swigCPtr, this);
    }
}
