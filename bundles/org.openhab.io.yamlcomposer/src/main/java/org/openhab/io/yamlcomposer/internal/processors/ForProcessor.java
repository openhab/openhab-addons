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
package org.openhab.io.yamlcomposer.internal.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.directives.ForDirective;
import org.openhab.io.yamlcomposer.internal.placeholders.ForPlaceholder;

/**
 * Parses {@link ForPlaceholder} directives into {@link ForDirective} instances.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class ForProcessor implements PlaceholderProcessor<ForPlaceholder> {

    private static final Pattern FOR_PATTERN = Pattern.compile("^(.+?)\\s+in\\s+(.+)$");

    @Override
    public Class<ForPlaceholder> getPlaceholderType() {
        return ForPlaceholder.class;
    }

    @Override
    public @Nullable Object process(ForPlaceholder placeholder, RecursiveTransformer transformer) {
        Object value = placeholder.value();
        if (value == null) {
            return null;
        }
        String raw = value.toString().trim();

        // 1. Strip quotes if present
        if ((raw.startsWith("\"") && raw.endsWith("\"")) || (raw.startsWith("'") && raw.endsWith("'"))) {
            raw = raw.substring(1, raw.length() - 1).trim();
        }

        // 2. Strip trailing YAML disambiguation comments (# comment)
        Integer commentIndex = ExpressionUtils.findTopLevelIndex(raw, "#");
        String expr = commentIndex != null ? raw.substring(0, commentIndex).trim() : raw;

        // 3. Extract optional 'if <condition>' clause from the end
        @Nullable
        String filterCondition = null;
        Integer ifIndex = ExpressionUtils.findTopLevelIndex(expr, "if");
        if (ifIndex != null) {
            filterCondition = expr.substring(ifIndex + 2).trim();
            expr = expr.substring(0, ifIndex).trim();
        }

        // 4. Parse LHS in RHS
        Matcher matcher = FOR_PATTERN.matcher(expr);
        if (!matcher.matches()) {
            return new ForDirective(List.of(), null, filterCondition, placeholder.sourceLocation());
        }

        // Clean LHS of enclosing brackets/parentheses if present, e.g. (key, value) or [key, value]
        String lhs = matcher.group(1).trim().replaceAll("^[(\\[]|[\\])]$", "").trim();
        String rhs = matcher.group(2).trim();

        // 5. Parse LHS variable names into a list supporting arbitrary unpacking
        String[] rawVars = lhs.split(",");
        List<String> variables = new ArrayList<>();
        for (String var : rawVars) {
            String trimmed = var.trim();
            if (!trimmed.isEmpty()) {
                variables.add(trimmed);
            }
        }

        return new ForDirective(variables, rhs, filterCondition, placeholder.sourceLocation());
    }
}
