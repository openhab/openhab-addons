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
package org.openhab.binding.unifiaccess.internal.dto;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.JsonAdapter;

/**
 * String enum for UniFi Access API response codes.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@JsonAdapter(ApiResponseCode.Adapter.class)
public enum ApiResponseCode {
    SUCCESS("Success"),

    // Common errors
    CODE_PARAMS_INVALID("The provided parameters are invalid."),
    CODE_SYSTEM_ERROR("An error occurred on the server's end."),
    CODE_RESOURCE_NOT_FOUND("The requested resource was not found."),
    CODE_OPERATION_FORBIDDEN("The requested operation is not allowed."),
    CODE_AUTH_FAILED("Authentication failed."),
    CODE_ACCESS_TOKEN_INVALID("The provided access token is invalid."),
    CODE_UNAUTHORIZED("You are not allowed to perform this action."),
    CODE_NOT_EXISTS("The requested item does not exist."),

    // User and account related
    CODE_USER_EMAIL_ERROR("The provided email format is invalid."),
    CODE_USER_ACCOUNT_NOT_EXIST("The requested user account does not exist."),
    CODE_USER_WORKER_NOT_EXISTS("The requested user does not exist."),
    CODE_USER_NAME_DUPLICATED("The provided name already exists."),
    CODE_USER_CSV_IMPORT_INCOMPLETE_PROP("Please provide both first name and last name."),

    // Access policy and schedule related
    CODE_ACCESS_POLICY_USER_TIMEZONE_NOT_FOUND("The requested workday schedule could not be found."),
    CODE_ACCESS_POLICY_HOLIDAY_TIMEZONE_NOT_FOUND("The requested holiday schedule could not be found."),
    CODE_ACCESS_POLICY_HOLIDAY_GROUP_NOT_FOUND("The requested holiday group could not be found."),
    CODE_ACCESS_POLICY_HOLIDAY_NOT_FOUND("The requested holiday could not be found."),
    CODE_ACCESS_POLICY_SCHEDULE_NOT_FOUND("The requested schedule could not be found."),
    CODE_ACCESS_POLICY_HOLIDAY_NAME_EXIST("The provided holiday name already exists."),
    CODE_ACCESS_POLICY_HOLIDAY_GROUP_NAME_EXIST("The provided holiday group name already exists."),
    CODE_ACCESS_POLICY_SCHEDULE_NAME_EXIST("The provided schedule name already exists."),
    CODE_ACCESS_POLICY_SCHEDULE_CAN_NOT_DELETE("The schedule could not be deleted."),
    CODE_ACCESS_POLICY_HOLIDAY_GROUP_CAN_NOT_DELETE("The holiday group could not be deleted."),

    // Credentials / NFC / PIN related
    CODE_CREDS_NFC_HAS_BIND_USER("The NFC card is already registered and assigned to another user."),
    CODE_CREDS_DISABLE_TRANSFER_UID_USER_NFC("The UniFi Identity Enterprise user's NFC card is not transferrable."),
    CODE_CREDS_NFC_READ_SESSION_NOT_FOUND("Failed to obtain the NFC read session."),
    CODE_CREDS_NFC_READ_POLL_TOKEN_EMPTY("The NFC token is empty."),
    CODE_CREDS_NFC_CARD_IS_PROVISION("The NFC card is already registered at another site."),
    CODE_CREDS_NFC_CARD_PROVISION_FAILED("Please hold the NFC card against the reader for more than 5 seconds."),
    CODE_CREDS_NFC_CARD_INVALID("The card type is not supported. Please use a UA Card."),
    CODE_CREDS_NFC_CARD_CANNOT_BE_DELETE("The NFC card could not be deleted."),
    CODE_CREDS_PIN_CODE_CREDS_ALREADY_EXIST("The PIN code already exists."),
    CODE_CREDS_PIN_CODE_CREDS_LENGTH_INVALID("The PIN code length does not meet the preset requirements."),

    // Space / Location / Device related
    CODE_SPACE_DEVICE_BOUND_LOCATION_NOT_FOUND("The device's location was not found."),
    CODE_DEVICE_DEVICE_VERSION_NOT_FOUND("The firmware version is up to date."),
    CODE_DEVICE_DEVICE_VERSION_TOO_OLD("The firmware version is too old. Please update to the latest version."),
    CODE_DEVICE_DEVICE_BUSY("The camera is currently in use."),
    CODE_DEVICE_DEVICE_NOT_FOUND("The device was not found."),
    CODE_DEVICE_DEVICE_OFFLINE("The device is currently offline."),
    CODE_DEVICE_WEBHOOK_ENDPOINT_DUPLICATED("The provided endpoint already exists."),
    CODE_DEVICE_API_NOT_SUPPORTED("The API is currently not available for this device."),

    // Others / migration related
    CODE_OTHERS_UID_ADOPTED_NOT_SUPPORTED("The API is not available after upgrading to Identity Enterprise."),

    /** Unknown/Unrecognized code */
    UNKNOWN("Unknown");

    private final String description;

    ApiResponseCode(String description) {
        this.description = description;
    }

    public String code() {
        return name();
    }

    public String description() {
        return description;
    }

    public static final class Adapter implements JsonDeserializer<ApiResponseCode>, JsonSerializer<ApiResponseCode> {
        @Override
        public ApiResponseCode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                String value = json.getAsString();
                for (ApiResponseCode c : ApiResponseCode.values()) {
                    if (c.name().equalsIgnoreCase(value)) {
                        return c;
                    }
                }
            } catch (Exception ignored) {
            }
            return ApiResponseCode.UNKNOWN;
        }

        @Override
        public JsonElement serialize(ApiResponseCode src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.name());
        }
    }
}
