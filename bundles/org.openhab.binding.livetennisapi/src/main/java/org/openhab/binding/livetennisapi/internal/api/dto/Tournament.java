/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.livetennisapi.internal.api.dto;

/**
 * A tournament of the catalogue as returned by the Live Tennis API.
 *
 * @author Ben - Initial contribution
 */
public class Tournament {

    public String id;
    public String name;
    public String tour;
    public String surface;
    public Boolean indoor;
    public String city;
    public String country;
    public String category;
}
