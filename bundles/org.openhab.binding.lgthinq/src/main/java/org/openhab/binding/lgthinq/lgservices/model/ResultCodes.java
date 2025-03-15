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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Enumeration representing various result codes that can be returned from the LG ThinQ service.
 * Each result code is associated with a description and one or more error codes.
 *
 * <p>
 * These result codes provide information about the success or failure of a request to the LG API.
 * Some result codes indicate specific errors, while others represent success or other types of issues.
 * </p>
 *
 * <p>
 * Usage Example:
 *
 * <pre>
 * ResultCodes result = ResultCodes.fromCode("0000");
 * System.out.println(result.getDescription()); // Outputs: "Success"
 * </pre>
 * </p>
 *
 * @author Nemer Daud - Initial contribution
 * @version 1.0
 */
@NonNullByDefault
public enum ResultCodes {

    /**
     * Indicates that the device is offline.
     */
    DEVICE_OFFLINE("Device Offline", "0106"),

    /**
     * Indicates that the request was successful.
     */
    OK("Success", "0000", "0001"),

    /**
     * Indicates that the device did not respond.
     */
    DEVICE_NOT_RESPONSE("Device Not Response", "0111", "0103", "0104", "0106"),

    /**
     * Indicates a portal internal error.
     */
    PORTAL_INTERWORKING_ERROR("Portal Internal Error", "0007"),

    /**
     * Indicates that the login attempt failed due to duplication.
     */
    LOGIN_DUPLICATED("Login Duplicated", "0004"),

    /**
     * Indicates that an update to the agreement terms is required in the LG app.
     */
    UPDATE_TERMS_NEEDED("Update Agreement Terms in LG App", "0110"),

    /**
     * Indicates a failed login or session failure.
     */
    LOGIN_FAILED("Login/Session Failed. Try to login and correct issues direct on LG Account Portal", "0102", "0114"),

    /**
     * Indicates a base64 decoding/encoding error.
     */
    BASE64_CODING_ERROR("Base64 Decoding/Encoding error", "9002", "9001"),

    /**
     * Indicates that the command or service is not supported.
     */
    NOT_SUPPORTED_CONTROL("Command/Control/Service is not supported", "0005", "0012", "8001"),

    /**
     * Indicates an error while controlling the device.
     */
    CONTROL_ERROR("Error in device control", "0105"),

    /**
     * Indicates an LG server error or an invalid request.
     */
    LG_SERVER_ERROR("LG Server Error/Invalid Request", "8101", "8102", "8103", "8104", "8105", "8106", "8107", "9003",
            "9004", "9005", "9000", "8900", "0107"),

    /**
     * Indicates a malformed or wrong payload in the request.
     */
    PAYLOAD_ERROR("Malformed or Wrong Payload", "9999"),

    /**
     * Indicates duplicated data or alias.
     */
    DUPLICATED_DATA("Duplicated Data/Alias", "0008", "0013"),

    /**
     * Indicates access denial. Suggests verifying the account and password in the LG Account Portal.
     */
    ACCESS_DENIED("Access Denied. Verify your account/password in LG Account Portal.", "9006", "0011", "0113"),

    /**
     * Indicates that the country is not supported.
     */
    NOT_SUPPORTED_COUNTRY("Country not supported.", "8000"),

    /**
     * Indicates a network failure or timeout.
     */
    NETWORK_FAILED("Timeout/Network has failed.", "9020"),

    /**
     * Indicates that the limit has been exceeded.
     */
    LIMIT_EXCEEDED_ERROR("Limit has been exceeded", "0112"),

    /**
     * Indicates that the customer number has expired.
     */
    CUSTOMER_NUMBER_EXPIRED("Customer number has been expired", "0119"),

    /**
     * Indicates that the customer data is invalid or does not exist.
     */
    INVALID_CUSTOMER_DATA("Customer data is invalid or Data Doesn't exist.", "0010"),

    /**
     * A general failure error.
     */
    GENERAL_FAILURE("General Failure", "0100"),

    /**
     * Indicates an invalid CSR (Certificate Signing Request).
     */
    INVALID_CSR("Invalid CSR", "9010"),

    /**
     * Indicates an invalid body or payload in the request.
     */
    INVALID_PAYLOAD("Invalid Body/Payload", "0002"),

    /**
     * Indicates an invalid customer number.
     */
    INVALID_CUSTOMER_NUMBER("Invalid Customer Number", "0118", "120"),

    /**
     * Indicates an invalid request header.
     */
    INVALID_HEAD("Invalid Request Head", "0003"),

