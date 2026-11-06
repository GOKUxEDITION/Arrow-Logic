# Arrow Logic

**Android logic puzzle game**  
Developer / Maintainer: Dipak Choudhary  
Repository: https://github.com/GOKUxEDITION/Arrow-Logic

Arrow Logic is an offline-first arrow escape puzzle. Tap an arrow only when its complete forward path to the board edge is clear. Removing one arrow can reveal another.

## Project status

- [x] Repository bootstrap and Android Kotlin + Compose project skeleton
- [x] Integer-grid arrow geometry using the PRD head-cell convention
- [x] Canonical forward-ray collision rule and atomic move engine
- [x] Independent backtracking solver with explicit node-budget outcome
- [x] Arbitrary-precision level identifiers
- [x] Versioned deterministic seed derivation and PRNG
- [x] Constructive on-demand generator with witness replay and independent solver gates
- [x] Interactive Compose gameplay board, tap handling, restart, pause and level progression
- [ ] Room persistence, DataStore preferences and process-death session recovery
- [ ] Home, tutorial, completion, level history, settings, privacy/about screens
- [x] English and Hindi string resources for gameplay copy and TalkBack descriptions
- [ ] Hints, optional undo and remaining accessibility polish
- [ ] Bounded recent fingerprint history, difficulty telemetry and stress/property tests
- [ ] Physical-device QA and release packaging

A checked item means source is committed. App text follows the Android device language where an English or Hindi resource exists. A CI pass applies only to the exact commit shown by its workflow run. No physical-device install, APK/AAB or release is claimed.

## Core rules

- Grid coordinates are integers; origin is top-left.
- Arrow coordinates identify the arrowhead cell; occupied cells extend backwards opposite the travel direction.
- Directions are UP, DOWN, LEFT and RIGHT.
- An arrow can escape only if every cell strictly ahead of its head through the board edge is empty.
- A blocked move changes no game state.
- A successful move removes exactly one arrow.
- Core gameplay and procedural progression run locally; no backend is required.

## Unlimited progression

Level identifiers are positive arbitrary-precision decimal strings, not Int or Long. There is no product-defined final level or fixed level catalogue. Generation is on demand; the app must never pre-generate future levels. No puzzle is playable until structural validation, constructive witness replay, independent solving and solver-solution replay pass.

## Build

The GitHub Actions workflow installs Android SDK 36 and runs `:app:testDebugUnitTest` using Gradle 8.13 and JDK 17. A complete Gradle wrapper JAR and launch scripts are not yet committed; CI currently uses the Gradle setup action. Android Studio can open the project after Gradle sync with Android SDK 36 installed.

## Engineering gates

See [docs/ENGINEERING_STATUS.md](docs/ENGINEERING_STATUS.md), [docs/PRODUCT_REQUIREMENTS.md](docs/PRODUCT_REQUIREMENTS.md), [docs/REQUIREMENTS_TRACEABILITY_MATRIX.md](docs/REQUIREMENTS_TRACEABILITY_MATRIX.md), and [docs/QA_TEST_PLAN.md](docs/QA_TEST_PLAN.md).

## Project identity

- Product: **Arrow Logic**
- Developer / Maintainer: **Dipak Choudhary**
- Android application ID: `com.gokuxedition.arrowlogic`
