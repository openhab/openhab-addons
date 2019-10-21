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

import java.util.Arrays;
import java.util.Collection;
import java.util.IllegalFormatConversionException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupItem;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.items.Metadata;
import org.eclipse.smarthome.core.items.MetadataKey;
import org.eclipse.smarthome.core.items.MetadataRegistry;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.voice.chat.Card;
import org.eclipse.smarthome.core.voice.chat.CardRegistry;
import org.eclipse.smarthome.core.voice.chat.Component;
import org.eclipse.smarthome.core.voice.text.Intent;
import org.osgi.framework.FrameworkUtil;
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
 * @author Laurent Garnier - class moved + null annotations added
 */
@NonNullByDefault
@org.osgi.service.component.annotations.Component(service = CardBuilder.class, immediate = true)
public class CardBuilder {

    private @NonNullByDefault({}) CardRegistry cardRegistry;
    private @NonNullByDefault({}) MetadataRegistry metadataRegistry;

    /**
     * Retrieves or build a card for the specified intent and matched items
     *
     * @param intent the intent including entities
     * @param matchedItems the matched items
     * @return the card (either retrieved or built)
     */
    public Card buildCard(Intent intent, Collection<Item> matchedItems) {
        String object = intent.getEntities().get("object");
        String location = intent.getEntities().get("location");

        Collection<Card> cardsInRegistry = this.cardRegistry.getCardMatchingAttributes(object, location).stream()
                .filter(c -> !c.isNotReuseableInChat() && !c.isEphemeral()).collect(Collectors.toList());
        if (cardsInRegistry.size() > 0) {
            // don't handle multiple cards, just return the first one
            Card existingCard = cardsInRegistry.iterator().next();
            existingCard.updateTimestamp();
            cardRegistry.update(existingCard);
            return existingCard;
        }

        Card card = new Card("HbCard");
        if (object != null) {
            card.addObjectAttribute(object);
        }
        if (location != null) {
            card.addLocationAttribute(location);
        }
        card.setEphemeral(true);
        card.addConfig("bigger", true);
        card.updateTimestamp();

        Supplier<Stream<Item>> matchingNonGroupItems = () -> matchedItems.stream()
                .filter(i -> !(i instanceof GroupItem));

        if (matchingNonGroupItems.get().count() == 1) {
            Item item = matchingNonGroupItems.get().findFirst().get();
            Metadata metadata = this.metadataRegistry.get(new MetadataKey("habot", item.getName()));

            card.setTitle(item.getLabel());
            card.setSubtitle(item.getName());

            switch (item.getType()) {
                case CoreItemFactory.SWITCH:
                    Component switchComponent = new Component("HbSwitch");
                    switchComponent.addConfig("item", item.getName());
                    card.addComponent("right", switchComponent);
                    break;
                case CoreItemFactory.DIMMER:
                    boolean buildKnob = (metadata != null && metadata.getConfiguration().containsKey("control")
                            && metadata.getConfiguration().get("control").equals("knob"));
                    if (item.hasTag("capability:Switchable")) {
                        Component dimmerSwitchComponent = new Component("HbSwitch");
                        dimmerSwitchComponent.addConfig("item", item.getName());
                        card.addComponent("right", dimmerSwitchComponent);
                    } else if (!buildKnob) {
                        Component dimmerValueComponent = new Component("HbSingleItemValue");
                        dimmerValueComponent.addConfig("item", item.getName());
                        card.addComponent("right", dimmerValueComponent);
                    }

                    Component dimmerContainerComponent = new Component("HbContainer");
                    dimmerContainerComponent.addConfig("classes", new String[] { "full-width", "text-center" });
                    if (buildKnob) {
                        Component knobComponent = new Component("HbKnob");
                        knobComponent.addConfig("item", item.getName());
                        knobComponent.addConfig("size", "200px");
                        knobComponent.addConfig("textSize", "2rem");
                        knobComponent.addConfig("color", "primary");
                        dimmerContainerComponent.addComponent("main", knobComponent);
                    } else {
                        Component sliderComponent = new Component("HbSlider");
                        sliderComponent.addConfig("item", item.getName());
                        dimmerContainerComponent.addComponent("main", sliderComponent);
                    }

                    card.addComponent("main", dimmerContainerComponent);
                    break;
                case CoreItemFactory.ROLLERSHUTTER:
                    Component shutterValueComponent = new Component("HbSingleItemValue");
                    shutterValueComponent.addConfig("item", item.getName());
                    card.addComponent("right", shutterValueComponent);
                    Component shutterContainerComponent = new Component("HbContainer");
                    shutterContainerComponent.addConfig("classes", new String[] { "full-width", "text-center" });
                    Component shutterControlComponent = new Component("HbShutterControl");
                    shutterControlComponent.addConfig("item", item.getName());
                    shutterControlComponent.addConfig("size", "lg");
                    shutterControlComponent.addConfig("rounded", true);
                    shutterControlComponent.addConfig("glossy", true);
                    shutterControlComponent.addConfig("push", true);
                    shutterControlComponent.addConfig("stopIcon", "close");
                    shutterContainerComponent.addComponent("main", shutterControlComponent);
                    card.addComponent("main", shutterContainerComponent);
                    break;
                case CoreItemFactory.PLAYER:
                    Component playerContainerComponent = new Component("HbContainer");
                    playerContainerComponent.addConfig("classes", new String[] { "full-width", "text-center" });
                    Component playerComponent = new Component("HbPlayer");
                    playerComponent.addConfig("item", item.getName());
                    playerComponent.addConfig("size", "lg");
                    playerContainerComponent.addComponent("main", playerComponent);
                    card.addComponent("main", playerContainerComponent);
                    break;
                case CoreItemFactory.COLOR:
                    Component colorPickerComponent = new Component("HbColorPicker");
                    colorPickerComponent.addConfig("item", item.getName());
                    card.addComponent("right", colorPickerComponent);
                    Component brightnessDimmerComponent = new Component("HbSwitch");
                    brightnessDimmerComponent.addConfig("item", item.getName());
                    card.addComponent("right", brightnessDimmerComponent);
                    Component brightnessDimmerContainerComponent = new Component("HbContainer");
                    brightnessDimmerContainerComponent.addConfig("classes",
                            new String[] { "full-width", "text-center" });
                    Component brightnessSliderComponent = new Component("HbSlider");
                    brightnessSliderComponent.addConfig("item", item.getName());
                    brightnessDimmerContainerComponent.addComponent("main", brightnessSliderComponent);
                    card.addComponent("main", brightnessDimmerContainerComponent);
                    break;
                default:
                    if (item.getType() == CoreItemFactory.IMAGE
                            || (metadata != null && metadata.getConfiguration().containsKey("imageSitemap"))) {
                        /*
                         * If the item is an image (or a String with a tag indicating it's an image), build a
                         * HbImage component in the "media" slot
                         */
                        Component singleImageComponent = new Component("HbImage");
                        singleImageComponent.addConfig("item", item.getName());
                        card.addComponent("media", singleImageComponent);
                    } else {
                        /*
                         * Try to get a formatted state to determine whether it's small enough to display
                         * in the "right" slot - otherwise add it to the "main" slot
                         */
                        String formattedState;
                        try {
                            formattedState = formatState(item, item.getState());
                        } catch (TransformationException e1) {
                            formattedState = item.getState().toString();
                        }
                        Component singleItemComponent = new Component("HbSingleItemValue");
                        singleItemComponent.addConfig("item", item.getName());
                        if (formattedState.length() < 10) {
                            card.addComponent("right", singleItemComponent);
                        } else {
                            card.addComponent("main", singleItemComponent);
                        }
                    }
                    break;
            }
        } else {
            GroupItem commonGroup = getMatchingGroup(matchedItems);

            if (commonGroup != null) {
                card.setTitle(commonGroup.getLabel());
                Item baseItem = commonGroup.getBaseItem();
                if (baseItem != null && baseItem.getType() == CoreItemFactory.SWITCH) {
                    Component switchComponent = new Component("HbSwitch");
                    switchComponent.addConfig("item", commonGroup.getName());
                    card.addComponent("right", switchComponent);
                } else if (!commonGroup.getState().toString().isEmpty()) {
                    Component singleItemComponent = new Component("HbSingleItemValue");
                    singleItemComponent.addConfig("item", commonGroup.getName());
                    card.addComponent("right", singleItemComponent);
                }
            } else {
                card.setTitle(String.join(", ", intent.getEntities().values()));
            }
            card.setSubtitle(matchingNonGroupItems.get().count() + " items"); // TODO: i18n

            // TODO: detect images and build a HbCarousel with them - for webcams etc.

            Component list = new Component("HbList");
            matchingNonGroupItems.get().forEach(item -> {
                Metadata metadata = this.metadataRegistry.get(new MetadataKey("habot", item.getName()));

                Component listItem = new Component("HbListItem");
                listItem.addConfig("item", item.getName());
                listItem.addConfig("label", item.getLabel());
                if (metadata != null) {
                    // Use selected keys from data as configuration for the HbListItem component
                    for (String configKey : Arrays.asList("label", "sublabel", "leftIcon", "leftLetter", "leftColor")) {
                        if (metadata.getConfiguration().containsKey(configKey)) {
                            listItem.addConfig(configKey, metadata.getConfiguration().get(configKey));
                        }
                    }
                }

                list.addComponent("items", listItem);
            });

            card.addComponent("list", list);
        }

        // Adds the (ephemeral) card to the registry anyways so it appears in the "recent cards" page
        cardRegistry.add(card);
        return card;
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
        String object = intent.getEntities().get("object");
        String location = intent.getEntities().get("location");

        Card card = new Card("HbCard");
        if (object != null) {
            card.addObjectAttribute(object);
        }
        if (location != null) {
            card.addLocationAttribute(location);
        }
        card.setEphemeral(true);
        card.addConfig("bigger", true);
        card.updateTimestamp();

        Supplier<Stream<Item>> matchingNonGroupItems = () -> matchedItems.stream()
                .filter(i -> !(i instanceof GroupItem));

        if (matchingNonGroupItems.get().count() == 1) {
            Item item = matchingNonGroupItems.get().findFirst().get();
            card.setTitle(item.getLabel());
            card.setSubtitle(period + " - " + item.getName());
        } else {
            GroupItem commonGroup = getMatchingGroup(matchedItems);
            if (commonGroup != null) {
                card.setTitle(commonGroup.getLabel());
                Item baseItem = commonGroup.getBaseItem();
                if (baseItem != null && baseItem.getType() == CoreItemFactory.SWITCH) {
                    Component switchComponent = new Component("HbSwitch");
                    switchComponent.addConfig("item", commonGroup.getName());
                    card.addComponent("right", switchComponent);
                } else if (!commonGroup.getState().toString().isEmpty()) {
                    Component singleItemComponent = new Component("HbSingleItemValue");
                    singleItemComponent.addConfig("item", commonGroup.getName());
                    card.addComponent("right", singleItemComponent);
                }
            } else {
                card.setTitle(intent.getEntities().entrySet().stream().filter(e -> !e.getKey().equals("period"))
                        .map(e -> e.getValue()).collect(Collectors.joining(", ")));
            }
            card.setSubtitle(period + " - " + matchingNonGroupItems.get().count() + " items"); // TODO: i18n
        }

        Component chart = new Component("HbChartImage");
        chart.addConfig("items",
                matchingNonGroupItems.get().map(i -> i.getName()).collect(Collectors.toList()).toArray(new String[0]));
        chart.addConfig("period", period);

        Component analyzeButton = new Component("HbAnalyzeActionButton");
        analyzeButton.addConfig("items", chart.getConfig().get("items"));
        analyzeButton.addConfig("period", chart.getConfig().get("period"));

        card.addComponent("media", chart);
        card.addComponent("actions", analyzeButton);

        cardRegistry.add(card);
        return card;
    }

