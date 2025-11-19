package draylar.tiered.api;

import elocindev.tierify.Tierify;
import elocindev.tierify.data.VerifierMapping;
import elocindev.tierify.util.TagParsingHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;

public class ItemVerifier {

    private final String id;
    private final String tag;

    public ItemVerifier(String id, String tag) {
        this.id = id;
        this.tag = tag;
    }

    /**
     * Returns whether the given {@link Identifier} is valid for this ItemVerifier, which may check direct against either a {@link Identifier} or {@link Tag<Item>}.
     * <p>
     * The given {@link Identifier} should be the ID of an {@link Item} in {@link Registry#ITEM}.
     *
     * @param itemID item registry ID to check against this verifier
     * @return whether the check succeeded
     */
    public boolean isValid(Identifier itemID) {
        return isValid(itemID.toString());
    }

    /**
     * Returns whether the given {@link String} is valid for this ItemVerifier, which may check direct against either a {@link Identifier} or {@link Tag<Item>}.
     * <p>
     * The given {@link String} should be the ID of an {@link Item} in {@link Registry#ITEM}.
     * <p>
     * This method now also checks verifier mappings, allowing datapacks to extend verifiers
     * without modifying the original attribute files.
     *
     * @param itemID item registry ID to check against this verifier
     * @return whether the check succeeded
     */
    public boolean isValid(String itemID) {
        // Check direct ID match
        if (id != null) {
            if (itemID.equals(id)) {
                return true;
            }
            // Check if this ID has mapped verifiers
            return checkMappedVerifiers(id, itemID);
        } 
        // Check tag match
        else if (tag != null) {
            TagKey<Item> itemTag = TagParsingHelper.createItemTagFromId(tag);
            
            if (itemTag != null) {
                // Check direct tag membership
                if (new ItemStack(Registries.ITEM.get(new Identifier(itemID))).isIn(itemTag)) {
                    return true;
                }
                // Check if this tag has mapped verifiers
                return checkMappedVerifiers(tag, itemID);
            } else {
                Tierify.LOGGER.error(tag + " was specified as an item verifier tag, but it does not exist!");
            }
        }

        return false;
    }
    
    /**
     * Checks if the item matches any mapped verifiers for the given base verifier.
     * 
     * @param baseVerifier The base verifier (tag or id) to look up mappings for
     * @param itemID The item ID to check
     * @return true if the item matches any mapped verifier
     */
    private boolean checkMappedVerifiers(String baseVerifier, String itemID) {
        if (Tierify.VERIFIER_MAPPING_LOADER == null) {
            return false;
        }
        
        List<VerifierMapping.MappedVerifier> mappedVerifiers = 
            Tierify.VERIFIER_MAPPING_LOADER.getMappedVerifiers(baseVerifier);
        
        for (VerifierMapping.MappedVerifier mapped : mappedVerifiers) {
            if ("id".equals(mapped.getType())) {
                // Check direct ID match
                if (itemID.equals(mapped.getVerifier())) {
                    return true;
                }
            } else if ("tag".equals(mapped.getType())) {
                // Check tag membership
                TagKey<Item> mappedTag = TagParsingHelper.createItemTagFromId(mapped.getVerifier());
                if (new ItemStack(Registries.ITEM.get(new Identifier(itemID))).isIn(mappedTag)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public String getId() {
        return id;
    }

    public TagKey<Item> getTagKey() {
        return TagParsingHelper.createItemTagFromId(tag);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode() * 17 + (tag == null ? 0 : tag.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemVerifier other)) {
            return false;
        }
        if (this != other) {
            return false;
        }
        String thisId = this.id == null ? "" : this.id;
        String thisTag = this.tag == null ? "" : this.tag;
        String otherId = other.id == null ? "" : other.id;
        String otherTag = other.tag == null ? "" : other.tag;
        return thisId.equals(otherId) && thisTag.equals(otherTag);
    }
}
