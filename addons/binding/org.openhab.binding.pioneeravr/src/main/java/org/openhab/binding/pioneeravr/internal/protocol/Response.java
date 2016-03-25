/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
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
 * @author Antoine Besnard
 *
 */
public class Response implements AvrResponse {

    /**
     * List of all supported responses coming from AVR.
     * 
     * @author Antoine Besnard
     *
     */
    public enum ResponseType implements AvrResponse.RepsonseType {
        POWER_STATE("PWR", "[0-2]"),
        VOLUME_LEVEL("VOL", "[0-9]{3}"),
        MUTE_STATE("MUT", "[0-1]"),
        INPUT_SOURCE_CHANNEL("FN", "[0-9]{2}"),
        DISPLAY_INFORMATION("FL", "[0-9a-fA-F]{30}");

        private String responsePrefix;

        private String parameterPattern;

        private Pattern matchPattern;

        private ResponseType(String responsePrefix, String parameterPattern) {
            this.responsePrefix = responsePrefix;
            this.parameterPattern = parameterPattern;
            this.matchPattern = Pattern.compile(
                    responsePrefix + "(" + (StringUtils.isNotEmpty(parameterPattern) ? parameterPattern : "") + ")");
        }

        public String getResponsePrefix() {
            return responsePrefix;
        }

        public boolean hasParameter() {
            return StringUtils.isNotEmpty(parameterPattern);
        }

        public String getParameterPattern() {
            return parameterPattern;
        }

        /**
         * Return true if the responseData matches with this responseType
         * 
         * @param responseData
         * @return
         */
        public boolean match(String responseData) {
            return matchPattern.matcher(responseData).matches();
        }

        /**
         * Return the parameter value of the given responseData.
         * 
         * @param responseData
         * @return
         */
        public String parseParameter(String responseData) {
            Matcher matcher = matchPattern.matcher(responseData);
            matcher.find();
            return matcher.group(1);
        }
    }

    private ResponseType responseType;

    private String parameter;

    public Response(String responseData) throws AvrConnectionException {
        if (StringUtils.isEmpty(responseData)) {
            throw new AvrConnectionException("responseData is empty. Cannot parse the response.");
        }

        this.responseType = parseResponseType(responseData);

        if (this.responseType == null) {
            throw new AvrConnectionException("Cannot find the responseType of the responseData " + responseData);
        }

        if (this.responseType.hasParameter()) {
            this.parameter = this.responseType.parseParameter(responseData);
        }
    }

    /**
     * Return the responseType corresponding to the given responseData. Return
     * null if no ResponseType can be matched.
     * 
     * @param responseData
     * @return
     */
    private ResponseType parseResponseType(String responseData) {
        ResponseType result = null;
        for (ResponseType responseType : ResponseType.values()) {
            if (responseType.match(responseData)) {
                result = responseType;
            }
        }
        return result;
    }

    public ResponseType getResponseType() {
        return this.responseType;
    }

    public String getParameterValue() {
        return parameter;
    }

    public boolean hasParameter() {
        return responseType.hasParameter();
    }

}
