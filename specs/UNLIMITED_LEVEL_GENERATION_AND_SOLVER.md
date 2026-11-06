# Unlimited Level Generation and Solver Contract

## Product rule
The campaign has no configured final level. A level is generated only when requested. Never pre-generate or store an infinite catalogue. Level identifiers are positive arbitrary-precision integers represented as canonical decimal strings in persistence and APIs.

## Canonical arrow geometry
- `Arrow.head` identifies the cell nearest the exit.
- Occupied cells are `head - directionVector * offset` for offsets `0 until length`.
- The escape ray begins one cell forward from the head and continues until leaving the board.
- X increases rightward; Y increases downward.
- UP=(0,-1), DOWN=(0,1), LEFT=(-1,0), RIGHT=(1,0).
- Geometry, generator, solver, renderer and tests must share this convention.

## Stable identity
A puzzle identity is the tuple:
- generator algorithm version
- canonical decimal level number
- root seed identifier
- difficulty profile version
- content fingerprint

The seed derivation is SHA-256 over a versioned, delimiter-safe canonical input. The first 64 digest bits seed the versioned SplitMix64 PRNG. Never silently change a generator algorithm; increment its version.

## Required generator pipeline
1. Parse the arbitrary-precision level number.
2. Derive a stable seed and a bounded per-level difficulty profile.
3. Construct a candidate board and a hidden constructive removal witness.
4. Validate dimensions, arrow IDs, axis lengths, bounds and non-overlap.
5. Run an independent solver with explicit node/time budgets.
6. Replay the solver solution using the production GameEngine.
7. Publish only if both the constructive witness and solver solution replay to an empty board.
8. Reject duplicates against a bounded recent fingerprint history; retry deterministically.
9. Persist the level definition and metadata only when played or otherwise needed.

## Solver result semantics
- SOLVED: returns an ordered sequence of every arrow ID.
- UNSOLVABLE: search completed within budget and found no solution.
- BUDGET_EXCEEDED: solver stopped before proving solvability or unsolvability.

Never report a budget timeout as an unsolvable puzzle.

## Current code status
Implemented: arbitrary-precision LevelNumber, deterministic seed derivation, SplitMix64 PRNG, head-cell arrow geometry, canonical collision engine, backtracking solver, constructive generator, solution replay validation and an interactive gameplay screen.

Pending: Room/DataStore persistence, bounded recent fingerprint store, complete multi-dimensional difficulty metrics, remaining screens, localization, hints/undo, stress/property tests and physical-device QA.
