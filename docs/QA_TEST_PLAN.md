# QA Test Plan

## Domain tests
- Arrow occupancy and head coordinates for all four directions.
- Board bounds, duplicate IDs and overlaps.
- Clear and blocked forward-ray checks.
- Blocked tap leaves state unchanged.
- Legal tap removes exactly one arrow.
- Solver distinguishes UNSOLVABLE from BUDGET_EXCEEDED.
- Level IDs parse, compare and increment beyond signed 64-bit range.

## Generator acceptance (pending)
- Same version/seed/profile yields identical puzzle fingerprint.
- No out-of-bounds or overlapping arrows.
- Every emitted puzzle has a solution witness.
- Replay through production move rules reaches an empty board.
- Sequential progression beyond 50, 100, 1,000 and very large decimal IDs.
- No hard-coded campaign maximum or future-level catalogue.

## Android QA (pending)
- Process death and restart restore exact in-progress state.
- Offline play, pause/resume, font scaling, TalkBack and reduced motion.
- Compose UI tests and physical-device smoke test.
- Record exact commands, environment and pass/fail counts. Never infer a pass.
