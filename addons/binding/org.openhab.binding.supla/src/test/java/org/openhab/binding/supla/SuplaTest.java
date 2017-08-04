package org.openhab.binding.supla;

import org.openhab.binding.supla.internal.supla.entities.SuplaCloudServer;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
public abstract class SuplaTest {
    protected final SuplaCloudServer server = new SuplaCloudServer("127.0.0.1", "clientId", "secret".toCharArray(), "username", "password".toCharArray());
}
