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
package org.openhab.binding.tado.internal.builder;

import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.TadoBindingConstants;
import org.openhab.binding.tado.internal.TadoTranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link ZoneChannelBuilder} class creates zone dynamic channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ZoneChannelBuilder {

    private static final String NOT_INITIALIZED = "ZoneChannelBuilder not initialized!";

    private final Thing thing;

    // attributes that are set via withSomething() methods
    private @Nullable String channelId;
    private @Nullable Boolean required;
    private @Nullable String acceptedItemType;
    private @Nullable ChannelTypeUID channelTypeUID;
    private @Nullable TadoTranslationProvider translationProvider;
    private InsertPosition insertPosition = InsertPosition.START;

    public static enum InsertPosition {
        START,
        END
    }

    /**
     * Constructor
     *
     * @param thing the thin which would host the channel.
     */
    public ZoneChannelBuilder(Thing thing) {
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

    private TadoTranslationProvider getTranslationProvider() throws IllegalStateException {
        TadoTranslationProvider translationProvider = this.translationProvider;
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

    public ZoneChannelBuilder withChannelId(String channelId) {
        this.channelId = channelId;
        this.channelTypeUID = new ChannelTypeUID(TadoBindingConstants.BINDING_ID, channelId);
        return this;
    }

    public ZoneChannelBuilder withRequired(boolean required) {
        this.required = required;
        return this;
    }

    public ZoneChannelBuilder withTranslationProvider(TadoTranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
        return this;
    }

    public ZoneChannelBuilder withAcceptedItemType(String acceptedItemType) {
        this.acceptedItemType = acceptedItemType;
        return this;
    }

    public ZoneChannelBuilder withInsertPosition(InsertPosition insertPosition) {
        this.insertPosition = insertPosition;
        return this;
    }

    public InsertPosition getInsertPosition() {
        return insertPosition;
    }

    /**
     * @return a search predicate to match channels with the given channelTypeUID
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public Predicate<Channel> getPredicate() throws IllegalStateException {
        return c -> getChannelTypeUID().equals(c.getChannelTypeUID());
    }

    /**
     * @return a channel built based on the attributes provided in the withXXX() methods.
     * @throws IllegalStateException if any attributes have not been properly set.
     */
    public Channel build() throws IllegalStateException {
        checkAllAttributesSet();

        ChannelTypeUID channelTypeUID = getChannelTypeUID();
        TadoTranslationProvider translationProvider = getTranslationProvider();
        String propertyKeyPrefix = "channel-type." + channelTypeUID.getAsString().replace(":", ".") + ".";

        // @formatter:off
        return ChannelBuilder.create(new ChannelUID(thing.getUID(), getChannelId()))
                .withType(channelTypeUID)
                .withLabel(translationProvider.getText(propertyKeyPrefix + "label"))
                .withDescription(translationProvider.getText(propertyKeyPrefix + "description"))
                .withAcceptedItemType(getAcceptedItemType())
                .withKind(ChannelKind.STATE)
                .build();
        // @formatter:on
    }
}
