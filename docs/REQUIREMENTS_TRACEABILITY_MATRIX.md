# Requirements Traceability Matrix

| ID | Requirement | Current implementation | Verification | Status |
|---|---|---|---|---|
| CORE-001 | Integer-grid arrow geometry | `domain/Arrow.kt` | `EscapeRulesTest` | Implemented; execution pending |
| CORE-002 | Four cardinal directions | `domain/Direction.kt` | Domain test suite | Implemented; execution pending |
| CORE-003 | Canonical forward-ray rule | `domain/EscapeRules.kt` | `EscapeRulesTest` | Implemented; execution pending |
| CORE-004 | Board bounds and overlap validation | `EscapeRules.validateBoard` | `EscapeRulesTest` | Implemented; execution pending |
| CORE-005 | Atomic move / no mutation on blocked tap | `domain/GameEngine.kt` | `GameEngineTest` | Implemented; execution pending |
| CORE-006 | Arbitrary-precision level identifiers | `domain/LevelNumber.kt` | `LevelNumberTest` | Implemented; execution pending |
| CORE-007 | Independent solver and budget distinction | `domain/PuzzleSolver.kt` | `PuzzleSolverTest` | Implemented; execution pending |
| GEN-001 | Versioned deterministic seed and PRNG | `LevelSeed.kt`, `DeterministicRandom.kt` | `DeterminismTest` | Implemented; execution pending |
| GEN-002 | Full solution replay validator | `SolutionReplayValidator.kt` | `SolutionReplayValidatorTest` | Implemented; execution pending |
| GEN-003 | Constructive procedural generator | Not yet implemented | Determinism + stress tests | Pending |
| DATA-001 | Room persistence and migration | Not yet implemented | Migration / recovery tests | Pending |
| UI-001 | Full gameplay board and interactions | Not yet implemented | Compose UI tests | Pending |
| QA-001 | Full automated acceptance suite | Domain tests authored; CI workflow added | CI run + test report | Pending |

**Evidence rule:** "Implemented" means source code and/or tests are committed. It does not mean tests have passed. No Android build, test, emulator, device install, APK or release pass is claimed until actual evidence is recorded.
