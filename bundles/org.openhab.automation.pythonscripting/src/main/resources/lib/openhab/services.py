import java

from openhab.jsr223 import scope


Java_FrameworkUtil = java.type("org.osgi.framework.FrameworkUtil")

scriptExtension   = scope.get("scriptExtension")

_BUNDLE = Java_FrameworkUtil.getBundle(scriptExtension.getClass())
BUNDLE_CONTEXT = _BUNDLE.getBundleContext() if _BUNDLE else None

def get_service(class_or_name):
    if BUNDLE_CONTEXT:
        classname = class_or_name.getName() if isinstance(class_or_name, type) else class_or_name
        ref = BUNDLE_CONTEXT.getServiceReference(classname)
        return BUNDLE_CONTEXT.getService(ref) if ref else None
    else:
        return None

def find_service(class_name, service_filter):
    if BUNDLE_CONTEXT:
        references = BUNDLE_CONTEXT.getServiceReferences(class_name, service_filter)
        if references:
            return [BUNDLE_CONTEXT.getService(reference) for reference in references]
        else:
            return []
    else:
        return None
