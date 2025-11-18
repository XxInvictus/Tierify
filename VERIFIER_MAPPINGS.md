# Verifier Mappings

Verifier mappings allow you to extend existing item attribute verifiers (like `c:swords`) to also match additional tags or specific item IDs **without modifying the base attribute files**.

This is especially useful for adding support for modded items that don't fit into conventional tags.

## How It Works

When Tierify checks if an item should receive a tier, it:
1. First checks the direct verifier (e.g., is the item in `c:swords`?)
2. Then checks any mapped verifiers you've defined (e.g., is it in `c:halberds`? or is it `spartanweaponry:wooden_halberd`?)

If either check passes, the item is eligible for that tier.

## File Location

Verifier mapping files go in: `data/<namespace>/verifier_mappings/*.json`

For example:
- `data/tiered/verifier_mappings/spartanweaponry_melee.json`

## File Format

```json
{
  "base_verifier": "c:swords",
  "base_verifier_type": "tag",
  "mapped_verifiers": [
    {
      "verifier": "c:halberds",
      "type": "tag"
    },
    {
      "verifier": "spartanweaponry:wooden_halberd",
      "type": "id"
    }
  ]
}
```

### Fields:

- **`base_verifier`** (string, required): The original verifier to extend (e.g., `"c:swords"`, `"minecraft:diamond_sword"`)
- **`base_verifier_type`** (string, required): Either `"tag"` or `"id"`
- **`mapped_verifiers`** (array, required): List of additional verifiers to check
  - **`verifier`** (string, required): The tag or item ID to add
  - **`type`** (string, required): Either `"tag"` or `"id"`

## Examples

### Example 1: Map Modded Weapon Tags to Existing Sword Attributes

This makes all polearms, halberds, and spears eligible for the same attributes as swords:

```json
{
  "base_verifier": "c:swords",
  "base_verifier_type": "tag",
  "mapped_verifiers": [
    {
      "verifier": "c:polearms",
      "type": "tag"
    },
    {
      "verifier": "c:halberds",
      "type": "tag"
    },
    {
      "verifier": "c:spears",
      "type": "tag"
    }
  ]
}
```

### Example 2: Add Specific Items by ID

For items that don't have conventional tags:

```json
{
  "base_verifier": "c:swords",
  "base_verifier_type": "tag",
  "mapped_verifiers": [
    {
      "verifier": "spartanweaponry:wooden_halberd",
      "type": "id"
    },
    {
      "verifier": "spartanweaponry:iron_halberd",
      "type": "id"
    },
    {
      "verifier": "spartanweaponry:diamond_halberd",
      "type": "id"
    }
  ]
}
```

### Example 3: Map Ranged Weapons

Extend bow attributes to cover crossbows and javelins:

```json
{
  "base_verifier": "c:bows",
  "base_verifier_type": "tag",
  "mapped_verifiers": [
    {
      "verifier": "c:crossbows",
      "type": "tag"
    },
    {
      "verifier": "c:javelins",
      "type": "tag"
    },
    {
      "verifier": "c:throwing_knives",
      "type": "tag"
    }
  ]
}
```

### Example 4: Map Specific Item ID to Another Item ID

Extend a specific item's attributes to another item:

```json
{
  "base_verifier": "minecraft:diamond_sword",
  "base_verifier_type": "id",
  "mapped_verifiers": [
    {
      "verifier": "betterend:aeternium_sword",
      "type": "id"
    }
  ]
}
```

## Multiple Mappings

You can create multiple mapping files for the same base verifier. They will be **merged automatically**.

For example:
- `spartanweaponry_melee.json` maps `c:swords` to `c:halberds`, `c:polearms`
- `additional_weapons.json` also maps `c:swords` to `c:katanas`, `c:rapiers`

Result: `c:swords` will match all of: swords, halberds, polearms, katanas, and rapiers.

## Use Cases

1. **Supporting Modded Weapons**: Add Spartan Weaponry halberds, Simply Swords katanas, etc.
2. **Cross-Mod Compatibility**: Make weapons from different mods share attribute pools
3. **Custom Weapon Categories**: Group unconventional weapons together
4. **Quick Fixes**: Add support for specific items without editing attribute files

## What Gets Mapped?

Verifier mappings affect **ALL attributes** that use the base verifier. For example:

If you map `c:halberds` to `c:swords`, then:
- All common sword attributes will work on halberds
- All uncommon sword attributes will work on halberds
- All rare/epic/legendary/mythic sword attributes will work on halberds
- Etc.

This is by design - it's a transparent extension system.

## Datapack Priority

If you're using multiple datapacks:
- Mappings from all datapacks are loaded
- Multiple mappings to the same base verifier are merged
- Load order doesn't matter

## Debugging

If your mappings aren't working:
1. Check the server log for "Loaded verifier mapping" messages
2. Verify your JSON syntax is correct
3. Ensure the base verifier matches exactly what's in the attribute files
4. For item IDs, use the full `namespace:item_name` format
5. For tags, include the namespace (e.g., `c:swords`, not just `swords`)

## Performance

Verifier mappings have minimal performance impact:
- Loaded once on server start/reload
- Checked only when items are crafted/looted/spawned
- Uses efficient lookup structures

## Compatibility

This system is:
- ✅ Compatible with all existing attribute files
- ✅ Compatible with other mods' datapacks
- ✅ Compatible with resource pack reloads (`/reload`)
- ✅ Backward compatible (works without any mappings)
- ✅ Forward compatible (new mods can add their own mappings)