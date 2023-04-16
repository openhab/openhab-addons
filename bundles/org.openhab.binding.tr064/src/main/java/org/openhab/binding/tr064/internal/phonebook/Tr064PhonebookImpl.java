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
package org.openhab.binding.tr064.internal.phonebook;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tr064.internal.dto.additions.PhonebooksType;
import org.openhab.binding.tr064.internal.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Tr064PhonebookImpl} class implements a phonebook
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Tr064PhonebookImpl implements Phonebook {
    private final Logger logger = LoggerFactory.getLogger(Tr064PhonebookImpl.class);

    protected Map<String, String> phonebook = new HashMap<>();

    private final HttpClient httpClient;
    private final String phonebookUrl;
    private final int httpTimeout;

    private String phonebookName = "";

    public Tr064PhonebookImpl(HttpClient httpClient, String phonebookUrl, int httpTimeout) {
        this.httpClient = httpClient;
        this.phonebookUrl = phonebookUrl;
        this.httpTimeout = httpTimeout;
        getPhonebook();
    }

    private void getPhonebook() {
        PhonebooksType phonebooksType = Util.getAndUnmarshalXML(httpClient, phonebookUrl, PhonebooksType.class,
                httpTimeout);
        if (phonebooksType == null) {
            logger.warn("Failed to get phonebook with URL '{}'", phonebookUrl);
            return;
        }
        phonebookName = phonebooksType.getPhonebook().getName();

        phonebook = phonebooksType.getPhonebook().getContact().stream().map(contact -> {
            String contactName = contact.getPerson().getRealName();
            if (contactName == null || contactName.isBlank()) {
                return new HashMap<String, String>();
            }
            return contact.getTelephony().getNumber().stream().collect(Collectors.toMap(
                    number -> normalizeNumber(number.getValue()), number -> contactName, this::mergeSameContactNames));
        }).collect(HashMap::new, HashMap::putAll, HashMap::putAll);
        logger.debug("Downloaded phonebook {}: {}", phonebookName, phonebook);
    }

    // in case there are multiple phone entries with same number -> name mapping, i.e. in phonebooks exported from
    // mobiles containing multiple accounts like: local, cloudprovider1, messenger1, messenger2,...
    private String mergeSameContactNames(String nameA, String nameB) {
        if (nameA.equals(nameB)) {
            return nameA;
        }
        throw new IllegalStateException(
                "Found different names for the same number: '" + nameA + "' and '" + nameB + "'");
    }

    @Override
    public String getName() {
        return phonebookName;
    }

    @Override
    public Optional<String> lookupNumber(String number, int matchCount) {
        String normalized = normalizeNumber(number);
        String matchString;
        if (matchCount > 0 && matchCount < normalized.length()) {
            matchString = normalized.substring(normalized.length() - matchCount);
        } else if (matchCount < 0 && (-matchCount) < normalized.length()) {
            matchString = normalized.substring(-matchCount);
        } else {
            matchString = normalized;
        }
        logger.trace("Normalized '{}' to '{}', matchString is '{}'", number, normalized, matchString);
        return matchString.isBlank() ? Optional.empty()
                : phonebook.keySet().stream().filter(n -> n.endsWith(matchString)).findFirst().map(phonebook::get);
    }

    @Override
    public String toString() {
        return "Phonebook{" + "phonebookName='" + phonebookName + "', phonebook=" + phonebook + '}';
    }

    /**
     * normalize a phone number (remove everything except digits and *) for comparison
     *
     * @param number the input phone number string
     * @return normalized phone number string
     */
    public final String normalizeNumber(String number) {
        return number.replaceAll("[^0-9\\*\\+]", "");
    }
}
