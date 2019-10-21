/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.voice.opennlp.internal.skill;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.events.ItemEventFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.voice.text.Intent;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.eclipse.smarthome.core.voice.text.InterpretationResult;
import org.eclipse.smarthome.core.voice.text.ItemResolver;
import org.openhab.voice.opennlp.AbstractItemIntentInterpreter;
import org.openhab.voice.opennlp.AnswerFormatter;
import org.openhab.voice.opennlp.CardBuilder;
import org.openhab.voice.opennlp.Skill;
import org.osgi.service.component.annotations.Reference;

/**
 * This {@link Skill} sets the matching item(s) to the specified numerical value (for dimmers, thermostats etc.) or
 * color (for Color items).
 *
 * @author Yannick Schaus - Initial contribution
 * @author Laurent Garnier - consider extended Skill interface + null annotations added
 * @author Laurent Garnier - remove usage of Google Guava
 */
@NonNullByDefault
@org.osgi.service.component.annotations.Component(service = Skill.class)
public class SetValueSkill extends AbstractItemIntentInterpreter {

    private @NonNullByDefault({}) CardBuilder cardBuilder;
    private @NonNullByDefault({}) EventPublisher eventPublisher;

    @Override
    public String getIntentId() {
        return "set-value";
    }

    @Override
    public boolean isSuitableForChat() {
        return true;
    }

    @Override
    public boolean isSuitableForVoice() {
        return false;
    }

    @Override
    public String interpretForVoice(Intent intent, String language) throws InterpretationException {
        throw new InterpretationException("Voice control not yet supported by the HABot OpenNLP interpreter");
    }

    @Override
    public InterpretationResult interpretForChat(Intent intent, String language) throws InterpretationException {
        InterpretationResult interpretation = new InterpretationResult(language, intent);
        AnswerFormatter formatter = answerFormatter;
        if (formatter == null) {
            formatter = answerFormatter = new AnswerFormatter(language);
        }

        Set<Item> matchedItems = findItems(intent);

        if (intent.getEntities().isEmpty()) {
            interpretation.setAnswer(formatter.getRandomAnswer("general_failure"));
            return interpretation;
        }
        if (matchedItems.isEmpty()) {
            interpretation.setAnswer(formatter.getRandomAnswer("answer_nothing_found"));
            interpretation.setHint(formatter.getStandardTagHint(intent.getEntities()));
        } else {
            interpretation.setMatchedItems(matchedItems);

            if (intent.getEntities().containsKey("color")) {
                interpretSetColor(intent, language, interpretation, matchedItems, formatter);
            } else if (intent.getEntities().containsKey("value")) {
                interpretSetValue(intent, language, interpretation, matchedItems, formatter);
            } else {
                interpretation.setAnswer(formatter.getRandomAnswer("value_misunderstood"));
            }
        }

        return interpretation;
    }

