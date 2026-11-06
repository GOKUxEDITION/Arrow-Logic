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
