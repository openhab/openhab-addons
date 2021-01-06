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
package org.openhab.binding.prometheusexporter.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PrometheusExporterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Robert Bach - Initial contribution
 */
@NonNullByDefault
public class PrometheusExporterBindingConstants {

    private static final String BINDING_ID = "prometheusexporter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");

    // The thing type as a set
    public static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_GENERIC);

    // List of all Channel ids
    public static final String CHANNEL_BUNDLE_STATE = "bundlestate";
    public static final String CHANNEL_EVENT_COUNT = "eventcount";
    public static final String CHANNEL_JVM = "jvm";
    public static final String CHANNEL_INBOX_COUNT = "inboxcount";
    public static final String CHANNEL_THING_STATE = "thingstate";
    public static final String CHANNEL_THREAD_POOLS = "threadpools";
    public static final String CHANNEL_ALL = "all";
}
