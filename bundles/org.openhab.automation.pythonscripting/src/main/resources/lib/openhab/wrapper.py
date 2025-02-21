def __import_wrapper__():
    import builtins
    import types
    import sys
    import java
    import os
    import traceback

    class Module(types.ModuleType):
        def __init__(self, name, modules):
            super().__init__(name)
            self.__all__ = list(modules.keySet() if hasattr(modules, 'keySet') else modules.keys() )
            for k in self.__all__:
                if hasattr(modules[k], 'getClass') and modules[k].getClass().getName() == "java.util.HashMap":
                    modules[k] = Module(k, modules[k])
                setattr(self, k, modules[k])
    def processModules(name, fromlist, modules):
        if modules:
            return Module(name, modules)
        msg = "No module named '{}{}'".format(name, '.' + '|'.join(fromlist) if fromlist else "")
        raise ModuleNotFoundError(msg)
    def getImportProxy():
        depth = 1
        while True:
            try:
                frame = sys._getframe(depth)
                if '__import_proxy__' in frame.f_globals:
                    return frame.f_globals['__import_proxy__']
                depth += 1
            except ValueError:
                raise EnvironmentError("No __import_proxy__ is available")
    importProxy = getImportProxy()
    def importWrapper(name, globals=None, locals=None, fromlist=(), level=0):
        if name.startswith("org.openhab"):
            modules = {}
            _modules = importProxy(name, fromlist)
            for _name in _modules['class_list']:
                try:
                    modules[_name.split(".")[-1]] = java.type(_name)
                except KeyError:
                    raise ModuleNotFoundError("Class '{}' not found".format(_name))
            return processModules(name, fromlist, modules)
        if name.startswith("scope"):
            modules = importProxy(name, fromlist)
            return processModules(name, fromlist, modules)
        return importOrg(name, globals, locals, fromlist, level)

    importOrg = builtins.__import__
    builtins.__import__ = importWrapper

    def excepthook(exctype, excvalue, tb):
        #filename = tb.tb_frame.f_code.co_filename
        #name = tb.tb_frame.f_code.co_name
        #line_no = tb.tb_lineno
        print("Traceback (most recent call last):", file=sys.stderr)
        for line in traceback.format_tb(tb):
            if "importWrapper" in line and "importOrg" in line:
                continue
            #print("  File \"{}\", line {}, in {}".format(filename, line_no, name))
            print(line, file=sys.stderr)
        print("{}, {}".format(exctype.__name__, excvalue), file=sys.stderr)
        #print("{}: {}".format(exctype, excvalue), file=sys.stderr)
    sys.excepthook = excepthook
__import_wrapper__()
