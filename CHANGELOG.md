# Changelog

All notable changes to Tierify will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2025-11-19

### Added
- **Verifier Mappings System**: New data-driven system to extend item attribute verifiers to modded items without modifying base attribute files
  - Add `data/<namespace>/verifier_mappings/*.json` files to map existing verifiers (like `c:swords`) to additional tags or items (like `c:halberds`)
  - Automatically includes mapped items when checking tier eligibility
  - See `VERIFIER_MAPPINGS.md` for full documentation
- **Tag Support in Reforge Items**: Both `items` and `base` fields now support tag references using `#` prefix
  - Use `"#c:swords"` to include all swords (vanilla + modded) in reforge recipes
  - Use `"#c:ingots/iron"` to accept any iron ingot as base material
  - Dramatically reduces file duplication - one entry can replace dozens of individual item entries
  - Tag expansion happens at data load time with logging for debugging
  - See `REFORGE_TAG_SUPPORT.md` for full documentation
- **TagParsingHelper Utility Class**: Consolidated tag parsing and validation logic for better code maintainability

### Changed
- Refactored `ReforgeDataLoader` to use centralized utility functions for tag parsing
- Refactored `ItemVerifier` to use centralized utility functions for tag creation
- Improved code organization and reduced duplication across data loaders

### Technical
- Created `elocindev.tierify.util.TagParsingHelper` with methods for tag detection, extraction, creation, expansion, and item validation
- Added `VerifierMapping` data class to represent verifier mappings
- Added `VerifierMappingLoader` to load and manage verifier mapping files
- Enhanced `ItemVerifier.isValid()` to check both direct matches and mapped verifiers
- Added `ReforgeDataLoader.ReforgeBaseData` inner class to hold both direct items and tag references

## [1.2.0] - Previous Release

### Added
- Added sounds for reforging which change depending on the rarity (Thanks Sweeney!)

### Changed
- Heavily toned down the negative effects of Common tier
- Modifiers from tiers are now base multipliers instead of total
- Removed negative effects from Rare tier
- Removed health effects from armor high tiers
- Limited speed effects from armor to 5% per piece

### Fixed
- Fixed tabs not being interchangeable when BCLib was installed