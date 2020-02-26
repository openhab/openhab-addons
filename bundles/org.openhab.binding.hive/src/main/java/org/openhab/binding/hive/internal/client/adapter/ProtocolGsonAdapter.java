/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client.adapter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hive.internal.client.HiveApiConstants;
import org.openhab.binding.hive.internal.client.Protocol;

/**
 * A gson {@link com.google.gson.TypeAdapter} for {@link Protocol}.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class ProtocolGsonAdapter extends ComplexEnumGsonTypeAdapterBase<Protocol> {
    public ProtocolGsonAdapter() {
        super(EnumMapper.builder(Protocol.class)
                .setUnexpectedValue(Protocol.UNEXPECTED)
                .add(Protocol.MQTT, HiveApiConstants.PROTOCOL_MQTT)
                .add(Protocol.PROXIED, HiveApiConstants.PROTOCOL_PROXIED)
                .add(Protocol.SYNTHETIC, HiveApiConstants.PROTOCOL_SYNTHETIC)
                .add(Protocol.VIRTUAL, HiveApiConstants.PROTOCOL_VIRTUAL)
                .add(Protocol.XMPP, HiveApiConstants.PROTOCOL_XMPP)
                .add(Protocol.ZIGBEE, HiveApiConstants.PROTOCOL_ZIGBEE)
                .ignore(Protocol.NONE)
                .build());
    }
}
