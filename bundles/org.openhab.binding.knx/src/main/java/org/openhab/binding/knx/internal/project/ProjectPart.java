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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The project part of a container: a folder {@code P-xxxx/} or a protected {@code P-xxxx.zip}. Both hold the
 * same files; this hides where they come from.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
interface ProjectPart {

    /** The part's name, {@code P-xxxx}. */
    String id();

    /** {@code project.xml}, decompressed and held to the read's limits. */
    InputStream openProjectXml() throws IOException, KnxProjectException;

    /** The installation file with the lowest number, normally {@code 0.xml}. */
    InputStream openInstallationXml() throws IOException, KnxProjectException;

    /** How many installation files the part holds; only the first is read. */
    int installationFileCount();
}
