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
package org.openhab.voice.whisperstt.internal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Plain Java inference of the Silero VAD model (https://github.com/snakers4/silero-vad) for 16 kHz audio.
 * Returns the speech probability of each 512 sample frame. Weights are exported from the official v6.2 ONNX model,
 * which uses the v5 architecture.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class SileroVAD {
    public static final int FRAME_SIZE = 512;
    private static final int CONTEXT_SIZE = 64;
    private static final int PAD = 64;
    private static final int WINDOW = 256;
    private static final int HOP = 128;
    private static final int BINS = 129;
    private static final int HIDDEN = 128;
    private static final String WEIGHTS = "/silero_vad_16k.bin";

    private static @Nullable Weights sharedWeights;

    private final Weights w;
    private final float[] input = new float[CONTEXT_SIZE + FRAME_SIZE];
    private final float[] padded = new float[CONTEXT_SIZE + FRAME_SIZE + PAD];
    private final float[] h = new float[HIDDEN];
    private final float[] c = new float[HIDDEN];
    private final float[] gates = new float[4 * HIDDEN];

    public SileroVAD() throws IOException {
        this.w = loadWeights();
    }

    /**
     * @param frame {@link #FRAME_SIZE} 16 bit samples
     * @return speech probability between 0 and 1
     */
    public float process(short[] frame) {
        System.arraycopy(input, FRAME_SIZE, input, 0, CONTEXT_SIZE);
        for (int i = 0; i < FRAME_SIZE; i++) {
            input[CONTEXT_SIZE + i] = frame[i] / 32768f;
        }
        // reflect pad the right side
        System.arraycopy(input, 0, padded, 0, input.length);
        for (int i = 0; i < PAD; i++) {
            padded[input.length + i] = input[input.length - 2 - i];
        }

        int frames = (padded.length - WINDOW) / HOP + 1;
        float[][] magnitude = new float[BINS][frames];
        for (int t = 0; t < frames; t++) {
            int offset = t * HOP;
            for (int bin = 0; bin < BINS; bin++) {
                float re = dot(w.stftBasis, bin * WINDOW, padded, offset, WINDOW);
                float im = dot(w.stftBasis, (bin + BINS) * WINDOW, padded, offset, WINDOW);
                magnitude[bin][t] = (float) Math.sqrt(re * re + im * im);
            }
        }

        float[][] x = magnitude;
        for (int i = 0; i < 4; i++) {
            x = convRelu(x, w.convWeights[i], w.convBias[i], i == 1 || i == 2 ? 2 : 1);
        }

        float[] features = new float[HIDDEN];
        for (int i = 0; i < HIDDEN; i++) {
            features[i] = x[i][0];
        }
        for (int g = 0; g < 4 * HIDDEN; g++) {
            gates[g] = w.lstmBias[g] + dot(w.lstmInput, g * HIDDEN, features, 0, HIDDEN)
                    + dot(w.lstmHidden, g * HIDDEN, h, 0, HIDDEN);
        }
        // PyTorch gate order: input, forget, cell, output
        for (int i = 0; i < HIDDEN; i++) {
            c[i] = sigmoid(gates[HIDDEN + i]) * c[i] + sigmoid(gates[i]) * (float) Math.tanh(gates[2 * HIDDEN + i]);
            h[i] = sigmoid(gates[3 * HIDDEN + i]) * (float) Math.tanh(c[i]);
        }

        float out = w.outBias;
        for (int i = 0; i < HIDDEN; i++) {
            out += w.outWeight[i] * Math.max(h[i], 0f);
        }
        return sigmoid(out);
    }

    /**
     * Kernel 3, padding 1.
     */
    private static float[][] convRelu(float[][] x, float[] weight, float[] bias, int stride) {
        int inChannels = x.length;
        int inLength = x[0].length;
        int outChannels = bias.length;
        int outLength = (inLength - 1) / stride + 1;
        float[][] y = new float[outChannels][outLength];
        for (int o = 0; o < outChannels; o++) {
            for (int t = 0; t < outLength; t++) {
                float sum = bias[o];
                for (int i = 0; i < inChannels; i++) {
                    int base = (o * inChannels + i) * 3;
                    for (int k = 0; k < 3; k++) {
                        int pos = t * stride + k - 1;
                        if (pos >= 0 && pos < inLength) {
                            sum += weight[base + k] * x[i][pos];
                        }
                    }
                }
                y[o][t] = Math.max(sum, 0f);
            }
        }
        return y;
    }

    private static float dot(float[] a, int aOffset, float[] b, int bOffset, int length) {
        float sum = 0f;
        for (int i = 0; i < length; i++) {
            sum += a[aOffset + i] * b[bOffset + i];
        }
        return sum;
    }

    private static float sigmoid(float v) {
        return 1f / (1f + (float) Math.exp(-v));
    }

    private static synchronized Weights loadWeights() throws IOException {
        Weights weights = sharedWeights;
        if (weights == null) {
            try (InputStream in = SileroVAD.class.getResourceAsStream(WEIGHTS)) {
                if (in == null) {
                    throw new IOException("Missing " + WEIGHTS);
                }
                weights = new Weights(
                        ByteBuffer.wrap(in.readAllBytes()).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer());
            }
            sharedWeights = weights;
        }
        return weights;
    }

    private static class Weights {
        final float[] stftBasis;
        final float[][] convWeights = new float[4][];
        final float[][] convBias = new float[4][];
        final float[] lstmInput;
        final float[] lstmHidden;
        final float[] lstmBias;
        final float[] outWeight;
        final float outBias;

        Weights(FloatBuffer buffer) throws IOException {
            stftBasis = read(buffer, 2 * BINS * WINDOW);
            int[][] shapes = { { 128, 129 }, { 64, 128 }, { 64, 64 }, { 128, 64 } };
            for (int i = 0; i < 4; i++) {
                convWeights[i] = read(buffer, shapes[i][0] * shapes[i][1] * 3);
                convBias[i] = read(buffer, shapes[i][0]);
            }
            lstmInput = read(buffer, 4 * HIDDEN * HIDDEN);
            lstmHidden = read(buffer, 4 * HIDDEN * HIDDEN);
            lstmBias = read(buffer, 4 * HIDDEN);
            outWeight = read(buffer, HIDDEN);
            outBias = read(buffer, 1)[0];
            if (buffer.hasRemaining()) {
                throw new IOException("Unexpected Silero weights size");
            }
        }

        private static float[] read(FloatBuffer buffer, int size) throws IOException {
            if (buffer.remaining() < size) {
                throw new IOException("Unexpected Silero weights size");
            }
            float[] values = new float[size];
            buffer.get(values);
            return values;
        }
    }
}
