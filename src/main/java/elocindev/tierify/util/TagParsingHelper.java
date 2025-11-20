package elocindev.tierify.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Helper utility for parsing and handling tag-based item references.
 * Supports the # prefix convention for tag identifiers.
 */
public class TagParsingHelper {
    
    /**
     * Checks if a string represents a tag reference (starts with #)
     */
    public static boolean isTagReference(String entry) {
        return entry != null && entry.startsWith("#");
    }
    
    /**
     * Extracts the tag ID from a tag reference by removing the # prefix
     * @param tagReference The tag reference string (e.g., "#c:swords")
     * @return The tag ID without the # prefix (e.g., "c:swords")
     */
    public static String extractTagId(String tagReference) {
        if (!isTagReference(tagReference)) {
            throw new IllegalArgumentException("Not a tag reference: " + tagReference);
        }
        return tagReference.substring(1);
    }
    
    /**
     * Creates a TagKey from a tag reference string
     * @param tagReference The tag reference string (e.g., "#c:swords")
     * @return The TagKey for items
     */
    public static TagKey<Item> createItemTag(String tagReference) {
        String tagId = extractTagId(tagReference);
        return TagKey.of(RegistryKeys.ITEM, new Identifier(tagId));
    }
    
    /**
     * Creates a TagKey from a tag ID (without # prefix)
     * @param tagId The tag identifier (e.g., "c:swords")
     * @return The TagKey for items
     */
    public static TagKey<Item> createItemTagFromId(String tagId) {
        // Validate that the tag ID doesn't contain #
        if (tagId != null && tagId.startsWith("#")) {
            throw new IllegalArgumentException("Tag ID should not contain # prefix. Use extractTagId() first. Got: " + tagId);
        }
        return TagKey.of(RegistryKeys.ITEM, new Identifier(tagId));
    }
    
    /**
     * Expands a tag to all items that are members of that tag
     * @param tag The TagKey to expand
     * @return List of all items in the tag
     */
    public static List<Item> expandItemTag(TagKey<Item> tag) {
        List<Item> items = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            if (item.getDefaultStack().isIn(tag)) {
                items.add(item);
            }
        }
        return items;
    }
    
    /**
     * Expands a tag reference string to all items in that tag
     * @param tagReference The tag reference string (e.g., "#c:swords")
     * @return List of all items in the tag
     */
    public static List<Item> expandItemTagFromReference(String tagReference) {
        TagKey<Item> tag = createItemTag(tagReference);
        return expandItemTag(tag);
    }
    
    /**
     * Expands a tag to all item identifiers that are members of that tag
     * @param tag The TagKey to expand
     * @return List of all item identifiers in the tag
     */
    public static List<Identifier> expandItemTagToIds(TagKey<Item> tag) {
        List<Identifier> ids = new ArrayList<>();
        for (Item item : Registries.ITEM) {
            if (item.getDefaultStack().isIn(tag)) {
                ids.add(Registries.ITEM.getId(item));
            }
        }
        return ids;
    }
    
    /**
     * Checks if an item identifier is valid (not air)
     * @param identifier The item identifier string
     * @return true if valid, false if it resolves to air
     */
    public static boolean isValidItemIdentifier(String identifier) {
        Item item = Registries.ITEM.get(new Identifier(identifier));
        return !item.toString().equals("air");
    }
    
    /**
     * Gets an item from an identifier, or null if invalid (air)
     * @param identifier The item identifier string
     * @return The Item, or null if it resolves to air
     */
    public static Item getValidItem(String identifier) {
        // Validate that the identifier doesn't contain #
        if (identifier != null && identifier.startsWith("#")) {
            throw new IllegalArgumentException("Item identifier should not contain # prefix. This is a tag reference, not an item ID: " + identifier);
        }
        Item item = Registries.ITEM.get(new Identifier(identifier));
        return item.toString().equals("air") ? null : item;
    }
}
