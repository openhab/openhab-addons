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
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AccessProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.AvailableProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.ContentTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.DataTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.EnumerationTypeProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.KeyProvider;
import org.openhab.binding.homeconnectdirect.internal.service.description.model.provider.RangeProvider;

/**
 * Status model from the device description.
 *
 * @author Jonas Brüstel - Initial contribution
 *
 * @param access possible values: READ, READ_STATIC, NONE
 * @param dataType the data type identifier (refDID) from the device description
 */
@NonNullByDefault
public record Status(int uid, String key, ContentType contentType, @Nullable DataType dataType, @Nullable Number min,
        @Nullable Number max, @Nullable Number stepSize, @Nullable Integer enumerationType,
        @Nullable String enumerationTypeKey, boolean available, Access access, boolean notifyOnChange,
        @Nullable String initValue)
        implements
            AccessProvider,
            AvailableProvider,
            RangeProvider,
            KeyProvider,
            EnumerationTypeProvider,
            ContentTypeProvider,
            DataTypeProvider {
}
