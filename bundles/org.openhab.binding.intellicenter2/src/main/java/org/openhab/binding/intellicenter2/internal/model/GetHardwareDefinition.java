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
package org.openhab.binding.intellicenter2.internal.model;

import static java.util.stream.Collectors.toList;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.OBJTYP;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class GetHardwareDefinition {

    private static final String GET_HARDWARE_DEFINITION = "GetHardwareDefinition";

    private final ICResponse response;

    // ['CIRCUITS', 'PUMPS', 'CHEMS', 'VALVES', 'HEATERS', 'SENSORS', 'GROUPS'];
    public enum Argument {
        DEFAULT,
        CIRCUITS,
        PUMPS;

        private final ICRequest request;

        Argument() {
            String argument = name();
            if ("DEFAULT".equals(argument)) {
                argument = "";
            }
            this.request = ICRequest.getQuery(GET_HARDWARE_DEFINITION, argument);
        }

        public ICRequest getRequest() {
            return request;
        }
    }

    public GetHardwareDefinition(ICResponse response) {
        this.response = response;
    }

    public List<Panel> getPanels() {
        return response.getAnswer().stream().filter(r -> r.getValueAsString(OBJTYP).equals("PANEL")).map(Panel::new)
                .collect(toList());
    }
}
