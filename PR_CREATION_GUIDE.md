# PR Creation Guide

## Step 1: Open GitHub PR

1. Go to: https://github.com/openhab/openhab-addons/compare
2. Click "compare across forks"
3. Set branches:
   - **base repository:** `openhab/openhab-addons`
   - **base:** `main`
   - **head repository:** `svnsssd/openhab-addons`
   - **compare:** `FSB14-STM`

## Step 2: Copy PR Description

Open `PR_DESCRIPTION.md` and copy the ENTIRE content into the PR description field on GitHub.

## Step 3: Set PR Title

```
[enocean] Add state machine support for Eltako FSB14 blinds
```

## Step 4: Add Labels (if possible)

- `enhancement`
- `binding`

## Step 5: Add Documentation Note

In the PR description, add at the end:

```markdown
## Documentation

Detailed user documentation has been added:
- `FSB14_BLIND_CONTROL.md` - Complete configuration and usage guide
- TODO comments in code for future architectural improvements
```

## Step 6: Submit

Click "Create Pull Request"

## What Happens Next?

1. **Automated checks** will run (CI/CD)
2. **Code review** from maintainers
3. **Discussion** about architectural decisions
4. **Possible changes requested**

## Tips for Review Process

- Be responsive to comments
- Explain architectural decisions (state machine coupling)
- Reference the TODO comments in code
- Link to community testing (forum post)
- Be open to suggestions for refactoring

## If Changes Are Requested

Simply commit to your `FSB14-STM` branch and push:
```bash
git add .
git commit -m "Address review comments: [description]"
git push origin FSB14-STM
```

The PR will update automatically!
