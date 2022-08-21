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

    private static final String NOT_INITIALIZED = "ShadeChannelBuilder(): not initialized!";

    private final Thing thing;

    // attributes that are set via withSomething() methods
    private @Nullable String channelId;
    private @Nullable Boolean required;
    private @Nullable String acceptedItemType;
    private @Nullable ChannelTypeUID channelTypeUID;
    private @Nullable HDPowerViewTranslationProvider translationProvider;

    /**
     * Constructor
     *
     * @param thing the thin which would host the channel.
     */
    public ShadeChannelBuilder(Thing thing) {
        this.thing = thing;
    }

    private boolean checkAllAttributesSet() throws IllegalStateException {
        if (channelId != null && required != null && acceptedItemType != null && channelTypeUID != null
                && translationProvider != null) {
            return true;
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    private String getChannelId() throws IllegalStateException {
        String channelId = this.channelId;
        if (channelId != null) {
            return channelId;
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    private HDPowerViewTranslationProvider getTranslationProvider() throws IllegalStateException {
        HDPowerViewTranslationProvider translationProvider = this.translationProvider;
        if (translationProvider != null) {
            return translationProvider;
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    private String getAcceptedItemType() throws IllegalStateException {
        String acceptedItemType = this.acceptedItemType;
        if (acceptedItemType != null) {
            return acceptedItemType;
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    private ChannelTypeUID getChannelTypeUID() throws IllegalStateException {
        ChannelTypeUID channelTypeUID = this.channelTypeUID;
        if (channelTypeUID != null) {
            return channelTypeUID;
        }
        throw new IllegalStateException(NOT_INITIALIZED);
    }

    /**
     * @return true if the channel already exists in our thing's channel list.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public boolean isExisting() throws IllegalStateException {
        return thing.getChannels().stream().anyMatch(getPredicate());
    }

    /**
     * @return true if the channel is supposed to be in our thing's channel list.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public boolean isRequired() throws IllegalStateException {
        Boolean required = this.required;
        if (required != null) {
            return required.booleanValue();
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
        ChannelTypeUID channelUID = getChannelTypeUID();
        return c -> channelUID.equals(c.getChannelTypeUID());
    }

    /**
     * @return a channel built based on the attributes provided in the withXXX() methods.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public Channel build() throws IllegalStateException {
        checkAllAttributesSet();

        String propertyKeyPrefix = "channel-type." + getChannelTypeUID().getAsString().replace(":", ".") + ".";

        // @formatter:off
        return ChannelBuilder.create(new ChannelUID(thing.getUID(), getChannelId()))
                .withType(getChannelTypeUID())
                .withLabel(getTranslationProvider().getText(propertyKeyPrefix + "label"))
                .withDescription(getTranslationProvider().getText(propertyKeyPrefix + "description"))
                .withAcceptedItemType(getAcceptedItemType())
                .withKind(ChannelKind.STATE)
                .build();
        // @formatter:on
    }
}
