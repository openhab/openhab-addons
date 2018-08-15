/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.device.command.errorstatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.openhab.binding.pjlinkdevice.internal.device.command.ErrorCode;
import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class ErrorStatusQueryResponse extends PrefixedResponse {

    public enum ErrorStatusQueryResponseState {
        OK_UNKOWN,
        WARNING,
        ERROR;

        public String getText() {
            final HashMap<ErrorStatusQueryResponseState, String> texts = new HashMap<ErrorStatusQueryResponseState, String>();
            texts.put(OK_UNKOWN, "OK/no failure detection");
            texts.put(WARNING, "Warning");
            texts.put(ERROR, "Error");
            return texts.get(this);
        }

        public static ErrorStatusQueryResponseState parseString(String code) throws ResponseException {
            final HashMap<String, ErrorStatusQueryResponseState> codes = new HashMap<String, ErrorStatusQueryResponseState>();
            codes.put("0", OK_UNKOWN);
            codes.put("1", WARNING);
            codes.put("2", ERROR);

            ErrorStatusQueryResponseState result = codes.get(code);
            if (result == null) {
                throw new ResponseException("Cannot understand status: " + code);
            }

            return result;
        }
    }

    public enum ErrorStatusDevicePart {
        FAN,
        LAMP,
        TEMPERATURE,
        COVER_OPEN,
        FILTER,
        OTHER;

        public String getText() {
            final HashMap<ErrorStatusDevicePart, String> texts = new HashMap<ErrorStatusDevicePart, String>();
            texts.put(FAN, "Fan error");
            texts.put(LAMP, "Lamp error");
            texts.put(TEMPERATURE, "Temperature error");
            texts.put(COVER_OPEN, "Cover open error");
            texts.put(FILTER, "Filter error");
            texts.put(OTHER, "Other errors");
            return texts.get(this);
        }

        public String getCamelCaseText() {
            final HashMap<ErrorStatusDevicePart, String> texts = new HashMap<ErrorStatusDevicePart, String>();
            texts.put(FAN, "FanError");
            texts.put(LAMP, "LampError");
            texts.put(TEMPERATURE, "TemperatureError");
            texts.put(COVER_OPEN, "CoverOpenError");
            texts.put(FILTER, "FilterError");
            texts.put(OTHER, "OtherErrors");
            return texts.get(this);
        }

        public static ErrorStatusDevicePart getDevicePartByResponsePosition(int pos) {
            ErrorStatusDevicePart[] ordered = new ErrorStatusDevicePart[] { FAN, LAMP, TEMPERATURE, COVER_OPEN, FILTER,
                    OTHER };
            return ordered[pos];
        }
    }

    private Map<ErrorStatusDevicePart, ErrorStatusQueryResponseState> result = null;

    public ErrorStatusQueryResponse() {
        super("ERST=", new HashSet<ErrorCode>(
                Arrays.asList(new ErrorCode[] { ErrorCode.UNAVAILABLE_TIME, ErrorCode.DEVICE_FAILURE })));
    }

    public Map<ErrorStatusDevicePart, ErrorStatusQueryResponseState> getResult() {
        return result;
    }

    @Override
    protected void parse0(String responseWithoutPrefix) throws ResponseException {
        this.result = new HashMap<ErrorStatusDevicePart, ErrorStatusQueryResponseState>();
        for (int i = 0; i < 6; i++) {
            this.result.put(ErrorStatusDevicePart.getDevicePartByResponsePosition(i),
                    ErrorStatusQueryResponseState.parseString(responseWithoutPrefix.substring(i, i + 1)));
        }

    }

}
