# PERFORMANCE

## setup

### test.items

```
Group  eOther_Error   "Test"

String eOther_Error_1 "Test 1" (eOther_Error) ["Point"]
String eOther_Error_2 "Test 2" (eOther_Error) ["Point"]
String eOther_Error_3 "Test 3" (eOther_Error) ["Point"]
String eOther_Error_4 "Test 4" (eOther_Error) ["Point"]
String eOther_Error_5 "Test 5" (eOther_Error) ["Point"]
String eOther_Error_6 "Test 6" (eOther_Error) ["Point"]
String eOther_Error_7 "Test 7" (eOther_Error) ["Point"]
String eOther_Error_8 "Test 8" (eOther_Error) ["Point"]
String eOther_Error_9 "Test 9" (eOther_Error) ["Point"]
```

### test.py

```python
@rule(
    trigger = [
        GenericCronTrigger("*/5 * * * * ?")
    ]
)
class Message:
    def execute(self, module, input):
        items = Registry.getItem("eOther_Error").getAllMembers()
        for item in items:
            if item.getState() != NULL and len(item.getState().toString()) > 0:
                pass
```

## runtime
- runtime in graalpy: 1.0ms until 3.5ms
- runtime in jython: 0.2ms
- runtime in jsscripting: 1.0ms until 4.0ms (similar implementation)

## conclusion
- first test results showing very fast performance if only python is involved
- but openhab rules are depending mostly on wrapped java objects (like item, state etc) and this is 5-10 times slower then jython
- same slow down is visible in jsscripting
- no difference if 'python-community' or just 'python' package 
 