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
package org.openhab.binding.logreader.internal.searchengine;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class implements logic for regular expression based searching.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class SearchEngine {

    private List<Pattern> matchers;
    private List<Pattern> blacklistingMatchers;

    private long matchCount;

    /**
     * Initialize search patterns.
     *
     * @param patterns search patterns.
     * @param blacklistingPatterns search patterns to bypass results which have found by the initial search patterns.
     *
     */
    public SearchEngine(String patterns, @Nullable String blacklistingPatterns) throws PatternSyntaxException {
        matchers = compilePatterns(patterns);
        blacklistingMatchers = compilePatterns(blacklistingPatterns);
    }

    /**
     * Check if data is matching to one of the provided search patterns.
     *
     * @param data data against search will be done.
     * @return true if one of the search patterns found.
     */
    public boolean isMatching(String data) {
        if (isMatching(matchers, data)) {
            if (notBlacklisted(data)) {
                matchCount++;
                return true;
            }
        }
        return false;
    }

    public long getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(long matchCount) {
        this.matchCount = matchCount;
    }

    public void clearMatchCount() {
        setMatchCount(0);
    }

    /**
     * Split pattern string and precompile search patterns.
     *
     * @param patterns patterns which will handled.
     * @return list of precompiled patterns. If pattern parameter is null, empty list is returned.
     */
    private List<Pattern> compilePatterns(@Nullable String patterns) throws PatternSyntaxException {
        List<Pattern> patternsList = new ArrayList<>();
        if (patterns != null && !patterns.isEmpty()) {
            String list[] = patterns.split("\\|");
            if (list.length > 0) {
                for (String patternStr : list) {
                    patternsList.add(Pattern.compile(patternStr));
                }
            }
        }
        return patternsList;
    }

    private boolean notBlacklisted(String data) {
        return !isMatching(blacklistingMatchers, data);
    }

    private boolean isMatching(@Nullable List<Pattern> patterns, String data) {
        if (patterns != null) {
            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(data);
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }
}
