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
package org.openhab.binding.knx.internal.wizard;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Keeps the text and the steps made from the checked model only: they and the model never name a class of the
 * REST layer, where the request classes live. No wizard class takes a registry or Gson.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class WizardImportsTest {
    private static final Path FOLDER = Path.of("src", "main", "java", "org", "openhab", "binding", "knx", "internal",
            "wizard");
    private static final List<String> MODEL_ONLY = List.of("CheckedProposal.java", "ProposalText.java",
            "ProposalPlan.java");
    private static final List<String> REST = List.of("org.openhab.binding.knx.internal.rest", "rest.dto", "DTO");
    private static final List<String> NEVER = List.of("com.google.gson", "jakarta.", "ThingRegistry", "ItemRegistry",
            "ItemChannelLinkRegistry", "org.openhab.core.items");

    /** The forbidden names a source holds outside comments. */
    static List<String> violations(String source, List<String> forbidden) {
        String code = source.replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\n]*", "");
        List<String> found = new ArrayList<>();
        for (String name : forbidden) {
            if (code.contains(name)) {
                found.add(name);
            }
        }
        return found;
    }

    static String read(String file) throws IOException {
        return Files.readString(FOLDER.resolve(file), StandardCharsets.UTF_8);
    }

    @Test
    void theRuleFindsABadSample() {
        assertEquals(List.of("org.openhab.binding.knx.internal.rest", "rest.dto", "DTO"),
                violations("import org.openhab.binding.knx.internal.rest.dto.ProposedThingDTO;\nclass A {}", REST));
        assertEquals(List.of("DTO"), violations("class A { void of(ProposedThingDTO thing) {} }", REST));
        assertEquals(List.of("com.google.gson", "ItemRegistry", "org.openhab.core.items"), violations(
                "import com.google.gson.Gson;\nimport org.openhab.core.items.ItemRegistry;\nclass A {}", NEVER));
        assertEquals(List.of(), violations("/** like core's ThingDTO */\nclass A {} // ItemRegistry", NEVER));
    }

    @Test
    void theModelTheTextAndTheStepsNameNoRestClass() throws IOException {
        for (String file : MODEL_ONLY) {
            assertEquals(List.of(), violations(read(file), REST), file);
        }
    }

    @Test
    void noWizardClassTakesARegistryOrGson() throws IOException {
        List<String> files;
        try (Stream<Path> paths = Files.list(FOLDER)) {
            files = paths.map(path -> path.getFileName().toString()).sorted().toList();
        }
        assertTrue(files.containsAll(MODEL_ONLY), files.toString());
        assertTrue(files.containsAll(List.of("ProposalNames.java", "ProposalItems.java")), files.toString());
        for (String file : files) {
            assertEquals(List.of(), violations(read(file), NEVER), file);
        }
    }
}
