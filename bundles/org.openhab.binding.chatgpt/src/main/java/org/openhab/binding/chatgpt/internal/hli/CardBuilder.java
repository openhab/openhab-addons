/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.chatgpt.internal.hli;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.hli.Card;
import org.openhab.core.hli.CardRegistry;
import org.openhab.core.hli.Component;
import org.openhab.core.hli.Intent;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.Metadata;
import org.openhab.core.items.MetadataKey;
import org.openhab.core.items.MetadataRegistry;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;

/**
 * Retrieves a {@link Card} to present as part of HABot's reply from a provided {@link Intent}.
 *
 * First, try to look up in the card registry ("card deck") for a card previously saved and having attributes (object
 * and location if both are provided) matching the recognized entities.
 *
 * If there's a match, simply use that {@link Card} - this allows the user to design new customized cards and control
 * exactly what they want to see. If no card was found, build one on the fly using the provided matching items. Note
 * that the user will be allowed the opportunity in the chat UI to add those generated cards to the card deck, with the
 * appropriate tags, and from there edit them so that they would appear with the eventual customizations the next time.
 *
 * @author Yannick Schaus - Initial contribution
 * @author Artur Fedjukevits - Refactored code
 */
@NonNullByDefault
@org.osgi.service.component.annotations.Component(service = CardBuilder.class, immediate = true)
public class CardBuilder {

    private static final int MAX_RIGHT_SLOT_LENGTH = 10;
    private static final List<String> HABOT_CONFIG_KEYS = List.of("label", "sublabel", "leftIcon", "leftLetter",
            "leftColor");
    private static final String HABOT_METADATA_NAMESPACE = "habot";

    private final CardRegistry cardRegistry;
    private final MetadataRegistry metadataRegistry;

    @Activate
    public CardBuilder(@Reference CardRegistry cardRegistry, @Reference MetadataRegistry metadataRegistry) {
        this.cardRegistry = cardRegistry;
        this.metadataRegistry = metadataRegistry;
    }

    /**
     * Retrieves or build a card for the specified intent and matched items
     *
     * @param intent the intent including entities
     * @param matchedItems the matched items
     * @return the card (either retrieved or built)
     */
    public Card buildCard(Intent intent, Collection<Item> matchedItems) {
        return findExistingCard(intent).orElseGet(() -> createNewCard(intent, matchedItems));
    }

    /**
     * Builds a card with a chart from an intent and matched items
     *
     * @param intent the intent
     * @param matchedItems the matched items
     * @param period the chart period
     * @return the card
     */
    public Card buildChartCard(Intent intent, Collection<Item> matchedItems, String period) {
        var card = createBaseCard(intent);
        var nonGroupItems = getNonGroupItems(matchedItems);

        configureChartCardTitleAndSubtitle(card, intent, matchedItems, period, nonGroupItems);
        addChartComponents(card, nonGroupItems, period);

        cardRegistry.add(card);
        return card;
    }

    private Optional<Card> findExistingCard(Intent intent) {
        String object = intent.getEntities().get("object");
        String location = intent.getEntities().get("location");

        return cardRegistry.getCardMatchingAttributes(object, location).stream()
                .filter(card -> !card.isNotReuseableInChat() && !card.isEphemeral()).findFirst()
                .map(this::updateAndReturnExistingCard);
    }

    private Card updateAndReturnExistingCard(Card card) {
        card.updateTimestamp();
        cardRegistry.update(card);
        return card;
    }

    private Card createNewCard(Intent intent, Collection<Item> matchedItems) {
        var card = createBaseCard(intent);
        var nonGroupItems = getNonGroupItems(matchedItems);

        if (nonGroupItems.size() == 1) {
            configureSingleItemCard(card, nonGroupItems.get(0));
        } else {
            configureMultiItemCard(card, intent, matchedItems, nonGroupItems);
        }

        cardRegistry.add(card);
        return card;
    }

    private Card createBaseCard(Intent intent) {
        var card = new Card("HbCard");

        Optional.ofNullable(intent.getEntities().get("object")).ifPresent(card::addObjectAttribute);
        Optional.ofNullable(intent.getEntities().get("location")).ifPresent(card::addLocationAttribute);

        card.setEphemeral(true);
        card.addConfig("bigger", true);
        card.updateTimestamp();

        return card;
    }

    private List<Item> getNonGroupItems(Collection<Item> matchedItems) {
        return matchedItems.stream().filter(item -> !(item instanceof GroupItem)).toList();
    }

