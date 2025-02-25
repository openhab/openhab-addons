TODO
- fix binding reload
- fix eclipse build
- fix dependency watcher for libs as symlinks
  - is currently not possible, because core openhab filewatcher is not able to handle it 
- extend SimpleRule => instead of wrapping in helper class (needs upcoming graalpy 24.2.0 release => register_interop_type) => expected March 18, 2025
  - not high prio. maybe not needed

DONE
- @when and @onlyif support
- better error messages if error is not catched by "helper.py => handle_exception". In java stack trace we do not get correct file and line number
  - stdout and stderr redirect to Logger => syntax errors
  - sys.excepthook in helper.py => handle import errors => "Failed to execute script: ModuleNotFoundError: No module named 'Time'"
  - sys.excepthook in helper.py => handle name errors => "Failed to execute script: NameError: name 'txf' is not defined"
- implement metadata access
- implement getStableState (average calculation which takes into account the values ​​depending on their duration)
- check why assigning tag 'Schedule' to rules with GenericCronTrigger is freezing webui schedule page
  - reason was the cron expression syntax which was not possible to visualize (unlimited and every 5 minutes) 
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
  
