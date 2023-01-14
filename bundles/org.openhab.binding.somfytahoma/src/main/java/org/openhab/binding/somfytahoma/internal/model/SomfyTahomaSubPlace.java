/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SomfyTahomaSubPlace} holds information about a room bound
 * to TahomaLink account.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaSubPlace {

    private String label = "";
    private int type;
    private String metadata = "";
    private String oid = "";

    public String getLabel() {
        return label;
    }

    public int getType() {
        return type;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getOid() {
        return oid;
    }
}
