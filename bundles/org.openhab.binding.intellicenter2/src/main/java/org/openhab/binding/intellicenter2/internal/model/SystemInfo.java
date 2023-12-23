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

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.MODE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.PROPNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.VER;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.RequestObject;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class SystemInfo extends ResponseModel {

    private static final List<Attribute> REQUEST_ATTRIBUTES = List.of(MODE, VER, PROPNAME, SNAME);
    public static final ICRequest REQUEST = ICRequest.getParamList("OBJTYP=SYSTEM",
            new RequestObject("INCR", REQUEST_ATTRIBUTES));

    SystemInfo() {
        super(REQUEST_ATTRIBUTES);
    }

    public SystemInfo(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    public String getPropertyName() {
        return getValueAsString(PROPNAME);
    }

    public String getMode() {
        return getValueAsString(MODE);
    }

    public String getVersion() {
        return getValueAsString(VER);
    }

    public String getIntellicenterVersion() {
        return getVersion().split(",")[0].split(":")[1].trim();
    }
}
