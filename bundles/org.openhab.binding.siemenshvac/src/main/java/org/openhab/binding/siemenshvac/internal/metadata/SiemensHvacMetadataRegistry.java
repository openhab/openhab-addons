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
package org.openhab.binding.siemenshvac.internal.metadata;

import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacException;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface SiemensHvacMetadataRegistry {

    /**
     * Initializes the type generator.
     */
    void initialize();

    void readMeta() throws SiemensHvacException;

    @Nullable
    SiemensHvacMetadataMenu getRoot();

    @Nullable
    ArrayList<SiemensHvacMetadataDevice> getDevices();

    @Nullable
    SiemensHvacMetadata getDptMap(@Nullable String key);

    @Nullable
    SiemensHvacChannelTypeProvider getChannelTypeProvider();

    @Nullable
    SiemensHvacConnector getSiemensHvacConnector();

    void invalidate();

    @Nullable
    SiemensHvacMetadataUser getUser();

    @Nullable
    Locale getUserLocale();
}
