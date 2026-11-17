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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.channel.GroupAddressConfiguration;
import org.openhab.binding.knx.internal.project.KnxProposal.Channel;
import org.openhab.binding.knx.internal.project.KnxProposal.Part;
import org.openhab.binding.knx.internal.project.KnxProposal.Thing;
import org.openhab.binding.knx.internal.project.KnxProposalBuilder.Selection;

import io.calimero.GroupAddress;

/**
 * Runs the proposal over the repository's test projects that have devices: every text it gives parses with the
 * binding's own parser, each key reads at most one address, and an address is read in one channel only.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProposalProjectsTest {

    private static final Map<String, TestContainer> PROJECTS = Map.of( //
            "ets4", generation("ets4", "P-0A1B", "M-00FA", "M-00FA_A-0022-33-4455"), //
            "ets5", generation("ets5", "P-1C2D", "M-0083", "M-0083_A-00AA-11-B2C3"), //
            "ets6", generation("ets6", "P-3E4F", "M-00C8", "M-00C8_A-0101-20-0F0F"), //
            "modular", generation("modular", "P-5A6B", "M-00C8", "M-00C8_A-0110-10-AAAA"));

    private static TestContainer generation(String fixture, String part, String folder, String program) {
        return TestContainer.of(fixture, part)
                .put(folder + "/Hardware.xml", TestContainer.resource("manufacturer/" + fixture + "/Hardware.xml"))
                .put(folder + "/" + program + ".xml",
                        TestContainer.resource("manufacturer/" + fixture + "/" + program + ".xml"));
    }

    private static KnxProject read(String name) throws Exception {
        TestContainer container = PROJECTS.get(name);
        assertNotNull(container, name);
        return new KnxProjectReader(ReadLimits.DEFAULT, entry -> {
        }).read(container.write(), null);
    }

    private static List<String> functionIds(KnxProject project) {
        List<String> ids = new ArrayList<>();
        Deque<KnxSpace> pending = new ArrayDeque<>(project.locations());
        while (!pending.isEmpty()) {
            KnxSpace space = pending.pop();
            space.functions().forEach(f -> ids.add(f.id()));
            pending.addAll(space.spaces());
        }
        return ids;
    }

    private static List<Selection> selections(KnxProject project, boolean readAtStart) {
        List<Integer> addresses = project.groupAddresses().stream().map(KnxGroupAddress::rawAddress).toList();
        List<String> devices = project.topology().devices().stream().map(KnxDevice::id).toList();
        List<String> spaces = project.locations().stream().map(KnxSpace::id).toList();
        List<String> functions = functionIds(project);
        return List.of(new Selection(addresses, List.of(), List.of(), List.of(), readAtStart),
                new Selection(List.of(), List.of(), List.of(), devices, readAtStart),
                new Selection(List.of(), functions, List.of(), List.of(), readAtStart),
                new Selection(List.of(), List.of(), spaces, List.of(), readAtStart),
                new Selection(addresses, functions, spaces, devices, readAtStart));
    }

    /** What is wrong with the channel's texts; reads of the top-level channels are added to {@code read}. */
    private static List<String> problems(Channel channel, Set<Integer> read, boolean count) {
        List<String> problems = new ArrayList<>();
        for (Part part : channel.parts()) {
            GroupAddressConfiguration parsed = GroupAddressConfiguration.parse(part.text());
            if (parsed == null) {
                problems.add(part.text() + ": does not parse");
                continue;
            }
            Set<GroupAddress> reads = parsed.getReadGAs();
            if (reads.size() > 1) {
                problems.add(part.text() + ": reads " + reads.size());
            }
            int marks = (part.read() ? 1 : 0) + (part.statusRead() ? 1 : 0);
            if (marks != reads.size()) {
                problems.add(part.text() + ": " + marks + " marks, " + reads.size() + " reads");
            }
            if (count) {
                for (GroupAddress address : reads) {
                    if (!read.add(address.getRawAddress())) {
                        problems.add(part.text() + ": " + address + " read twice");
                    }
                }
            }
            KnxDatapointType type = part.dataType();
            String channelType = channel.channelType();
            if (channel.selected()
                    && (type == null || channelType == null || !ChannelKeyTypes.works(channelType, part.key(), type))) {
                problems.add(part.text() + ": selected but " + channelType + " " + part.key() + " " + type);
            }
        }
        for (Channel split : channel.split()) {
            problems.addAll(problems(split, new HashSet<>(read), false));
        }
        return problems;
    }

    @Test
    void everyTextParsesAndEachKeyReadsAtMostOneAddress() throws Exception {
        int reads = 0;
        for (String name : List.of("ets4", "ets5", "ets6", "modular")) {
            KnxProject project = read(name);
            assertFalse(project.topology().devices().isEmpty(), name);
            int channels = 0;
            List<String> problems = new ArrayList<>();
            for (boolean readAtStart : List.of(true, false)) {
                for (Selection selection : selections(project, readAtStart)) {
                    KnxProposal proposal = new KnxProposalBuilder(project).propose(selection, Set.of());
                    assertEquals(List.of(), proposal.unknown(), name);
                    Set<Integer> read = new HashSet<>();
                    for (Thing thing : proposal.things()) {
                        for (Channel channel : thing.channels()) {
                            channels++;
                            problems.addAll(problems(channel, read, true));
                        }
                    }
                    reads += read.size();
                    if (!readAtStart) {
                        assertEquals(Set.of(), read, name);
                    }
                }
            }
            assertTrue(channels > 0, name);
            assertEquals(List.of(), problems, name);
        }
        assertTrue(reads > 0);
    }
}
