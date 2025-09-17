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
package org.openhab.binding.meross.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ContentAnonymizer} has static methods that can be used to remove sensitive content from Meross messages,
 * for use in logging.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ContentAnonymizer {

    private static final Map<String, String> ANONYMIZED_DEVICE_UUID_MAP = new HashMap<>();
    private static final String ANONYMIZED_DEVICE_UUID = "#############################_";
    private static int anonymizedDeviceUuidIndex = 0;

    private static final Map<String, String> ANONYMIZED_USER_ID_MAP = new HashMap<>();
    private static final String ANONYMIZED_USER_ID = "########_";
    private static int anonymizedUserIdIndex = 0;

    private static final Map<String, String> ANONYMIZED_APP_ID_MAP = new HashMap<>();
    private static final String ANONYMIZED_APP_ID = "#########_";
    private static int anonymizedAppIdIndex = 0;

    private static final Pattern APPLIANCE_TOPIC_PATTERN = Pattern
            .compile("(?<leading>\\/appliance\\/)(?<deviceUuid>\\w+)");
    private static final Pattern APP_TOPIC_PATTERN = Pattern
            .compile("(?<leading>\\/app\\/)(?<userId>\\w+)(-(?<appId>\\w+))?");
    private static final Pattern DEVICE_UUID_PATTERN = Pattern.compile("(?<leading>\"uuid\"):\"(?<deviceUuid>\\w+)\"");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("(?<leading>\"userId\"):\"(?<userId>\\w+)\"");

    private static final Pattern MAC_ADDRESS_PATTERN = Pattern
            .compile("(?<leading>\"\\w*[Mm]ac\\w*\"):\"(?<macAddress>(\\w{2}:){5}\\w{2})\"");
    private static final Pattern IP_ADDRESS_PATTERN = Pattern
            .compile("(?<leading>\"\\w*[Ii][Pp]\\w*\"):\"(?<ipAddress>(\\d{1,3}\\.){3}\\d{1,3})\"");

    public static @Nullable String anonymizeTopic(@Nullable String topic) {
        if (topic == null) {
            return topic;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = APPLIANCE_TOPIC_PATTERN.matcher(topic);
        if (matcher.find()) {
            String deviceUuid = matcher.group("deviceUuid");
            String anonymous = ANONYMIZED_DEVICE_UUID_MAP.get(deviceUuid);
            if (anonymous == null) {
                anonymous = ANONYMIZED_DEVICE_UUID + String.valueOf(++anonymizedDeviceUuidIndex);
                ANONYMIZED_DEVICE_UUID_MAP.put(deviceUuid, anonymous);
            }
            matcher.appendReplacement(result, matcher.group("leading") + anonymous);
            matcher.appendTail(result);
            return result.toString();
        }

        matcher = APP_TOPIC_PATTERN.matcher(topic);
        if (matcher.find()) {
            String userId = matcher.group("userId");
            String anonymous = ANONYMIZED_USER_ID_MAP.get(userId);
            if (anonymous == null) {
                anonymous = ANONYMIZED_USER_ID + String.valueOf(++anonymizedUserIdIndex);
                ANONYMIZED_USER_ID_MAP.put(userId, anonymous);
            }
            String appID = matcher.group("appId");
            if (appID != null) {
                String anonymousAppId = ANONYMIZED_APP_ID_MAP.get(appID);
                if (anonymousAppId == null) {
                    anonymousAppId = ANONYMIZED_APP_ID + String.valueOf(++anonymizedAppIdIndex);
                    ANONYMIZED_APP_ID_MAP.put(appID, anonymousAppId);
                }
                anonymous = anonymous + "-" + anonymousAppId;
            }
            matcher.appendReplacement(result, matcher.group("leading") + anonymous);
            matcher.appendTail(result);
            return result.toString();
        }

        return topic;
    }

    public static @Nullable String anonymizeMessage(@Nullable String message) {
        if (message == null) {
            return message;
        }

        String anonymized = anonymizeTopic(message); // Handle from keys in message

        StringBuffer result = new StringBuffer();
        Matcher matcher = DEVICE_UUID_PATTERN.matcher(anonymized);
        while (matcher.find()) {
            String leading = matcher.group("leading");
            String deviceUuid = matcher.group("deviceUuid");
            String anonymous = ANONYMIZED_DEVICE_UUID_MAP.get(deviceUuid);
            if (anonymous == null) {
                anonymous = ANONYMIZED_DEVICE_UUID + String.valueOf(++anonymizedDeviceUuidIndex);
                ANONYMIZED_DEVICE_UUID_MAP.put(deviceUuid, anonymous);
            }
            matcher.appendReplacement(result, leading + ":\"" + anonymous + "\"");
        }
        matcher.appendTail(result);
        anonymized = result.toString();

        result = new StringBuffer();
        matcher = USER_ID_PATTERN.matcher(anonymized);
        while (matcher.find()) {
            String leading = matcher.group("leading");
            String userId = matcher.group("userId");
            String anonymous = ANONYMIZED_USER_ID_MAP.get(userId);
            if (anonymous == null) {
                anonymous = ANONYMIZED_USER_ID + String.valueOf(++anonymizedUserIdIndex);
                ANONYMIZED_USER_ID_MAP.put(userId, anonymous);
            }
            matcher.appendReplacement(result, leading + ":\"" + anonymous + "\"");
        }
        matcher.appendTail(result);
        anonymized = result.toString();

        result = new StringBuffer();
        matcher = MAC_ADDRESS_PATTERN.matcher(anonymized);
        while (matcher.find()) {
            String leading = matcher.group("leading");
            matcher.appendReplacement(result, leading + ":\"xx:xx:xx:xx:xx:xx\"");
        }
        matcher.appendTail(result);
        anonymized = result.toString();

        result = new StringBuffer();
        matcher = IP_ADDRESS_PATTERN.matcher(anonymized);
        while (matcher.find()) {
            String leading = matcher.group("leading");
            matcher.appendReplacement(result, leading + ":\"xxx.xxx.xxx.xxx\"");
        }
        matcher.appendTail(result);
        anonymized = result.toString();

        return anonymized;
    }
}
