import tempfile
import yaml
from pathlib import Path
import subprocess


SCRIPT = Path(__file__).parents[1] / 'sort_openapi_enums.py'


def run_sort(input_data: dict, exceptions=None):
    if exceptions is None:
        exceptions = []
    with tempfile.TemporaryDirectory() as td:
        inp = Path(td) / 'in.yaml'
        out = Path(td) / 'out.yaml'
        with inp.open('w') as f:
            yaml.safe_dump(input_data, f, default_flow_style=False, sort_keys=False)
        cmd = ['python3', str(SCRIPT), str(inp), str(out)]
        for ex in exceptions:
            cmd += ['--exception', ex]
        subprocess.check_call(cmd)
        with out.open('r') as f:
            return yaml.safe_load(f)


def test_sorts_enums_simple():
    data = {'components': {'schemas': {'Thing': {'properties': {'state': {'enum': ['b','A','c']}}}}}}
    out = run_sort(data)
    assert out['components']['schemas']['Thing']['properties']['state']['enum'] == ['A','b','c']


def test_respects_exception_by_schema():
    data = {'components': {'schemas': {'DayOfWeek': {'properties': {'value': {'enum': ['Mon','Tue','Wed']}}},
                                       'Other': {'properties': {'value': {'enum': ['z','a']}}}}}}
    out = run_sort(data, exceptions=['#sym:DayOfWeek'])
    # DayOfWeek must remain in original order
    assert out['components']['schemas']['DayOfWeek']['properties']['value']['enum'] == ['Mon','Tue','Wed']
    # Other should be sorted
    assert out['components']['schemas']['Other']['properties']['value']['enum'] == ['a','z']


def test_sorts_case_insensitive():
    data = {'components': {'schemas': {'S': {'properties': {'p': {'enum': ['apple','Banana','cherry']}}}}}}
    out = run_sort(data)
    assert out['components']['schemas']['S']['properties']['p']['enum'] == ['apple','Banana','cherry']
