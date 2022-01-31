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
package org.openhab.binding.loxone.internal.types;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.loxone.internal.controls.LxControl;

/**
 * Channel tags for a {@link LxControl} object.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxTags {
    public static final Set<String> SCENE = Collections.singleton("Scene");
    public static final Set<String> LIGHTING = Collections.singleton("Lighting");
    public static final Set<String> SWITCHABLE = Collections.singleton("Switchable");
    public static final Set<String> TEMPERATURE = Collections.singleton("CurrentTemperature");
}
