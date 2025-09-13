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

package org.openhab.binding.smartthings.internal.dto;

/**
 * Data object for smartthings error object
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class ErrorObject {

    public String requestId;

    public class Error {
        public String code;
        public String message;

        public record Detail(String code, String target, String message) {

        }

        public Detail[] details;
    }

    public Error error;
}
