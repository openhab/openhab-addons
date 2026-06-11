#!/usr/bin/env python3
"""Sort enum arrays in an OpenAPI YAML file except for configured exceptions.

Usage: sort_openapi_enums.py input.yaml output.yaml [--exception '#sym:DayOfWeek']...
"""
from __future__ import annotations
import sys
import argparse
import yaml
from typing import List


def load_yaml(path: str):
    with open(path, 'r') as f:
        return yaml.safe_load(f)


def dump_yaml(data, path: str):
    with open(path, 'w') as f:
        yaml.safe_dump(data, f, default_flow_style=False, sort_keys=False)


def path_contains_schema(path: List[str], schema_name: str) -> bool:
    for i in range(len(path) - 2):
        if path[i] == 'components' and path[i+1] == 'schemas' and path[i+2] == schema_name:
            return True
    return False


def sort_enums(data, exceptions: List[str]):
    def visit(obj, path=None):
        if path is None:
            path = []
        if isinstance(obj, dict):
            for k, v in list(obj.items()):
                if k == 'enum' and isinstance(v, list):
                    excluded = False
                    for ex in exceptions:
                        if ex.startswith('#sym:'):
                            schema = ex.split(':', 1)[1]
                            if path_contains_schema(path, schema):
                                excluded = True
                                break
                    if not excluded:
                        try:
                            obj[k] = sorted(v, key=lambda s: str(s).lower())
                        except Exception:
                            obj[k] = sorted(v)
                else:
                    visit(v, path + [k])
        elif isinstance(obj, list):
            for idx, item in enumerate(obj):
                visit(item, path + [str(idx)])

    visit(data)


def parse_args(argv: List[str]):
    p = argparse.ArgumentParser()
    p.add_argument('input')
    p.add_argument('output')
    p.add_argument('--exception', '-e', action='append', default=[], help="Exception like '#sym:DayOfWeek'")
    return p.parse_args(argv)


def main(argv: List[str]):
    args = parse_args(argv)
    data = load_yaml(args.input)
    sort_enums(data, args.exception)
    dump_yaml(data, args.output)


if __name__ == '__main__':
    main(sys.argv[1:])
