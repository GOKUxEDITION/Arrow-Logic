# Unlimited Level Generation and Solver Contract

## Product rule
The campaign has no configured final level. A level is generated only when requested. Never pre-generate or store an infinite catalogue. Level identifiers are positive arbitrary-precision integers represented as canonical decimal strings in persistence and APIs.

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
2. Derive a stable seed and a difficulty profile.
3. Construct a candidate board and a hidden constructive removal witness.
4. Validate dimensions, arrow IDs, bounds, positive lengths and non-overlap.
5. Run an independent solver with explicit node/time budgets.
6. Replay the solver witness using the production GameEngine.
7. Publish only if replay reaches an empty board.
8. Reject duplicates against a bounded recent fingerprint history; retry deterministically.
9. Persist the level definition and metadata only when played or otherwise needed.

## Solver result semantics
- SOLVED: returns an ordered sequence of every arrow ID.
- UNSOLVABLE: search completed within budget and found no solution.
- BUDGET_EXCEEDED: solver stopped before proving solvability or unsolvability.

Never report a budget timeout as an unsolvable puzzle.

## Current code status
Implemented: arbitrary-precision LevelNumber, deterministic seed derivation, SplitMix64 PRNG, canonical collision engine, backtracking solver and solution replay validator.

Pending: constructive generator, difficulty model, duplicate fingerprint history, stress/performance budgets, and generator-to-solver integration tests.
