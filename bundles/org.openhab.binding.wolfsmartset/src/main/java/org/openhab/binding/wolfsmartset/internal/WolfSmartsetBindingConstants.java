/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wolfsmartset.internal;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.wolfsmartset.internal.dto.GetSystemListDTO;
import org.openhab.binding.wolfsmartset.internal.dto.SubMenuEntryWithMenuItemTabView;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WolfSmartsetBindingConstants} class defines common constants that are
 * used across the whole binding.
 *
 * @author Bo Biene - Initial contribution
 */
@NonNullByDefault
public class WolfSmartsetBindingConstants {

    public static final String BINDING_ID = "wolfsmartset";

    // Account bridge
    public static final String THING_TYPE_ACCOUNT = "account";
    public static final ThingTypeUID UID_ACCOUNT_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_ACCOUNT);
    public static final Set<ThingTypeUID> SUPPORTED_ACCOUNT_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_ACCOUNT_BRIDGE).collect(Collectors.toSet()));

    // System bridge
    public static final String THING_TYPE_SYSTEM = "system";
    public static final ThingTypeUID UID_SYSTEM_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_SYSTEM);
    public static final Set<ThingTypeUID> SUPPORTED_SYSTEM_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_SYSTEM_BRIDGE).collect(Collectors.toSet()));

    // unit thing
    public static final String THING_TYPE_UNIT = "unit";
    public static final ThingTypeUID UID_UNIT_THING = new ThingTypeUID(BINDING_ID, THING_TYPE_UNIT);
    public static final Set<ThingTypeUID> SUPPORTED_UNIT_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_UNIT_THING).collect(Collectors.toSet()));

    // Collection of system and unit thing types
    public static final Set<ThingTypeUID> SUPPORTED_SYSTEM_AND_UNIT_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_SYSTEM_BRIDGE_THING_TYPES_UIDS.stream(), SUPPORTED_UNIT_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(UID_ACCOUNT_BRIDGE, UID_SYSTEM_BRIDGE, UID_UNIT_THING).collect(Collectors.toSet()));

    // System Properties
    public static final String THING_PROPERTY_GATEWAY_ID = "GatewayId";
    public static final String THING_PROPERTY_GATEWAY_USERNAME = "GatewayUsername";
    public static final String THING_PROPERTY_INSTALLATION_DATE = "InstallationDate";
    public static final String THING_PROPERTY_LOCATION = "Location";
    public static final String THING_PROPERTY_OPERATOR_NAME = "OperatorName";
    public static final String THING_PROPERTY_USERNAME_OWNER = "UserNameOwner";
    public static final String THING_PROPERTY_ACCESSLEVEL = "AccessLevel";

    public static final String CH_TEMPERATURE = "temperature";
    public static final String CH_PRESSURE = "barometric-pressure";
    public static final String CH_STRING = "string";
    public static final String CH_CONTACT = "contact";
    public static final String CH_NUMBER = "number";
    public static final String CH_DATETIME = "datetime";

    // Background discovery frequency
    public static final int DISCOVERY_INTERVAL_SECONDS = 300;
    public static final int DISCOVERY_INITIAL_DELAY_SECONDS = 10;

    // System bridge and remote unit thing config parameters
    public static final String CONFIG_SYSTEM_ID = "systemId";
    public static final String CONFIG_UNIT_ID = "unitId";

    public static final List<SubMenuEntryWithMenuItemTabView> EMPTY_UNITS = Collections
            .<SubMenuEntryWithMenuItemTabView> emptyList();
    public static final List<GetSystemListDTO> EMPTY_SYSTEMS = Collections.<GetSystemListDTO> emptyList();
}
