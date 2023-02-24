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
package org.openhab.binding.tr064.internal;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.soap.SOAPMessage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.tr064.internal.dto.scpd.root.SCPDServiceType;
import org.openhab.binding.tr064.internal.phonebook.Phonebook;
import org.openhab.binding.tr064.internal.soap.SOAPRequest;
import org.openhab.binding.tr064.internal.util.SCPDUtil;
import org.openhab.binding.tr064.internal.util.Util;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FritzboxActions} is responsible for handling phone book actions
 *
 * @author Jan N. Klug - Initial contribution
 */
@ThingActionsScope(name = "tr064")
@NonNullByDefault
public class FritzboxActions implements ThingActions {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy_HHmm");

    private final Logger logger = LoggerFactory.getLogger(FritzboxActions.class);

    private @Nullable Tr064RootHandler handler;

    @RuleAction(label = "@text/phonebookLookupActionLabel", description = "@text/phonebookLookupActionDescription")
    public @ActionOutput(name = "name", label = "@text/phonebookLookupActionOutputLabel", description = "@text/phonebookLookupActionOutputDescription", type = "java.lang.String") String phonebookLookup(
            @ActionInput(name = "phonenumber", label = "@text/phonebookLookupActionInputPhoneNumberLabel", description = "@text/phonebookLookupActionInputPhoneNumberDescription", type = "java.lang.String", required = true) @Nullable String phonenumber,
            @ActionInput(name = "matches", label = "@text/phonebookLookupActionInputMatchesLabel", description = "@text/phonebookLookupActionInputMatchesDescription", type = "java.lang.Integer") @Nullable Integer matchCount) {
        return phonebookLookup(phonenumber, null, matchCount);
    }

    @RuleAction(label = "@text/phonebookLookupActionLabel", description = "@text/phonebookLookupActionDescription")
    public @ActionOutput(name = "name", label = "@text/phonebookLookupActionOutputLabel", description = "@text/phonebookLookupActionOutputDescription", type = "java.lang.String") String phonebookLookup(
            @ActionInput(name = "phonenumber", label = "@text/phonebookLookupActionInputPhoneNumberLabel", description = "@text/phonebookLookupActionInputPhoneNumberDescription", type = "java.lang.String", required = true) @Nullable String phonenumber) {
        return phonebookLookup(phonenumber, null, null);
    }

    @RuleAction(label = "@text/phonebookLookupActionLabel", description = "@text/phonebookLookupActionDescription")
    public @ActionOutput(name = "name", label = "@text/phonebookLookupActionOutputLabel", description = "@text/phonebookLookupActionOutputDescription", type = "java.lang.String") String phonebookLookup(
            @ActionInput(name = "phonenumber", label = "@text/phonebookLookupActionInputPhoneNumberLabel", description = "@text/phonebookLookupActionInputPhoneNumberDescription", type = "java.lang.String", required = true) @Nullable String phonenumber,
            @ActionInput(name = "phonebook", label = "@text/phonebookLookupActionInputPhoneBookLabel", description = "@text/phonebookLookupActionInputPhoneBookDescription", type = "java.lang.String") @Nullable String phonebook) {
        return phonebookLookup(phonenumber, phonebook, null);
    }

