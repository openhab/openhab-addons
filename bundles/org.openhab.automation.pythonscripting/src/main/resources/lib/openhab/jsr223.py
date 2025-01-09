import sys


def get_scope():
    depth = 1
    while True:
        try:
            frame = sys._getframe(depth)
            if 'context' in frame.f_globals:
                return frame.f_globals
            depth += 1
        except ValueError:
            raise EnvironmentError("No JSR223 scope is available")

scope = get_scope()
