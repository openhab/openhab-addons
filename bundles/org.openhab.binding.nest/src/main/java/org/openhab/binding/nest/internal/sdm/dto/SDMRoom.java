/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.dto;

/**
 * An instance of enterprise managed room in a structure.
 *
 * @author Wouter Born - Initial contribution
 */
public class SDMRoom {
    /**
     * The resource name of the room.
     */
    public SDMResourceName name = SDMResourceName.NAMELESS;

    /**
     * Room traits.
     */
    public SDMTraits traits = new SDMTraits();
}
