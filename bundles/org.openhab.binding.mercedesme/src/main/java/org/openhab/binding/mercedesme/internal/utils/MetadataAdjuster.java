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
package org.openhab.binding.mercedesme.internal.utils;

import java.nio.file.Path;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.ChannelUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MetadataAdjuster} changes Metadata for channels not providing the system default unit
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MetadataAdjuster {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataAdjuster.class);
    String baseDir = "";
    Path metadataJsonDatabasePath = Path.of(baseDir, "jsondb", "org.openhab.core.items.Metadata.json");
    // JsonStorage<Metadata> metadataStorage = new JsonStorage<>(metadataJsonDatabasePath.toFile(), null, 5, 0, 0,
    // List.of());

    private static LocaleProvider localeProvider = new LocaleProvider() {
        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    };

    public static void initialze(TimeZoneProvider tzp, LocaleProvider lp) {
        localeProvider = lp;
    }

    public static void adjust(ChannelUID cuid) {
        // ItemChannelLinkRegistry ICLR;
        // //ICLR.
        // ItemChannelLink icl;
        // icl.
        // Metadata md;
        // //md.
        // MetadataRegistry mdr;
        // mdr.get
        // mdr.get(AbstractUID.SEGMENT_PATTERN;
        // MetadataProvider mdpr;
        // mdpr.
    }
}
