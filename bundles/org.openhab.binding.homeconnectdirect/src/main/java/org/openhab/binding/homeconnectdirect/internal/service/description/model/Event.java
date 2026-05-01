/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal.service.description.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.ContentTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.DataTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.EnumerationTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.KeyProvider;

/**
 * Event model from the device description.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public record Event(int uid, String key, ContentType contentType, @Nullable DataType dataType, Handling handling,
        Level level, @Nullable Integer enumerationType, String enumerationTypeKey)
        implements
            KeyProvider,
            ContentTypeProvider,
            EnumerationTypeProvider,
            DataTypeProvider {
}
