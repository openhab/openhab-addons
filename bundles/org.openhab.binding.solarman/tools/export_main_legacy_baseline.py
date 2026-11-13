"""Export an independent fixture by executing pinned upstream Java, never the working sources.

SPDX-License-Identifier: EPL-2.0
Build once first so a Surefire report records the resolved dependency classpath.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile
import xml.etree.ElementTree as ET

REVISION = '821bb618754b6a0973ae4a11a28ac93133be278c'
MODULE = Path(__file__).resolve().parent.parent
REPO = MODULE.parent.parent
PREFIX = MODULE.relative_to(REPO).as_posix()


def git(*args):
    return subprocess.run(['git', '-c', f'safe.directory={REPO.as_posix()}', '-C', str(REPO),
                           *args], check=True, capture_output=True, cwd=REPO).stdout


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path, default=os.environ.get('JAVA_HOME'))
    parser.add_argument('--classpath-report', type=Path, help='Surefire XML containing resolved dependency JARs')
    parser.add_argument('--output', type=Path,
                        default=MODULE / 'src/test/resources/legacy/main-baseline.json')
    args = parser.parse_args()
    if not args.java_home:
        parser.error('Set JAVA_HOME or pass --java-home for Java 21.')
    reports = [args.classpath_report] if args.classpath_report else sorted((MODULE / 'target/surefire-reports').glob('TEST-*.xml'))
    if not reports:
        parser.error('Run the official module tests first to record the dependency classpath.')
    properties = ET.parse(reports[0]).findall('./properties/property')
    recorded = next(p.get('value') for p in properties if p.get('name') == 'java.class.path')
    dependency = os.pathsep.join(path for path in recorded.split(os.pathsep)
                                 if path.endswith('.jar'))
    tree = [line.split('\t', 1) for line in git('ls-tree', '-r', REVISION, '--', PREFIX).decode().splitlines()]
    blobs = {path: metadata.split()[2] for metadata, path in tree}
    paths = sorted(blobs)
    java_prefix = PREFIX + '/src/main/java/'
    selected = [path for path in paths if path.startswith(java_prefix) and (
        '/defmodel/' in path or '/channel/' in path
        or path.endswith(('/DefinitionParser.java', '/SolarmanBindingConstants.java',
                          '/typeprovider/ChannelUtils.java')))]
    definitions = [path for path in paths if '/src/main/resources/definitions/' in path
                   and path.endswith('.yaml')]
    assert len(definitions) == 18
    hashes = {}
    with tempfile.TemporaryDirectory(prefix='solarman-upstream-baseline-') as temporary:
        temp = Path(temporary)
        sources = []
        for path in selected + definitions:
            content = git('cat-file', 'blob', blobs[path])
            hashes[path.removeprefix(PREFIX + '/')] = hashlib.sha256(content).hexdigest()
            if path in selected:
                target = temp / 'src' / path.removeprefix(java_prefix)
                sources.append(target)
            else:
                target = temp / 'classes/definitions' / Path(path).name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(content)
        sources.append(MODULE / 'tools/legacy-baseline/ExportLegacyBaseline.java')
        classes = temp / 'classes'
        classes.mkdir(exist_ok=True)
        javac = args.java_home / 'bin/javac'
        java = args.java_home / 'bin/java'

        def run(executable, arguments, name):
            argument_file = temp / name
            argument_file.write_text('\n'.join('"' + str(arg).replace('\\', '/') + '"'
                                                for arg in arguments), encoding='utf-8')
            subprocess.run([str(executable), '@' + str(argument_file)], check=True)

        run(javac, ['-encoding', 'UTF-8', '-proc:none', '-cp', dependency,
                    '-d', classes, *sources], 'compile.args')
        output = temp / 'baseline.json'
        run(java, ['-Duser.language=en', '-Duser.country=US', '-cp',
                   os.pathsep.join([str(classes), dependency]),
                   'ExportLegacyBaseline', output,
                   *[Path(path).stem for path in definitions]], 'run.args')
        profiles = json.loads(output.read_text(encoding='utf-8'))
    result = dict(repository='https://github.com/openhab/openhab-addons', revision=REVISION,
                  provenance='Pinned upstream Java classes compiled in isolation; no working production classes used.',
                  source_sha256=dict(sorted(hashes.items())), profiles=profiles)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
    print(f'Exported {len(profiles)} upstream profiles to {args.output}')


if __name__ == '__main__':
    main()
