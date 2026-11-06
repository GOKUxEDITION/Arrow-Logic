# Requirements Traceability Matrix

| ID | Requirement | Current implementation | Verification | Status |
|---|---|---|---|---|
| CORE-001 | Arrow head-cell geometry and backward occupancy | `domain/Arrow.kt` | `EscapeRulesTest` | Implemented; latest CI pending |
| CORE-002 | Four cardinal directions | `domain/Direction.kt` | Domain tests | Implemented; latest CI pending |
| CORE-003 | Canonical forward-ray escape rule | `domain/EscapeRules.kt` | `EscapeRulesTest` | Implemented; latest CI pending |
| CORE-004 | Bounds, axis length and overlap validation | `EscapeRules.validateBoard` | `EscapeRulesTest` | Implemented; latest CI pending |
| CORE-005 | Atomic legal move / blocked tap no-op | `domain/GameEngine.kt` | `GameEngineTest` | Implemented; latest CI pending |
| CORE-006 | Arbitrary-precision level IDs | `domain/LevelNumber.kt` | `LevelNumberTest` | Implemented; CI passed on earlier commit |
| CORE-007 | Independent solver and budget distinction | `domain/PuzzleSolver.kt` | `PuzzleSolverTest` | Implemented; CI passed on earlier commit |
| GEN-001 | Deterministic seed and PRNG | `LevelSeed.kt`, `DeterministicRandom.kt` | `DeterminismTest` | Implemented; CI passed on earlier commit |
| GEN-002 | Constructive solution witness | `ConstructivePuzzleGenerator.kt` | Generator/replay tests | Implemented; CI passed on earlier commit |
| GEN-003 | Independent solver and replay publication gate | Generator + solver + replay validator | Generator/replay tests | Implemented; CI passed on earlier commit |
| UI-001 | Interactive board, arrow taps and game state | `presentation/ArrowLogicScreen.kt`, `ArrowLogicViewModel.kt` | Android compile + UI tests | Implemented; latest CI pending |
| UI-002 | Required supporting screens | Not yet implemented | Compose navigation/UI tests | Pending |
| DATA-001 | Room persistence and migrations | Not yet implemented | Migration / recovery tests | Pending |
| DATA-002 | Process-death session recovery | Not yet implemented | Restore integration test | Pending |
| OFF-001 | Offline campaign and local generation | Domain/engine/generator run locally | Airplane-mode device test | Source supports offline; device test pending |
| A11Y-001 | 48dp arrow targets and TalkBack semantics | Arrow composables | Accessibility scanner / TalkBack QA | Partial; device QA pending |
| LOC-001 | Externalized strings and Hindi localization | Hard-coded UI strings remain | Resource audit/UI tests | Pending |
| GEN-004 | Difficulty quality and bounded duplicate history | Basic deterministic profile; no recent fingerprint DAO | Stress/eviction tests | Partial |
| QA-001 | Complete acceptance test suite | Domain tests and Android CI workflow | CI run + device reports | Partial |

**Evidence rule:** A source commit is not a successful build or test. The last confirmed CI pass is run #17; the current head-cell and UI commits require their own CI result. No APK/AAB, emulator or physical-device pass is claimed.
