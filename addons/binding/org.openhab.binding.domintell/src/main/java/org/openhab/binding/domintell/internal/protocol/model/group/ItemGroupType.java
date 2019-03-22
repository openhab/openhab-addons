/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.domintell.internal.protocol.model.group;

/**
* The {@link ItemGroupType} class is represents an item group of type
*
* @author Gabor Bicskei - Initial contribution
*/
public enum ItemGroupType {
    variable("Domintell Custom Variables"), system("Domintell System Variables");

    private String name;

    ItemGroupType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
