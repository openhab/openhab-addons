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
package org.openhab.binding.homeconnectdirect.internal.common.utils;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HexFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnectdirect.internal.common.DoubleKeyMap;
import org.openhab.binding.homeconnectdirect.internal.common.json.adapter.DoubleKeyMapAdapter;
import org.openhab.binding.homeconnectdirect.internal.common.json.adapter.OffsetDateTimeAdapter;
import org.openhab.binding.homeconnectdirect.internal.configuration.HomeConnectDirectConfiguration;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.serializer.ResourceAdapter;
import org.openhab.core.OpenHAB;
import org.openhab.core.util.StringUtils;
import org.osgi.service.cm.ConfigurationAdmin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

/**
 *
 * Home Connect Direct configuration utilities.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ConfigurationUtils {

    public static final String WS_DEVICE_ID_PREFIX = "cafebabe0000";
    public static final String WS_DEVICE_ID_PATH = OpenHAB.getUserDataFolder() + File.separator + BINDING_ID
            + File.separator + "device.id";

    private static final String DEVICE_ID_HASH_ALGORITHM = "SHA-256";
    private static final int DEVICE_ID_HASH_LENGTH = 12;
    private static final HexFormat HEX_FORMATTER = HexFormat.of();

    private ConfigurationUtils() {
        // Utility class
    }

    /**
     * Get the binding configuration.
     *
     * @param configurationAdmin the configuration admin service
     * @return binding configuration
     */
    public static HomeConnectDirectConfiguration getConfiguration(ConfigurationAdmin configurationAdmin) {
        var configuration = new HomeConnectDirectConfiguration();
        try {
            var config = configurationAdmin.getConfiguration(CONFIGURATION_PID);
            var properties = config.getProperties();

            if (properties != null) {
                configuration.loginEnabled = Boolean
                        .parseBoolean(String.valueOf(properties.get(CONFIGURATION_LOGIN_ENABLED)));
                var passwordObject = properties.get(CONFIGURATION_LOGIN_PASSWORD);
                if (passwordObject instanceof String password && !password.isBlank()) {
                    configuration.loginPassword = password;
                }
                var queueSizeObject = properties.get(CONFIGURATION_MESSAGE_QUEUE_SIZE);
                if (queueSizeObject != null) {
                    int queueSize = Integer.parseInt(String.valueOf(queueSizeObject));
                    configuration.messageQueueSize = Math.min(86400, Math.max(10, queueSize));
                }
            }
        } catch (IOException | NumberFormatException ignored) {
            // could not get configuration, use defaults
        }

        return configuration;
    }

    /**
     * Create a Gson instance with custom serializers and deserializers.
     *
     * @return Gson instance
     */
    public static Gson createGson() {
        return new GsonBuilder().registerTypeAdapter(Resource.class, new ResourceAdapter())
                .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter())
                .registerTypeAdapter(DoubleKeyMap.class, new DoubleKeyMapAdapter())
                .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();
    }

    public static String getDeviceId() {
        var path = Path.of(WS_DEVICE_ID_PATH);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                var deviceId = generateDeviceId();
                Files.writeString(path, deviceId, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                return deviceId;
            } else {
                return Files.readString(path).trim();
            }
        } catch (IOException e) {
            // use fallback device id
            return generateDeviceId();
        }
    }

    private static String generateDeviceId() {
        try {
            var hostname = InetAddress.getLocalHost().getHostName().trim();
            var digest = MessageDigest.getInstance(DEVICE_ID_HASH_ALGORITHM);
            byte[] fullHash = digest.digest(hostname.getBytes(StandardCharsets.UTF_8));

            byte[] truncatedHash = Arrays.copyOf(fullHash, DEVICE_ID_HASH_LENGTH);

            return HEX_FORMATTER.formatHex(truncatedHash);
        } catch (SecurityException | UnknownHostException | NoSuchAlgorithmException e) {
            return WS_DEVICE_ID_PREFIX + StringUtils
                    .getRandomHex(2 * DEVICE_ID_HASH_LENGTH - WS_DEVICE_ID_PREFIX.length()).toLowerCase(LOCALE);
        }
    }
}
