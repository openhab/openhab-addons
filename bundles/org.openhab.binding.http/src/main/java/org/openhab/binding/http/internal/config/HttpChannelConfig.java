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
package org.openhab.binding.http.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.binding.generic.ChannelValueConverterConfig;

/**
 * The {@link HttpChannelConfig} class contains fields mapping channel configuration parameters.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HttpChannelConfig extends ChannelValueConverterConfig {

    public @Nullable String stateExtension;
    public @Nullable String commandExtension;
    public @Nullable List<String> stateTransformation;
    public @Nullable List<String> commandTransformation;
    public String stateContent = "";
}
