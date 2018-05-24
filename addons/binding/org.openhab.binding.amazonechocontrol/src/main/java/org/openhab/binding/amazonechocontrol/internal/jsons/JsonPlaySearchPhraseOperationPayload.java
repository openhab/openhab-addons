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
 * The {@link JsonPlaySearchPhraseOperationPayload} encapsulate the GSON for validation requests and results
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class JsonPlaySearchPhraseOperationPayload {
    public @Nullable String deviceType = "ALEXA_CURRENT_DEVICE_TYPE";
    public @Nullable String deviceSerialNumber = "ALEXA_CURRENT_DSN";
    public @Nullable String locale = "ALEXA_CURRENT_LOCALE";
    public @Nullable String customerId;
    public @Nullable String searchPhrase;
    public @Nullable String sanitizedSearchPhrase;
    public @Nullable String musicProviderId = "ALEXA_CURRENT_DSN";
}
