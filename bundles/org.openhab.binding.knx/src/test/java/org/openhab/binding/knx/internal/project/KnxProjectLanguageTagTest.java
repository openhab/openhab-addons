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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.project.ProgramFixture.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for the language tag of a read: the form with an underscore, and the program's default language. All
 * data is made up.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectLanguageTagTest {

    private static final String CONTENT = objectsThenReferences(comObject(1, "Text=\"Switch\""), reference(1, ""));
    private static final String SIBLINGS = language("de-AT", translation(APP + "_O-1", "Text", "Schalter AT"))
            + language("fr-FR", translation(APP + "_O-1", "Text", "Interrupteur"));
    private static final String LANGUAGES = SIBLINGS
            + language("de-DE", translation(APP + "_O-1", "Text", "Schalten DE"));

    private final KnxProjectReader reader = new KnxProjectReader();

    /** The name of the one object, in a program with this default language, read with this tag. */
    private String name(String defaultLanguage, String tag) throws Exception {
        return name(defaultLanguage, tag, LANGUAGES);
    }

    private String name(String defaultLanguage, String tag, String languages) throws Exception {
        TestContainer container = ProgramFixture.container(instance(1, ""), "", CONTENT, "");
        container.put(PROGRAM_ENTRY, program(APP, CONTENT, languages).replace("DefaultLanguage=\"en-US\"",
                "DefaultLanguage=\"" + defaultLanguage + "\""));
        return reader.read(container.write(), null, tag).groupObjects().get(0).name();
    }

    @Test
    void aTagWithAnUnderscoreIsTheTagWithAHyphen() throws Exception {
        assertEquals("Schalten DE", name("en-US", "de_DE"));
        assertEquals("Schalten DE", name("en-US", "DE_de"));
        assertEquals("Schalter AT", name("en-US", "de_AT"));
        assertEquals("Schalten DE", name("en-US", "de-DE"));
        assertEquals("Interrupteur", name("en-US", "fr_BE"), "the language part is found with an underscore too");
    }

    @Test
    void aTagEqualToTheDefaultLanguageGivesTheDefaultTexts() throws Exception {
        assertEquals("Switch", name("de-DE", "de-DE", SIBLINGS), "not the de-AT texts, which share the language part");
        assertEquals("Switch", name("de-DE", "DE-de", SIBLINGS));
        assertEquals("Switch", name("de-DE", "de_DE", SIBLINGS));
        assertEquals("Switch", name("de-DE", "de-DE"), "even where a translation has the same identifier");
    }

    @Test
    void aSiblingOfTheDefaultLanguageStillTakesItsOwnTexts() throws Exception {
        assertEquals("Schalter AT", name("de-DE", "de-AT", SIBLINGS));
        assertEquals("Schalter AT", name("de-DE", "de", SIBLINGS), "the first of the language part, as before");
        assertEquals("Schalten DE", name("en-US", "de-DE"));
    }
}
