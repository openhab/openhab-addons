# Clarify QA Instructions Session

**Status**: ⏳ NOT STARTED (2025-12-05)

## Objective
Prepare a dedicated session to clarify and confirm the required QA/validation steps for this repository (openHAB Jellyfin binding) so future runs avoid ambiguity.

## Context
- Current QA references: `.github/03-code-quality/03-code-quality-core.md`, `.github/00-agent-workflow/00.6-quality-validation-checklist.md`, `.github/06-build-warnings/06-build-warnings-core.md`.
- Repository type: `openhab-binding` per `project-type.txt`.
- Recent asks: use Maven directly (no build.sh); ensure zero-warnings policy and formatting via `mvn spotless:apply` / `mvn clean install`.

## Tasks
- [ ] Confirm required Maven targets for routine validation (format, tests, packaging). 
- [ ] Confirm handling of baseline warnings from generated API models (unused locals, serialVersionUID) and whether exemptions apply when files are untouched.
- [ ] Confirm expectation for logging test-induced errors in negative tests (e.g., invalid URI tests) and whether they violate “zero warnings” policy.
- [ ] Capture definitive step-by-step QA checklist tailored to this binding (commands + pass criteria).
- [ ] Update instructions if clarification results in changes (version/footer per doc rules).

## Proposed Closed Questions (answer with numbers/yes-no)
1. Should routine validation always run `mvn spotless:apply` followed by `mvn clean install` (no build.sh)?
2. Are warnings from generated model classes acceptable when those files are untouched, or must they be suppressed/eliminated?
3. Do logged errors from intentional negative tests (e.g., invalid URI) require additional handling or documentation to satisfy “zero warnings” policy?
4. Is any additional static analysis step required beyond the Maven build for this binding?
5. Should session reports explicitly list baseline warnings that are outside the touched files?

## Outputs for the session
- Final, unambiguous QA runbook for this repo.
- Updated instructions/docs if needed, with version/footer adjustments.
- Recorded decisions to avoid re-asking.
