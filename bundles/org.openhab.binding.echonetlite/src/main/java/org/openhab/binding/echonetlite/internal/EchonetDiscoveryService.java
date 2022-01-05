package org.openhab.binding.echonetlite.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.echonetlite.internal.EchonetLiteBindingConstants.THING_TYPE_ECHONET_DEVICE;

@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.echonetlite")
public class EchonetDiscoveryService extends AbstractDiscoveryService implements EchonetDiscoveryListener {

    private final Logger logger = LoggerFactory.getLogger(EchonetDiscoveryService.class);
    @Reference
    @Nullable
    private EchonetMessengerService messengerService;

    public EchonetDiscoveryService() {
        super(Set.of(THING_TYPE_ECHONET_DEVICE), 10);
    }

    @Override
    protected void startScan() {
        logger.debug("startScan: {}", messengerService);
        if (null != messengerService) {
            messengerService.startDiscovery(this);
        } else {
            logger.error("messengerService not initialized");
        }
    }

    @Override
    protected synchronized void stopScan() {
        if (null != messengerService) {
            messengerService.stopDiscovery();
        }
    }

    @Override
    public void onDeviceFound(String identifier, InstanceKey instanceKey) {
        final DiscoveryResult discoveryResult = DiscoveryResultBuilder
                .create(new ThingUID(THING_TYPE_ECHONET_DEVICE, identifier))
                .withRepresentationProperty(instanceKey.representationProperty())
                .withProperty("hostname", instanceKey.address.getAddress().getHostAddress())
                .withProperty("port", instanceKey.address.getPort())
                .withProperty("groupCode", instanceKey.klass.groupCode())
                .withProperty("classCode", instanceKey.klass.classCode())
                .withProperty("instance", instanceKey.instance)
                .build();
        thingDiscovered(discoveryResult);
    }
}
