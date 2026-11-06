# Arrow Logic

**Android logic puzzle game**  
Developer / Maintainer: Dipak Choudhary  
Repository: https://github.com/GOKUxEDITION/Arrow-Logic

Arrow Logic is an offline-first arrow escape puzzle. Tap an arrow only when its complete forward path to the board edge is clear. Removing one arrow can reveal another.

## Project status

This repository is being initialized from the Arrow Escape Puzzle PRD v2.0, with the product name updated to **Arrow Logic**.

- [x] Repository bootstrap and product identity
- [x] Android project skeleton
- [x] Integer-grid domain model and canonical escape rule
- [x] Atomic move engine
- [x] Independent backtracking solver
- [ ] Deterministic procedural generator and solution-witness validation
- [ ] Room persistence and session recovery
- [ ] Full Compose gameplay UI, accessibility and localization
- [ ] Full QA, device verification and release packaging

A checked item means source has been committed, not that a release build or physical-device test has been performed. No APK or release is claimed.

## Core rules

- Grid coordinates are integers; origin is top-left.
- Directions are UP, DOWN, LEFT and RIGHT.
- An arrow can escape only if every cell strictly ahead of its head through the board edge is empty.
- A blocked move changes no game state.
- A successful move removes exactly one arrow.
- Core gameplay is designed to work offline.

## Unlimited progression

Level identifiers use positive arbitrary-precision decimal strings at the domain boundary. There is no product-defined final level or fixed level catalogue. Generation is on demand; the app must never pre-generate an infinite catalogue. The generator must validate every candidate with an independent solver and replay the solution before publishing it.

## Build

Open the project in a recent Android Studio version with Android SDK 36 installed, allow Gradle sync, then run the `:app` configuration. The Gradle wrapper is not committed yet; use the Gradle version supported by the installed Android Studio or generate a wrapper from that environment.

## Engineering gates

See [docs/ENGINEERING_STATUS.md](docs/ENGINEERING_STATUS.md), [docs/PRODUCT_REQUIREMENTS.md](docs/PRODUCT_REQUIREMENTS.md), and [docs/REQUIREMENTS_TRACEABILITY_MATRIX.md](docs/REQUIREMENTS_TRACEABILITY_MATRIX.md).

## Project identity

- Product: **Arrow Logic**
- Developer / Maintainer: **Dipak Choudhary**
- Android application ID: `com.gokuxedition.arrowlogic`
