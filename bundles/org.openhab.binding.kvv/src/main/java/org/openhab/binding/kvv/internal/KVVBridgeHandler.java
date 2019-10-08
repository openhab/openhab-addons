package org.openhab.binding.kvv.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.TimerTask;

import com.google.gson.Gson;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class KVVBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KVVBridgeHandler.class);

    public KVVBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        scheduler.execute(() -> {
            updateStatus(ThingStatus.ONLINE);
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

}
