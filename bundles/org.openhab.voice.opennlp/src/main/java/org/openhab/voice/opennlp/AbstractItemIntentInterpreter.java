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
package org.openhab.voice.opennlp;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.ItemRegistry;
import org.eclipse.smarthome.core.voice.text.Intent;
import org.eclipse.smarthome.core.voice.text.ItemResolver;
import org.eclipse.smarthome.core.voice.text.UnsupportedLanguageException;

/**
 * An abstract implmentation of a {@link Skill} with helper methods to find items matching an {@link Intent}
 * object and location entities.
 *
 * It also contains a default implementation of the training data sourcing (text file in train/(language)/(intent).txt).
 *
 * @author Yannick Schaus - Initial contribution
 * @author Laurent Garnier - add null annotations
 */
@NonNullByDefault
public abstract class AbstractItemIntentInterpreter implements Skill {

    protected @NonNullByDefault({}) ItemRegistry itemRegistry;
    protected @NonNullByDefault({}) ItemResolver itemResolver;
    protected @Nullable AnswerFormatter answerFormatter;

    /**
     * Returns the items matching the entities in the intent.
     *
     * The resulting items should match the object AND the location if both are provided.
     *
     * @param intent the {@link Intent} containing the entities to match to items' tags.
     * @return the set of matching items
     * @throws UnsupportedLanguageException
     */
    protected Set<Item> findItems(Intent intent) {
        String object = intent.getEntities().get("object");
        String location = intent.getEntities().get("location");

        Set<Item> items = this.itemResolver.getMatchingItems(object, location).collect(Collectors.toSet());

        // expand group items
        for (Item item : items.toArray(new Item[0])) {
            if (item instanceof GroupItem) {
                GroupItem gItem = (GroupItem) item;
                items.addAll(gItem.getMembers());
            }
        }

        return items;
    }

    @Override
    public InputStream getTrainingData(String language) throws UnsupportedLanguageException {
        answerFormatter = new AnswerFormatter(language);

        InputStream trainingData = Skill.class.getClassLoader()
                .getResourceAsStream("train/" + language + "/" + this.getIntentId() + ".txt");

        if (trainingData == null) {
            throw new UnsupportedLanguageException(language);
        }

        return trainingData;
    }
}
