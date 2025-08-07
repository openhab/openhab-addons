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
package org.openhab.binding.viessmann.internal.dto.installation;

import java.util.List;

/**
 * * The {@link Data} provides all data of the installation
 *
 * @author Ronny Grun - Initial contribution
 */
public class Data {
    public Integer id;
    public String description;
    public Address address;
    public List<Gateway> gateways = null;
    public String registeredAt;
    public String updatedAt;
    public String aggregatedStatus;
    public Object servicedBy;
    public Object heatingType;
    public Boolean ownedByMaintainer;
    public Boolean endUserWlanCommissioned;
    public Boolean withoutViCareUser;
    public String installationType;
    public String buildingName;
    public String buildingEmail;
    public String buildingPhone;
    public String accessLevel;
    public String ownershipType;
    public String brand;
}
