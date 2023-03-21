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
package org.openhab.voice.actiontemplatehli.internal.utils;

import static org.openhab.voice.actiontemplatehli.internal.ActionTemplateInterpreterConstants.DYNAMIC_PLACEHOLDER_SYMBOL;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;

/**
 * The {@link ActionTemplateBaseComparator} class represents each configured action
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public abstract class ActionTemplateBaseComparator {
    protected final String[] tokens;
    protected final String[] lemmas;
    protected final String[] tags;

    public ActionTemplateBaseComparator(String[] tokens, String[] lemmas, String[] tags) {
        this.tokens = tokens;
        this.lemmas = lemmas;
        this.tags = tags;
    }

    abstract ActionTemplateComparatorResult compare(String[] tokensTemplate);

    protected abstract Logger getLogger();

    /**
     * Create a matrix of tokens with each row been the alternatives templates after resolving optional and non required
     * tokens
     * 
     * @param tokensTemplate the action template tokens
     * @return tokensTemplateAlternatives
     */
    protected @Nullable ArrayList<ArrayList<String>> getTokensTemplateAlternatives(String[] tokensTemplate) {
        if (tokensTemplate.length == 0) {
            return null;
        }
        var tokensTemplateAlternatives = new ArrayList<ArrayList<String>>();
        // There will be always at least one alternative
        tokensTemplateAlternatives.add(new ArrayList<>());
        // generate all alternatives
        for (int i = 0; i < tokensTemplate.length; i++) {
            var newTokensTemplateAlternatives = new ArrayList<ArrayList<String>>();
            String tokenTemplate = tokensTemplate[i];
            var tokenAlternatives = splitAlternatives(tokenTemplate);
            boolean isTokenRequired = true;
            for (var tokenAlternative : tokenAlternatives) {
                if (!this.isValidTokenAlternative(tokenAlternative, tokenAlternatives, tokensTemplate)) {
                    return null;
                }
                if (tokenAlternative.endsWith("?")) {
                    isTokenRequired = false;
                    tokenAlternative = tokenAlternative.substring(0, tokenAlternative.length() - 1);
                }
                for (var tokensTemplateAlternative : tokensTemplateAlternatives) {
                    var alternativeClone = (ArrayList<String>) tokensTemplateAlternative.clone();
                    alternativeClone.add(tokenAlternative);
                    newTokensTemplateAlternatives.add(alternativeClone);
                }
            }
            if (!isTokenRequired) {
                newTokensTemplateAlternatives.addAll(tokensTemplateAlternatives);
            }
            tokensTemplateAlternatives = newTokensTemplateAlternatives;
        }
        return tokensTemplateAlternatives;
    }

    private String[] splitAlternatives(String template) {
        return Arrays.stream(template.split("\\|")).map(String::trim).toArray(String[]::new);
    }

    protected boolean isValidTokenAlternative(String tokenAlternative, String[] tokenAlternatives,
            String[] tokensTemplate) {
        boolean isTokenRequired = true;
        var token = tokenAlternative;
        if (token.endsWith("?")) {
            isTokenRequired = false;
            token = token.substring(0, token.length() - 1);
        }
        if (token.startsWith("$") && !isTokenRequired) {
            getLogger().warn("Providing the placeholder as a non required token is not allowed");
            return false;
        }
        if (DYNAMIC_PLACEHOLDER_SYMBOL.equals(token)) {
            if (tokenAlternatives.length > 1) {
                getLogger().warn("Providing the dynamic placeholder as an optional token is not allowed");
                return false;
            }
            if (Arrays.stream(tokensTemplate).filter(t -> !t.contains("?")).count() == 1) {
                getLogger().warn("Providing the dynamic placeholder alone is not allowed");
                return false;
            }
            if (!isTokenRequired) {
                getLogger().warn("Providing the dynamic placeholder as a non required token is not allowed");
                return false;
            }
            return true;
        }
        return true;
    }
}
