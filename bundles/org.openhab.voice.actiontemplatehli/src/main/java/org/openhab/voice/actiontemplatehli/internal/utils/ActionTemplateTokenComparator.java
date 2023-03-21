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

import java.util.Arrays;
import java.util.Collections;

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
public class ActionTemplateTokenComparator extends ActionTemplateBaseComparator {

    private final Logger logger = LoggerFactory.getLogger(ActionTemplateTokenComparator.class);

    public ActionTemplateTokenComparator(String[] tokens, String[] lemmas, String[] tags) {
        super(tokens, lemmas, tags);
    }

    public ActionTemplateComparatorResult countMatches(String[] tokens, String[] lemmas, String[] tags,
            String[] tokensTemplate) {
        if (tokens.length == 0 || tokensTemplate.length == 0) {
            return ActionTemplateComparatorResult.ZERO;
        }
        var tokensTemplateAlternatives = getTokensTemplateAlternatives(tokensTemplate);
        if (tokensTemplateAlternatives == null) {
            return ActionTemplateComparatorResult.ZERO;
        }
        double maxMatchCount = 0;
        @Nullable
        Span dynamicSpan = null;
        for (var tokensTemplateAlternative : tokensTemplateAlternatives) {
            double tokenMatchCount = 0;
            Span currentDynamicSpan = null;
            // avoid use tags if not available for all tokens
            for (int i = 0; i < tokensTemplateAlternative.size(); i++) {
                if (i == tokens.length) {
                    tokenMatchCount = 0;
                    break;
                }
                String token = tokens[i];
                String tokenTemplate = tokensTemplateAlternative.get(i);
                if (tokenTemplate.startsWith("<lemma>")) {
                    if (this.lemmas.length == 0) {
                        logger.warn("Lemmas are not available, this feature can not be used");
                        tokenMatchCount = 0;
                        break;
                    }
                    token = this.lemmas[i];
                    tokenTemplate = tokenTemplate.substring("<lemma>".length());
                } else if (tokenTemplate.startsWith("<tag>")) {
                    if (this.tags.length == 0) {
                        logger.warn("Tags are not available, this feature can not be used");
                        tokenMatchCount = 0;
                        break;
                    }
                    token = this.tags[i];
                    tokenTemplate = tokenTemplate.substring("<tag>".length());
                }
                if (DYNAMIC_PLACEHOLDER_SYMBOL.equals(tokenTemplate)) {
                    if (i + 1 == tokensTemplateAlternative.size()) {
                        // the dynamic placeholder is the last value in the template token array, returning score
                        // note that the dynamic placeholder does not count for score
                        currentDynamicSpan = new Span(i, tokensTemplateAlternative.size());
                        break;
                    }
                    // here we cut and reverse the arrays to run score backwards until the dynamic placeholder
                    var unprocessedTokens = Arrays.copyOfRange(tokens, i, tokens.length);
                    var unprocessedLemmas = lemmas.length > 0 ? Arrays.copyOfRange(lemmas, i, lemmas.length)
                            : new String[] {};
                    var unprocessedTags = tags.length > 0 ? Arrays.copyOfRange(tags, i, tags.length) : new String[] {};
                    var unprocessedTokensTemplate = Arrays.copyOfRange(tokensTemplate, i, tokensTemplate.length);
                    Collections.reverse(Arrays.asList(unprocessedTokens));
                    Collections.reverse(Arrays.asList(unprocessedLemmas));
                    Collections.reverse(Arrays.asList(unprocessedTags));
                    Collections.reverse(Arrays.asList(unprocessedTokensTemplate));
                    if (DYNAMIC_PLACEHOLDER_SYMBOL.equals(unprocessedTokens[0])) {
                        // here dynamic placeholder should be at the end, but if it's also at the beginning we should
                        // abort
                        logger.warn("Using multiple dynamic placeholders is not supported");
                        tokenMatchCount = 0;
                        break;
                    }
                    var partialScoreResult = this.countMatches(unprocessedTokens, unprocessedLemmas, unprocessedTags,
                            unprocessedTokensTemplate);
                    if (ActionTemplateComparatorResult.ZERO.equals(partialScoreResult)) {
                        tokenMatchCount = 0;
                        break;
                    } else {
                        var partialDynamicSpan = partialScoreResult.dynamicSpan;
                        if (partialDynamicSpan == null) {
                            logger.error(
                                    "dynamic span missed, this should never happen, please open an issue; aborting");
                            tokenMatchCount = 0;
                            break;
                        }
                        tokenMatchCount = tokenMatchCount + partialScoreResult.score + 0.99;
                        currentDynamicSpan = new Span(i, tokens.length - (partialDynamicSpan.getStart()));
                        break;
                    }
                } else {
                    if (tokenTemplate.equals(token)) {
                        tokenMatchCount++;
                    } else {
                        tokenMatchCount = 0;
                        break;
                    }
                }
            }
            // normalize
            tokenMatchCount = tokenMatchCount / (currentDynamicSpan != null ? tokensTemplateAlternative.size() - 1
                    : tokensTemplateAlternative.size());
            if (tokenMatchCount > maxMatchCount) {
                maxMatchCount = tokenMatchCount;
                dynamicSpan = currentDynamicSpan;
            }
        }
        return new ActionTemplateComparatorResult(maxMatchCount, dynamicSpan);
    }

    public ActionTemplateComparatorResult compare(String[] tokensTemplate) {
        var countMatchesResult = countMatches(this.tokens, this.lemmas, this.tags, tokensTemplate);
        // transform to percent
        return new ActionTemplateComparatorResult(
                calculateScore(countMatchesResult.score * tokensTemplate.length, tokensTemplate),
                countMatchesResult.dynamicSpan);
    }

    private double calculateScore(double tokenMatchCount, String[] tokensTemplate) {
        return (tokenMatchCount / tokensTemplate.length) * 100;
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }
}
