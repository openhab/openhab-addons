#!/usr/bin/env python3

import fileinput
import re

def main():
    for line in fileinput.input():
        m = re.match(r'.*channel-type id="([^"]*)"', line)
        if m:
            name=m.group(1)
            var=name.upper().replace("-", "_")
            print("public static final String CHANNEL_{} = \"{}\";".format(var, name))
            print("updateState(CHANNEL_{}, );".format(var))
            print("<channel id=\"{}\" typeId=\"{}\" />".format(name, name))


if __name__ == "__main__":
    main()
