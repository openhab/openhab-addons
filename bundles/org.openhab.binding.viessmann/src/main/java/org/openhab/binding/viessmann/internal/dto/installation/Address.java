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
package org.openhab.binding.viessmann.internal.dto.installation;

/**
 * The {@link Address} provides address data of the installation
 *
 * @author Ronny Grun - Initial contribution
 */
public class Address {
    public String street;
    public String houseNumber;
    public String zip;
    public String city;
    public Object region;
    public String country;
    public Object phoneNumber;
    public Object faxNumber;
    public Geolocation geolocation;
}
