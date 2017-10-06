/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.action.lgwebos.internal;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.eclipse.smarthome.model.script.engine.action.ParamDoc;
import org.openhab.binding.lgwebos.internal.discovery.LGWebOSDiscovery;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.AppInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.CapabilityMethods;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.capability.TextInputControl;
import com.connectsdk.service.capability.ToastControl;
import com.connectsdk.service.capability.listeners.ResponseListener;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.sessions.LaunchSession;
import com.google.common.collect.Iterables;

/**
 * This class provides a rules action to start applications and send toast messages.
 *
 * @author Sebastian Prehn
 * @since 2.1.0
 */
@Component(service = ActionService.class, immediate = true, name = "action.lgwebos")
public class LGWebOSAction implements ActionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LGWebOSAction.class);

    private static LGWebOSDiscovery discovery;
    private static ResponseListener<LaunchSession> responseListenerLaunchSession = createDefaultResponseListener();
    private static ResponseListener<Object> responseListenerObject = createDefaultResponseListener();

    @Override
    public String getActionClassName() {
        return getActionClass().getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return LGWebOSAction.class;
    }

    @Reference
    protected void bindDiscovery(LGWebOSDiscovery discovery) {
        LGWebOSAction.discovery = discovery;
    }

    protected void unbindDiscovery(LGWebOSDiscovery discovery) {
        LGWebOSAction.discovery = null;
    }

    @ActionDoc(text = "sends a toast message to a web os device with openhab icon")
    public static void showToast(@ParamDoc(name = "deviceId") String deviceId,
            @ParamDoc(name = "text") final String text) throws IOException {
        showToast(deviceId, LGWebOSAction.class.getResource("/openhab-logo-square.png").toString(), text);
    }

    @ActionDoc(text = "sends a toast message to a web os device with custom icon")
    public static void showToast(@ParamDoc(name = "deviceId") String deviceId,
            @ParamDoc(name = "icon") final String icon, @ParamDoc(name = "text") final String text) throws IOException {
        ToastControl control = getControl(ToastControl.class, deviceId);
        if (control != null) {
            BufferedImage bi = ImageIO.read(new URL(icon));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputStream b64 = Base64.getEncoder().wrap(os);
            ImageIO.write(bi, "png", b64);
            control.showToast(text, os.toString("UTF-8"), "png", responseListenerObject);
        }
    }

    @ActionDoc(text = "opens the given URL in the TV's browser app")
    public static void launchBrowser(@ParamDoc(name = "deviceId") String deviceId,
            @ParamDoc(name = "url") final String url) {
        Launcher control = getControl(Launcher.class, deviceId);
        if (control != null) {
            control.launchBrowser(url, responseListenerLaunchSession);
        }
    }

    @ActionDoc(text = "opens the application with given appId")
    public static void launchApplication(@ParamDoc(name = "deviceId") String deviceId,
            @ParamDoc(name = "appId") final String appId) {
        Launcher control = getControl(Launcher.class, deviceId);
        if (control != null) {
            control.launchApp(appId, responseListenerLaunchSession);
        }
    }

    @ActionDoc(text = "opens the application with given appId and passes additional parameters")
    public static void launchApplicationWithParams(@ParamDoc(name = "deviceId") String deviceId,
            @ParamDoc(name = "appId") final String appId, Object param) {
        Launcher control = getControl(Launcher.class, deviceId);
        if (control != null) {
            control.getAppList(new Launcher.AppListListener() {
                @Override
                public void onError(ServiceCommandError error) {
                    LOGGER.warn("error requesting application list: {}.", error.getMessage());
                }

                @Override
                public void onSuccess(List<AppInfo> appInfos) {
                    try {
                        AppInfo appInfo = Iterables.find(appInfos, a -> a.getId().equals(appId));
                        control.launchAppWithInfo(appInfo, param, responseListenerLaunchSession);
                    } catch (NoSuchElementException ex) {
                        LOGGER.warn("TV does not support any app with id: {}.", appId);
                    }
                }
            });
        }
    }

    @ActionDoc(text = "returns a list of all application in the format \"<appId> - <human readable name>\"")
    public static List<String> getApplications(@ParamDoc(name = "deviceId") String deviceId) {
        Launcher control = getControl(Launcher.class, deviceId);
        if (control == null) {
            return Collections.emptyList();
        }
        BlockingQueue<List<String>> result = new ArrayBlockingQueue<>(1);
        control.getAppList(new Launcher.AppListListener() {
            @Override
            public void onError(ServiceCommandError error) {
                LOGGER.warn("Error {}", error.getMessage());
                try {
                    result.put(Collections.emptyList());
                } catch (InterruptedException e) {
                    LOGGER.warn("interruppted", e);
                }
            }

            @Override
            public void onSuccess(List<AppInfo> appInfos) {
                if (LOGGER.isDebugEnabled()) {
                    for (AppInfo a : appInfos) {
                        LOGGER.debug("AppInfo {} - {}", a.getId(), a.getName());
                    }
                }
                try {
                    result.put(appInfos.stream()
                            .map((appInfo) -> String.format("%s - %s", appInfo.getId(), appInfo.getName()))
                            .collect(Collectors.toList()));
                } catch (InterruptedException e) {
                    LOGGER.warn("interruppted", e);
                }
            }
        });
        try {
            return result.take();
        } catch (InterruptedException e) {
            LOGGER.warn("interruppted", e);
            return Collections.emptyList();
        }
    }

    @ActionDoc(text = "sends a text input to a web os device")
    public static void sendText(@ParamDoc(name = "deviceId") String deviceId,
            @ParamDoc(name = "text") final String text) {
        TextInputControl control = getControl(TextInputControl.class, deviceId);
        if (control != null) {
            control.sendText(text);
        }
    }

    @ActionDoc(text = "sends the ender key to a web os device")
    public static void sendEnter(@ParamDoc(name = "deviceId") String deviceId) {
        TextInputControl control = getControl(TextInputControl.class, deviceId);
        if (control != null) {
            control.sendEnter();
        }
    }

    @ActionDoc(text = "sends the delete key to a web os device")
    public static void sendDelete(@ParamDoc(name = "deviceId") String deviceId) {
        TextInputControl control = getControl(TextInputControl.class, deviceId);
        if (control != null) {
            control.sendDelete();
        }
    }

    private static <C extends CapabilityMethods> C getControl(Class<C> clazz, String deviceId) {
        final ConnectableDevice d = discovery.getDiscoveryManager().getCompatibleDevices().get(deviceId);
        if (d == null) {
            LOGGER.warn("No device found with id: {}", deviceId);
            return null;
        }
        C control = d.getCapability(clazz);
        if (control == null) {
            LOGGER.warn("Device {} does not have the ability: {}", deviceId, clazz.getName());
            return null;
        }
        return control;
    }

    private static <O> ResponseListener<O> createDefaultResponseListener() {
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
