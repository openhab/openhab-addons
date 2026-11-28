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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The two forms of a group address id in a project: full ({@code P-xxxx-n_GA-n}) and short ({@code GA-n}).
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class ProjectIds {

    private static final Pattern TOKEN = Pattern.compile("\\S+");

    private ProjectIds() {
    }

    /** The matcher over the white-space separated tokens of a text; the text is not split into strings at once. */
    static Matcher tokens(String text) {
        return TOKEN.matcher(text);
    }

    /** The short ids of a white-space separated list of ids; each one counts as a record of the read. */
    static List<String> shortIds(String text, ReadBudget budget) throws KnxProjectException {
        List<String> ids = new ArrayList<>();
        Matcher tokens = tokens(text);
        while (tokens.find()) {
            budget.countRecord();
            ids.add(shortId(tokens.group()));
        }
        return ids;
    }

    /** The part after the last {@code _}, or the whole id when it has none. */
    static String shortId(String id) {
        return id.substring(id.lastIndexOf('_') + 1);
    }
}