    private void interpretSetColor(Intent intent, String language, InterpretationResult interpretation,
            Set<Item> matchedItems, AnswerFormatter answerFormatter) {
        String colorString = intent.getEntities().get("color");

        // filter out the items which can't receive an HSB command
        List<Item> filteredItems = matchedItems.stream()
                .filter(i -> !(i instanceof GroupItem) && i.getAcceptedCommandTypes().contains(HSBType.class))
                .collect(Collectors.toList());

        String hsbValue;
        try {
            ResourceBundle colors = ResourceBundle.getBundle("colors", new Locale(language));
            hsbValue = colors.getString("color_" + colorString);
        } catch (MissingResourceException e) {
            interpretation.setAnswer(answerFormatter.getRandomAnswer("set_color_unknown",
                    Collections.unmodifiableMap(Collections.singletonMap("color", colorString))));
            return;
        }

        if (filteredItems.isEmpty()) {
            interpretation.setAnswer(answerFormatter.getRandomAnswer("set_color_no_item",
                    Collections.unmodifiableMap(Collections.singletonMap("color", colorString))));
            interpretation.setHint(answerFormatter.getStandardTagHint(intent.getEntities()));
        } else if (filteredItems.size() == 1) {
            interpretation.setCard(cardBuilder.buildCard(intent, filteredItems));
            eventPublisher
                    .post(ItemEventFactory.createCommandEvent(filteredItems.get(0).getName(), new HSBType(hsbValue)));
            interpretation.setAnswer(answerFormatter.getRandomAnswer("set_color_single",
                    Collections.unmodifiableMap(Collections.singletonMap("color", colorString))));
        } else {
            interpretation.setCard(cardBuilder.buildCard(intent, filteredItems));
            for (Item item : filteredItems) {
                eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), new HSBType(hsbValue)));
            }
            Map<String, String> params = new HashMap<>();
            params.put("count", String.valueOf(filteredItems.size()));
            params.put("color", colorString);
            interpretation.setAnswer(
                    answerFormatter.getRandomAnswer("set_color_multiple", Collections.unmodifiableMap(params)));
        }
    }

    private void interpretSetValue(Intent intent, String language, InterpretationResult interpretation,
            Set<Item> matchedItems, AnswerFormatter answerFormatter) {
        String rawValue = intent.getEntities().get("value");

        // Set a color
        String cleanedValue = rawValue.replaceAll("[^0-9\\.,]", "");

        // only consider items which can receive an DecimalType command - includes PercentType, HSBType
        List<Item> filteredItems = matchedItems.stream()
                .filter(i -> !(i instanceof GroupItem) && i.getAcceptedCommandTypes().contains(DecimalType.class)
                        || i.getAcceptedCommandTypes().contains(PercentType.class))
                .collect(Collectors.toList());

        if (filteredItems.isEmpty()) {
            interpretation.setAnswer(answerFormatter.getRandomAnswer("nothing_set_no_item",
                    Collections.unmodifiableMap(Collections.singletonMap("value", rawValue))));
            interpretation.setHint(answerFormatter.getStandardTagHint(intent.getEntities()));
        } else if (filteredItems.size() == 1) {
            DecimalType value = (filteredItems.get(0).getAcceptedCommandTypes().contains(DecimalType.class))
                    ? DecimalType.valueOf(cleanedValue)
                    : PercentType.valueOf(cleanedValue);
            interpretation.setCard(cardBuilder.buildCard(intent, filteredItems));
            eventPublisher.post(ItemEventFactory.createCommandEvent(filteredItems.get(0).getName(), value));
            interpretation.setAnswer(answerFormatter.getRandomAnswer("set_value_single",
                    Collections.unmodifiableMap(Collections.singletonMap("value", rawValue))));
        } else {
            interpretation.setCard(cardBuilder.buildCard(intent, filteredItems));
            for (Item item : filteredItems) {
                DecimalType value = (item.getAcceptedCommandTypes().contains(DecimalType.class))
                        ? DecimalType.valueOf(cleanedValue)
                        : PercentType.valueOf(cleanedValue);
                eventPublisher.post(ItemEventFactory.createCommandEvent(item.getName(), value));
            }
            Map<String, String> params = new HashMap<>();
            params.put("count", String.valueOf(filteredItems.size()));
            params.put("value", rawValue);
            interpretation.setAnswer(
                    answerFormatter.getRandomAnswer("set_value_multiple", Collections.unmodifiableMap(params)));
        }
    }

    @Reference
    protected void setCardBuilder(CardBuilder cardBuilder) {
        this.cardBuilder = cardBuilder;
    }

    protected void unsetCardBuilder(CardBuilder cardBuilder) {
        this.cardBuilder = null;
    }

    @Reference
    protected void setItemResolver(ItemResolver itemResolver) {
        this.itemResolver = itemResolver;
    }

    protected void unsetItemResolver(ItemResolver itemResolver) {
        this.itemResolver = null;
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }
}
