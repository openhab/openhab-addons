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
package org.openhab.binding.tr064.internal.phonebook;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tr064.internal.Tr064RootHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhonebookActions} is responsible for handling phonebook actions
 *
 * @author Jan N. Klug - Initial contribution
 */
@ThingActionsScope(name = "tr064")
@NonNullByDefault
public class PhonebookActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(PhonebookActions.class);

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
                return handler.getPhonebookByName(phonebook).flatMap(p -> p.lookupNumber(phonenumber, matchCountInt))
                        .orElse(phonenumber);
            } else {
                Collection<Phonebook> phonebooks = handler.getPhonebooks();
                return phonebooks.stream().map(p -> p.lookupNumber(phonenumber, matchCountInt))
                        .filter(Optional::isPresent).map(Optional::get).findAny().orElse(phonenumber);
            }
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
        return ((PhonebookActions) actions).phonebookLookup(phonenumber, phonebook, matchCount);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof Tr064RootHandler) {
            this.handler = (Tr064RootHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
