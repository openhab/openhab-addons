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

import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * One part of the single pass over an installation file. The {@link InstallationReader} hands a section
 * every element from its root element (a child of {@code Installation}) down to that root's end tag.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
interface InstallationSection {

    /**
     * @param element the local name
     * @param parent the local name of the enclosing element
     * @param depth the element's depth below the section root, which has depth 0
     * @param xml positioned on the start tag; a section reads attributes only and does not move it
     */
    void start(String element, @Nullable String parent, int depth, XMLStreamReader xml) throws KnxProjectException;

    /** The end tag of an element passed to {@link #start}, with the same depth. */
    void end(String element, int depth) throws KnxProjectException;
}
