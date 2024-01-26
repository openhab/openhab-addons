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
package org.openhab.binding.generacmobilelink.internal.dto;

/**
 * The {@link ApparatusInfo} represents the info of a Generac Apparatus
 *
 * @author Dan Cunningham - Initial contribution
 */
public class ApparatusInfo {
    public int apparatusId;
    public String apparatusName;
    public String productType;
    public String description;
    public Property[] properties;
    public Attribute[] attributes;

    public class Property {
        public String name;
        public String value;
        public int type;
    }

    public class Attribute {
        public String name;
        public String value;
        public int type;
    }
}
