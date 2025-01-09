import java
#import sys
from openhab.services import find_service


actions = find_service("org.openhab.core.model.script.engine.action.ActionService", None)

#_MODULE = sys.modules[__name__]
#logger.info(str(_MODULE))

#for action in actions:
#    action_class = action.getActionClass()
#    name = str(action_class.getSimpleName())
#    setattr(_MODULE, name, action_class)

Audio = java.type("org.openhab.core.model.script.actions.Audio")
Exec = java.type("org.openhab.core.model.script.actions.Exec")
HTTP = java.type("org.openhab.core.model.script.actions.HTTP")
Log = java.type("org.openhab.core.model.script.actions.Log")
Ping = java.type("org.openhab.core.model.script.actions.Ping")
ScriptExecution = java.type("org.openhab.core.model.script.actions.ScriptExecution")
Semantics = java.type("org.openhab.core.model.script.actions.Semantics")
Transformation = java.type("org.openhab.core.model.script.actions.Transformation")
Voice = java.type("org.openhab.core.model.script.actions.Voice")

#STATIC_IMPORTS = [Audio, Exec, HTTP, Log, Ping, ScriptExecution, Semantics, Transformation, Voice]
#for action in STATIC_IMPORTS:
#    logger.info(str(action.getActionClass().getSimpleName()))
#    name = str(action.getSimpleName())
#    setattr(_MODULE, name, action)
