/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PagedResultSet} is a DTO generic class representing a set of paged results.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class PagedResultSet<T> {
    // {
    // "count":6,
    // "next":null,
    // "previous":null,
    // "results":[
    // ]
    // }

    public int count;
    public String next = "null";
    public String previous = "null";
    public ArrayList<T> results = new ArrayList<>();
}
