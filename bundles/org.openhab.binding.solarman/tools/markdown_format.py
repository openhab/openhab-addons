"""Format generated documentation using the repository's Markdown table/list conventions."""
import re


def format_markdown(text):
    lines = text.rstrip().splitlines()
    output = []
    index = 0
    fenced = False
    while index < len(lines):
        line = lines[index]
        if line.lstrip().startswith('```'):
            fenced = not fenced
        if not fenced and line.startswith('|'):
            rows = []
            while index < len(lines) and lines[index].startswith('|'):
                rows.append([cell.strip() for cell in re.split(r'(?<!\\)\|', lines[index].strip()[1:-1])])
                index += 1
            if len(rows) >= 2 and len({len(row) for row in rows}) == 1:
                widths = [max(3, max(len(row[column]) for row in rows)) for column in range(len(rows[0]))]
                for row in rows:
                    cells = []
                    for cell, width in zip(row, widths):
                        if re.fullmatch(r':?-+:?', cell):
                            cells.append((':' if cell.startswith(':') else '') + '-' * (width - cell.count(':')) + (':' if cell.endswith(':') else ''))
                        else:
                            cells.append(cell.ljust(width))
                    output.append('| ' + ' | '.join(cells) + ' |')
            else:
                output.extend('| ' + ' | '.join(row) + ' |' for row in rows)
            continue
        if not fenced:
            line = re.sub(r'^(\s*)\d+\. ', r'\g<1>1. ', line)
            if not line and output and not output[-1]:
                index += 1
                continue
        output.append(line)
        index += 1
    return '\n'.join(output).rstrip() + '\n'
