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
package org.openhab.binding.chatgpt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ChatGPTChannelConfiguration} class contains fields mapping chat channel configuration parameters.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class ChatGPTChannelConfiguration {

    public String model = "gpt-3.5-turbo";

    public float temperature = 0.5f;

    public String systemMessage = "";

    int maxTokens = 500;
}
