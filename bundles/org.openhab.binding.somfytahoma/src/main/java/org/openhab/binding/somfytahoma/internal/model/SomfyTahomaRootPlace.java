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
package org.openhab.binding.somfytahoma.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyTahomaRootPlace} holds information about all rooms bound
 * to TahomaLink account.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaRootPlace {

    private String label = "";
    private int type;
    private String oid = "";
    private List<SomfyTahomaSubPlace> subPlaces = new ArrayList<>();

    public String getLabel() {
        return label;
    }

    public int getType() {
        return type;
    }

    public String getOid() {
        return oid;
    }

    public List<SomfyTahomaSubPlace> getSubPlaces() {
        return subPlaces;
    }
}
