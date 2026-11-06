# Requirements Traceability Matrix

| ID | Requirement | Current implementation | Verification | Status |
|---|---|---|---|---|
| CORE-001 | Integer-grid arrow geometry | `domain/Arrow.kt` | `EscapeRulesTest` | Implemented; CI pending |
| CORE-002 | Four cardinal directions | `domain/Direction.kt` | Domain tests | Implemented; CI pending |
| CORE-003 | Canonical forward-ray escape rule | `domain/EscapeRules.kt` | `EscapeRulesTest` | Implemented; CI pending |
| CORE-004 | Bounds and overlap validation | `EscapeRules.validateBoard` | `EscapeRulesTest` | Implemented; CI pending |
| CORE-005 | Atomic legal move / blocked tap no-op | `domain/GameEngine.kt` | `GameEngineTest` | Implemented; CI pending |
| CORE-006 | Arbitrary-precision level IDs | `domain/LevelNumber.kt` | `LevelNumberTest` | Implemented; CI pending |
| CORE-007 | Independent solver and budget distinction | `domain/PuzzleSolver.kt` | `PuzzleSolverTest` | Implemented; CI pending |
| GEN-001 | Deterministic seed and PRNG | `LevelSeed.kt`, `DeterministicRandom.kt` | `DeterminismTest` | Implemented; CI pending |
| GEN-002 | Constructive solution witness | `ConstructivePuzzleGenerator.kt` | Generator/replay tests | Implemented; CI pending |
| GEN-003 | Independent solver and replay publication gate | Generator + solver + replay validator | Generator/replay tests | Implemented; CI pending |
| DATA-001 | Room persistence and migrations | Not yet implemented | Migration / recovery tests | Pending |
| UI-001 | Full gameplay board and supporting screens | Starter Compose activity only | Compose UI tests | Pending |
| QA-001 | Full automated acceptance suite | Domain tests and CI workflow | Successful CI run + reports | Pending |

**Evidence rule:** A source commit is not a successful build or test. No Android build, test, emulator, device install, APK, AAB or release pass is claimed until captured evidence exists.
