# Reforge Item Tag Support

Starting with this version, Tierify supports using **tags** in both the `base` and `items` fields of reforge item definitions. This allows for more flexible and cross-mod compatible reforge specifications with significantly reduced duplication.

## How It Works

In your `data/<namespace>/reforge_items/*.json` files, you can now use tags in both fields by prefixing them with `#`:

```json
{
  "items": [
    "#c:swords"
  ],
  "base": [
    "#c:ingots/iron"
  ]
}
```

When using tags in the `items` field, the system automatically expands the tag to include all items in that tag, creating reforge entries for each one.

## Syntax

### Base Field - Using Tags for Materials

Prefix tag identifiers with `#` to match any material in that tag:
```json
"base": ["#c:ingots/iron"]
```

### Base Field - Using Direct Item IDs

No prefix needed (backward compatible):
```json
"base": ["minecraft:iron_ingot"]
```

### Base Field - Mixing Both

You can combine tags and direct item IDs:
```json
{
  "items": ["minecraft:diamond_sword"],
  "base": [
    "#c:gems/diamond",
    "minecraft:diamond"
  ]
}
```

This accepts either any item in the `c:gems/diamond` tag **OR** the direct `minecraft:diamond` item.

### Items Field - Using Tags for Tools/Items

Prefix tag identifiers with `#` to automatically create reforge entries for all items in that tag:
```json
"items": ["#c:swords"]
```

This automatically expands to all items tagged with `c:swords` (vanilla swords, modded swords, etc.).

### Items Field - Using Direct Item IDs

No prefix needed (backward compatible):
```json
"items": ["minecraft:iron_sword"]
```

### Items Field - Mixing Both

You can combine tags and direct item IDs:
```json
{
  "items": [
    "#c:swords",
    "minecraft:trident",
    "custom_mod:special_weapon"
  ],
  "base": ["#c:ingots/iron"]
}
```

This creates reforge entries for all swords (via tag) PLUS the trident and special weapon (direct IDs).

## Examples

### Example 1: All Swords with Iron (Tag Expansion)

```json
{
  "items": [
    "#c:swords"
  ],
  "base": [
    "#c:ingots/iron"
  ]
}
```

**Result:** Creates reforge entries for **every** sword (vanilla + modded) that uses iron ingots as the base material. This single entry replaces dozens of individual entries.

**Items expanded:** `minecraft:wooden_sword`, `minecraft:stone_sword`, `minecraft:iron_sword`, `minecraft:golden_sword`, `minecraft:diamond_sword`, `minecraft:netherite_sword`, plus any modded swords tagged with `c:swords`.

### Example 2: Iron Tools Using Base Tag (Backward Compatible)

```json
{
  "items": [
    "minecraft:iron_sword",
    "minecraft:iron_axe",
    "minecraft:iron_pickaxe"
  ],
  "base": [
    "#c:ingots/iron"
  ]
}
```

**Result:** Any item tagged with `c:ingots/iron` can be used to reforge these specific tools. This includes:
- `minecraft:iron_ingot`
- Any modded iron ingots that are properly tagged

### Example 3: Gold Items Using Convention Tags

```json
{
  "items": [
    "minecraft:golden_sword",
    "minecraft:golden_pickaxe",
    "minecraft:golden_axe"
  ],
  "base": [
    "#c:ingots/gold"
  ]
}
```

### Example 4: Mixed Tags and Direct Items

```json
{
  "items": [
    "#c:swords",
    "#c:axes",
    "minecraft:trident"
  ],
  "base": [
    "#c:ingots/netherite",
    "minecraft:netherite_scrap"
  ]
}
```

**Result:** All swords and axes (via tags) PLUS the trident can be reforged using netherite ingots (via tag) OR netherite scraps (direct ID).

### Example 5: Multiple Tag Types for Base Materials

```json
{
  "items": ["#minecraft:wooden_items"],
  "base": [
    "#minecraft:planks",
    "#minecraft:logs"
  ]
}
```

**Result:** All wooden items can be reforged using any planks OR logs.

## Common Tag Conventions

