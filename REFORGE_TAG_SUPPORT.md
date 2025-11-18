# Reforge Item Tag Support

Starting with this version, Tierify supports using **tags** in the `base` field of reforge item definitions. This allows for more flexible and cross-mod compatible reforge material specifications.

## How It Works

In your `data/<namespace>/reforge_items/*.json` files, you can now use tags by prefixing them with `#`:

```json
{
  "items": [
    "minecraft:iron_sword"
  ],
  "base": [
    "#c:ingots/iron"
  ]
}
```

## Syntax

### Using Tags

Prefix tag identifiers with `#`:
```json
"base": ["#c:ingots/iron"]
```

### Using Direct Item IDs

No prefix needed (backward compatible):
```json
"base": ["minecraft:iron_ingot"]
```

### Mixing Both

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

## Examples

### Example 1: Iron Tools Using Tag

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

**Result:** Any item tagged with `c:ingots/iron` can be used to reforge these tools. This includes:
- `minecraft:iron_ingot`
- Any modded iron ingots that are properly tagged

### Example 2: Gold Items Using Convention Tags

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

### Example 3: Mixed Direct Items and Tags

```json
{
  "items": ["minecraft:netherite_sword"],
  "base": [
    "#c:ingots/netherite",
    "minecraft:netherite_ingot",
    "minecraft:netherite_scrap"
  ]
}
```

**Result:** Accepts netherite ingots (via tag OR direct ID) AND netherite scraps.

### Example 4: Multiple Tag Types

```json
{
  "items": ["minecraft:wooden_sword"],
  "base": [
    "#minecraft:planks",
    "#minecraft:logs"
  ]
}
```

**Result:** Any planks OR logs can be used to reforge wooden swords.

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

### 1. Cross-Mod Compatibility
Works with any mod that adds materials using conventional tags:

```json
{
  "items": ["custom_mod:steel_sword"],
  "base": ["#c:ingots/steel"]
}
```

This works with:
- Create's steel ingots
- Immersive Engineering steel
- Tech Reborn steel
- Any other mod's steel (if properly tagged)

### 2. Reduced File Duplication
Instead of creating separate files for each material variant:

**Before (multiple files needed):**
```json
// File 1: iron_tools_forge.json
{"items": [...], "base": ["minecraft:iron_ingot"]}

// File 2: iron_tools_create.json  
{"items": [...], "base": ["create:iron_sheet"]}
```

**After (single file):**
```json
{"items": [...], "base": ["#c:ingots/iron"]}
```

### 3. Future-Proof
New mods that add materials will automatically work if they use standard tags.

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

### Tag Resolution
- Tags are resolved at runtime when checking reforge validity
- If a tag doesn't exist, it's treated as empty (no items match)
- No errors or crashes occur with non-existent tags

### Performance
- Tag checks are cached by Minecraft's tag system
- Minimal performance impact compared to direct item checks
- Tags are only evaluated when items are placed in the reforge interface

### Priority
When multiple base options exist, ANY match is valid:
```json
"base": ["#c:ingots/iron", "minecraft:copper_ingot"]
```
Either iron ingots (any) OR copper ingot will work.

## Migration Guide

### Migrating Existing Files

**Old format:**
```json
{
  "items": ["minecraft:iron_sword"],
  "base": ["minecraft:iron_ingot"]
}
```

**New format (recommended):**
```json
{
  "items": ["minecraft:iron_sword"],
  "base": ["#c:ingots/iron"]
}
```

### When to Use Tags vs Direct Items

**Use tags when:**
- ✅ Material has cross-mod equivalents (iron, gold, copper)
- ✅ Want to support modded variants automatically
- ✅ Material is part of convention tags

**Use direct items when:**
- ✅ Item is unique to one mod
- ✅ No standard tag exists
- ✅ Need precise control over accepted items

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
- `data/tiered/reforge_items/iron_tools.json` - Uses `#c:ingots/iron`
- `data/tiered/reforge_items/diamond_tools.json` - Mixes tags and direct items
- `data/tiered/reforge_items/elytra.json` - Direct item (no tag)

## Best Practices

1. **Use convention tags** (`c:`) when available for maximum compatibility
2. **Mix formats** when needed - tags for common materials, direct items for unique ones
3. **Document custom tags** if creating new ones for your modpack
4. **Test thoroughly** with your modpack's installed mods

## Related Documentation

- [Verifier Mappings](VERIFIER_MAPPINGS.md) - For extending which items can receive tiers
- [README.md](README.md) - General customization documentation
