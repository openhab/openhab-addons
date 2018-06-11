package org.openhab.binding.qivicon.internal;

import static org.openhab.binding.qivicon.internal.QiviconBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.osgi.service.component.annotations.Component;

@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true)
public class QiviconDiscovery extends AbstractDiscoveryService {
    public QiviconDiscovery() {
        super(SUPPORTED_THING_TYPES_UIDS, 10, false);
    }

    @Override
    protected void startScan() {
        // TODO Auto-generated method stub

        // DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
        // .withLabel(deviceName).withProperties(device.getBulbInfo())
        // .withProperty(PARAMETER_NETWORK_ADDRESS, device.getNetworkAddress()).build();

        // thingDiscovered(discoveryResult);
    }
}
