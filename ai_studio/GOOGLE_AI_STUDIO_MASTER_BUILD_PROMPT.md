# Google AI Studio — Arrow Logic Build Prompt

You are implementing **Arrow Logic**, a native Android logic puzzle game. Treat `docs/PRODUCT_REQUIREMENTS.md` and `specs/UNLIMITED_LEVEL_GENERATION_AND_SOLVER.md` as the requirements source of truth.

## Hard requirements
- Kotlin + Jetpack Compose + Material 3.
- Offline-first; no login or network requirement for core gameplay.
- MVVM and clear domain/data/presentation package boundaries.
- One canonical integer-grid forward-ray collision rule.
- Blocked moves must not mutate game state.
- A legal move removes exactly one arrow atomically.
- Exactly unlimited on-demand campaign progression. No MAX_LEVEL, no fixed level array, no final campaign screen.
- Use arbitrary-precision positive level numbers; persist as canonical decimal TEXT strings, not Int or Long.
- Deterministic, versioned generation. Every candidate must pass structural validation, independent solver validation and full solution replay before becoming playable.
- Distinguish UNSOLVABLE from BUDGET_EXCEEDED.
- Preserve progress and in-progress sessions across process death.
- Never claim a build, test, install or release succeeded without actual evidence.

## Work protocol
1. Inspect the complete repository and report the actual workspace inventory.
2. Map every requirement to source files and tests.
3. Implement one gated phase at a time; do not skip failed gates.
4. Keep source compiling at each stable checkpoint.
5. Do not overwrite unrelated files or introduce unapproved scope.
6. Report exact commands, environment, logs and pass/fail counts.

## Required order
A. Validate build baseline and app identity.
B. Complete domain geometry and move-state tests.
C. Complete independent solver and replay validation.
D. Implement constructive procedural generation and deterministic retry.
E. Add Room persistence, DataStore and session recovery.
F. Build full Compose gameplay and supporting screens.
G. Add accessibility, localization, performance and stress tests.
H. Run Android CI and device QA.
I. Prepare release artifacts only after all required gates pass.

Start by auditing the repository. Do not jump directly to UI polish while generator and solver correctness remain incomplete.
