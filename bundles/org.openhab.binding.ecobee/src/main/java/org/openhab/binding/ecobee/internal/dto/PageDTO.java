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
 * The {@link PageDTO} is optional and will only appear for responses which
 * can be paged. It will not appear for responses which do not contain pageable content.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class PageDTO {

    /*
     * The page retrieved or, in the case of a request parameter, the specific page requested.
     */
    public Integer page;

    /*
     * The total pages available.
     */
    public Integer totalPages;

    /*
     * The number of objects on this page.
     */
    public Integer pageSize;

    /*
     * The total number of objects available.
     */
    public Integer total;
}
