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
 * An instance of an enterprise managed structure.
 *
 * @author Wouter Born - Initial contribution
 */
public class SDMStructure {
    /**
     * The resource name of the structure.
     */
    public SDMResourceName name = SDMResourceName.NAMELESS;

    /**
     * Structure traits.
     */
    public SDMTraits traits = new SDMTraits();
}
