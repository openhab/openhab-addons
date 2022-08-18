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
package org.openhab.binding.hdpowerview.internal.builders;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link ShadeChannelBuilder} class creates vane and secondary position channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeChannelBuilder {

    private static final String NOT_INITIALIZED = "ShadeChannelBuilder not initialized!";

    private final Thing thing;

    // attributes that are set via withSomething() methods
    private @Nullable String channelId;
    private @Nullable ChannelTypeUID channelTypeUID;
    private @Nullable HDPowerViewTranslationProvider translationProvider;
    private @Nullable Boolean required;
    private @Nullable String acceptedItemType;

    // attributes that are set when initialize() is called
    private @Nullable Predicate<Channel> predicate;
    private @Nullable Boolean existing;
    private boolean ready = false;

    /**
     * Constructor
     *
     * @param thing the thin which would host the channel.
     */
    public ShadeChannelBuilder(Thing thing) {
        this.thing = thing;
    }

    /**
     * Helper method that prepares the class to be used.
     *
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    private void prepare() throws IllegalStateException {
        if (ready) {
            return;
        }
        ChannelTypeUID channelTypeUID = this.channelTypeUID;
        if (channelTypeUID != null) {
            predicate = c -> channelTypeUID.equals(c.getChannelTypeUID());
            if (channelId != null && translationProvider != null && required != null) {
                Predicate<Channel> predicate = this.predicate;
                if (predicate != null) {
                    existing = thing.getChannels().stream().anyMatch(predicate);
                    ready = true;
                    return;
                }
            }
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    /**
     * @return true if the channel already exists in our thing's channel list.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public boolean isExisting() throws IllegalStateException {
        prepare();
        if (ready) {
            return Boolean.TRUE.equals(existing);
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    /**
     * @return true if the channel is supposed to be in our thing's channel list.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public boolean isRequired() throws IllegalStateException {
        prepare();
        if (ready) {
            return Boolean.TRUE.equals(required);
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    /**
     * @return true if the channel is supposed to be in our thing's channel list but is not; or vice versa.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public boolean isDirty() throws IllegalStateException {
        return isExisting() != isRequired();
    }

    /**
     * @return true if the channel shall be added to our thing's channel list.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public boolean isAddingRequired() throws IllegalStateException {
        return isRequired() && !isExisting();
    }

    /**
     * @return true if the channel shall be removed from our thing's channel list.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public boolean isRemovingRequired() throws IllegalStateException {
        return isExisting() && !isRequired();
    }

    public ShadeChannelBuilder withChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public ShadeChannelBuilder withChannelTypeUID(ChannelTypeUID channelTypeUID) {
        this.channelTypeUID = channelTypeUID;
        return this;
    }

    public ShadeChannelBuilder withRequired(boolean required) {
        this.required = required;
        return this;
    }

    public ShadeChannelBuilder withTranslationProvider(HDPowerViewTranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
        return this;
    }

    public ShadeChannelBuilder withAcceptedItemType(String acceptedItemType) {
        this.acceptedItemType = acceptedItemType;
        return this;
    }

    /**
     * @return a search predicate to match channels with the given channelTypeUID
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public Predicate<Channel> getPredicate() throws IllegalStateException {
        prepare();
        Predicate<Channel> predicate = this.predicate;
        if (predicate != null) {
            return predicate;
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    /**
     * @return a channel built based on the attributes provided in the withXXX() methods.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public Channel build() throws IllegalStateException {
        prepare();
        if (ready) {
            String channelId = this.channelId;
            String acceptedItemType = this.acceptedItemType;
            ThingUID thingUID = thing.getUID();
            ChannelTypeUID channelTypeUID = this.channelTypeUID;
            HDPowerViewTranslationProvider translationProvider = this.translationProvider;

            if (channelId != null && channelTypeUID != null && translationProvider != null
                    && acceptedItemType != null) {
                // make translations
                String propertyKeyPrefix = "channel-type." + channelTypeUID.getAsString().replace(":", ".") + ".";
                String label = translationProvider.getText(propertyKeyPrefix + "label");
                String description = translationProvider.getText(propertyKeyPrefix + "description");

                ChannelUID channelUID = new ChannelUID(thingUID, channelId);

                // @formatter:off
                return ChannelBuilder.create(channelUID)
                        .withType(channelTypeUID)
                        .withLabel(label)
                        .withDescription(description)
                        .withAcceptedItemType(acceptedItemType)
                        .withKind(ChannelKind.STATE)
                        .build();
                // @formatter:on
            }
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }
}
