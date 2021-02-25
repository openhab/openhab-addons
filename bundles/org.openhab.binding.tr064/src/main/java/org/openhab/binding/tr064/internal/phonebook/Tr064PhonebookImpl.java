/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tr064.internal.dto.additions.PhonebooksType;
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

    private Map<String, String> phonebook = new HashMap<>();

    private final HttpClient httpClient;
    private final String phonebookUrl;

    private String phonebookName = "";

    public Tr064PhonebookImpl(HttpClient httpClient, String phonebookUrl) {
        this.httpClient = httpClient;
        this.phonebookUrl = phonebookUrl;
        getPhonebook();
    }

    private void getPhonebook() {
        try {
            ContentResponse contentResponse = httpClient.newRequest(phonebookUrl).method(HttpMethod.GET)
                    .timeout(2, TimeUnit.SECONDS).send();
            InputStream xml = new ByteArrayInputStream(contentResponse.getContent());

            JAXBContext context = JAXBContext.newInstance(PhonebooksType.class);
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StreamSource(xml));
            Unmarshaller um = context.createUnmarshaller();
            PhonebooksType phonebooksType = um.unmarshal(xsr, PhonebooksType.class).getValue();

            phonebookName = phonebooksType.getPhonebook().getName();

            phonebook = phonebooksType.getPhonebook().getContact().stream().map(contact -> {
                String contactName = contact.getPerson().getRealName();
                return contact.getTelephony().getNumber().stream()
                        .collect(Collectors.toMap(number -> normalizeNumber(number.getValue()), number -> contactName,
                                this::mergeSameContactNames));
            }).collect(HashMap::new, HashMap::putAll, HashMap::putAll);
            logger.debug("Downloaded phonebook {}: {}", phonebookName, phonebook);
        } catch (JAXBException | InterruptedException | ExecutionException | TimeoutException | XMLStreamException e) {
            logger.warn("Failed to get phonebook with URL {}:", phonebookUrl, e);
        }
    }

    // in case there are multiple phone entries with same number -> name mapping, i.e. in phonebooks exported from
    // mobiles containing multiple accounts like: local, cloudprovider1, messenger1, messenger2,...
    private String mergeSameContactNames(String nameA, String nameB) {
        if (nameA != null && nameA.equals(nameB)) {
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
        String matchString = matchCount > 0 && matchCount < normalized.length()
                ? normalized.substring(normalized.length() - matchCount)
                : normalized;
        logger.trace("Normalized '{}' to '{}', matchString is '{}'", number, normalized, matchString);
        return matchString.isBlank() ? Optional.empty()
                : phonebook.keySet().stream().filter(n -> n.endsWith(matchString)).findFirst().map(phonebook::get);
    }

    @Override
    public String toString() {
        return "Phonebook{" + "phonebookName='" + phonebookName + "', phonebook=" + phonebook + '}';
    }

    private String normalizeNumber(String number) {
        // Naive normalization: remove all non-digit characters
        return number.replaceAll("[^0-9]\\+\\*", "");
    }
}
