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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.Item;
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
 * This {@link Skill} is used to reply with a card containing a hourly chart of the matching item(s).
 *
 * @author Yannick Schaus - Initial contribution
 * @author Laurent Garnier - consider extended Skill interface + null annotations added
 */
@NonNullByDefault
@org.osgi.service.component.annotations.Component(service = Skill.class)
public class HistoryHourlyGraphSkill extends AbstractItemIntentInterpreter {

    private @NonNullByDefault({}) CardBuilder cardBuilder;

    @Override
    public String getIntentId() {
        return "get-history-hourly";
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

            String period = "h";
            if (intent.getEntities().containsKey("period")) {
                period = intent.getEntities().get("period").concat(period);
            }

            interpretation.setCard(this.cardBuilder.buildChartCard(intent, matchedItems, period));
        }

        interpretation.setAnswer(formatter.getRandomAnswer("info_found_simple"));

        return interpretation;
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
}
