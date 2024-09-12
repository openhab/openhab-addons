/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * The {@link AbstractResponseDTO} represents the common objects included in
 * all responses.
 *
 * @author Mark Hilbush - Initial contribution
 */
public abstract class AbstractResponseDTO {

    /*
     * The Page object is optional and will only appear for responses which
     * can be paged. It will not appear for responses which do not contain pageable content.
     */
    public PageDTO page;

    /*
     * The Status object contains the response code for the request. It will also contain
     * an appropriate message when an error occurs. The status is always returned from all
     * GET and POST calls. A non-zero code means that an error occurred. Refer to the Response
     * Codes section for details of each error which may be returned.
     */
    public StatusDTO status;
}
