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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.persistence.HistoricItem;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.voice.chat.Card;
import org.eclipse.smarthome.core.voice.chat.CardRegistry;
import org.eclipse.smarthome.core.voice.chat.Component;
import org.eclipse.smarthome.core.voice.text.Intent;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.eclipse.smarthome.core.voice.text.InterpretationResult;
import org.eclipse.smarthome.core.voice.text.ItemResolver;
import org.eclipse.smarthome.model.persistence.extensions.PersistenceExtensions;
import org.openhab.voice.opennlp.AbstractItemIntentInterpreter;
import org.openhab.voice.opennlp.AnswerFormatter;
import org.openhab.voice.opennlp.Skill;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Reference;

/**
 * This {@link Skill} tries to retrieves when the matching item (only supports a single item for now) was changed and
 * displays a card with an HbTimeline component.
 *
 * @author Yannick Schaus - Initial contribution
 * @author Laurent Garnier - consider extended Skill interface + null annotations added
 */
@NonNullByDefault
@org.osgi.service.component.annotations.Component(service = Skill.class)
public class HistoryLastChangesSkill extends AbstractItemIntentInterpreter {

    private @NonNullByDefault({}) CardRegistry cardRegistry;

    @Override
    public String getIntentId() {
        return "get-history-last-changes";
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
            return interpretation;
        }

        Set<String> tags = intent.getEntities().entrySet().stream().map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.toSet());

        Card card = new Card("HbCard");
        card.addTags(tags);
        card.updateTimestamp();
        card.setEphemeral(true);
        card.setAddToDeckDenied(true);

        Component timeline = new Component("HbTimeline");

        if (matchedItems.size() == 1) {
            Item item = matchedItems.stream().findFirst().get();
            HistoricItem historicItem = PersistenceExtensions.previousState(item, false); // TODO figure out a solution
                                                                                          // for rrd4j

            if (historicItem == null) {
                interpretation.setAnswer(formatter.getRandomAnswer("answer_nothing_found"));
                interpretation.setHint(formatter.getRandomAnswer("no_historical_data"));
                return interpretation;
            }

            card.setTitle(item.getLabel());
            card.setSubtitle(item.getName());

            DateFormat dateFormat = new SimpleDateFormat();
            Component pastTimelineEntry = new Component("HbTimelineEntry");
            pastTimelineEntry.addConfig("title", formatState(item, historicItem.getState()));
            pastTimelineEntry.addConfig("subtitle", dateFormat.format(historicItem.getTimestamp()));
            timeline.addComponent("main", pastTimelineEntry);

            Component nowTimelineEntry = new Component("HbTimelineEntry");
            nowTimelineEntry.addConfig("title", formatState(item, historicItem.getState()));
            nowTimelineEntry.addConfig("subtitle", dateFormat.format(new Date()));
            timeline.addComponent("main", nowTimelineEntry);
        } else {
            interpretation.setAnswer(formatter.getRandomAnswer("multiple_items_error"));
            return interpretation;
        }

        card.addComponent("main", timeline);

        this.cardRegistry.add(card);

        interpretation.setAnswer(formatter.getRandomAnswer("info_found_simple"));
        interpretation.setCard(card);
        return interpretation;
    }

    private @Nullable String formatState(Item item, State state) {
        if (item.getStateDescription() != null) {
            StateDescription stateDescription = item.getStateDescription();
            if (stateDescription != null) {
                final String pattern = stateDescription.getPattern();
                if (pattern != null) {
                    try {
                        String transformedState = TransformationHelper.transform(
                                FrameworkUtil.getBundle(HistoryLastChangesSkill.class).getBundleContext(), pattern,
                                state.toString());
                        if (transformedState != null && transformedState.equals(state.toString())) {
                            return state.format(pattern);
                        } else {
                            return transformedState;
                        }
                    } catch (NoClassDefFoundError | TransformationException ex) {
                        // TransformationHelper is optional dependency, so ignore if class not found
                        // return state as it is without transformation
                        return state.toString();
                    }
                } else {
                    return state.toString();
                }
            } else {
                return state.toString();
            }
        } else {
            return state.toString();
        }
    }

    @Reference
    protected void setItemResolver(ItemResolver itemResolver) {
        this.itemResolver = itemResolver;
    }

    protected void unsetItemResolver(ItemResolver itemResolver) {
        this.itemResolver = null;
    }

    @Reference
    protected void setCardRegistry(CardRegistry cardRegistry) {
        this.cardRegistry = cardRegistry;
    }

    protected void unsetCardRegistry(CardRegistry cardRegistry) {
        this.cardRegistry = null;
    }

}
