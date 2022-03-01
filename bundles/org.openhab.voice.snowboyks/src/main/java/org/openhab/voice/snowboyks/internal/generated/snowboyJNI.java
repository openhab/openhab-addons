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
 * The {@link snowboyJNI} auto generated with
 *
 * @author Miguel Álvarez - Initial contribution
 */
public class snowboyJNI {
    public final static native long new_SnowboyDetect(String jarg1, String jarg2);

    public final static native boolean SnowboyDetect_Reset(long jarg1, SnowboyDetect jarg1_);

    public final static native int SnowboyDetect_RunDetection__SWIG_0(long jarg1, SnowboyDetect jarg1_, String jarg2,
            boolean jarg3);

    public final static native int SnowboyDetect_RunDetection__SWIG_1(long jarg1, SnowboyDetect jarg1_, String jarg2);

    public final static native int SnowboyDetect_RunDetection__SWIG_2(long jarg1, SnowboyDetect jarg1_, float[] jarg2,
            int jarg3, boolean jarg4);

    public final static native int SnowboyDetect_RunDetection__SWIG_3(long jarg1, SnowboyDetect jarg1_, float[] jarg2,
            int jarg3);

    public final static native int SnowboyDetect_RunDetection__SWIG_4(long jarg1, SnowboyDetect jarg1_, short[] jarg2,
            int jarg3, boolean jarg4);

    public final static native int SnowboyDetect_RunDetection__SWIG_5(long jarg1, SnowboyDetect jarg1_, short[] jarg2,
            int jarg3);

    public final static native int SnowboyDetect_RunDetection__SWIG_6(long jarg1, SnowboyDetect jarg1_, int[] jarg2,
            int jarg3, boolean jarg4);

    public final static native int SnowboyDetect_RunDetection__SWIG_7(long jarg1, SnowboyDetect jarg1_, int[] jarg2,
            int jarg3);

    public final static native void SnowboyDetect_SetSensitivity(long jarg1, SnowboyDetect jarg1_, String jarg2);

    public final static native void SnowboyDetect_SetHighSensitivity(long jarg1, SnowboyDetect jarg1_, String jarg2);

    public final static native String SnowboyDetect_GetSensitivity(long jarg1, SnowboyDetect jarg1_);

    public final static native void SnowboyDetect_SetAudioGain(long jarg1, SnowboyDetect jarg1_, float jarg2);

    public final static native void SnowboyDetect_UpdateModel(long jarg1, SnowboyDetect jarg1_);

    public final static native int SnowboyDetect_NumHotwords(long jarg1, SnowboyDetect jarg1_);

    public final static native void SnowboyDetect_ApplyFrontend(long jarg1, SnowboyDetect jarg1_, boolean jarg2);

    public final static native int SnowboyDetect_SampleRate(long jarg1, SnowboyDetect jarg1_);

    public final static native int SnowboyDetect_NumChannels(long jarg1, SnowboyDetect jarg1_);

    public final static native int SnowboyDetect_BitsPerSample(long jarg1, SnowboyDetect jarg1_);

    public final static native void delete_SnowboyDetect(long jarg1);

    public final static native long new_SnowboyVad(String jarg1);

    public final static native boolean SnowboyVad_Reset(long jarg1, SnowboyVad jarg1_);

    public final static native int SnowboyVad_RunVad__SWIG_0(long jarg1, SnowboyVad jarg1_, String jarg2,
            boolean jarg3);

    public final static native int SnowboyVad_RunVad__SWIG_1(long jarg1, SnowboyVad jarg1_, String jarg2);

    public final static native int SnowboyVad_RunVad__SWIG_2(long jarg1, SnowboyVad jarg1_, float[] jarg2, int jarg3,
            boolean jarg4);

    public final static native int SnowboyVad_RunVad__SWIG_3(long jarg1, SnowboyVad jarg1_, float[] jarg2, int jarg3);

    public final static native int SnowboyVad_RunVad__SWIG_4(long jarg1, SnowboyVad jarg1_, short[] jarg2, int jarg3,
            boolean jarg4);

    public final static native int SnowboyVad_RunVad__SWIG_5(long jarg1, SnowboyVad jarg1_, short[] jarg2, int jarg3);

    public final static native int SnowboyVad_RunVad__SWIG_6(long jarg1, SnowboyVad jarg1_, int[] jarg2, int jarg3,
            boolean jarg4);

    public final static native int SnowboyVad_RunVad__SWIG_7(long jarg1, SnowboyVad jarg1_, int[] jarg2, int jarg3);

    public final static native void SnowboyVad_SetAudioGain(long jarg1, SnowboyVad jarg1_, float jarg2);

    public final static native void SnowboyVad_ApplyFrontend(long jarg1, SnowboyVad jarg1_, boolean jarg2);

    public final static native int SnowboyVad_SampleRate(long jarg1, SnowboyVad jarg1_);

    public final static native int SnowboyVad_NumChannels(long jarg1, SnowboyVad jarg1_);

    public final static native int SnowboyVad_BitsPerSample(long jarg1, SnowboyVad jarg1_);

    public final static native void delete_SnowboyVad(long jarg1);
}
