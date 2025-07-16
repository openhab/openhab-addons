/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.awtrixlight.internal;

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.SCREEN_HEIGHT;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.SCREEN_WIDTH;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mqtt.awtrixlight.internal.app.AwtrixApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link Helper} Various helper methods used througout the binding
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class Helper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Helper.class);

    private static final Gson GSON = new GsonBuilder().create();

    public static Map<String, Object> decodeStatsJson(String statsJson) {
        Map<String, Object> stats = GSON.fromJson(statsJson, new TypeToken<Map<String, Object>>() {
        }.getType());
        return stats != null ? stats : new HashMap<String, Object>();
    }

    public static AwtrixApp decodeAppJson(String appJson) {
        @Nullable
        AwtrixApp app = GSON.fromJson(appJson, AwtrixApp.class);
        return app != null ? app : new AwtrixApp();
    }

    public static String encodeJson(Map<String, Object> params) {
        return GSON.toJson(params);
    }

    public static byte[] decodeImage(String messageJSON) {
        String cutBrackets = messageJSON.substring(1, messageJSON.length() - 1);

        String[] pixelStrings = cutBrackets.split(",");
        int[] values = Arrays.stream(pixelStrings).mapToInt(Integer::parseInt).toArray();

        int numPixels = SCREEN_HEIGHT * SCREEN_WIDTH;
        int[][] pixels = new int[SCREEN_HEIGHT][SCREEN_WIDTH];
        for (int i = 0; i < numPixels; i++) {
            pixels[i / SCREEN_WIDTH][i % SCREEN_WIDTH] = values[i];
        }

        // Resize and add gaps between pixels
        int factor = 10;
        int resizedHeight = SCREEN_HEIGHT * factor + (SCREEN_HEIGHT - 1);
        int resizedWidth = SCREEN_WIDTH * factor + (SCREEN_WIDTH - 1);
        int[][] resizedPixels = new int[resizedHeight][resizedWidth];
        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                int yOffset = y * factor + y;
                int xOffset = x * factor + x;
                for (int y2 = 0; y2 < factor; y2++) {
                    for (int x2 = 0; x2 < factor; x2++) {
                        resizedPixels[yOffset + y2][xOffset + x2] = pixels[y][x];
                    }
                }
            }
        }

        BufferedImage image = new BufferedImage(resizedWidth, resizedHeight, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < resizedHeight; y++) {
            for (int x = 0; x < resizedWidth; x++) {
                int rgb = resizedPixels[y][x];
                image.setRGB(x, y, rgb);
            }
        }

        byte[] bytes = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            bytes = baos.toByteArray();
        } catch (IOException e) {
            LOGGER.warn("Failed to decode image", e);
        }
        return bytes == null ? new byte[0] : bytes;
    }

    public static int[] leftTrim(int[] data, int length) {
        if (length < data.length) {
            int[] trimmed = new int[length];
            for (int i = data.length - length; i < data.length; i++) {
                trimmed[i - (data.length - length)] = data[i];
            }
            return trimmed;
        }
        return data;
    }
}
