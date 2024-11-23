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
 * Represents device relationships, for instance, structure/room to which the device is assigned to.
 *
 * @author Wouter Born - Initial contribution
 */
public class SDMParentRelation {
    /**
     * The name of the relation.
     */
    public SDMResourceName parent;

    /**
     * The custom name of the relation.
     */
    public String displayName;
}
