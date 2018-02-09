/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.openhab.binding.lgwebos.internal.discovery.LGWebOSDiscovery;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.AppInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.KeyControl;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.TextInputControl;
import com.connectsdk.service.capability.ToastControl;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;

/**
 * This servers as API for other components to this binding.
 *
 * @author Sebastian Prehn - initial contribution
 *
 */
@Component(service = LGWebOS.class)
public class LGWebOS {
    private final Logger LOGGER = LoggerFactory.getLogger(LGWebOS.class);
    private LGWebOSDiscovery discovery;

    public enum Button {
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

    @Reference
    protected void bindDiscovery(LGWebOSDiscovery discovery) {
        this.discovery = discovery;
    }

    protected void unbindDiscovery(LGWebOSDiscovery discovery) {
        this.discovery = null;
    }

    /** Sends a toast message to a WebOS device with openhab icon. */
    public void showToast(String thingId, String text) throws IOException {
        showToast(thingId, LGWebOS.class.getResource("/openhab-logo-square.png").toString(), text);
    }

    /** Sends a toast message to a WebOS device with custom icon. */
    public void showToast(String thingId, String icon, String text) throws IOException {
        Optional<ToastControl> control = getControl(ToastControl.class, thingId);
        if (control.isPresent()) {
            BufferedImage bi = ImageIO.read(new URL(icon));
            try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                    OutputStream b64 = Base64.getEncoder().wrap(os);) {
                ImageIO.write(bi, "png", b64);
                control.get().showToast(text, os.toString(StandardCharsets.UTF_8.name()), "png",
                        createResponseListener());
            }
        }
    }

    /** Opens the given URL in the TV's browser application. */
    public void launchBrowser(String thingId, String url) {
        getControl(Launcher.class, thingId).ifPresent(control -> control.launchBrowser(url, createResponseListener()));
    }

    /** Opens the application with given appId. */
    public void launchApplication(String thingId, String appId) {
        getControl(Launcher.class, thingId).ifPresent(control -> control.launchApp(appId, createResponseListener()));
    }

    /** Opens the application with given appId and passes additional parameters. */
    public void launchApplicationWithParam(String thingId, String appId, Object param) {
        Optional<Launcher> control = getControl(Launcher.class, thingId);
        if (control.isPresent()) {
            control.get().getAppList(new Launcher.AppListListener() {
                @Override
                public void onError(ServiceCommandError error) {
                    LOGGER.warn("error requesting application list: {}.", error.getMessage());
                }

                @Override
                public void onSuccess(List<AppInfo> appInfos) {
                    Optional<AppInfo> appInfo = appInfos.stream().filter(a -> a.getId().equals(appId)).findFirst();
                    if (appInfo.isPresent()) {
                        control.get().launchAppWithInfo(appInfo.get(), param, createResponseListener());
                    } else {
                        LOGGER.warn("TV does not support any app with id: {}.", appId);
                    }
                }
            });
        }
    }

    /** Sends a text input to a WebOS device. */
    public void sendText(String thingId, String text) {
        getControl(TextInputControl.class, thingId).ifPresent(control -> control.sendText(text));
    }

    /** Sends the button press event to a WebOS device. */
    public void sendButton(String thingId, Button button) {
        switch (button) {
            case UP:
                getControl(KeyControl.class, thingId).ifPresent(control -> control.up(createResponseListener()));
                break;
            case DOWN:
                getControl(KeyControl.class, thingId).ifPresent(control -> control.down(createResponseListener()));
                break;
            case LEFT:
                getControl(KeyControl.class, thingId).ifPresent(control -> control.left(createResponseListener()));
                break;
            case RIGHT:
                getControl(KeyControl.class, thingId).ifPresent(control -> control.right(createResponseListener()));
                break;
            case BACK:
                getControl(KeyControl.class, thingId).ifPresent(control -> control.back(createResponseListener()));
                break;
            case DELETE:
                getControl(TextInputControl.class, thingId).ifPresent(control -> control.sendDelete());
                break;
            case ENTER:
                getControl(TextInputControl.class, thingId).ifPresent(control -> control.sendEnter());
                break;
            case HOME:
                getControl(KeyControl.class, thingId).ifPresent(control -> control.home(createResponseListener()));
                break;
            case OK:
                getControl(KeyControl.class, thingId).ifPresent(control -> control.ok(createResponseListener()));
                break;
        }
    }

    private <C extends CapabilityMethods> Optional<C> getControl(Class<C> clazz, String thingId) {
        final Optional<ConnectableDevice> connectableDevice = discovery.getDiscoveryManager().getCompatibleDevices()
                .values().stream().filter(device -> thingId.equals(device.getId())).findFirst();
        if (!connectableDevice.isPresent()) {
            LOGGER.warn("No device found online with id: {}", thingId);
            return Optional.empty();
        }
        C control = connectableDevice.get().getCapability(clazz);
        if (control == null) {
            LOGGER.warn("Device {} does not have the ability: {}", thingId, clazz.getName());
            return Optional.empty();
        }
        return Optional.of(control);
    }

    private <O> ResponseListener<O> createResponseListener() {
        return new ResponseListener<O>() {

            @Override
            public void onError(ServiceCommandError error) {
                LOGGER.warn("Response: {}", error.getMessage());
            }

            @Override
            public void onSuccess(O object) {
                LOGGER.debug("Response: {}", object == null ? "OK" : object.toString());
            }
        };
    }

}
