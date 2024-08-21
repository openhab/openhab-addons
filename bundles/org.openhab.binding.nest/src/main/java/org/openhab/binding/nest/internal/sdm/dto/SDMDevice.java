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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An instance of enterprise managed device in the property.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMDevice {
    /**
     * The resource name of the device.
     */
    public SDMResourceName name = SDMResourceName.NAMELESS;

    /**
     * Type of the device for general display purposes.
     */
    public @Nullable SDMDeviceType type;

    /**
     * Device traits.
     */
    public SDMTraits traits = new SDMTraits();

    /**
     * Assignee details of the device.
     */
    public List<SDMParentRelation> parentRelations = List.of();
}
