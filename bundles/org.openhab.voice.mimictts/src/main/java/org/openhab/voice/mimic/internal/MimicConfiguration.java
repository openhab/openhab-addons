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
package org.openhab.voice.mimic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MimicConfiguration} class contains fields mapping configuration parameters.
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class MimicConfiguration {
    public String url = "http://localhost:59125";
    public Double speakingRate = 1.0;
    public Double audioVolatility = 0.667;
    public Double phonemeVolatility = 0.8;
}
