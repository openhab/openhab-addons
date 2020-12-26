/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import javax.xml.transform.stream.StreamSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.tr064.internal.dto.additions.NumberType;
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
            Unmarshaller um = context.createUnmarshaller();
            PhonebooksType phonebooksType = um.unmarshal(new StreamSource(xml), PhonebooksType.class).getValue();

            phonebookName = phonebooksType.getPhonebook().getName();

            phonebook = phonebooksType.getPhonebook().getContact().stream().map(contact -> {
                String contactName = contact.getPerson().getRealName();
                return contact.getTelephony().getNumber().stream()
                        .collect(Collectors.toMap(NumberType::getValue, number -> contactName));
            }).collect(HashMap::new, HashMap::putAll, HashMap::putAll);
            logger.debug("Downloaded phonebook {}: {}", phonebookName, phonebook);
        } catch (JAXBException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("Failed to get phonebook with URL {}:", phonebookUrl, e);
        }
    }

    @Override
    public String getName() {
        return phonebookName;
    }

    @Override
    public Optional<String> lookupNumber(String number, int matchCount) {
        String matchString = matchCount > 0 && matchCount < number.length()
                ? number.substring(number.length() - matchCount)
                : number;
        logger.trace("matchString for '{}' is '{}'", number, matchString);
        return matchString.isBlank() ? Optional.empty()
                : phonebook.keySet().stream().filter(n -> n.endsWith(matchString)).findFirst().map(phonebook::get);
    }

    @Override
    public String toString() {
        return "Phonebook{" + "phonebookName='" + phonebookName + "', phonebook=" + phonebook + '}';
    }
}
