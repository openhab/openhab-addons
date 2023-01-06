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
package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link JsonStartRoutineRequest} encapsulate the GSON for starting a routine
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonStartRoutineRequest {
    public @Nullable String behaviorId = "PREVIEW";
    public @Nullable String sequenceJson;
    public @Nullable String status = "ENABLED";
}
