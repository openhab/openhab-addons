/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.util.Span;

/**
 * The {@link ActionTemplateBaseComparator} class represents each configured action
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateDiceComparator extends ActionTemplateBaseComparator {
    private final Logger logger = LoggerFactory.getLogger(ActionTemplateDiceComparator.class);
    private final double threshold;

    public ActionTemplateDiceComparator(String[] tokens, String[] lemmas, String[] tags, double threshold) {
        super(tokens, lemmas, tags);
        this.threshold = threshold;
    }

    @Override
    public ActionTemplateComparatorResult compare(String[] tokensTemplate) {
        if (tokens.length == 0 || tokensTemplate.length == 0) {
            return ActionTemplateComparatorResult.ZERO;
        }
        var tokensTemplateAlternatives = getTokensTemplateAlternatives(tokensTemplate);
        if (tokensTemplateAlternatives == null) {
            return ActionTemplateComparatorResult.ZERO;
        }
        double score = 0;
        @Nullable
        Span dynamicSpan = null;
        for (var tokensTemplateAlternative : tokensTemplateAlternatives) {
            if (tokensTemplateAlternative.size() > tokens.length) {
                continue;
            }
            var tokensList = Arrays.stream(tokens).collect(Collectors.toList());
            var lemmasList = Arrays.stream(lemmas).collect(Collectors.toList());
            var tagsList = Arrays.stream(tags).collect(Collectors.toList());
            Span currentDynamicSpan = null;
            var dynamicPlaceholdersCount = tokensTemplateAlternative.stream().filter(DYNAMIC_PLACEHOLDER_SYMBOL::equals)
                    .count();
            if (dynamicPlaceholdersCount > 1) {
                logger.warn("Using multiple dynamic placeholders is not supported");
                continue;
            }
            if (dynamicPlaceholdersCount == 1) {
                var dynamicPlaceholderIndex = tokensTemplateAlternative.indexOf(DYNAMIC_PLACEHOLDER_SYMBOL);
                var dynamicPlaceholderEndIndex = tokensList.size()
                        - (tokensTemplateAlternative.size() - (dynamicPlaceholderIndex + 1));
                var templateDynamicPlaceholderEndIndex = dynamicPlaceholderIndex + 1;
                if (dynamicPlaceholderIndex > dynamicPlaceholderEndIndex) {
                    continue;
                }
                currentDynamicSpan = new Span(dynamicPlaceholderIndex - 1, dynamicPlaceholderEndIndex);
                var newTokenList = tokensList.subList(0, dynamicPlaceholderIndex);
                newTokenList.addAll(tokensList.subList(dynamicPlaceholderEndIndex, tokensList.size()));
                tokensList = newTokenList;
                if (lemmasList.size() > 0) {
                    var newLemmasList = lemmasList.subList(0, dynamicPlaceholderIndex);
                    newLemmasList.addAll(lemmasList.subList(dynamicPlaceholderEndIndex, lemmasList.size()));
                    lemmasList = newLemmasList;
                }
                if (tagsList.size() > 0) {
                    var newTagsList = tagsList.subList(0, dynamicPlaceholderIndex);
                    newTagsList.addAll(tagsList.subList(dynamicPlaceholderEndIndex, tagsList.size()));
                    tagsList = newTagsList;
                }
                var newTokensTemplateAlternative = tokensTemplateAlternative.subList(0, dynamicPlaceholderIndex);
                newTokensTemplateAlternative.addAll(tokensTemplateAlternative
                        .subList(templateDynamicPlaceholderEndIndex, tokensTemplateAlternative.size()));
                tokensTemplateAlternative = new ArrayList<>(newTokensTemplateAlternative);
            }

            for (int i = 0; i < tokensTemplateAlternative.size(); i++) {
                if (i > tokensList.size()) {
                    break;
                }
                String tokenTemplate = tokensTemplateAlternative.get(i);
                if (tokenTemplate.startsWith("<lemma>")) {
                    if (lemmasList.size() == 0) {
                        logger.warn("Lemmas are not available, this feature can not be used");
                        return ActionTemplateComparatorResult.ZERO;
                    }
                    tokensList.set(i, lemmasList.get(i));
                    tokensTemplateAlternative.set(i, tokenTemplate.substring("<lemma>".length()));
                } else if (tokenTemplate.startsWith("<tag>")) {
                    if (tagsList.size() == 0) {
                        logger.warn("Tags are not available, this feature can not be used");
                        return ActionTemplateComparatorResult.ZERO;
                    }
                    tokensList.set(i, tagsList.get(i));
                    tokensTemplateAlternative.set(i, tokenTemplate.substring("<tag>".length()));
                }
            }
            var currentScore = diceCoefficientOptimized(String.join(" ", tokensList),
                    String.join(" ", tokensTemplateAlternative));
            if (currentDynamicSpan != null) {
                currentScore = currentScore - 0.01;
            }
            if (currentScore > score) {
                score = currentScore;
                dynamicSpan = currentDynamicSpan;
            }
        }
        var scorePercent = score * 100;
        return new ActionTemplateComparatorResult(scorePercent >= threshold ? scorePercent : 0, dynamicSpan);
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    /**
     * From: <a href="https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Dice%27s_coefficient">wikipedia</a>
     * Here's an optimized version of the dice coefficient calculation. It takes
     * advantage of the fact that a bigram of 2 chars can be stored in 1 int, and
     * applies a matching algorithm of O(n*log(n)) instead of O(n*n).
     *
     * @param s The first string
     * @param t The second String
     * @return The dice coefficient between the two input strings. Returns 0 if one
     *         or both of the strings are {@code null}. Also returns 0 if one or both
     *         of the strings contain less than 2 characters and are not equal.
     * @author Jelle Fresen
     */
    public static double diceCoefficientOptimized(String s, String t) {
        // Quick check to catch identical objects:
        if (s.equals(t)) {
            return 1;
        }
        // avoid exception for single character searches
        if (s.length() < 2 || t.length() < 2) {
            return 0;
        }

        // Create the bigrams for string s:
        final int n = s.length() - 1;
        final int[] sPairs = new int[n];
        for (int i = 0; i <= n; i++) {
            if (i == 0) {
                sPairs[i] = s.charAt(i) << 16;
            } else if (i == n) {
                sPairs[i - 1] |= s.charAt(i);
            } else {
                sPairs[i] = (sPairs[i - 1] |= s.charAt(i)) << 16;
            }
        }
        // Create the bigrams for string t:
        final int m = t.length() - 1;
        final int[] tPairs = new int[m];
        for (int i = 0; i <= m; i++) {
            if (i == 0) {
                tPairs[i] = t.charAt(i) << 16;
            } else if (i == m) {
                tPairs[i - 1] |= t.charAt(i);
            } else {
                tPairs[i] = (tPairs[i - 1] |= t.charAt(i)) << 16;
            }
        }
        // Sort the bigram lists:
        Arrays.sort(sPairs);
        Arrays.sort(tPairs);

        // Count the matches:
        int matches = 0, i = 0, j = 0;
        while (i < n && j < m) {
            if (sPairs[i] == tPairs[j]) {
                matches += 2;
                i++;
                j++;
            } else if (sPairs[i] < tPairs[j]) {
                i++;
            } else {
                j++;
            }
        }
        return (double) matches / (n + m);
    }
}
