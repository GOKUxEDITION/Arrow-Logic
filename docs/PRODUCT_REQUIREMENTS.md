# Arrow Logic — Product Requirements (PRD-derived baseline)

**Developer / Maintainer:** Dipak Choudhary  
**Product name:** Arrow Logic  
**Platform:** Android native, Kotlin + Jetpack Compose + Material 3  
**Baseline:** offline-first single-player puzzle game  
**Progression:** unlimited on-demand procedural levels

## Product definition

The player removes directional arrows by tapping an arrow whose complete forward ray to the board edge is empty. Removing an arrow may expose another arrow. Arrows do not rotate, slide or wrap. The board is a finite rectangular integer grid.

## Non-negotiable rules

1. Directions are UP, DOWN, LEFT and RIGHT.
2. Collision and legality are evaluated in integer grid coordinates.
3. One canonical domain rule must be shared by gameplay, hints, solver, generator validation and tests.
4. A blocked tap changes no state.
5. A legal tap removes exactly one arrow atomically.
6. Every generated puzzle must pass structural validation, independent solving and solution replay before becoming playable.
7. Core gameplay and progression must work offline.
8. Do not pre-generate all future levels.
9. There is no product-defined maximum level and no final campaign level.
10. Persist arbitrary-precision positive level numbers as canonical decimal strings (SQLite TEXT / API string), not 32-bit or 64-bit numeric columns.

## Required screens

- Splash / loading
- Home / continue
- Gameplay board
- Pause / resume
- Level complete
- Level history / jump
- Settings
- Tutorial / how to play
- Generation / validation progress
- Recovery / generation error
- Privacy / about

## Architecture target

Single Android application module initially, with package boundaries for domain, data and presentation. Kotlin, Compose, MVVM, Room, DataStore, Coroutines and Flow. Use manual dependency injection unless a later approved decision changes this.

## Accessibility

Minimum 48dp touch targets; TalkBack labels for arrows, direction and legal/blocked state; never encode direction by color alone; support font scaling, contrast, reduced motion, keyboard/switch access where practical.

## Delivery rule

A feature is complete only when implementation, tests and evidence exist. A source commit is not proof of a successful Android build, emulator run, physical-device install or release. Never report unexecuted checks as passing.
