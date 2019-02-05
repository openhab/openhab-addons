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
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.openhab.binding.lgwebos.internal.handler.LGWebOSHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.AppInfo;
import com.connectsdk.core.TextInputStatusInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.KeyControl;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.TVControl;
import com.connectsdk.service.capability.TextInputControl;
import com.connectsdk.service.capability.TextInputControl.TextInputStatusListener;
import com.connectsdk.service.capability.ToastControl;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;

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
    private final TextInputStatusListener textInputListener = createTextInputStatusListener();
    private @Nullable LGWebOSHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (LGWebOSHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return this.handler;
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
        showToast(LGWebOSActions.class.getResource("/openhab-logo-square.png").toString(), text);
    }

    @RuleAction(label = "@text/actionShowToastWithIconLabel", description = "@text/actionShowToastWithIconLabel")
    public void showToast(
            @ActionInput(name = "icon", label = "@text/actionShowToastInputIconLabel", description = "@text/actionShowToastInputIconDesc") String icon,
            @ActionInput(name = "text", label = "@text/actionShowToastInputTextLabel", description = "@text/actionShowToastInputTextDesc") String text)
            throws IOException {
        BufferedImage bi = ImageIO.read(new URL(icon));
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(); OutputStream b64 = Base64.getEncoder().wrap(os);) {
            ImageIO.write(bi, "png", b64);
            String string = os.toString(StandardCharsets.UTF_8.name());
            getControl(ToastControl.class)
                    .ifPresent(control -> control.showToast(text, string, "png", createResponseListener()));
        }
    }

    @RuleAction(label = "@text/actionLaunchBrowserLabel", description = "@text/actionLaunchBrowserDesc")
    public void launchBrowser(
            @ActionInput(name = "url", label = "@text/actionLaunchBrowserInputUrlLabel", description = "@text/actionLaunchBrowserInputUrlDesc") String url) {
        getControl(Launcher.class).ifPresent(control -> control.launchBrowser(url, createResponseListener()));
    }

    private List<AppInfo> getAppInfos() {
        LGWebOSHandler lgWebOSHandler = this.handler;
        if (lgWebOSHandler == null) {
            throw new IllegalStateException(
                    "ThingHandler must be set before any action may be invoked on LGWebOSActions.");
        }

        final Optional<ConnectableDevice> connectableDevice = lgWebOSHandler.getDevice();
        if (!connectableDevice.isPresent()) {
            logger.warn("Device not present.");
            return Collections.emptyList();
        }
        ConnectableDevice device = connectableDevice.get();

        List<AppInfo> appInfos = lgWebOSHandler.getLauncherApplication().getAppInfos(device);
        if (appInfos == null) {
            logger.warn("No application list cached for {}, ignoring command.", lgWebOSHandler.getThing().getLabel());
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
        List<AppInfo> appInfos = getAppInfos();
        getControl(Launcher.class).ifPresent(control -> {
            Optional<AppInfo> appInfo = appInfos.stream().filter(a -> a.getId().equals(appId)).findFirst();
            if (appInfo.isPresent()) {
                control.launchApp(appId, createResponseListener());
            } else {
                logger.warn("TV does not support any app with id: {}.", appId);
            }
        });
    }

    @RuleAction(label = "@text/actionLaunchApplicationWithParamsLabel", description = "@text/actionLaunchApplicationWithParamsDesc")
    public void launchApplication(
            @ActionInput(name = "appId", label = "@text/actionLaunchApplicationInputAppIDLabel", description = "@text/actionLaunchApplicationInputAppIDDesc") String appId,
            @ActionInput(name = "params", label = "@text/actionLaunchApplicationInputParamsLabel", description = "@text/actionLaunchApplicationInputParamsDesc") Object params) {
        JSONObject parameters;
        try {
            parameters = new JSONObject(params);
        } catch (JSONException ex) {
            logger.warn("Parameters value ({}) is not in a valid JSON format. {}", params, ex.getMessage());
            return;
        }
        List<AppInfo> appInfos = getAppInfos();
        getControl(Launcher.class).ifPresent(control -> {
            Optional<AppInfo> appInfo = appInfos.stream().filter(a -> a.getId().equals(appId)).findFirst();
            if (appInfo.isPresent()) {
                control.launchAppWithInfo(appInfo.get(), parameters, createResponseListener());
            } else {
                logger.warn("TV does not support any app with id: {}.", appId);
            }
        });
    }

    @RuleAction(label = "@text/actionSendTextLabel", description = "@text/actionSendTextDesc")
    public void sendText(
            @ActionInput(name = "text", label = "@text/actionSendTextInputTextLabel", description = "@text/actionSendTextInputTextDesc") String text) {
        getControl(TextInputControl.class).ifPresent(control -> {
            control.subscribeTextInputStatus(textInputListener);
            control.sendText(text);
        });
    }

    @RuleAction(label = "@text/actionSendButtonLabel", description = "@text/actionSendButtonDesc")
    public void sendButton(
            @ActionInput(name = "text", label = "@text/actionSendButtonInputButtonLabel", description = "@text/actionSendButtonInputButtonDesc") String button) {
        try {
            switch (Button.valueOf(button)) {
                case UP:
                    getControl(KeyControl.class).ifPresent(control -> control.up(createResponseListener()));
                    break;
                case DOWN:
                    getControl(KeyControl.class).ifPresent(control -> control.down(createResponseListener()));
                    break;
                case LEFT:
                    getControl(KeyControl.class).ifPresent(control -> control.left(createResponseListener()));
                    break;
                case RIGHT:
                    getControl(KeyControl.class).ifPresent(control -> control.right(createResponseListener()));
                    break;
                case BACK:
                    getControl(KeyControl.class).ifPresent(control -> control.back(createResponseListener()));
                    break;
                case DELETE:
                    getControl(TextInputControl.class).ifPresent(control -> {
                        control.subscribeTextInputStatus(textInputListener);
                        control.sendDelete();
                    });
                    break;
                case ENTER:
                    getControl(TextInputControl.class).ifPresent(control -> {
                        control.subscribeTextInputStatus(textInputListener);
                        control.sendEnter();
                    });
                    break;
                case HOME:
                    getControl(KeyControl.class).ifPresent(control -> control.home(createResponseListener()));
                    break;
                case OK:
                    getControl(KeyControl.class).ifPresent(control -> control.ok(createResponseListener()));
                    break;
            }
        } catch (IllegalArgumentException ex) {
            logger.warn("{} is not a valid value for button - available are: {}", button,
                    Stream.of(Button.values()).map(b -> b.name()).collect(Collectors.joining(", ")));
        }
    }

    @RuleAction(label = "@text/actionIncreaseChannelLabel", description = "@text/actionIncreaseChannelDesc")
    public void increaseChannel() {
        getControl(TVControl.class).ifPresent(control -> control.channelUp(createResponseListener()));
    }

    @RuleAction(label = "@text/actionDecreaseChannelLabel", description = "@text/actionDecreaseChannelDesc")
    public void decreaseChannel() {
        getControl(TVControl.class).ifPresent(control -> control.channelDown(createResponseListener()));
    }

    private <C extends @Nullable CapabilityMethods> Optional<C> getControl(Class<C> clazz) {
        LGWebOSHandler lgWebOSHandler = this.handler;
        if (lgWebOSHandler == null) {
            throw new IllegalStateException(
                    "ThingHandler must be set before any action may be invoked on LGWebOSActions.");
        }

        ThingStatus status = lgWebOSHandler.getThing().getStatus();
        if (!ThingStatus.ONLINE.equals(status)) {
            logger.info("Device not online.");
            return Optional.empty();
        }

        final Optional<ConnectableDevice> connectableDevice = lgWebOSHandler.getDevice();
        if (!connectableDevice.isPresent()) {
            logger.warn("Device not present.");
            return Optional.empty();
        }

        C control = connectableDevice.get().getCapability(clazz);
        if (control == null) {
            logger.warn("Device does not have the ability: {}.", clazz.getName());
            return Optional.empty();
        }
        return Optional.of(control);
    }

    private TextInputStatusListener createTextInputStatusListener() {
        return new TextInputStatusListener() {

            @Override
            public void onError(@Nullable ServiceCommandError error) {
                logger.warn("Response: {}", error == null ? "" : error.getMessage());
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
            public void onError(@Nullable ServiceCommandError error) {
                logger.warn("Response: {}", error == null ? "" : error.getMessage());
            }

            @Override
            public void onSuccess(O object) {
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
