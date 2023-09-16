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
package org.openhab.binding.mqtt.awtrixlight.internal;

import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * The {@link Helper} Removes the need for any external JSON libs
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    /**
     * convertJson will return a map with key value pairs retrieved from a simple json message.
     *
     */
    public static HashMap<String, Object> decodeJson(String messageJSON) {
        HashMap<String, Object> results = new HashMap<String, Object>();
        JsonElement parsed = JsonParser.parseString(messageJSON);
        if (parsed.isJsonObject()) {
            JsonObject root = parsed.getAsJsonObject();
            Set<?> keySet = root.keySet();
            for (Object key : keySet) {
                String keyString = (String) key;
                if (keyString != null) {
                    JsonElement jsonElement = root.get((String) key);
                    if (jsonElement.isJsonPrimitive()) {
                        JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                        if (jsonPrimitive.isString()) {
                            String s = jsonPrimitive.getAsString();
                            if (s != null) {
                                results.put(keyString, s);
                            }
                        } else if (jsonPrimitive.isBoolean()) {
                            results.put(keyString, jsonPrimitive.getAsBoolean());
                        } else if (jsonPrimitive.isNumber()) {
                            BigDecimal bd = jsonPrimitive.getAsBigDecimal();
                            if (bd != null) {
                                results.put(keyString, bd);
                            }
                        }
                    } else if (jsonElement.isJsonArray()) {
                        JsonArray jsonArray = jsonElement.getAsJsonArray();
                        if (!jsonArray.isEmpty()) {
                            if (jsonArray.get(0).isJsonPrimitive()) {
                                BigDecimal[] primitiveArray = decodeJsonPrimitiveArray(jsonArray);
                                results.put(keyString, primitiveArray);
                            } else if (jsonArray.get(0).isJsonArray()) {
                                BigDecimal[][] primitiveMultiArray = new BigDecimal[jsonArray.size()][];
                                Iterator<?> iterator = jsonArray.iterator();
                                int i = 0;
                                while (iterator.hasNext()) {
                                    JsonElement arrayElement = (JsonElement) iterator.next();
                                    if (arrayElement != null && arrayElement.isJsonArray()) {
                                        BigDecimal[] primitiveArray = decodeJsonPrimitiveArray(jsonArray);
                                        primitiveMultiArray[i] = primitiveArray;
                                    }
                                    i += 1;
                                }
                                results.put(keyString, primitiveMultiArray);
                            }

                        }
                    } else if (jsonElement.isJsonObject()) {
                        String jsonString = jsonElement.toString();
                        if (jsonString != null) {
                            results.put(keyString, decodeJson(jsonString));
                        }
                    }
                }
            }
        }
        return results;
    }

    private static BigDecimal[] decodeJsonPrimitiveArray(JsonArray jsonArray) {
        BigDecimal[] array = new BigDecimal[jsonArray.size()];
        Iterator<?> iterator = jsonArray.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            JsonElement arrayElement = (JsonElement) iterator.next();
            if (arrayElement != null && arrayElement.isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = arrayElement.getAsJsonPrimitive();
                array[i] = jsonPrimitive.getAsBigDecimal();
            }
            i += 1;
        }
        return array;
    }

    public static String encodeJson(Map<String, Object> params) {
        String json = new Gson().toJson(params);
        return json == null ? "" : json;
    }

    public static byte[] decodeImage(String messageJSON) {

        if (messageJSON != null) {
            String cutBrackets = messageJSON.substring(1, messageJSON.length() - 1);
            if (cutBrackets != null) {
                messageJSON = cutBrackets;
            }
        }

        String[] pixelStrings = messageJSON.split(",");
        int[] values = Arrays.stream(pixelStrings).mapToInt(Integer::parseInt).toArray();
        int[][] pixels = new int[SCREEN_HEIGHT][SCREEN_WIDTH];
        for (int i = 0; i < 256; i++) {
            pixels[i / 32][i % 32] = values[i];
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

        byte[] bytes = new byte[256];
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            bytes = baos.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to decode image", e);
        }
        return bytes == null ? new byte[0] : bytes;
    }
}
