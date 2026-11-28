"""Export pinned Sunsynk definitions without installing Home Assistant dependencies.

Only definition modules and their dataclasses are loaded. Network clients are not
imported. The small adapters below replace table rendering and MQTT constants,
not register conversion or profile logic. No Python is required by the binding.
"""

import argparse
import ast
import copy
import importlib
import json
import sys
import subprocess
import types
from collections import Counter
from pathlib import Path



def package(name, path=None):
    module = types.ModuleType(name)
    if path is not None:
        module.__path__ = [str(path)]
    sys.modules[name] = module
    return module


def export(source, destination):
    git = ["git", "--no-optional-locks", "-c", "safe.directory=" + source.as_posix(), "-C", str(source)]
    revision = subprocess.check_output(git + ["rev-parse", "HEAD"], text=True).strip()
    if revision != "f1ecbc28e3ccf2b9f87b1ccee85a41628567da43":
        raise ValueError("The source checkout does not match the pinned Sunsynk revision")
    subprocess.run(git + ["diff", "--quiet", "HEAD", "--", "src/sunsynk"], check=True)
    lib = source / "src/sunsynk"
    root = package("sunsynk", lib)
    for name, value in {"AMPS": "A", "CELSIUS": "°C", "HZ": "Hz", "KWH": "kWh", "VOLT": "V", "WATT": "W"}.items():
        setattr(root, name, value)
    utilities = package("sunsynk.utils")
    utilities.import_module = importlib.import_module
    utilities.table_data = lambda data: ([], [])
    utilities.pretty_table = lambda *args: types.SimpleNamespace(get_string=lambda: "")
    package("mqtt_entity", source)
    mqtt = package("mqtt_entity.utils")
    mqtt.BOOL_ON, mqtt.BOOL_OFF = "ON", "OFF"
    sensors = importlib.import_module("sunsynk.sensors")
    root.SensorDefinitions = sensors.SensorDefinitions
    rw = importlib.import_module("sunsynk.rwsensors")
    faults = {}
    tree = ast.parse((lib / "sensors.py").read_text(encoding="utf-8"))
    for node in tree.body:
        if isinstance(node, ast.ClassDef) and node.name in ("FaultSensor", "HVFaultSensor"):
            for item in ast.walk(node):
                if isinstance(item, ast.Assign) and any(isinstance(t, ast.Name) and t.id == "faults" for t in item.targets):
                    faults[node.name] = ast.literal_eval(item.value)
    profiles = {}
    oracle = []
    write_oracle = []
    for name in ["single_phase", "single_phase_16kw", "three_phase_lv", "three_phase_hv", "micro_hybrid_ess"]:
        definitions = importlib.import_module("sunsynk.definitions." + name).SENSORS
        result = []
        for sensor in definitions.all.values():
            item = {"id": sensor.id, "name": sensor.name, "kind": type(sensor).__name__, "registers": sensor.address,
                    "unit": sensor.unit, "factor": sensor.factor, "mask": sensor.bitmask,
                    "writable": isinstance(sensor, rw.RWSensor)}
            for attribute in ["min", "max", "on", "off", "offset", "value", "options", "unknown", "factors", "absolute", "no_negative", "year_offset", "source_ids"]:
                if hasattr(sensor, attribute):
                    value = getattr(sensor, attribute)
                    item[attribute] = {"sensor": value.id} if isinstance(value, sensors.Sensor) else value
            if item["kind"] in faults:
                item["faults"] = faults[item["kind"]]
            result.append(item)
            kind = item["kind"]
            commands = list(sensor.options.values()) if kind == "SelectRWSensor" else ["ON", "OFF"] if kind == "SwitchRWSensor" else ["23:59"] if kind == "TimeRWSensor" else ["2026-10-30 12:34:56"] if kind == "SystemTimeRWSensor" else []
            for command in commands:
                expected_words = list(sensor.value_to_reg(command, None))
                if sensor.bitmask:
                    patch = importlib.import_module("sunsynk.helpers").patch_bitmask
                    expected_words[0] = patch(0xA5A5, expected_words[0], sensor.bitmask)
                write_oracle.append({"profile": name, "sensor": sensor.id, "kind": kind,
                                     "command": command.replace(" ", "T") + "Z" if kind == "SystemTimeRWSensor" else command,
                                     "expected": expected_words})
            if item["kind"] != "PVDynamicTotalSensor":
                instance = copy.copy(sensor)
                if item["kind"] == "Sensor16":
                    instance.history0, instance.history1 = [], []
                count = len(sensor.address)
                sequences = [[0] * count, [32767] * count, [32768] * count, [65535] * count]
                if item["kind"] == "Sensor16":
                    sequences += [[65535, 65535]] * 11 + [[0, 32768], [0, 65535]]
                if item["kind"] == "TempSensor" or abs(sensor.factor) == 0.001:
                    sequences += [[2675] * count, [2685] * count]
                if item["kind"] == "SystemTimeRWSensor":
                    sequences = [[(26 << 8) | 10, (30 << 8) | 12, (34 << 8) | 56]]
                if item["kind"] == "TimeRWSensor":
                    sequences = [[0], [1], [2359]]
                cases = []
                for words in sequences:
                    expected = instance.reg_to_value(tuple(words))
                    if item["kind"] == "TimeRWSensor":
                        expected = expected.zfill(5)
                    cases.append({"words": words, "expected": expected})
                oracle.append({"profile": name, "sensor": sensor.id, "kind": item["kind"], "cases": cases})
        profiles[name] = result
    destination.mkdir(parents=True, exist_ok=True)
    for name, items in profiles.items():
        (destination / (name + ".json")).write_text(json.dumps(items, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        print(name, len(items), dict(Counter(i["kind"] for i in items)))
    test_resource = Path(__file__).resolve().parent.parent / "src/test/resources/sunsynk/oracle.json"
    test_resource.parent.mkdir(parents=True, exist_ok=True)
    test_resource.write_text(json.dumps(oracle, indent=2, ensure_ascii=True) + "\n", encoding="utf-8")
    test_resource.with_name("write-oracle.json").write_text(json.dumps(write_oracle, indent=2, ensure_ascii=True) + "\n", encoding="utf-8")
    print("Python differential fixtures:", len(oracle))
    print("Python write fixtures:", len(write_oracle))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    export(args.source.resolve(), args.destination.resolve())
