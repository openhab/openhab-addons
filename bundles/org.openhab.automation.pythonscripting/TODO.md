TODO
- fix binding reload
- check why assigning tag 'Schedule' to rules with GenericCronTrigger is freezing webui schedule page
- extend SimpleRule instead of wrapping in helper class (needs upcoming graalpy 24.2.0 release => register_interop_type) => expected March 18, 2025
   maybe this will solve the freezing schedule tag assignment

DONE
- implement dependency tracker and reloader
- implement persistence access
- implement semantic access
- implement caching
- find a good concept for handling datetime conversion
- find a way to use SET or Collection as arguments in python for native java objects
  - https://github.com/oracle/graalpython/issues/260
  - In python I try to add tags to SimpleRule. The problem is that SimpleRule.setTags expects a SET as a parameter which is currently not supported in graalpy
  RESOLUTION: list is mapped to Collection and a set must be wrapped into a 'GraalWrapperSet' python object. This will force java to convert it to a SET 
  (not perfect but it works)
  
