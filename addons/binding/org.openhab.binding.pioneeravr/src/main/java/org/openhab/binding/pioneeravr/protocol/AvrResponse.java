/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol;

import org.openhab.binding.pioneeravr.internal.protocol.Response.ResponseType;

/**
 * Represent a response of the AVR.
 * 
 * @author Antoine Besnard
 *
 */
public interface AvrResponse {

    /**
     * Represent the type of a response.
     * 
     * @author Antoine Besnard
     *
     */
    public interface RepsonseType {

        /**
         * Return the prefix of the command of this type.
         * 
         * @return
         */
        public String getResponsePrefix();

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
        public String getParameterPattern();

        /**
         * Return true if the responseData matches with this responseType
         * 
         * @param responseData
         * @return
         */
        public boolean match(String responseData);

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

}
