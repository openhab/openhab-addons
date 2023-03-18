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
package org.openhab.binding.windcentrale.internal.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps a subset of the Windcentrale API project details that is required for discovering windmill things.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class Project {

    public static final class Participation {
        public int share;
    }

    public String projectCode = "";
    public String projectName = "";

    public List<Participation> participations = List.of();
}
