/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.pioneeravr.internal.protocol.avr;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pioneeravr.internal.protocol.Response.ResponseType;

/**
 * Represent a response of the AVR.
 *
 * @author Antoine Besnard - Initial contribution
 */
public interface AvrResponse {

    /**
     * Represent the type of a response.
     */
    public interface AvrResponseType {

        /**
         * Return the prefix of the command of this type.
         *
         * @param zone
         * @return
         */
        public String getResponsePrefix(int zone);

        /**
         * Return true if the responses of this type has to have a parameter.
         *
         * @return
         */
        public boolean hasParameter();

        /**
         * Return the parameter pattern (RegEx) of the response.
         *
         * @return
         */
        public @Nullable String getParameterPattern();

        /**
         * Return the zone number if the responseData matches a zone of this responseType.
         *
         * If any zone matches, return null.
         *
         * @param responseData
         * @return
         */
        public Integer match(String responseData);

        /**
         * Return the parameter value of the given responseData.
         *
         * @param responseData
         * @return
         */
        public String parseParameter(String responseData);
    }

    /**
     * Return the response type of this response
     *
     * @return
     */
    public ResponseType getResponseType();

    /**
     * Return the parameter of this response or null if the resposne has no parameter.
     *
     * @return
     */
    public String getParameterValue();

    /**
     * Return true if this response has a parameter.
     *
     * @return
     */
    public boolean hasParameter();

    /**
     * Return the zone number which is concerned by this response.
     *
     * @return
     */
    public Integer getZone();
}
