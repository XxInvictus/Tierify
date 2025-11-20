# Changelog

All notable changes to Tierify will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0/1] - 2025-11-20

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
- **Improved logging output**: Reduced log verbosity by moving granular details to debug level
  - Per-entry success messages now use `LOGGER.debug()` instead of `LOGGER.info()`
  - Added summary statistics at info level (e.g., "Loaded 5 reforge definitions with 12 tags")
  - Cleaner logs during normal operation while maintaining detailed debugging capability
- Refactored `ReforgeDataLoader` to use centralized utility functions for tag parsing
- Refactored `ItemVerifier` to use centralized utility functions for tag creation
- Improved code organization and reduced duplication across data loaders

### Fixed
- **Fixed modded tag support**: Tags in `items` field now use lazy evaluation instead of load-time expansion
  - Tags are stored as references and expanded at runtime when checking reforge eligibility
  - This ensures modded tags (like `spartanweaponry:clubs`) work immediately without requiring `/reload`
  - The `minecraft:tags` dependency ensures vanilla tags are available, but modded tags are checked dynamically
  - Legacy system still supported for backward compatibility
- **Improved tag loading reliability**: Reforge items and verifier mappings declare dependency on `minecraft:tags`
  - Changed `ReforgeDataLoader` from `SimpleSynchronousResourceReloadListener` to `IdentifiableResourceReloadListener`
  - Changed `VerifierMappingLoader` to implement `IdentifiableResourceReloadListener`
  - Ensures vanilla tags are loaded before datapack processing begins
- Fixed `ResourceLocationException` error when parsing tag references with `#` prefix
- Fixed `NullPointerException` when JSON files are missing required fields:
  - Added validation for required `base` and `items` fields in reforge_items JSON files
  - Added validation for required `base_verifier`, `base_verifier_type`, and `mapped_verifiers` fields in verifier mapping files
  - Added validation for `verifier` and `type` fields in mapped_verifiers array entries
- Added proper error handling to prevent single malformed entries from breaking entire file loading
- Skip registration of reforge data when no valid items or base materials are found
- Skip registration of verifier mappings when no valid mapped verifiers exist

### Technical
- **Lazy Tag Loading System**:
  - Added `ReforgeDataLoader.ReforgeDefinition` class to store both direct items and tag references
  - Tags in `items` field are now stored as `TagKey<Item>` references rather than expanded at load time
  - Added `ReforgeDefinition.matchesItem()` method for runtime tag expansion
  - Added `canReforge()` and `getReforgeDefinitionFor()` methods to check item eligibility
  - Updated `ReforgeScreenHandler` to use lazy tag evaluation for runtime checks
  - Legacy expansion system maintained for backward compatibility with existing code
- **Utility Classes**:
  - Created `elocindev.tierify.util.TagParsingHelper` with methods for tag detection, extraction, creation, expansion, and item validation
  - Added `VerifierMapping` data class to represent verifier mappings
  - Added `VerifierMappingLoader` to load and manage verifier mapping files
- **Code Improvements**:
  - Enhanced `ItemVerifier.isValid()` to check both direct matches and mapped verifiers
  - Added `ReforgeDataLoader.ReforgeBaseData` inner class to hold both direct items and tag references for base materials
  - Added validation in `TagParsingHelper` methods to prevent `#` prefix from being passed to `Identifier` constructor
  - Wrapped individual entry parsing in try-catch blocks for graceful error recovery
  - Added JSON field validation before processing reforge_items data

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