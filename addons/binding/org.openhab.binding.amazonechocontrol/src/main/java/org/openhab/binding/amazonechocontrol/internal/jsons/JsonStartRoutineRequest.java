/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
