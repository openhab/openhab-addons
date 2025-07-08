package org.openhab.binding.evcc.internal.discovery.mapper;

import java.util.Collection;

import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;

import com.google.gson.JsonObject;

public interface EvccDiscoveryMapper {
    Collection<DiscoveryResult> discover(JsonObject root, EvccBridgeHandler bridgeHandler);
}