    /**
     * Tries to find a Group item to act as the card title.
     * It should contain all other members of the provided collection except itself
     *
     * @param items the group of matching items including an eventual GroupItem to find
     * @return an optional group eligible for the card's title, or null if none was found
     */
    private @Nullable GroupItem getMatchingGroup(Collection<Item> items) {
        Optional<Item> groupItem = items.stream().filter(i -> i instanceof GroupItem)
                .filter(g -> items.stream().allMatch(i -> i.getName().equals(g.getName())
                        || ((GroupItem) g).getAllMembers().stream().anyMatch(i2 -> i2.getName().contains(i.getName()))))
                .findFirst();
        return groupItem.isPresent() ? (GroupItem) groupItem.get() : null;
    }

    private String formatState(Item item, State state) throws TransformationException {
        if (item.getStateDescription() != null) {
            try {
                StateDescription stateDescription = item.getStateDescription();
                if (stateDescription != null) {
                    final String pattern = stateDescription.getPattern();
                    if (pattern != null) {
                        String transformedState = TransformationHelper.transform(
                                FrameworkUtil.getBundle(CardBuilder.class).getBundleContext(), pattern,
                                state.toString());
                        if (transformedState == null) {
                            return state.toString();
                        }
                        if (transformedState.equals(state.toString())) {
                            return state.format(pattern);
                        } else {
                            return transformedState;
                        }
                    } else {
                        return state.toString();
                    }
                } else {
                    return state.toString();
                }
            } catch (IllegalFormatConversionException e) {
                return state.toString();
            }
        } else {
            return state.toString();
        }
    }

    @Reference
    protected void setCardRegistry(CardRegistry cardRegistry) {
        this.cardRegistry = cardRegistry;
    }

    protected void unsetCardRegistry(CardRegistry cardRegistry) {
        this.cardRegistry = null;
    }

    @Reference
    protected void setMetadataRegistry(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    protected void unsetMetadataRegistry(MetadataRegistry metadataRegistry) {
        this.metadataRegistry = null;
    }
}
