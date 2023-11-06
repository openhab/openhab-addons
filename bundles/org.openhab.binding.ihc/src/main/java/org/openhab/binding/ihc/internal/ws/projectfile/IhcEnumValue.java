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
package org.openhab.binding.ihc.internal.ws.projectfile;

/**
 * Class to store IHC / ELKO LS controller's enum value information.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcEnumValue {
    private final int id;
    private final String name;

    public IhcEnumValue(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("[ id=%d, name='%s' ]", id, name);
    }
}
