/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.gmailparadoxparser.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link GmailParadoxParserBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Konstantin_Polihronov - Initial contribution
 */
@NonNullByDefault
public class GmailParadoxParserBindingConstants {

    private static final String BINDING_ID = "gmailparadoxparser";

    // List of all Thing Type UIDs
    public static final ThingTypeUID PARTITION_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "ParadoxSystem");

    // List of all Channel ids
    public static final String PARTITION_CHANNEL_ID = "partition";
}
