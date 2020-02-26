/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.handler.strategy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hive.internal.client.FeatureAttribute;
import org.openhab.binding.hive.internal.client.Node;
import org.openhab.binding.hive.internal.client.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * A base class that makes implementing {@link ThingHandlerStrategy}s easier.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public abstract class ThingHandlerStrategyBase implements ThingHandlerStrategy {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Represents a method that requires a nonnull {@link ChannelUID}.
     * 
     * @see #useChannel(Thing, String, DoWithChannelUid)
     */
    @FunctionalInterface
    protected interface DoWithChannelUid {
        void useChannelUid(ChannelUID channelUID);
    }

    /**
     * Represents a method that requires a nonnull {@link Feature} and returns
     * something.
     *
     * @see #useFeature(Node, Class, DoWithFeatureAndReturn)
     */
    @FunctionalInterface
    protected interface DoWithFeatureAndReturn<F extends Feature> {
        @Nullable Node useFeature(F feature);
    }

    /**
     * Represents a method that requires a nonnull {@link Feature}.
     *
     * @see #useFeature(Node, Class, DoWithFeature)
     */
    @FunctionalInterface
    protected interface DoWithFeature<F extends Feature> {
        void useFeature(F feature);
    }

    /**
     * Represents a method that requires a nonnull {@link FeatureAttribute}.
     *
     * @see #useAttribute(Node, Class, String, FeatureAttribute, DoWithFeatureAttribute)
     */
    @FunctionalInterface
    protected interface DoWithFeatureAttribute<T, A extends FeatureAttribute<T>> {
        void useAttribute(A featureAttribute);
    }

    /**
     * Helper method to allow use of channels without worrying about
     * {@linkplain NullPointerException}s occurring if a {@link Thing}
     * does not have a {@link Channel} that you expect it to have.
     *
     * <p>
     *     e.g. If the binding is upgraded with new channels but the
     *     {@linkplain Thing} is not upgraded.
     * </p>
     *
     * @param thing
     *      The {@linkplain Thing} to get the channel from.
     *
     * @param channelName
     *      The name of the {@linkplain Channel} you want to work with.
     *
     * @param thingToDo
     *      The thing you want to do with the channel.
     */
    protected void useChannel(
            final Thing thing,
            final String channelName,
            final DoWithChannelUid thingToDo
    ) {
        Objects.requireNonNull(thing);
        Objects.requireNonNull(channelName);
        Objects.requireNonNull(thingToDo);

        final @Nullable Channel channel = thing.getChannel(channelName);

        if (channel != null) {
            final ChannelUID channelUID = channel.getUID();

            thingToDo.useChannelUid(channelUID);
        } else {
            final @Nullable String thingLabel = thing.getLabel();
            final String thingName = thingLabel != null ? thingLabel : thing.getUID().toString();

            this.logger.debug(
                    "Tried to do something with the nonexistent channel \"{}\" of thing \"{}\". Do you need to update your thing?",
                    channelName,
                    thingName
            );
        }
    }

    /**
     * Helper method to allow use of features without worrying about
     * {@linkplain NullPointerException}s occurring if a {@link Node} does
     * not have a {@link Feature} that you expect it to have.
     *
     * <p>
     *     e.g. If an incorrect {@link org.eclipse.smarthome.core.thing.binding.ThingHandler}
     *     has been assigned to a {@linkplain Node}.
     * </p>
     *
     * @param hiveNode
     *      The {@linkplain Node} to get the {@linkplain Feature} from.
     *
     * @param featureClass
     *      The {@linkplain Class} of the {@linkplain Feature} you want to use.
     *
     * @param thingToDo
     *      The thing you want to do with the {@linkplain Feature}.
     *
     * @param <F>
     *      The type of {@linkplain Feature} you want to use.
     *
     * @return
     *      Either the result of {@code thingToDo.doStuff(F)} if the feature
     *      is available or {@code null} if the feature is not available.
     */
    protected <F extends Feature> @Nullable Node useFeature(
            final Node hiveNode,
            final Class<F> featureClass,
            final DoWithFeatureAndReturn<F> thingToDo
    ) {
        Objects.requireNonNull(hiveNode);
        Objects.requireNonNull(featureClass);
        Objects.requireNonNull(thingToDo);

        final @Nullable F feature = hiveNode.getFeature(featureClass);

        if (feature != null) {
            return thingToDo.useFeature(feature);
        } else {
            this.logger.debug(
                    "Could not get feature {} for node {} ({}). Has it been given the wrong kind of handler?",
                    featureClass.getName(),
                    hiveNode.getName(),
                    hiveNode.getId()
            );

            return null;
        }
    }

    /**
     * Helper method to allow use of features without worrying about
     * {@linkplain NullPointerException}s occurring if a {@link Node} does
     * not have a {@link Feature} that you expect it to have.
     *
     * <p>
     *     e.g. If an incorrect {@link org.eclipse.smarthome.core.thing.binding.ThingHandler}
     *     has been assigned to a {@linkplain Node}.
     * </p>
     *
     * @param hiveNode
     *      The {@linkplain Node} to get the {@linkplain Feature} from.
     *
     * @param featureClass
     *      The {@linkplain Class} of the {@linkplain Feature} you want to use.
     *
     * @param thingToDo
     *      The thing you want to do with the {@linkplain Feature}.
     *
     * @param <F>
     *      The type of {@linkplain Feature} you want to use.
     */
    protected <F extends Feature> void useFeature(
            final Node hiveNode,
            final Class<F> featureClass,
            final DoWithFeature<F> thingToDo
    ) {
        useFeature(hiveNode, featureClass, (feature) -> { thingToDo.useFeature(feature); return null;});
    }

    protected <T, A extends FeatureAttribute<T>> void useAttribute(
            final Node hiveNode,
            final Class<? extends Feature> featureClass,
            final String attributeName,
            final @Nullable A featureAttribute,
            final DoWithFeatureAttribute<T, A> thingToDo
    ) {
        Objects.requireNonNull(hiveNode);
        Objects.requireNonNull(featureClass);
        Objects.requireNonNull(attributeName);
        Objects.requireNonNull(thingToDo);

        if (featureAttribute != null) {
            thingToDo.useAttribute(featureAttribute);
        } else {
            this.logger.trace(
                    "Tried to do something with the unavailable attribute \"{}\" of feature \"{}\" in node \"{}\". Old hub?",
                    attributeName,
                    featureClass.getName(),
                    hiveNode.getName()
            );
        }
    }

    // Default implementation that does nothing.
    // Useful for read-only features.
    @Override
    public @Nullable Node handleCommand(
            final ChannelUID channelUID,
            final Command command,
            final Node hiveNode
    ) {
        return null;
    }
}
