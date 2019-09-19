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
package org.openhab.binding.lgwebos.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVMouseSocket.ButtonType;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSTVSocket;
import org.openhab.binding.lgwebos.internal.handler.command.ServiceSubscription;
import org.openhab.binding.lgwebos.internal.handler.core.AppInfo;
import org.openhab.binding.lgwebos.internal.handler.core.ResponseListener;
import org.openhab.binding.lgwebos.internal.handler.core.TextInputStatusInfo;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * This is the automation engine action handler service for the
 * lgwebos action.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@ThingActionsScope(name = "lgwebos")
@NonNullByDefault
public class LGWebOSActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(LGWebOSActions.class);
    private final ResponseListener<TextInputStatusInfo> textInputListener = createTextInputStatusListener();
    private @Nullable LGWebOSHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (LGWebOSHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
    }

    // a NonNull getter for handler
    private LGWebOSHandler getLGWebOSHandler() {
        LGWebOSHandler lgWebOSHandler = this.handler;
        if (lgWebOSHandler == null) {
            throw new IllegalStateException(
                    "ThingHandler must be set before any action may be invoked on LGWebOSActions.");
        }
        return lgWebOSHandler;
    }

    private enum Button {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        BACK,
        DELETE,
        ENTER,
        HOME,
        OK
    }

    @RuleAction(label = "@text/actionShowToastLabel", description = "@text/actionShowToastDesc")
    public void showToast(
            @ActionInput(name = "text", label = "@text/actionShowToastInputTextLabel", description = "@text/actionShowToastInputTextDesc") String text)
            throws IOException {
        getSocket().ifPresent(control -> control.showToast(text, createResponseListener()));
    }

    @RuleAction(label = "@text/actionShowToastWithIconLabel", description = "@text/actionShowToastWithIconLabel")
    public void showToast(
            @ActionInput(name = "icon", label = "@text/actionShowToastInputIconLabel", description = "@text/actionShowToastInputIconDesc") String icon,
            @ActionInput(name = "text", label = "@text/actionShowToastInputTextLabel", description = "@text/actionShowToastInputTextDesc") String text)
            throws IOException {
        BufferedImage bi = ImageIO.read(new URL(icon));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); OutputStream b64 = Base64.getEncoder().wrap(os)) {
            ImageIO.write(bi, "png", b64);
            String string = os.toString(StandardCharsets.UTF_8.name());
            getSocket().ifPresent(control -> control.showToast(text, string, "png", createResponseListener()));
        }
    }

    @RuleAction(label = "@text/actionLaunchBrowserLabel", description = "@text/actionLaunchBrowserDesc")
    public void launchBrowser(
            @ActionInput(name = "url", label = "@text/actionLaunchBrowserInputUrlLabel", description = "@text/actionLaunchBrowserInputUrlDesc") String url) {
        getSocket().ifPresent(control -> control.launchBrowser(url, createResponseListener()));
    }

    private List<AppInfo> getAppInfos() {
        LGWebOSHandler lgWebOSHandler = getLGWebOSHandler();

        final LGWebOSTVSocket socket = lgWebOSHandler.getSocket();
        if (!socket.isConnected()) {
            logger.warn("Device with ThingID {} is currently not connected.", lgWebOSHandler.getThing().getUID());
            return Collections.emptyList();
        }

        List<AppInfo> appInfos = lgWebOSHandler.getLauncherApplication()
                .getAppInfos(lgWebOSHandler.getThing().getUID());
        if (appInfos == null) {
            logger.warn("No AppInfos found for device with ThingID {}.", lgWebOSHandler.getThing().getUID());
            return Collections.emptyList();
        }
        return appInfos;

    }

    public List<Application> getApplications() {
        return getAppInfos().stream().map(appInfo -> new Application(appInfo.getId(), appInfo.getName()))
                .collect(Collectors.toList());
    }

    @RuleAction(label = "@text/actionLaunchApplicationLabel", description = "@text/actionLaunchApplicationDesc")
    public void launchApplication(
            @ActionInput(name = "appId", label = "@text/actionLaunchApplicationInputAppIDLabel", description = "@text/actionLaunchApplicationInputAppIDDesc") String appId) {
        Optional<AppInfo> appInfo = getAppInfos().stream().filter(a -> a.getId().equals(appId)).findFirst();
        if (appInfo.isPresent()) {
            getSocket().ifPresent(control -> control.launchAppWithInfo(appInfo.get(), createResponseListener()));
        } else {
            logger.warn("Device with ThingID {} does not support any app with id: {}.",
                    getLGWebOSHandler().getThing().getUID(), appId);
        }

    }

    @RuleAction(label = "@text/actionLaunchApplicationWithParamsLabel", description = "@text/actionLaunchApplicationWithParamsDesc")
    public void launchApplication(
            @ActionInput(name = "appId", label = "@text/actionLaunchApplicationInputAppIDLabel", description = "@text/actionLaunchApplicationInputAppIDDesc") String appId,
            @ActionInput(name = "params", label = "@text/actionLaunchApplicationInputParamsLabel", description = "@text/actionLaunchApplicationInputParamsDesc") String params) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject payload = (JsonObject) parser.parse(params);

            Optional<AppInfo> appInfo = getAppInfos().stream().filter(a -> a.getId().equals(appId)).findFirst();
            if (appInfo.isPresent()) {
                getSocket().ifPresent(
                        control -> control.launchAppWithInfo(appInfo.get(), payload, createResponseListener()));
            } else {
                logger.warn("Device with ThingID {} does not support any app with id: {}.",
                        getLGWebOSHandler().getThing().getUID(), appId);
            }

        } catch (JsonParseException ex) {
            logger.warn("Parameters value ({}) is not in a valid JSON format. {}", params, ex.getMessage());
            return;
        }
    }

    @RuleAction(label = "@text/actionSendTextLabel", description = "@text/actionSendTextDesc")
    public void sendText(
            @ActionInput(name = "text", label = "@text/actionSendTextInputTextLabel", description = "@text/actionSendTextInputTextDesc") String text) {
        getSocket().ifPresent(control -> {
            ServiceSubscription<TextInputStatusInfo> subscription = control.subscribeTextInputStatus(textInputListener);
            control.sendText(text);
            control.unsubscribe(subscription);
        });
    }

    @RuleAction(label = "@text/actionSendButtonLabel", description = "@text/actionSendButtonDesc")
    public void sendButton(
            @ActionInput(name = "text", label = "@text/actionSendButtonInputButtonLabel", description = "@text/actionSendButtonInputButtonDesc") String button) {
        try {
            switch (Button.valueOf(button)) {
                case UP:
                    getSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.UP)));
                    break;
                case DOWN:
                    getSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.DOWN)));
                    break;
                case LEFT:
                    getSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.LEFT)));
                    break;
                case RIGHT:
                    getSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.RIGHT)));
                    break;
                case BACK:
                    getSocket().ifPresent(control -> control.executeMouse(s -> s.button(ButtonType.BACK)));
                    break;
                case DELETE:
                    getSocket().ifPresent(control -> control.sendDelete());
                    break;
                case ENTER:
                    getSocket().ifPresent(control -> control.sendEnter());
                    break;
                case HOME:
                    getSocket().ifPresent(control -> control.executeMouse(s -> s.button("HOME")));
                    break;
                case OK:
                    getSocket().ifPresent(control -> control.executeMouse(s -> s.click()));
                    break;
            }
        } catch (IllegalArgumentException ex) {
            logger.warn("{} is not a valid value for button - available are: {}", button,
                    Stream.of(Button.values()).map(b -> b.name()).collect(Collectors.joining(", ")));
        }
    }

    @RuleAction(label = "@text/actionIncreaseChannelLabel", description = "@text/actionIncreaseChannelDesc")
    public void increaseChannel() {
        getSocket().ifPresent(control -> control.channelUp(createResponseListener()));
    }

    @RuleAction(label = "@text/actionDecreaseChannelLabel", description = "@text/actionDecreaseChannelDesc")
    public void decreaseChannel() {
        getSocket().ifPresent(control -> control.channelDown(createResponseListener()));
    }

    private Optional<LGWebOSTVSocket> getSocket() {
        LGWebOSHandler lgWebOSHandler = getLGWebOSHandler();
        final LGWebOSTVSocket socket = lgWebOSHandler.getSocket();
        if (!socket.isConnected()) {
            logger.warn("Device with ThingID {} is currently not connected.", lgWebOSHandler.getThing().getUID());
            return Optional.empty();
        }

        return Optional.of(socket);
    }

    private ResponseListener<TextInputStatusInfo> createTextInputStatusListener() {
        return new ResponseListener<TextInputStatusInfo>() {

            @Override
            public void onError(@Nullable String error) {
                logger.warn("Response: {}", error);
            }

            @Override
            public void onSuccess(@Nullable TextInputStatusInfo info) {
                logger.debug("Response: {}", info == null ? "OK" : info.getRawData());
            }
        };
    }

    private <O> ResponseListener<O> createResponseListener() {
        return new ResponseListener<O>() {

            @Override
            public void onError(@Nullable String error) {
                logger.warn("Response: {}", error);
            }

            @Override
            public void onSuccess(@Nullable O object) {
                logger.debug("Response: {}", object == null ? "OK" : object.toString());
            }
        };
    }

    // delegation methods for "legacy" rule support

    public static void showToast(@Nullable ThingActions actions, String text) throws IOException {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).showToast(text);
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void showToast(@Nullable ThingActions actions, String icon, String text) throws IOException {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).showToast(icon, text);
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void launchBrowser(@Nullable ThingActions actions, String url) {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).launchBrowser(url);
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static List<Application> getApplications(@Nullable ThingActions actions) {
        if (actions instanceof LGWebOSActions) {
            return ((LGWebOSActions) actions).getApplications();
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void launchApplication(@Nullable ThingActions actions, String appId) {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).launchApplication(appId);
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void launchApplication(@Nullable ThingActions actions, String appId, String param) {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).launchApplication(appId, param);
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void sendText(@Nullable ThingActions actions, String text) {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).sendText(text);
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void sendButton(@Nullable ThingActions actions, String button) {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).sendButton(button);
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void increaseChannel(@Nullable ThingActions actions) {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).increaseChannel();
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }

    public static void decreaseChannel(@Nullable ThingActions actions) {
        if (actions instanceof LGWebOSActions) {
            ((LGWebOSActions) actions).decreaseChannel();
        } else {
            throw new IllegalArgumentException("Instance is not an LGWebOSActions class.");
        }
    }
}