    private void configureSingleItemCard(Card card, Item item) {
        Metadata metadata = getHabotMetadata(item);

        card.setTitle(item.getLabel());
        card.setSubtitle(item.getName());

        switch (item.getType()) {
            case CoreItemFactory.SWITCH -> addSwitchComponent(card, item.getName());
            case CoreItemFactory.DIMMER -> configureDimmerCard(card, item, metadata);
            case CoreItemFactory.ROLLERSHUTTER -> configureShutterCard(card, item.getName());
            case CoreItemFactory.PLAYER -> configurePlayerCard(card, item.getName());
            case CoreItemFactory.COLOR -> configureColorCard(card, item.getName());
            default -> configureDefaultItemCard(card, item, metadata);
        }
    }

    private void configureMultiItemCard(Card card, Intent intent, Collection<Item> matchedItems,
            List<Item> nonGroupItems) {
        GroupItem commonGroup = findMatchingGroup(matchedItems);

        if (commonGroup != null) {
            configureGroupCard(card, commonGroup);
        } else {
            card.setTitle(String.join(", ", intent.getEntities().values()));
        }

        card.setSubtitle(nonGroupItems.size() + " items");
        addItemsList(card, nonGroupItems);
    }

    private void configureChartCardTitleAndSubtitle(Card card, Intent intent, Collection<Item> matchedItems,
            String period, List<Item> nonGroupItems) {
        if (nonGroupItems.size() == 1) {
            Item item = nonGroupItems.get(0);
            card.setTitle(item.getLabel());
            card.setSubtitle(period + " - " + item.getName());
        } else {
            GroupItem commonGroup = findMatchingGroup(matchedItems);
            if (commonGroup != null) {
                configureGroupCard(card, commonGroup);
            } else {
                var title = intent.getEntities().entrySet().stream().filter(entry -> !"period".equals(entry.getKey()))
                        .map(Map.Entry::getValue).collect(Collectors.joining(", "));
                card.setTitle(title);
            }
            card.setSubtitle(period + " - " + nonGroupItems.size() + " items");
        }
    }

    private void configureGroupCard(Card card, GroupItem group) {
        card.setTitle(group.getLabel());

        Item baseItem = group.getBaseItem();
        if (baseItem != null && CoreItemFactory.SWITCH.equals(baseItem.getType())) {
            addSwitchComponent(card, group.getName());
        } else if (!group.getState().toString().isEmpty()) {
            addSingleValueComponent(card, group.getName(), "right");
        }
    }

    private void configureDimmerCard(Card card, Item item, @Nullable Metadata metadata) {
        boolean buildKnob = metadata != null && "knob".equals(metadata.getConfiguration().get("control"));

        if (item.hasTag("capability:Switchable")) {
            addSwitchComponent(card, item.getName());
        } else if (!buildKnob) {
            addSingleValueComponent(card, item.getName(), "right");
        }

        var containerComponent = new Component("HbContainer");
        containerComponent.addConfig("classes", new String[] { "full-width", "text-center" });

        if (buildKnob) {
            var knobComponent = new Component("HbKnob");
            knobComponent.addConfig("item", item.getName());
            knobComponent.addConfig("size", "200px");
            knobComponent.addConfig("textSize", "2rem");
            knobComponent.addConfig("color", "primary");
            containerComponent.addComponent("main", knobComponent);
        } else {
            var sliderComponent = new Component("HbSlider");
            sliderComponent.addConfig("item", item.getName());
            containerComponent.addComponent("main", sliderComponent);
        }

        card.addComponent("main", containerComponent);
    }

    private void configureShutterCard(Card card, String itemName) {
        addSingleValueComponent(card, itemName, "right");

        var containerComponent = new Component("HbContainer");
        containerComponent.addConfig("classes", new String[] { "full-width", "text-center" });

        var shutterComponent = new Component("HbShutterControl");
        shutterComponent.addConfig("item", itemName);
        shutterComponent.addConfig("size", "lg");
        shutterComponent.addConfig("rounded", true);
        shutterComponent.addConfig("glossy", true);
        shutterComponent.addConfig("push", true);
        shutterComponent.addConfig("stopIcon", "close");

        containerComponent.addComponent("main", shutterComponent);
        card.addComponent("main", containerComponent);
    }

    private void configurePlayerCard(Card card, String itemName) {
        var containerComponent = new Component("HbContainer");
        containerComponent.addConfig("classes", new String[] { "full-width", "text-center" });

        var playerComponent = new Component("HbPlayer");
        playerComponent.addConfig("item", itemName);
        playerComponent.addConfig("size", "lg");

        containerComponent.addComponent("main", playerComponent);
        card.addComponent("main", containerComponent);
    }

