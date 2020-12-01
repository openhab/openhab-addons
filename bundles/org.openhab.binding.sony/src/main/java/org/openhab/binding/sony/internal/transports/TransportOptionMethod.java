/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.transports;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A transport option that describes the type of method to use on a call
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public enum TransportOptionMethod implements TransportOption {

    /** Represents a GET method */
    GET,

    /** Represents a POST method for XML */
    POST_XML,

    /** Represents a POST method for JSON */
    POST_JSON,

    /** Represents a DELETE method */
    DELETE
}
