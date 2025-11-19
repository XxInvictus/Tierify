package elocindev.tierify.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import elocindev.tierify.util.TagParsingHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ReforgeDataLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("TieredZ");

    private List<Identifier> reforgeIdentifiers = new ArrayList<>();
    private Map<Identifier, List<Item>> reforgeBaseMap = new HashMap<>();
    private Map<Identifier, ReforgeBaseData> reforgeBaseDataMap = new HashMap<>();
    
    /**
     * Holds both direct item references and tag references for reforge base materials
     */
    public static class ReforgeBaseData {
        private final List<Item> directItems;
        private final List<TagKey<Item>> tags;
        
        public ReforgeBaseData(List<Item> directItems, List<TagKey<Item>> tags) {
            this.directItems = directItems;
            this.tags = tags;
        }
        
        public List<Item> getDirectItems() {
            return directItems;
        }
        
        public List<TagKey<Item>> getTags() {
            return tags;
        }
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("tiered", "reforge_loader");
    }

    @Override
    public void reload(ResourceManager resourceManager) {

        resourceManager.findResources("reforge_items", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                // First, parse the base materials (items or tags)
                List<Item> baseItems = new ArrayList<Item>();
                List<TagKey<Item>> baseTags = new ArrayList<TagKey<Item>>();
                
                for (int i = 0; i < data.getAsJsonArray("base").size(); i++) {
                    String baseEntry = data.getAsJsonArray("base").get(i).getAsString();
                    
                    // Check if this is a tag (starts with #)
                    if (TagParsingHelper.isTagReference(baseEntry)) {
                        String tagId = TagParsingHelper.extractTagId(baseEntry);
                        TagKey<Item> tag = TagParsingHelper.createItemTagFromId(tagId);
                        baseTags.add(tag);
                        LOGGER.info("Loaded tag-based reforge base: {}", tagId);
                    } else {
                        // Handle as direct item ID
                        Item item = TagParsingHelper.getValidItem(baseEntry);
                        if (item == null) {
                            LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), baseEntry);
                            continue;
                        }
                        baseItems.add(item);
                    }
                }
                
                // Now process the items (which can also be tags)
                List<Identifier> targetItems = new ArrayList<>();
                
                for (int u = 0; u < data.getAsJsonArray("items").size(); u++) {
                    String itemEntry = data.getAsJsonArray("items").get(u).getAsString();
                    
                    // Check if this is a tag (starts with #)
                    if (TagParsingHelper.isTagReference(itemEntry)) {
                        String tagId = TagParsingHelper.extractTagId(itemEntry);
                        TagKey<Item> itemTag = TagParsingHelper.createItemTagFromId(tagId);
                        
                        // Expand the tag to all items it contains
                        List<Identifier> expandedIds = TagParsingHelper.expandItemTagToIds(itemTag);
                        targetItems.addAll(expandedIds);
                        LOGGER.info("Expanded tag {} to {} items for reforge", tagId, expandedIds.size());
                    } else {
                        // Handle as direct item ID
                        Item item = TagParsingHelper.getValidItem(itemEntry);
                        if (item == null) {
                            LOGGER.info("Resource {} was not loaded cause {} is not a valid item identifier", id.toString(), itemEntry);
                            continue;
                        }
                        targetItems.add(new Identifier(itemEntry));
                    }
                }
                
                // Register all target items with the same base data
                ReforgeBaseData baseData = new ReforgeBaseData(baseItems, baseTags);
                for (Identifier itemId : targetItems) {
                    reforgeIdentifiers.add(itemId);
                    reforgeBaseMap.put(itemId, baseItems); // Keep for backward compatibility
                    reforgeBaseDataMap.put(itemId, baseData);
                }
                
            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

    public List<Item> getReforgeBaseItems(Item item) {
        ArrayList<Item> list = new ArrayList<Item>();
        if (reforgeBaseMap.containsKey(Registries.ITEM.getId(item))) {
            return reforgeBaseMap.get(Registries.ITEM.getId(item));
        }
        return list;
    }
    
    /**
     * Get complete reforge base data including both direct items and tags
     */
    public ReforgeBaseData getReforgeBaseData(Item item) {
        if (reforgeBaseDataMap.containsKey(Registries.ITEM.getId(item))) {
            return reforgeBaseDataMap.get(Registries.ITEM.getId(item));
        }
        return new ReforgeBaseData(new ArrayList<>(), new ArrayList<>());
    }

    public void putReforgeBaseItems(Identifier id, List<Item> items) {
        reforgeBaseMap.put(id, items);
    }

    public void clearReforgeBaseItems() {
        reforgeBaseMap.clear();
        reforgeBaseDataMap.clear();
    }

    public List<Identifier> getReforgeIdentifiers() {
        return reforgeIdentifiers;
    }

}
