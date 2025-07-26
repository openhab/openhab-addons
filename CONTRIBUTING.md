# Contributing to openHAB

Want to hack on openHAB? Awesome! Here are instructions to get you
started. They are probably not perfect, please let us know if anything
feels wrong or incomplete.

## Build Environment

For instructions on setting up your development environment, please
see our dedicated [IDE setup guide](https://www.openhab.org/docs/developer/).

## Contribution guidelines

### Pull requests are always welcome

We are always thrilled to receive pull requests, and do our best to
process them as fast as possible. Not sure if that typo is worth a pull
request? Do it! We will appreciate it.

If your pull request is not accepted on the first try, don't be
discouraged! If there's a problem with the implementation, hopefully you
received feedback on what to improve.

We're trying very hard to keep openHAB lean and focused. We don't want it
to do everything for everybody. This means that we might decide against
incorporating a new feature. However, there might be a way to implement
that feature _on top of_ openHAB.

### Discuss your design in the discussion forum

We recommend discussing your plans [in the discussion forum](https://community.openhab.org/c/add-ons)
before starting to code - especially for more ambitious contributions.
This gives other contributors a chance to point you in the right
direction, give feedback on your design, and maybe point out if someone
else is working on the same thing.

### Create issues...

Any significant improvement should be documented as [a GitHub
issue](https://github.com/openhab/openhab-addons/issues?labels=enhancement&page=1&state=open) before anybody
starts working on it.

### ...but check for existing issues first!

Please take a moment to check that an issue doesn't already exist
documenting your bug report or improvement proposal. If it does, it
never hurts to add a quick "+1" or "I have this problem too". This will
help prioritize the most common problems and requests.

### Conventions

Fork the repo and make changes on your fork in a feature branch.

Submit unit tests for your changes.  openHAB has a great test framework built in; use
it! Take a look at existing tests for inspiration. Run the full test suite on
your branch before submitting a pull request.

Update the documentation when creating or modifying features. Test
your documentation changes for clarity, concision, and correctness, as
well as a clean documentation build.

Write clean code. Universally formatted code promotes ease of writing, reading,
and maintenance.

Pull requests descriptions should be as clear as possible and include a
reference to all the issues that they address.

Pull requests must not contain commits from other users or branches.

Commit messages must start with a capitalized and short summary (max. 50
chars) written in the imperative, followed by an optional, more detailed
explanatory text which is separated from the summary by an empty line.

Code review comments may be added to your pull request. Discuss, then make the
suggested modifications and push additional commits to your feature branch. Be
sure to post a comment after pushing. The new commits will show up in the pull
request automatically, but the reviewers will not be notified unless you
comment.

Commits that fix or close an issue should include a reference like `Fixes #XXX`,
which will automatically close the issue when merged.

### Sign your work

The sign-off is a simple line at the end of the explanation for the
patch, which certifies that you wrote it or otherwise have the right to
pass it on as an open-source patch.  The rules are pretty simple: if you
can certify the below (from
[developercertificate.org](https://developercertificate.org/)):

```text
Developer Certificate of Origin
Version 1.1

Copyright (C) 2004, 2006 The Linux Foundation and its contributors.
660 York Street, Suite 102,
San Francisco, CA 94110 USA

Everyone is permitted to copy and distribute verbatim copies of this
license document, but changing it is not allowed.


Developer's Certificate of Origin 1.1

By making a contribution to this project, I certify that:

(a) The contribution was created in whole or in part by me and I
    have the right to submit it under the open source license
    indicated in the file; or

(b) The contribution is based upon previous work that, to the best
    of my knowledge, is covered under an appropriate open source
    license and I have the right under that license to submit that
    work with modifications, whether created in whole or in part
    by me, under the same open source license (unless I am
    permitted to submit under a different license), as indicated
    in the file; or

(c) The contribution was provided directly to me by some other
    person who certified (a), (b) or (c) and I have not modified
    it.

(d) I understand and agree that this project and the contribution
    are public and that a record of the contribution (including all
    personal information I submit with it, including my sign-off) is
    maintained indefinitely and may be redistributed consistent with
    this project or the open source license(s) involved.
```

then you just add a line to every git commit message:

```text
Signed-off-by: Joe Smith <joe.smith@email.com>
```

using your real name (sorry, no pseudonyms or anonymous contributions.) and an
e-mail address under which you can be reached (sorry, no github noreply e-mail
addresses (such as `username@users.noreply.github.com`) or other non-reachable
addresses are allowed).

On the command line you can use `git commit -s` to sign off the commit.

### How can I become a maintainer?

- Step 1: learn the component inside out
- Step 2: make yourself useful by contributing code, bugfixes, support etc.
- Step 3: volunteer on [the discussion group](https://github.com/openhab/openhab-addons/issues?labels=question&page=1&state=open)

Don't forget: being a maintainer is a time investment. Make sure you will have time to make yourself available.
You don't have to be a maintainer to make a difference on the project!

## Community Guidelines

We want to keep the openHAB community awesome, growing and collaborative. We
need your help to keep it that way. To help with this we have come up with some
general guidelines for the community as a whole:

- Be nice: Be courteous, respectful and polite to fellow community members: no
  regional, racial, gender, or other abuse will be tolerated. We like nice people
  way better than mean ones!

- Encourage diversity and participation: Make everyone in our community
  feel welcome, regardless of their background and the extent of their
  contributions, and do everything possible to encourage participation in
  our community.

- Keep it legal: Basically, don't get us in trouble. Share only content that
  you own, do not share private or sensitive information, and don't break the
  law.

- Stay on topic: Make sure that you are posting to the correct channel
  and avoid off-topic discussions. Remember when you update an issue or
  respond to an email you are potentially sending to a large number of
  people.  Please consider this before you update.  Also remember that
  nobody likes spam.
