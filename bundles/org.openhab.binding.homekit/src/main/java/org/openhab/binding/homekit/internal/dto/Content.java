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
package org.openhab.binding.homekit.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Used to encapsulate different types Characteristic's contents. Either a Channel Definition or a Property.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public sealed interface Content {
    record ChannelDefinition(org.openhab.core.thing.type.ChannelDefinition definition) implements Content {
    }

    record Property(String name, String value) implements Content {
    }
}