    @RuleAction(label = "@text/phonebookLookupActionLabel", description = "@text/phonebookLookupActionDescription")
    public @ActionOutput(name = "name", label = "@text/phonebookLookupActionOutputLabel", description = "@text/phonebookLookupActionOutputDescription", type = "java.lang.String") String phonebookLookup(
            @ActionInput(name = "phonenumber", label = "@text/phonebookLookupActionInputPhoneNumberLabel", description = "@text/phonebookLookupActionInputPhoneNumberDescription", type = "java.lang.String", required = true) @Nullable String phonenumber,
            @ActionInput(name = "phonebook", label = "@text/phonebookLookupActionInputPhoneBookLabel", description = "@text/phonebookLookupActionInputPhoneBookDescription", type = "java.lang.String") @Nullable String phonebook,
            @ActionInput(name = "matches", label = "@text/phonebookLookupActionInputMatchesLabel", description = "@text/phonebookLookupActionInputMatchesDescription", type = "java.lang.Integer") @Nullable Integer matchCount) {
        if (phonenumber == null) {
            logger.warn("Cannot lookup a missing number.");
            return "";
        }

        final Tr064RootHandler handler = this.handler;
        if (handler == null) {
            logger.info("Handler is null, cannot lookup number.");
            return phonenumber;
        } else {
            int matchCountInt = matchCount == null ? 0 : matchCount;
            if (phonebook != null && !phonebook.isEmpty()) {
                return Objects.requireNonNull(handler.getPhonebookByName(phonebook)
                        .flatMap(p -> p.lookupNumber(phonenumber, matchCountInt)).orElse(phonenumber));
            } else {
                Collection<Phonebook> phonebooks = handler.getPhonebooks();
                return Objects.requireNonNull(phonebooks.stream().map(p -> p.lookupNumber(phonenumber, matchCountInt))
                        .filter(Optional::isPresent).map(Optional::get).findAny().orElse(phonenumber));
            }
        }
    }

    @RuleAction(label = "create configuration backup", description = "Creates a configuration backup")
    public void createConfigurationBackup() {
        Tr064RootHandler handler = this.handler;

        if (handler == null) {
            logger.warn("TR064 action service ThingHandler is null!");
            return;
        }

        SCPDUtil scpdUtil = handler.getSCPDUtil();
        if (scpdUtil == null) {
            logger.warn("Could not get SCPDUtil, handler seems to be uninitialized.");
            return;
        }

        Optional<SCPDServiceType> scpdService = scpdUtil.getDevice("")
                .flatMap(deviceType -> deviceType.getServiceList().stream().filter(
                        service -> service.getServiceId().equals("urn:DeviceConfig-com:serviceId:DeviceConfig1"))
                        .findFirst());
        if (scpdService.isEmpty()) {
            logger.warn("Could not get service.");
            return;
        }

        BackupConfiguration configuration = handler.getBackupConfiguration();
        try {
            SOAPRequest soapRequest = new SOAPRequest(scpdService.get(), "X_AVM-DE_GetConfigFile",
                    Map.of("NewX_AVM-DE_Password", configuration.password));
            SOAPMessage soapMessage = handler.getSOAPConnector().doSOAPRequestUncached(soapRequest);
            String configBackupURL = Util.getSOAPElement(soapMessage, "NewX_AVM-DE_ConfigFileUrl")
                    .orElseThrow(() -> new Tr064CommunicationException("Empty URL"));

            ContentResponse content = handler.getUrl(configBackupURL);

            String fileName = String.format("%s %s.export", handler.getFriendlyName(),
                    DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            Path filePath = FileSystems.getDefault().getPath(configuration.directory, fileName);
            Path folder = filePath.getParent();
            if (folder != null) {
                Files.createDirectories(folder);
            }
            Files.write(filePath, content.getContent());
        } catch (Tr064CommunicationException e) {
            logger.warn("Failed to get configuration backup URL: {}", e.getMessage());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.warn("Failed to get remote backup file: {}", e.getMessage());
        } catch (IOException e) {
            logger.warn("Failed to create backup file: {}", e.getMessage());
        }
    }

    public static String phonebookLookup(ThingActions actions, @Nullable String phonenumber,
            @Nullable Integer matchCount) {
        return phonebookLookup(actions, phonenumber, null, matchCount);
    }

    public static String phonebookLookup(ThingActions actions, @Nullable String phonenumber) {
        return phonebookLookup(actions, phonenumber, null, null);
    }

    public static String phonebookLookup(ThingActions actions, @Nullable String phonenumber,
            @Nullable String phonebook) {
        return phonebookLookup(actions, phonenumber, phonebook, null);
    }

    public static String phonebookLookup(ThingActions actions, @Nullable String phonenumber, @Nullable String phonebook,
            @Nullable Integer matchCount) {
        return ((FritzboxActions) actions).phonebookLookup(phonenumber, phonebook, matchCount);
    }

    public static void createConfigurationBackup(ThingActions actions) {
        ((FritzboxActions) actions).createConfigurationBackup();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (Tr064RootHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    public record BackupConfiguration(String directory, String password) {
    }
}
