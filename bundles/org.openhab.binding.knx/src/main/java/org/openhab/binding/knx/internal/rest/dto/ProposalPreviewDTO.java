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
package org.openhab.binding.knx.internal.rest.dto;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The checked proposal: its problems and reads; without an error also the text, the counts and the
 * create steps.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposalPreviewDTO {
    public @Nullable String thingsText;
    public @Nullable String itemsText;
    public List<ProblemDTO> problems;
    public List<String> readAddresses;
    public int readTelegrams;
    public @Nullable ProposalCountsDTO counts;
    public @Nullable List<CreateStepDTO> steps;
    public @Nullable List<String> existingGroups;
    public @Nullable ProposalLinesDTO lines;
}
