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
package org.openhab.binding.ecobee.internal.dto;

/**
 * The {@link StatusDTO} The StatusDTO object contains the processing status of
 * the request. It will contain any relevant error information should an error
 * occur. The status object is returned with every response regardless of success or failure status. It is suitable for
 * logging request failures.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class StatusDTO {

    /*
     * The status code for this status. Success is indicated by a code of 0.
     */
    public Integer code;

    /*
     * The detailed message for this status.
     */
    public String message;
}
