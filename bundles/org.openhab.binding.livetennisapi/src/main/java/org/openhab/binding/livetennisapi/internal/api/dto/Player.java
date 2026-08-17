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
 * A player (or doubles team) as returned by the Live Tennis API.
 *
 * @author Ben - Initial contribution
 */
public class Player {

    public Long id;
    public String name;
    public String country;
    public Integer ranking;
    public Integer rankingPoints;
    public String rankingMovement;
    public Boolean isDoublesTeam;
}