The [Common Convention Tags](https://github.com/FabricMC/fabric/blob/1.20.1/fabric-convention-tags-v1/src/main/resources/data/c/tags/items/) provide standardized tags:

### Material Tags
- `#c:ingots/iron` - Iron ingots
- `#c:ingots/gold` - Gold ingots
- `#c:ingots/copper` - Copper ingots
- `#c:gems/diamond` - Diamonds
- `#c:gems/emerald` - Emeralds
- `#c:dusts/redstone` - Redstone dust

### Block Type Tags
- `#minecraft:planks` - All wooden planks
- `#minecraft:logs` - All logs
- `#minecraft:stone_tool_materials` - Cobblestone, blackstone, etc.

## Benefits

### 1. Dramatic Reduction in File Duplication

Using tags in the `items` field eliminates the need for separate entries for each item:

**Before (dozens of entries needed):**
```json
// File 1: wooden_sword.json
{"items": ["minecraft:wooden_sword"], "base": ["#minecraft:planks"]}

// File 2: stone_sword.json
{"items": ["minecraft:stone_sword"], "base": ["#minecraft:stone_tool_materials"]}

// File 3: iron_sword.json
{"items": ["minecraft:iron_sword"], "base": ["#c:ingots/iron"]}

// ... dozens more files for each sword type ...
```

**After (single entry):**
```json
// File: all_swords.json
{"items": ["#c:swords"], "base": ["#c:ingots/iron"]}
```

### 2. Automatic Modded Item Support

When using tags in the `items` field, newly added modded items are automatically included:

```json
{
  "items": ["#c:swords"],
  "base": ["#c:ingots/steel"]
}
```

This automatically includes:
- All vanilla swords
- Spartan Weaponry swords
- Simply Swords content
- Any future mod's swords (if properly tagged)

**No file updates needed** when adding new mods!

### 3. Cross-Mod Material Compatibility

Works with any mod that adds materials using conventional tags:

```json
{
  "items": ["#c:tools"],
  "base": ["#c:ingots/steel"]
}
```

This works with:
- Create's steel ingots
- Immersive Engineering steel
- Tech Reborn steel
- Any other mod's steel (if properly tagged)

### 4. Simplified Maintenance

Instead of maintaining hundreds of individual entries, you can manage a handful of tag-based rules:

```json
// One entry covers all swords from all mods
{"items": ["#c:swords"], "base": ["#c:ingots/iron"]}

// One entry covers all axes from all mods  
{"items": ["#c:axes"], "base": ["#c:ingots/iron"]}

// One entry covers all pickaxes from all mods
{"items": ["#c:pickaxes"], "base": ["#c:ingots/iron"]}
```

### 5. Future-Proof

New mods that add items will automatically work if they use standard tags. No datapack updates required.

## Backward Compatibility

✅ **Fully backward compatible** - existing reforge files without `#` continue to work exactly as before.

Old format still works:
```json
{
  "items": ["minecraft:iron_sword"],
  "base": ["minecraft:iron_ingot"]
}
```

## Tag Discovery

To find available tags in your modpack:

1. **In-game**: Use commands like `/tag list items` (requires permissions)
2. **Datapacks**: Look in `data/c/tags/items/` folders
3. **Mod sources**: Check mod GitHub repositories for tag definitions
4. **Convention tags**: See [Fabric Convention Tags](https://github.com/FabricMC/fabric/tree/1.20.1/fabric-convention-tags-v1/src/main/resources/data/c/tags/items)

## Technical Details

### Tag Expansion in Items Field

When tags are used in the `items` field, the system performs **expansion at data load time**:

1. **Parse Phase**: Reads `"#c:swords"` from JSON
2. **Expansion Phase**: Iterates through all registered items
3. **Matching Phase**: Checks if each item has the tag using `isIn(tag)`
4. **Registration Phase**: Creates individual reforge entries for each matched item

Example log output:
```
[Tierify] Expanded tag c:swords to 12 items for reforge
```

This means the single `"#c:swords"` entry became 12 individual reforge entries (one per sword).

**Important:** Tag expansion happens during world load/resource reload, not during gameplay. This means:
- ✅ No performance impact during reforging
- ✅ Changes require `/reload` command to take effect
- ⚠️ Items added by mods after load won't be included until next reload

### Tag Resolution in Base Field

Tags in the `base` field work differently - they're resolved **at runtime** when checking reforge validity:

- Tags are evaluated when items are placed in the reforge interface
- If a tag doesn't exist, it's treated as empty (no items match)
- No errors or crashes occur with non-existent tags

### Performance

#### Items Field Tags
- Expansion happens once during resource load
- Zero runtime overhead - works exactly like direct item IDs afterward
- May increase initial load time slightly with very large tag sets

#### Base Field Tags  
- Tag checks are cached by Minecraft's tag system
- Minimal performance impact compared to direct item checks
- Only evaluated when items are placed in the reforge interface

### Priority
When multiple base options exist, ANY match is valid:
```json
"base": ["#c:ingots/iron", "minecraft:copper_ingot"]
```
Either iron ingots (any) OR copper ingot will work.

## Migration Guide

### Migrating Existing Files

**Old format (many individual files):**
```json
// File 1: wooden_sword.json
{"items": ["minecraft:wooden_sword"], "base": ["minecraft:stick"]}

// File 2: stone_sword.json
{"items": ["minecraft:stone_sword"], "base": ["minecraft:cobblestone"]}

// File 3: iron_sword.json
{"items": ["minecraft:iron_sword"], "base": ["minecraft:iron_ingot"]}

// ... dozens more ...
```

**New format (single file with tags):**
```json
// File: all_swords.json
{
  "items": ["#c:swords"],
  "base": ["#c:ingots/iron"]
}
```

### When to Use Tags vs Direct Items

#### Use Tags in `items` Field When:
- ✅ Want to apply reforge to all items of a type (all swords, all pickaxes)
- ✅ Want automatic support for modded items
- ✅ Items are grouped in standard tags (c:swords, c:axes, etc.)
- ✅ Want to reduce file count dramatically

#### Use Direct Items in `items` Field When:
- ✅ Need specific control over individual items
- ✅ Item requires unique base materials
- ✅ Creating exceptions to tag-based rules

#### Use Tags in `base` Field When:
- ✅ Material has cross-mod equivalents (iron, gold, copper)
- ✅ Want to support modded variants automatically
- ✅ Material is part of convention tags

#### Use Direct Items in `base` Field When:
- ✅ Item is unique to one mod
- ✅ No standard tag exists
- ✅ Need precise control over accepted materials

### Recommended Strategy

**Start with broad tag-based rules:**
```json
// Cover 90% of items with a few files
{"items": ["#c:swords"], "base": ["#c:ingots/iron"]}
{"items": ["#c:axes"], "base": ["#c:ingots/iron"]}
{"items": ["#c:pickaxes"], "base": ["#c:ingots/iron"]}
```

**Add specific exceptions as needed:**
```json
// Special case for unique items
{
  "items": ["minecraft:trident"],
  "base": ["minecraft:prismarine_shard", "minecraft:prismarine_crystals"]
}
```

## Troubleshooting

### Tag not working in-game?

**Check:**
1. Is the tag name correct? (Check for typos)
2. Does the tag exist? (Use `/tag list items`)
3. Are items properly tagged? (Mods must register their items to tags)
4. Is the `#` prefix included?

### Items not being accepted?

**Verify:**
1. Tag syntax: `"#c:ingots/iron"` (with quotes and #)
2. Tag namespace: Include namespace (e.g., `c:` or `minecraft:`)
3. Tag actually contains items: Some tags may be empty without certain mods

## Examples in This Mod

See the following example files:
- `data/tiered/reforge_items/all_swords.json` - Uses `#c:swords` in items field for all swords
- `data/tiered/reforge_items/all_tools.json` - Uses multiple tags in items field (`#c:pickaxes`, `#c:axes`, `#c:hoes`)
- `data/tiered/reforge_items/iron_tools.json` - Uses `#c:ingots/iron` in base field
- `data/tiered/reforge_items/diamond_tools.json` - Mixes tags and direct items
- `data/tiered/reforge_items/elytra.json` - Direct item (no tag)

## Best Practices

1. **Prefer tags in items field** for item categories - drastically reduces file count
   - `{"items": ["#c:swords"]}` instead of listing every sword
   
2. **Use convention tags** (`c:`) when available for maximum compatibility
   - `#c:swords`, `#c:axes`, `#c:pickaxes` for tools
   - `#c:ingots/iron`, `#c:gems/diamond` for materials

3. **Mix formats strategically**
   - Tags for broad categories: `"items": ["#c:swords"]`
   - Direct items for exceptions: `"items": ["minecraft:trident"]`
   - Both together when needed: `"items": ["#c:swords", "minecraft:trident"]`

4. **Start broad, add specifics**
   - Create general tag-based rules first
   - Add specific entries for unique items with special requirements

5. **Document custom tags** if creating new ones for your modpack

6. **Test thoroughly** with your modpack's installed mods
   - Verify tag expansion works (check logs for "Expanded tag X to Y items")
   - Test reforge interface with modded items

## Related Documentation

- [Verifier Mappings](VERIFIER_MAPPINGS.md) - For extending which items can receive tiers
- [README.md](README.md) - General customization documentation
