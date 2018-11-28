/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SensorConfig} is send by the the Rest API.
 * It is part of a {@link SensorMessage}.
 *
 * This should be in sync with the supported sensors from
 * https://dresden-elektronik.github.io/deconz-rest-doc/sensors/.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorConfig {
    public boolean on = true;
    public boolean reachable = true;
    public @Nullable Integer battery;
}
