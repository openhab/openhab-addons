package org.openhab.binding.restify.internal;

import static org.openhab.binding.restify.internal.RestifyBindingConstants.BINDING_ID;

import java.util.Hashtable;

import javax.servlet.ServletException;

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

    @Activate
    public RestifyEndpoint(@Reference HttpService httpService, @Reference ItemRegistry itemRegistry,
            @Reference ThingRegistry thingRegistry) throws ServletException, NamespaceException {
        this.httpService = httpService;
        var engine = new Engine(itemRegistry, thingRegistry);
        var foo = new RequestProcessor(engine);

        httpService.registerServlet(BINDING_ID, new DispatcherServlet(foo), new Hashtable<>(),
                httpService.createDefaultHttpContext());
    }

    @Deactivate
    public void deactivate() {
        httpService.unregister(BINDING_ID);
    }
}
