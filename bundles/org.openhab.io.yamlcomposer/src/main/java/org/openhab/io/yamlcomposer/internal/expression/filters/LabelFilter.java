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
package org.openhab.io.yamlcomposer.internal.expression.filters;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.StringUtils;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;

/**
 * Custom Jinjava filter to convert a string to a label.
 *
 * <p>
 * It inserts spaces before capital letters, replaces '_' , ':' and '-' with spaces,
 * and converts the result to title case.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class LabelFilter implements Filter {
    @Override
    public String getName() {
        return "label";
    }

    @Override
    @NonNullByDefault({})
    public @Nullable Object filter(@Nullable Object var, JinjavaInterpreter interpreter, String... args) {
        if (var == null) {
            return null;
        }
        String input = var.toString();
        // Insert space before capital letters, replace _ : and - with space
        String result = input.replaceAll("([a-z])([A-Z])", "$1 $2").replaceAll("[_:-]+", " ");
        // Title case
        result = StringUtils.capitalizeByWhitespace(result);
        return result;
    }
}
