import sys


def get_top_call_stack_frame():
    depth = 1
    while True:
        try:
            frame = sys._getframe(depth)
            if '__context__' in frame.f_globals:
                return frame.f_globals
            depth += 1
        except ValueError:
            raise EnvironmentError("No JSR223 scope is available")

TopCallStackFrame = get_top_call_stack_frame()
