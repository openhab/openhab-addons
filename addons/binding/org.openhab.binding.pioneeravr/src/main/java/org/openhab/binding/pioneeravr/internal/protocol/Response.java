/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.pioneeravr.protocol.AvrConnectionException;
import org.openhab.binding.pioneeravr.protocol.AvrResponse;

/**
 * Represent an AVR response.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class Response implements AvrResponse {

    /**
     * List of all supported responses coming from AVR.
     */
    public enum ResponseType implements AvrResponse.AvrResponseType {
        POWER_STATE("[0-2]", "PWR", "APR", "BPR", "ZEP"),
        VOLUME_LEVEL("[0-9]{2,3}", "VOL", "ZV", "YV", "HZV"),
        MUTE_STATE("[0-1]", "MUT", "Z2MUT", "Z3MUT", "HZM"),
        INPUT_SOURCE_CHANNEL("[0-9]{2}", "FN", "Z2F", "Z3F", "ZEA"),
        DISPLAY_INFORMATION("[0-9a-fA-F]{30}", "FL");

        private String[] responsePrefixZone;

        private String parameterPattern;

        private Pattern[] matchPatternZone;

        private ResponseType(String parameterPattern, String... responsePrefixZone) {
            this.responsePrefixZone = responsePrefixZone;
            this.parameterPattern = parameterPattern;

            matchPatternZone = new Pattern[responsePrefixZone.length];

            for (int zoneIndex = 0; zoneIndex < responsePrefixZone.length; zoneIndex++) {
                String responsePrefix = responsePrefixZone[zoneIndex];
                matchPatternZone[zoneIndex] = Pattern.compile(responsePrefix + "("
                        + (StringUtils.isNotEmpty(parameterPattern) ? parameterPattern : "") + ")");
            }
        }

        @Override
        public String getResponsePrefix(int zone) {
            return responsePrefixZone[zone - 1];
        }

        @Override
        public boolean hasParameter() {
            return StringUtils.isNotEmpty(parameterPattern);
        }

        @Override
        public String getParameterPattern() {
            return parameterPattern;
        }

        @Override
        public Integer match(String responseData) {
            Integer zone = null;
            // Check the response data against all zone prefixes.
            for (int zoneIndex = 0; zoneIndex < matchPatternZone.length; zoneIndex++) {
                if (matchPatternZone[zoneIndex].matcher(responseData).matches()) {
                    zone = zoneIndex + 1;
                    break;
                }
            }

            return zone;
        }

        /**
         * Return the parameter value of the given responseData.
         *
         * @param responseData
         * @return
         */
        @Override
        public String parseParameter(String responseData) {
            String result = null;
            // Check the response data against all zone prefixes.
            for (int zoneIndex = 0; zoneIndex < matchPatternZone.length; zoneIndex++) {
                Matcher matcher = matchPatternZone[zoneIndex].matcher(responseData);
                if (matcher.find()) {
                    result = matcher.group(1);
                    break;
                }
            }
            return result;
        }
    }

    private ResponseType responseType;

    private Integer zone;

    private String parameter;

    public Response(String responseData) throws AvrConnectionException {
        if (StringUtils.isEmpty(responseData)) {
            throw new AvrConnectionException("responseData is empty. Cannot parse the response.");
        }

        parseResponseType(responseData);

        if (this.responseType == null) {
            throw new AvrConnectionException("Cannot find the responseType of the responseData " + responseData);
        }

        if (this.responseType.hasParameter()) {
            this.parameter = this.responseType.parseParameter(responseData);
        }
    }

    /**
     * Parse the given response data and fill the
     *
     * @param responseData
     * @return
     */
    private void parseResponseType(String responseData) {
        for (ResponseType responseType : ResponseType.values()) {
            zone = responseType.match(responseData);
            if (zone != null) {
                this.responseType = responseType;
                break;
            }
        }
    }

    @Override
    public ResponseType getResponseType() {
        return this.responseType;
    }

    @Override
    public String getParameterValue() {
        return parameter;
    }

    @Override
    public boolean hasParameter() {
        return responseType.hasParameter();
    }

    @Override
    public Integer getZone() {
        return this.zone;
    }

}
