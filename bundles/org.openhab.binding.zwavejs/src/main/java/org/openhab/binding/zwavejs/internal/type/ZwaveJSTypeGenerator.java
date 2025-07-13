/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.core.thing.ThingUID;

/**
 * Interface for generating ChannelTypes and ConfigDescriptions for a given Z-Wave JS node.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public interface ZwaveJSTypeGenerator {

    /*
     * Generates a ZwaveJSTypeGeneratorResult based on the provided ThingUID, Node, and configurationAsChannels flag.
     *
     * @param thingUID the unique identifier of the thing
     * 
     * @param node the node for which the type is being generated
     * 
     * @param configurationAsChannels a flag indicating whether the configuration should be treated as channels
     * 
     * @return a ZwaveJSTypeGeneratorResult containing the generated type information
     */
    ZwaveJSTypeGeneratorResult generate(ThingUID thingUID, Node node, boolean configurationAsChannels);
}
