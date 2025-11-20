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

**As of v1.3.0+**, tags in the `items` field use **lazy evaluation** for maximum compatibility:

1. **Parse Phase**: Reads `"#c:swords"` from JSON and stores as a tag reference
2. **Runtime Evaluation**: When checking if an item can be reforged, the tag is evaluated on-demand
3. **Matching Phase**: Checks if the item has the tag using `isIn(tag)` at runtime
4. **Dynamic Support**: Works with modded tags that may not be loaded yet during datapack loading

**Why this matters:**
- ✅ **Modded tags work immediately** - No need for `/reload` after joining a world
- ✅ **Load order independent** - Doesn't matter when modded tags are registered
- ✅ **No "expanded to 0 items" issues** - Tags are checked when actually needed
- ✅ **Backward compatible** - Legacy expansion system still runs for compatibility

You may still see log messages like:
```
[Tierify] Loaded tag-based reforge items: spartanweaponry:clubs
[Tierify] Tag spartanweaponry:clubs expanded to 0 items for legacy system in tiered:reforge_items/wooden_club.json. Tags will still work at runtime.
```

This is **normal** - the warning only applies to the legacy backward compatibility system. The new lazy evaluation system will work correctly at runtime.

### Tag Resolution in Base Field

Tags in the `base` field work differently - they're resolved **at runtime** when checking reforge validity:

- Tags are evaluated when items are placed in the reforge interface
- If a tag doesn't exist, it's treated as empty (no items match)
- No errors or crashes occur with non-existent tags

### Performance

#### Items Field Tags (v1.3.0+ Lazy Evaluation)
- Tags are stored as references, minimal memory overhead
- Tag checks happen when items are placed in reforge interface
- Minecraft's tag system provides efficient caching
- Negligible performance impact compared to direct item checks

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

### Error: "Non [a-z0-9_.-] character in namespace"?

This error means the `#` prefix wasn't properly handled. **Common causes:**

1. **Mixing tag and item syntax**: Ensure entries with `#` are treated as tags
   ```json
   // ✅ CORRECT
   {"items": ["#spartanweaponry:longbows"], "base": ["#c:ingots/iron"]}
   
   // ❌ WRONG - missing # prefix
   {"items": ["spartanweaponry:longbows"], "base": ["c:ingots/iron"]}
   ```

2. **JSON parsing error**: Check for proper quotes and commas
   ```json
   // ✅ CORRECT
   "items": ["#c:swords", "#c:axes"]
   
   // ❌ WRONG - missing comma
   "items": ["#c:swords" "#c:axes"]
   ```

3. **Invalid tag format**: Tags must use valid namespace:path format
   ```json
   // ✅ CORRECT
   "#spartanweaponry:longbows"
   "#c:swords"
   
   // ❌ WRONG - invalid characters or format
   "##c:swords"
   "#c::swords"
   ```

**Solution**: Check your JSON file for syntax errors and ensure all tag references start with exactly one `#` character.

### Tag expanded to 0 items warning?

**As of v1.3.0+**: This warning is **informational only** and doesn't affect functionality!

The warning appears because the legacy backward compatibility system tries to expand tags at load time. However, the **new lazy evaluation system** will work correctly at runtime regardless of this warning.

**What the warning looks like:**
```
[Tierify] Tag spartanweaponry:clubs expanded to 0 items for legacy system in tiered:reforge_items/wooden_club.json. Tags will still work at runtime.
```

**Why it appears:**
- Modded tags may not be loaded when the legacy expansion runs
- The new system stores tags as references and checks them when actually needed
- The warning only applies to the backward compatibility layer

**What to do:**
1. **Ignore the warning** - Your tags will work correctly at runtime
2. **Test in-game** - Place the modded item in the reforge interface to verify
3. **Only investigate if reforge doesn't work in-game:**
   - Verify the tag exists: `/tag list items <modid>`
   - Check the mod actually creates that tag (look in the mod's data files)
   - Try using direct item IDs if the tag genuinely doesn't exist

**If reforge still doesn't work after testing:**

1. **Verify the tag exists** (Most common issue)
   - Use `/tag list items` to see all available tags
   - Search for your tag: `/tag list items spartan` (partial match)
   - If the tag doesn't exist, the mod may use different tag names

2. **Check mod tags in data files**
   - Check the mod's JAR file or source code
   - Look in `data/<modid>/tags/items/` folders
   - Some mods don't create tags for all their item categories

3. **Use direct item IDs instead** (If tag doesn't exist)
   ```json
   {
     "items": [
       "spartanweaponry:dagger_iron",
       "spartanweaponry:dagger_gold",
       "spartanweaponry:dagger_diamond"
     ],
     "base": ["#c:ingots/iron"]
   }
   ```

### Items not being accepted?

**Verify:**
1. Tag syntax: `"#c:ingots/iron"` (with quotes and #)
2. Tag namespace: Include namespace (e.g., `c:` or `minecraft:`)
3. Tag actually contains items: Some tags may be empty without certain mods
4. Check logs for "Expanded tag X to Y items" - if Y is 0, see "Tag expanded to 0 items?" above
5. Try `/reload` command to reload datapacks and tags

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
