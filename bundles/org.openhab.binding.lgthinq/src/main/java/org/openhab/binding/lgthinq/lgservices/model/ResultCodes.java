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
package org.openhab.binding.lgthinq.lgservices.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * The {@link ResultCodes}
 *
 * @author Nemer Daud - Initial contribution
 */

public enum ResultCodes {

    DEVICE_OFFLINE("Device Offline", "0106"),
    OK("Success", "0000", "0001"),
    DEVICE_NOT_RESPONSE("Device Not Response", "0111", "0103", "0104", "0106"),
    PORTAL_INTERWORKING_ERROR("Portal Internal Error", "0007"),
    LOGIN_FAILED(
            "Login/Session Failed, Duplicated or Terms Not Agreed. Try to login and correct issues direct on LG Account Portal",
            "0004", "0102", "0110", "0114"),
    BASE64_CODING_ERROR("Base64 Decoding/Encoding error", "9002", "9001"),
    NOT_SUPPORTED_CONTROL("Command/Control/Service is not supported", "0005", "0012", "8001"),
    CONTROL_ERROR("Error in device control", "0105"),
    LG_SERVER_ERROR("LG Server Error/Invalid Request", "8101", "8102", "8103", "8104", "8105", "8106", "8107", "9003",
            "9004", "9005", "9000", "8900", "0107"),
    PAYLOAD_ERROR("Malformed or Wrong Payload", "9999"),
    DUPLICATED_DATA("Duplicated Data/Alias", "0008", "0013"),
    ACCESS_DENIED("Access Denied. Verify your account/password in LG Account Portal.", "9006", "0011", "0113"),
    NOT_SUPPORTED_COUNTRY("Country not supported.", "8000"),
    NETWORK_FAILED("Timeout/Network has failed.", "9020"),
    LIMIT_EXCEEDED_ERROR("Limit has been exceeded", "0112"),
    CUSTOMER_NUMBER_EXPIRED("Customer number has been expired", "0119"),
    INVALID_CUSTOMER_DATA("Customer data is invalid or Data Doesn't exist.", "0010"),
    GENERAL_FAILURE("General Failure", "0100"),
    INVALID_CSR("Invalid CSR", "9010"),
    INVALID_PAYLOAD("Invalid Body/Payload", "0002"),
    INVALID_CUSTOMER_NUMBER("Invalid Customer Number", "0118", "120"),
    INVALID_HEAD("Invalid Request Head", "0003"),
    INVALID_PUSH_TOKEN("Invalid Push Token", "0301"),
    INVALID_REQUEST("Invalid request", "0116"),
    NOT_REGISTERED_SMART_CARE("Smart Care not registered", "0121"),
    DEVICE_MISMATCH("Device/Group mismatch or device/model doesn't exist in your account.", "0115", "0006", "0009",
            "0117", "0014"),
    NO_INFORMATION_FOUND("No information found for the arguments", "109", "108"),
    OTHER("Error processing request."),
    UNKNOWN("UNKNOWN", "");

    public static final Map<String, String> OTHER_ERROR_CODE_RESPONSE = Map.ofEntries(

            Map.entry("0109", "NO_INFORMATION_DR"), Map.entry("0108", "NO_INFORMATION_SLEEP_MODE"));

    private final String description;
    private final List<String> codes;

    public boolean containsResultCode(String code) {
        return codes.contains(code);
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCodes() {
        return codes;
    }

    ResultCodes(String description, String... codes) {
        this.codes = Arrays.asList(codes);
        this.description = description;
    }

    public static ResultCodes fromCode(String code) {
        switch (code) {
            case "0000":
            case "0001":
                return OK;
            case "0002":
                return INVALID_PAYLOAD;
            case "0003":
                return INVALID_HEAD;
            case "0004":
            case "0102":
            case "0110": // Email duplicated
            case "0114":
                return LOGIN_FAILED;
            case "0100":
                return GENERAL_FAILURE;
            case "0116":
                return INVALID_REQUEST;
            case "0108":
            case "0109":
                return NO_INFORMATION_FOUND;
            case "0115":
            case "0006":
            case "0009":
            case "0117":
            case "0014":
            case "0101":
                return DEVICE_MISMATCH;
            case "0010":
                return INVALID_CUSTOMER_DATA;
            case "0112":
                return LIMIT_EXCEEDED_ERROR;
            case "0118":
            case "0120":
                return INVALID_CUSTOMER_NUMBER;
            case "0121":
                return NOT_REGISTERED_SMART_CARE;
            case "0007":
                return PORTAL_INTERWORKING_ERROR;
            case "0008":
            case "0013":
                return DUPLICATED_DATA;
            case "0005":
            case "0012":
            case "8001":
                return NOT_SUPPORTED_CONTROL;
            case "0111":
            case "0103":
            case "0104":
            case "0106":
                return DEVICE_NOT_RESPONSE;
            case "0105":
                return CONTROL_ERROR;
            case "9001":
            case "9002":
                return BASE64_CODING_ERROR;
            case "0107":
            case "8101":
            case "8102":
            case "8203":
            case "8204":
            case "8205":
            case "8206":
            case "8207":
            case "8900":
            case "9000":
            case "9003":
            case "9004":
            case "9005":
                return LG_SERVER_ERROR;
            case "9999":
                return PAYLOAD_ERROR;
            case "9006":
            case "0011":
            case "0113":
                return ACCESS_DENIED;
            case "9010":
                return INVALID_CSR;
            case "0301":
                return INVALID_PUSH_TOKEN;
            default:
                if (OTHER_ERROR_CODE_RESPONSE.containsKey(code)) {
                    return OTHER;
                }
                return UNKNOWN;

        }
    }
}
