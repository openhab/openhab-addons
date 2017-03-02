package org.openhab.binding.elkm1.internal.action;

import org.eclipse.smarthome.model.script.engine.action.ActionService;
import org.openhab.binding.elkm1.internal.ElkM1HandlerFactory;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElkM1ActionService implements ActionService, BundleListener {
    private static final Logger logger = LoggerFactory.getLogger(ElkM1Actions.class);

    private ComponentContext componentContext;

    public void activate(ComponentContext componentContext) {
        this.componentContext = componentContext;
        logger.error("Activate {}", componentContext);
        ServiceReference<ElkM1HandlerFactory> reference = componentContext.getBundleContext()
                .getServiceReference(ElkM1HandlerFactory.class);
        if (reference != null) {
            if (reference.isAssignableTo(componentContext.getBundleContext().getBundle(),
                    ElkM1HandlerFactory.class.getName())) {
                logger.error("Can assign to {}", ElkM1HandlerFactory.class.getName());
                componentContext.getBundleContext().getService(reference);
            } else {
                logger.error("Cannot assign to {}", ElkM1HandlerFactory.class.getName());
            }
        } else {
            logger.error("Reference is null");
        }
        componentContext.getBundleContext().addBundleListener(this);
    }

    protected void deactivate(ComponentContext componentContext) {
        logger.error("Deactivate {}", componentContext);
    }

    @Override
    public String getActionClassName() {
        return ElkM1Actions.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return ElkM1Actions.class;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        ServiceReference<ElkM1HandlerFactory> reference = componentContext.getBundleContext()
                .getServiceReference(ElkM1HandlerFactory.class);
        if (reference != null) {
            if (reference.isAssignableTo(componentContext.getBundleContext().getBundle(),
                    ElkM1HandlerFactory.class.getName())) {
                logger.error("Can assign to {}", ElkM1HandlerFactory.class.getName());
                componentContext.getBundleContext().getService(reference);
            } else {
                logger.error("Cannot assign to {}", ElkM1HandlerFactory.class.getName());
            }
        } else {
            logger.error("Reference is null");
        }
    }
}
