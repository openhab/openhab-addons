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

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for the count of program files that still missed wanted entries when the pass bound was reached. All
 * data is made up. No valid file reaches the bound of the reader (3 passes are the longest chain), so the tests
 * lower it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectPassBoundTest {

    private static final class Counts {
        int passBound;
        int passes;
    }

    /** The longest chain of a valid file needs 3 passes, so the bound is lowered to reach it. */
    private static Counts read(TestContainer container, int maxPasses) throws Exception {
        Path file = container.write();
        KnxProject project = new KnxProjectReader().read(file, null);
        ReadBudget budget = new ReadBudget(ReadLimits.DEFAULT);
        try (OuterContainer opened = OuterContainer.open(file, budget, name -> {
        })) {
            ManufacturerReader manufacturer = new ManufacturerReader(opened, budget, null, maxPasses);
            manufacturer.read(project);
            Counts counts = new Counts();
            counts.passBound = manufacturer.passBoundPrograms();
            counts.passes = manufacturer.passes();
            return counts;
        }
    }

    /** The objects come before the references, so the reference found in the first pass wants an earlier object. */
    private static TestContainer needsTwoPasses() {
        return ProgramFixture.container(instance(1, ""), objectsThenReferences(comObject(1, ""), reference(1, "")));
    }

    @Test
    void aProgramStillMissingAnEntryAfterTheLastPassIsCounted() throws Exception {
        Counts counts = read(needsTwoPasses(), 1);

        assertEquals(1, counts.passBound);
        assertEquals(1, counts.passes);
    }

    @Test
    void aProgramThatFindsEverythingInTheLastPassIsNotCounted() throws Exception {
        Counts counts = read(needsTwoPasses(), 2);

        assertEquals(0, counts.passBound);
        assertEquals(2, counts.passes);
    }

    @Test
    void theBoundOfTheReaderLeavesTheUsualFilesUncounted() throws Exception {
        Counts counts = read(needsTwoPasses(), ProgramReader.MAX_PASSES);

        assertEquals(0, counts.passBound);
        assertEquals(2, counts.passes);
    }
}
