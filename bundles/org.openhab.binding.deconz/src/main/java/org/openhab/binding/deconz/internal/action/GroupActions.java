/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.action;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.deconz.internal.dto.NewSceneResponse;
import org.openhab.binding.deconz.internal.handler.GroupThingHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link GroupActions} provides actions for managing scenes in groups
 *
 * @author Jan N. Klug - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = GroupActions.class)
@ThingActionsScope(name = "deconz")
@NonNullByDefault
public class GroupActions implements ThingActions {
    private static final String NEW_SCENE_ID_OUTPUT = "newSceneId";
    private static final Type NEW_SCENE_RESPONSE_TYPE = new TypeToken<List<NewSceneResponse>>() {
    }.getType();

    private final Logger logger = LoggerFactory.getLogger(GroupActions.class);
    private final Gson gson = new Gson();

    private @Nullable GroupThingHandler handler;

    @RuleAction(label = "@text/action.create-scene.label", description = "@text/action.create-scene.description")
    public @ActionOutput(name = NEW_SCENE_ID_OUTPUT, type = "java.lang.Integer") Map<String, Object> createScene(
            @ActionInput(name = "name", label = "@text/action.create-scene.name.label", description = "@text/action.create-scene.name.description") @Nullable String name) {
        GroupThingHandler handler = this.handler;

        if (handler == null) {
            logger.warn("Deconz GroupActions service ThingHandler is null!");
            return Map.of();
        }

        if (name == null) {
            logger.debug("Skipping scene creation due to missing scene name");
            return Map.of();
        }

        CompletableFuture<String> newSceneId = new CompletableFuture<>();
        handler.doNetwork(Map.of("name", name), "scenes", HttpMethod.POST, newSceneId::complete);

        try {
            String returnedJson = newSceneId.get(2000, TimeUnit.MILLISECONDS);
            List<NewSceneResponse> newSceneResponses = gson.fromJson(returnedJson, NEW_SCENE_RESPONSE_TYPE);
            if (newSceneResponses != null && !newSceneResponses.isEmpty()) {
                return Map.of(NEW_SCENE_ID_OUTPUT, newSceneResponses.get(0).success.id);
            }
            throw new IllegalStateException("response is empty");
        } catch (InterruptedException | ExecutionException | TimeoutException | JsonParseException
                | IllegalStateException e) {
            logger.warn("Couldn't get newSceneId", e);
            return Map.of();
        }
    }

    public static Map<String, Object> createScene(ThingActions actions, @Nullable String name) {
        if (actions instanceof GroupActions groupActions) {
            return groupActions.createScene(name);
        }
        return Map.of();
    }

    @RuleAction(label = "@text/action.delete-scene.label", description = "@text/action.delete-scene.description")
    public void deleteScene(
            @ActionInput(name = "sceneId", label = "@text/action.delete-scene.sceneId.label", description = "@text/action.delete-scene.sceneId.description") @Nullable Integer sceneId) {
        GroupThingHandler handler = this.handler;

        if (handler == null) {
            logger.warn("Deconz GroupActions service ThingHandler is null!");
            return;
        }

        if (sceneId == null) {
            logger.warn("Skipping scene deletion due to missing scene id");
            return;
        }

        handler.doNetwork(null, "scenes/" + sceneId, HttpMethod.DELETE, null);
    }

    public static void deleteScene(ThingActions actions, @Nullable Integer sceneId) {
        if (actions instanceof GroupActions groupActions) {
            groupActions.deleteScene(sceneId);
        }
    }

    @RuleAction(label = "@text/action.store-as-scene.label", description = "@text/action.store-as-scene.description")
    public void storeScene(
            @ActionInput(name = "sceneId", label = "@text/action.store-as-scene.sceneId.label", description = "@text/action.store-as-scene.sceneId.description") @Nullable Integer sceneId) {
        GroupThingHandler handler = this.handler;

        if (handler == null) {
            logger.warn("Deconz GroupActions service ThingHandler is null!");
            return;
        }

        if (sceneId == null) {
            logger.warn("Skipping scene storage due to missing scene id");
            return;
        }

        handler.doNetwork(null, "scenes/" + sceneId + "/store", HttpMethod.PUT, null);
    }

    public static void storeScene(ThingActions actions, @Nullable Integer sceneId) {
        if (actions instanceof GroupActions groupActions) {
            groupActions.storeScene(sceneId);
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof GroupThingHandler thingHandler) {
            this.handler = thingHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
