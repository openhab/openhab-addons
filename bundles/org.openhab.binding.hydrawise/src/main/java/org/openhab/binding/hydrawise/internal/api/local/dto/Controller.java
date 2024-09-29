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
package org.openhab.binding.hydrawise.internal.api.local.dto;

import java.util.List;

/**
 * The {@link Controller} class models a Hydrawise controller unit
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Controller {

    public String name;

    public Integer lastContact;

    public String serialNumber;

    public Integer controllerId;

    public String swVersion;

    public String hardware;

    public Boolean isBoc;

    public String address;

    public String timezone;

    public Integer deviceId;

    public Object parentDeviceId;

    public String image;

    public String description;

    public Integer customerId;

    public Double latitude;

    public Double longitude;

    public String lastContactReadable;

    public String status;

    public String statusIcon;

    public List<String> tags = null;
}
