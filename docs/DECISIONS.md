# Architecture Decision Records

## ADR-001: Single Android app module
**Decision:** Start with one app module and keep domain, data and presentation in separate packages.  
**Reason:** A simple Android Studio workflow without abandoning separation of concerns.  
**Status:** Accepted.

## ADR-002: Arbitrary-precision level identifiers
**Decision:** Use positive canonical decimal strings at persistence/API boundaries and BigInteger in domain logic.  
**Reason:** A signed 64-bit integer has a finite maximum and conflicts with literal unlimited progression.  
**Status:** Accepted.

## ADR-003: One canonical collision rule
**Decision:** Gameplay and solver use the same integer-grid forward-ray legality function.  
**Reason:** Prevent rule drift between user moves and validation.  
**Status:** Accepted.

## ADR-004: Generator publication gate
**Decision:** No generated puzzle is playable until structural validation, independent solving and solution replay pass.  
**Status:** Accepted; implementation pending.


## ADR-005: Arrow head-cell coordinate convention
**Decision:** `Arrow.head` identifies the cell nearest the exit. Occupied cells extend backwards opposite the direction vector.  
**Reason:** Matches the canonical data contract in PRD v2.0 and avoids generator/renderer ambiguity.  
**Status:** Accepted; implementation committed.

## ADR-006: Dark neon visual direction
**Decision:** Use deep navy/purple surfaces, high-contrast arrow tiles, restrained neon accents and warm gold completion emphasis.  
**Reason:** Matches the PRD's UX direction; effects must never obscure legality.  
**Status:** Accepted; initial gameplay screen aligned.

## ADR-007: Arbitrary-precision campaign identifiers
**Decision:** Keep level IDs as canonical decimal strings at persistence/API boundaries and BigInteger in domain logic, even though one appendix mentions 64-bit support.  
**Reason:** The core v2.0 product rule explicitly states positive integers with no maximum; 64-bit has a finite representation ceiling.  
**Status:** Accepted.
