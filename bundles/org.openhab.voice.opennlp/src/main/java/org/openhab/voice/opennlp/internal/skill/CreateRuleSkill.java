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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.voice.chat.Card;
import org.eclipse.smarthome.core.voice.text.Intent;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.eclipse.smarthome.core.voice.text.InterpretationResult;
import org.openhab.voice.opennlp.AbstractItemIntentInterpreter;
import org.openhab.voice.opennlp.AnswerFormatter;
import org.openhab.voice.opennlp.Skill;

/**
 * This {@link Skill} is used to reply with a HbCreateRuleCard
 *
 * @author Yannick Schaus - Initial contribution
 * @author Laurent Garnier - consider extended Skill interface + null annotations added
 */
@NonNullByDefault
@org.osgi.service.component.annotations.Component(service = Skill.class)
public class CreateRuleSkill extends AbstractItemIntentInterpreter {

    @Override
    public String getIntentId() {
        return "create-rule";
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
        Card card = new Card("HbCreateRuleCard");
        // TODO: try to parse a day/time to pre-configure the new rule card
        interpretation.setAnswer(formatter.getRandomAnswer("answer_create_rule"));
        interpretation.setCard(card);
        return interpretation;
    }

}
