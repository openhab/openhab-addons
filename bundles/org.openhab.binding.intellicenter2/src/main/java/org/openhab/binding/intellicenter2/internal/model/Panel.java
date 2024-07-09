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
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.BODY;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.CIRCUIT;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.CIRCUITS;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.HNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.LISTORD;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.MODULE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.OBJLIST;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.OBJTYP;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.PANID;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.PUMP;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.VER;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class Panel extends ResponseModel {

    private static final List<Attribute> REQUEST_ATTRIBUTES = List.of(HNAME, SNAME, PANID, LISTORD, VER, OBJLIST);

    Panel() {
        super(REQUEST_ATTRIBUTES);
    }

    public Panel(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    public List<Body> getBodies() {
        return getModuleCircuits().filter(r -> BODY.toString().equals(r.getValueAsString(OBJTYP))).map(Body::new)
                .collect(toList());
    }

    public List<Circuit> getCircuits() {
        return getModuleCircuits().filter(r -> CIRCUIT.toString().equals(r.getValueAsString(OBJTYP))).map(Circuit::new)
                .collect(toList());
    }

    public List<Pump> getPumps() {
        return getValueAsResponseObjects(OBJLIST).stream()
                .filter(r -> PUMP.toString().equals(r.getValueAsString(OBJTYP))).map(Pump::new).collect(toList());
    }

    private Stream<ResponseObject> getModuleCircuits() {
        return getValueAsResponseObjects(OBJLIST).stream()
                .filter(r -> MODULE.toString().equals(r.getValueAsString(OBJTYP)))
                .map(r -> r.getValueAsResponseObjects(CIRCUITS)).flatMap(Collection::stream);
    }
}
