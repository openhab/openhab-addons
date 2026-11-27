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
package org.openhab.binding.philipsair.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.StateOption;

/**
 * Checks that the labels of dynamically set state options are the default texts of the translations. The framework
 * translates a state option by its value, so a label that differs from the text of the translation file is shown to
 * users of the default language only.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public final class OptionLabelTranslations {

    private static final Properties TRANSLATIONS = load();

    private OptionLabelTranslations() {
    }

    private static Properties load() {
        Properties properties = new Properties();
        try (InputStream in = OptionLabelTranslations.class.getResourceAsStream("/OH-INF/i18n/philipsair.properties")) {
            assertNotNull(in);
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("The translations cannot be read", e);
        }
        assertFalse(properties.isEmpty());
        return properties;
    }

    /**
     * Asserts that each option has a translation with its label as text.
     *
     * @param channelId the id of the channel, without group, which is also the id of its channel type
     * @param options the options set for the channel
     */
    public static void assertLabelsAreTranslated(String channelId, List<StateOption> options) {
        for (StateOption option : options) {
            String key = "channel-type.philipsair." + channelId + ".state.option." + option.getValue();
            assertEquals(option.getLabel(), TRANSLATIONS.getProperty(key), key);
        }
    }
}
