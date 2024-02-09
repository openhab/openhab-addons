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
 * The {@link Account} represents a Generac Account
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Account {
    public String userId;
    public String firstName;
    public String lastName;
    public String[] emails;
    public String[] phoneNumbers;
    public String[] groups;
    public MobileLinkSettings mobileLinkSettings;

    public class MobileLinkSettings {
        public DisplaySettings displaySettings;

        public class DisplaySettings {
            public String distanceUom;
            public String temperatureUom;
        }
    }
}
