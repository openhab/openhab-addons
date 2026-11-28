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

/**
 * The checked proposal as lines to paste into Things that exist: per Thing of the request with a {@code target}
 * its channel lines, and the Items linked to the channels of the target.
 *
 * @author Stamate Viorel - Initial contribution
 */
public class ProposalLinesDTO {
    public String thingsText;
    public String itemsText;
    public List<ExistingChannelsDTO> existingChannels;
    public int withoutTarget;
}
