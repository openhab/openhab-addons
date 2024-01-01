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
package org.openhab.binding.sncf.internal.dto;

/**
 * The {@link VJDisplayInformation} class holds informations displayed
 * to traveller regarding a stop in the station
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class VJDisplayInformation {
    public String code;
    public String network;
    public String name;
    public String commercialMode;
    public String direction;
}