    /**
     * Indicates an invalid push token.
     */
    INVALID_PUSH_TOKEN("Invalid Push Token", "0301"),

    /**
     * Indicates an invalid request.
     */
    INVALID_REQUEST("Invalid request", "0116"),

    /**
     * Indicates that Smart Care is not registered.
     */
    NOT_REGISTERED_SMART_CARE("Smart Care not registered", "0121"),

    /**
     * Indicates a device or group mismatch, or a device/model does not exist in the account.
     */
    DEVICE_MISMATCH("Device/Group mismatch or device/model doesn't exist in your account.", "0115", "0006", "0009",
            "0117", "0014"),

    /**
     * Indicates that no information was found for the given arguments.
     */
    NO_INFORMATION_FOUND("No information found for the arguments", "109", "108"),

    /**
     * A generic error when processing a request.
     */
    OTHER("Error processing request."),

    /**
     * Represents an unknown result code.
     */
    UNKNOWN("UNKNOWN", "");

    /**
     * A map of other error codes that do not directly map to predefined result codes.
     */
    public static final Map<String, String> OTHER_ERROR_CODE_RESPONSE = Map
            .ofEntries(Map.entry("0109", "NO_INFORMATION_DR"), Map.entry("0108", "NO_INFORMATION_SLEEP_MODE"));
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final String description;
    private final List<String> codes;

    /**
     * Constructor for the enum. Initializes the description and associated error codes.
     *
     * @param description the description of the result code
     * @param codes the error codes associated with the result code
     */
    ResultCodes(String description, String... codes) {
        this.codes = Arrays.asList(codes);
        this.description = description;
    }

    /**
     * Gets the reason response for a given JSON response string.
     *
     * @param jsonResponse the JSON response string to process
     * @return a formatted string containing the result code and its corresponding description
     */
    public static String getReasonResponse(String jsonResponse) {
        try {
            JsonNode devicesResult = MAPPER.readValue(jsonResponse, new TypeReference<>() {
            });
            String resultCode = devicesResult.path("resultCode").asText();
            return String.format("%s - %s", resultCode, fromCode(resultCode).description);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * Returns the ResultCodes enum corresponding to the given error code.
     *
     * @param code the error code to map to a ResultCodes enum
     * @return the ResultCodes enum corresponding to the provided code
     */
    public static ResultCodes fromCode(String code) {
        return switch (code) {
            case "0000", "0001" -> OK;
            case "0002" -> INVALID_PAYLOAD;
            case "0003" -> INVALID_HEAD;
            case "0110" -> UPDATE_TERMS_NEEDED;
            case "0004" -> LOGIN_DUPLICATED;
            case "0102", "0114" -> LOGIN_FAILED;
            case "0100" -> GENERAL_FAILURE;
            case "0116" -> INVALID_REQUEST;
            case "0108", "0109" -> NO_INFORMATION_FOUND;
            case "0115", "0006", "0009", "0117", "0014", "0101" -> DEVICE_MISMATCH;
            case "0010" -> INVALID_CUSTOMER_DATA;
            case "0112" -> LIMIT_EXCEEDED_ERROR;
            case "0118", "0120" -> INVALID_CUSTOMER_NUMBER;
            case "0121" -> NOT_REGISTERED_SMART_CARE;
            case "0007" -> PORTAL_INTERWORKING_ERROR;
            case "0008", "0013" -> DUPLICATED_DATA;
            case "0005", "0012", "8001" -> NOT_SUPPORTED_CONTROL;
            case "0111", "0103", "0104", "0106" -> DEVICE_NOT_RESPONSE;
            case "0105" -> CONTROL_ERROR;
            case "9001", "9002" -> BASE64_CODING_ERROR;
            case "0107", "8101", "8102", "8203", "8204", "8205", "8206", "8207", "8900", "9000", "9003", "9004",
                    "9005" ->
                LG_SERVER_ERROR;
            case "9999" -> PAYLOAD_ERROR;
            case "9006", "0011", "0113" -> ACCESS_DENIED;
            case "9010" -> INVALID_CSR;
            case "0301" -> INVALID_PUSH_TOKEN;
            default -> {
                if (OTHER_ERROR_CODE_RESPONSE.containsKey(code)) {
                    yield OTHER;
                }
                yield UNKNOWN;
            }
        };
    }

    /**
     * Checks if the result code contains the specified code.
     *
     * @param code the error code to check
     * @return true if the result code contains the specified code, false otherwise
     */
    public boolean containsResultCode(String code) {
        return codes.contains(code);
    }

    /**
     * Gets the description of the result code.
     *
     * @return the description of the result code
     */
    public String getDescription() {
        return description;
    }
}
