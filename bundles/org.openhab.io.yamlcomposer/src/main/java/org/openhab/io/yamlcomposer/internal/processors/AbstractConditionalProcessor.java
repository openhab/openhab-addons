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

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.yamlcomposer.internal.BufferedLogger;
import org.openhab.io.yamlcomposer.internal.StringInterpolator;
import org.openhab.io.yamlcomposer.internal.core.RecursiveTransformer;
import org.openhab.io.yamlcomposer.internal.expression.ExpressionEvaluator;

/**
 * Base class for resolving {@link IfPlaceholder} and the {@link ElseIfPlaceholder} in YAML models.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractConditionalProcessor {
    protected final BufferedLogger logger;
    protected final Consumer<String> envVarCallback;

    protected AbstractConditionalProcessor(BufferedLogger logger, Consumer<String> envVarCallback) {
        this.logger = logger;
        this.envVarCallback = envVarCallback;
    }

    /**
     * Handle the case of
     *
     * <pre>{@code
     * foo:
     *   !if "true expression":
     *     bar: include_me
     *   !elseif "false or true expression":
     *     baz: exclude_me
     * }</pre>
     *
     * We want this to resolve to:
     *
     * <pre>{@code
     * foo:
     *   bar: include_me
     * }</pre>
     *
     * Since the !if tag is applied to a scalar node (i.e. the key),
     * the IfPlaceholder's value only contains the expression, not the entire mapping.
     *
     * We'll let RecursiveTransformer promote the value to the parent mapping
     * if the condition is true, and remove it if false.
     */
    protected @Nullable Boolean processSimpleSyntax(@Nullable Object value, String sourceLocation,
            RecursiveTransformer recursiveTransformer) {

        if (value instanceof Boolean || value instanceof Number || value instanceof String) {
            String exprStr = value.toString();
            if (value instanceof String) {
                // Strip trailing YAML disambiguation comments (# comment)
                Integer commentIndex = ExpressionUtils.findTopLevelIndex(exprStr, "#");
                if (commentIndex != null) {
                    exprStr = exprStr.substring(0, commentIndex).trim();
                }
            }

            Object result = StringInterpolator.evaluateExpression(exprStr, recursiveTransformer.getVariables(),
                    envVarCallback, logger.getLogSession(), sourceLocation);
            return ExpressionEvaluator.isTruthy(result);
        }

        return null;
    }
}
