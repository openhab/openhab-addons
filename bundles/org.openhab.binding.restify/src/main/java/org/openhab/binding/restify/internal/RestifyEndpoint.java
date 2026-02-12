package org.openhab.binding.restify.internal;

import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;

import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.ServletException;

import org.openhab.binding.restify.internal.config.ConfigException;
import org.openhab.binding.restify.internal.config.ConfigWatcher;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

@Component(service = RestifyEndpoint.class, immediate = true)
public class RestifyEndpoint {
    private final HttpService httpService;
    private final ScheduledExecutorService scheduledPool;
    private final ConfigWatcher configWatcher;

    @Activate
    public RestifyEndpoint(@Reference HttpService httpService, @Reference ItemRegistry itemRegistry,
            @Reference ThingRegistry thingRegistry)
            throws ServletException, NamespaceException, ConfigException, IOException {
        this.httpService = httpService;
        this.scheduledPool = ThreadPoolManager.getScheduledPool(BINDING_ID);
        this.configWatcher = new ConfigWatcher(scheduledPool);
        var engine = new Engine(itemRegistry, thingRegistry);
        var processor = new RequestProcessor(configWatcher, engine);

        httpService.registerServlet(BINDING_ID, new DispatcherServlet(processor, new JsonEncoder()), new Hashtable<>(),
                httpService.createDefaultHttpContext());
    }

    @Deactivate
    public void deactivate() throws Exception {
        httpService.unregister(BINDING_ID);
        configWatcher.close();
        scheduledPool.close();
    }
}
