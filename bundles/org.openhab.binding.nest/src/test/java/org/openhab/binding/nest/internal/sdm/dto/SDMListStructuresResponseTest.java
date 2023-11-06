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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.nest.internal.sdm.dto.SDMDataUtil.fromJson;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.nest.internal.sdm.dto.SDMTraits.SDMStructureInfoTrait;

/**
 * Tests deserialization of {@link
 * org.openhab.binding.nest.internal.sdm.dto.SDMListStructuresResponse}s from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMListStructuresResponseTest {

    @Test
    public void deserializeListDevicesResponse() throws IOException {
        SDMListStructuresResponse response = fromJson("list-structures-response.json", SDMListStructuresResponse.class);
        assertThat(response, is(notNullValue()));

        List<SDMStructure> structures = response.structures;
        assertThat(structures, is(notNullValue()));
        assertThat(structures, hasSize(2));

        SDMStructure structure = structures.get(0);
        assertThat(structure, is(notNullValue()));
        assertThat(structure.name.name, is("enterprises/project-id/structures/beach-house-structure-id"));
        SDMTraits traits = structure.traits;
        assertThat(traits.traitList(), hasSize(1));
        SDMStructureInfoTrait structureInfo = structure.traits.structureInfo;
        assertThat(structureInfo, is(notNullValue()));
        assertThat(structureInfo.customName, is("Beach House"));

        structure = structures.get(1);
        assertThat(structure, is(notNullValue()));
        assertThat(structure.name.name, is("enterprises/project-id/structures/home-structure-id"));
        traits = structure.traits;
        assertThat(traits.traitList(), hasSize(1));
        structureInfo = structure.traits.structureInfo;
        assertThat(structureInfo, is(notNullValue()));
        assertThat(structureInfo.customName, is("Home"));
    }
}
