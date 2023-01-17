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
package org.openhab.binding.nest.internal.sdm.dto;

/**
 * An error response of the SDM API.
 *
 * @author Wouter Born - Initial contribution
 *
 * @see https://developers.google.com/nest/device-access/reference/errors/api
 */
public class SDMError {

    public static class SDMErrorDetails {
        public int code;
        public String message;
        public String status;
    }

    public SDMErrorDetails error;
}
