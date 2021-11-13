/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mycroft.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mycroft.internal.channels.MycroftChannel;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * This class defines Actions.
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@ThingActionsScope(name = "mycroft") // Your bindings id is usually the scope
@NonNullByDefault
public class MycroftActions implements ThingActions {

    private @Nullable MycroftHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (MycroftHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "Speak", description = "Ask Mycroft to say something")
    public void speak(
            @ActionInput(name = "speak", label = "speak", description = "What to say") @Nullable String speak) {
        getChannel(MycroftBindingConstants.SPEAK_CHANNEL).handleCommand(new StringType(speak));
    }

    private MycroftChannel<?> getChannel(String channelName) {
        MycroftHandler handlerFinal = handler;
        if (handlerFinal != null) {
            Channel channel = handlerFinal.getThing().getChannel(channelName);
            if (channel != null) {
                MycroftChannel<?> mycroftChannel = handlerFinal.mycroftChannels.get(channel.getUID());
                if (mycroftChannel != null) {
                    return mycroftChannel;
                }
            }
        }
        throw new IllegalArgumentException("channelName is not a valid mycroft channel");
    }

    public static void speak(@Nullable ThingActions actions, @Nullable String speak) {
        if (actions instanceof MycroftActions) {
            ((MycroftActions) actions).speak(speak);
        } else {
            throw new IllegalArgumentException("Instance is not an MycroftActions class.");
        }
    }

    @RuleAction(label = "Utterance", description = "Ask Mycroft something")
    public void utterance(
            @ActionInput(name = "utterance", label = "utterance", description = "What to ask") @Nullable String utterance) {
        getChannel(MycroftBindingConstants.UTTERANCE_CHANNEL).handleCommand(new StringType(utterance));
    }

    public static void utterance(@Nullable ThingActions actions, @Nullable String utterance) {
        if (actions instanceof MycroftActions) {
            ((MycroftActions) actions).utterance(utterance);
        } else {
            throw new IllegalArgumentException("Instance is not an MycroftActions class.");
        }
    }
}