    private void configureColorCard(Card card, String itemName) {
        var colorPickerComponent = new Component("HbColorPicker");
        colorPickerComponent.addConfig("item", itemName);
        card.addComponent("right", colorPickerComponent);

        addSwitchComponent(card, itemName);

        var containerComponent = new Component("HbContainer");
        containerComponent.addConfig("classes", new String[] { "full-width", "text-center" });

        var sliderComponent = new Component("HbSlider");
        sliderComponent.addConfig("item", itemName);
        containerComponent.addComponent("main", sliderComponent);

        card.addComponent("main", containerComponent);
    }

    private void configureDefaultItemCard(Card card, Item item, @Nullable Metadata metadata) {

        if (isImageItem(item, metadata)) {
            var imageComponent = new Component("HbImage");
            imageComponent.addConfig("item", item.getName());
            card.addComponent("media", imageComponent);
        } else {
            var formattedState = formatItemState(item);
            var component = new Component("HbSingleItemValue");
            component.addConfig("item", item.getName());

            String slot = formattedState.length() < MAX_RIGHT_SLOT_LENGTH ? "right" : "main";
            card.addComponent(slot, component);
        }
    }

    private boolean isImageItem(Item item, @Nullable Metadata metadata) {
        if (CoreItemFactory.IMAGE.equals(item.getType())) {
            return true;
        }
        return metadata != null && metadata.getConfiguration().containsKey("imageSitemap");
    }

    private String formatItemState(Item item) {
        try {
            return formatState(item, item.getState());
        } catch (TransformationException e) {
            return item.getState().toString();
        }
    }

    private void addSwitchComponent(Card card, String itemName) {
        var switchComponent = new Component("HbSwitch");
        switchComponent.addConfig("item", itemName);
        card.addComponent("right", switchComponent);
    }

    private void addSingleValueComponent(Card card, String itemName, String slot) {
        var component = new Component("HbSingleItemValue");
        component.addConfig("item", itemName);
        card.addComponent(slot, component);
    }

    private void addItemsList(Card card, List<Item> items) {
        var listComponent = new Component("HbList");

        items.forEach(item -> {
            var listItemComponent = createListItemComponent(item);
            listComponent.addComponent("items", listItemComponent);
        });

        card.addComponent("list", listComponent);
    }

    private Component createListItemComponent(Item item) {
        var listItem = new Component("HbListItem");
        listItem.addConfig("item", item.getName());
        listItem.addConfig("label", item.getLabel());

        Metadata metadata = getHabotMetadata(item);
        if (metadata != null) {
            HABOT_CONFIG_KEYS.forEach(configKey -> {
                Object value = metadata.getConfiguration().get(configKey);
                if (value != null) {
                    listItem.addConfig(configKey, value);
                }
            });
        }

        return listItem;
    }

    private void addChartComponents(Card card, List<Item> items, String period) {
        var chartComponent = new Component("HbChartImage");
        var itemNames = items.stream().map(Item::getName).toArray(String[]::new);
        chartComponent.addConfig("items", itemNames);
        chartComponent.addConfig("period", period);

        var analyzeButton = new Component("HbAnalyzeActionButton");
        analyzeButton.addConfig("items", itemNames);
        analyzeButton.addConfig("period", period);

        card.addComponent("media", chartComponent);
        card.addComponent("actions", analyzeButton);
    }

    private @Nullable Metadata getHabotMetadata(Item item) {
        return metadataRegistry.get(new MetadataKey(HABOT_METADATA_NAMESPACE, item.getName()));
    }

    /**
     * Tries to find a Group item to act as the card title.
     * It should contain all other members of the provided collection except itself
     *
     * @param items the group of matching items including an eventual GroupItem to find
     * @return an optional group eligible for the card's title
     */
    private @Nullable GroupItem findMatchingGroup(Collection<Item> items) {
        return items.stream().filter(GroupItem.class::isInstance).map(GroupItem.class::cast)
                .filter(group -> isValidMatchingGroup(group, items)).findFirst().orElse(null);
    }

    private boolean isValidMatchingGroup(GroupItem group, Collection<Item> items) {
        return items.stream().allMatch(item -> item.getName().equals(group.getName())
                || group.getAllMembers().stream().anyMatch(member -> member.getName().contains(item.getName())));
    }

    private String formatState(Item item, State state) throws TransformationException {
        StateDescription stateDescription = item.getStateDescription();
        if (stateDescription == null) {
            return state.toString();
        }

        String pattern = stateDescription.getPattern();
        if (pattern == null) {
            return state.toString();
        }

        try {
            var transformedState = TransformationHelper.transform(pattern, state.toString());
            if (transformedState == null) {
                return state.toString();
            }

            return transformedState.equals(state.toString()) ? state.format(pattern) : transformedState;
        } catch (IllegalArgumentException e) {
            return state.toString();
        }
    }
}
