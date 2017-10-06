/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos.handler;

import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.connectsdk.core.AppInfo;
import com.connectsdk.device.ConnectableDevice;
import com.connectsdk.service.capability.Launcher;
import com.connectsdk.service.command.ServiceCommandError;
import com.connectsdk.service.command.ServiceSubscription;
import com.connectsdk.service.sessions.LaunchSession;
import com.google.common.collect.Iterables;

/**
 * Provides ability to launch an application on the TV.
 *
 * @author Sebastian Prehn
 * @since 2.1.0
 */
public class LauncherApplication extends BaseChannelHandler<Launcher.AppInfoListener> {
    private final Logger logger = LoggerFactory.getLogger(LauncherApplication.class);

    private Launcher getControl(final ConnectableDevice device) {
        return device.getCapability(Launcher.class);
    }

    @Override
    public void onReceiveCommand(final ConnectableDevice d, Command command) {
        if (d.hasCapabilities(Launcher.Application_List, Launcher.Application)) {
            final String value = command.toString();
            final Launcher control = getControl(d);
            control.getAppList(new Launcher.AppListListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    logger.warn("error requesting application list: {}.", error.getMessage());
                }

                @Override
                public void onSuccess(List<AppInfo> appInfos) {
                    if (logger.isDebugEnabled()) {
                        for (AppInfo a : appInfos) {
                            logger.debug("AppInfo {} - {}", a.getId(), a.getName());
                        }
                    }
                    try {
                        AppInfo appInfo = Iterables.find(appInfos, a -> a.getId().equals(value));
                        control.launchApp(appInfo.getId(),
                                LauncherApplication.this.<LaunchSession> createDefaultResponseListener());
                    } catch (NoSuchElementException ex) {
                        logger.warn("TV does not support any app with id: {}.", value);
                    }
                }
            });
        }
    }

    @Override
    protected ServiceSubscription<Launcher.AppInfoListener> getSubscription(ConnectableDevice device,
            final String channelId, final LGWebOSHandler handler) {
        if (device.hasCapability(Launcher.RunningApp_Subscribe)) {
            return getControl(device).subscribeRunningApp(new Launcher.AppInfoListener() {

                @Override
                public void onError(ServiceCommandError error) {
                    logger.warn("{} {} {}", error.getCode(), error.getPayload(), error.getMessage());
                }

                @Override
                public void onSuccess(AppInfo appInfo) {
                    handler.postUpdate(channelId, new StringType(appInfo.getId()));
                }
            });
        } else {
            return null;
        }
    }
}
