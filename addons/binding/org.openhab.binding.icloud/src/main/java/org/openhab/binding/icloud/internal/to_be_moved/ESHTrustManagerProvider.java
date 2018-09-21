package org.openhab.binding.icloud.internal.to_be_moved;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.io.net.http.TrustManagerProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component
public class ESHTrustManagerProvider implements TrustManagerProvider {
    private final List<EndpointKeyStore> endpointTrustManagers = new CopyOnWriteArrayList<>();

    private X509ExtendedTrustManager eshTrustManager;

    @Activate
    protected void activate() {
        eshTrustManager = new ESHTrustManager(endpointTrustManagers);
    }

    public X509ExtendedTrustManager getEshTrustManager() {
        return eshTrustManager;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    protected void addEndpointTrustManager(EndpointKeyStore endpointTrustManager) {
        endpointTrustManagers.add(endpointTrustManager);
    }

    protected void removeEndpointTrustManager(EndpointKeyStore endpointTrustManager) {
        endpointTrustManagers.remove(endpointTrustManager);
    }

    @Override
    public @NonNull Stream<@NonNull TrustManager> getTrustManagers(@NonNull String endpoint) {
        return Stream.of(getEshTrustManager());
    }
}
