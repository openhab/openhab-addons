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

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.OBJNAM;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.OBJTYP;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SNAME;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.SUBTYP;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.ICRequest;
import org.openhab.binding.intellicenter2.internal.protocol.RequestObject;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class ResponseModel extends ResponseObject {

    private final Set<Attribute> requestAttributes;

    protected ResponseModel(Collection<Attribute> requestAttributes) {
        this(requestAttributes, null);
    }

    public ResponseModel(Collection<Attribute> requestAttributes, @Nullable ResponseObject response) {
        super(response);
        this.requestAttributes = new HashSet<>(requestAttributes);
        this.requestAttributes.add(OBJTYP);
        this.requestAttributes.add(SUBTYP);
        this.requestAttributes.add(OBJNAM);
        this.requestAttributes.add(SNAME);
    }

    public String getSubType() {
        return getValueAsString(SUBTYP);
    }

    public String getObjectType() {
        return getValueAsString(OBJTYP);
    }

    public String getSname() {
        return getValueAsString(SNAME);
    }

    public RequestObject asRequestObject() {
        return new RequestObject(getObjectName(), requestAttributes);
    }

    public ICRequest createRefreshRequest() {
        return ICRequest.getParamList(null, asRequestObject());
    }
}
