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
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ReforgeDataLoader implements IdentifiableResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("TieredZ");

    // Maps reforge definitions by their file identifier (e.g., "tiered:wooden_club")
    private Map<Identifier, ReforgeDefinition> reforgeDefinitions = new HashMap<>();
    
    // Legacy maps for backward compatibility
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
    
    /**
     * Complete reforge definition including both items and base materials with tag support
     */
    public static class ReforgeDefinition {
        private final List<Identifier> directItems;
        private final List<TagKey<Item>> itemTags;
        private final ReforgeBaseData baseData;
        
        public ReforgeDefinition(List<Identifier> directItems, List<TagKey<Item>> itemTags, ReforgeBaseData baseData) {
            this.directItems = directItems;
            this.itemTags = itemTags;
            this.baseData = baseData;
        }
        
        public List<Identifier> getDirectItems() {
            return directItems;
        }
        
        public List<TagKey<Item>> getItemTags() {
            return itemTags;
        }
        
        public ReforgeBaseData getBaseData() {
            return baseData;
        }
        
        /**
         * Check if an item matches this reforge definition (either direct or via tag)
         */
        public boolean matchesItem(Item item) {
            Identifier itemId = Registries.ITEM.getId(item);
            
            // Check direct item matches
            if (directItems.contains(itemId)) {
                return true;
            }
            
            // Check tag matches
            for (TagKey<Item> tag : itemTags) {
                if (item.getRegistryEntry().isIn(tag)) {
                    return true;
                }
            }
            
            return false;
        }
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("tiered", "reforge_loader");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        // Depend on tags being loaded first
        return Collections.singletonList(new Identifier("minecraft", "tags"));
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer synchronizer, ResourceManager resourceManager,
                                          Profiler prepareProfiler, Profiler applyProfiler,
                                          Executor prepareExecutor, Executor applyExecutor) {
        return CompletableFuture.runAsync(() -> {
            prepareProfiler.startTick();
            prepareProfiler.endTick();
        }, prepareExecutor).thenCompose(synchronizer::whenPrepared).thenRunAsync(() -> {
            applyProfiler.startTick();
            applyProfiler.push("reforge_items");
            loadReforgeData(resourceManager);
            applyProfiler.pop();
            applyProfiler.endTick();
        }, applyExecutor);
    }

    private void loadReforgeData(ResourceManager resourceManager) {
        // Clear existing data
        reforgeDefinitions.clear();
        reforgeIdentifiers.clear();
        reforgeBaseMap.clear();
        reforgeBaseDataMap.clear();

        // Use arrays for counter to allow modification in lambda
        final int[] filesProcessed = {0};
        final int[] filesSkipped = {0};
        final int[] tagsLoaded = {0};

        resourceManager.findResources("reforge_items", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                // Validate required fields exist
                if (!data.has("base") || !data.get("base").isJsonArray()) {
                    LOGGER.error("Resource {} is missing required 'base' array field", id.toString());
                    filesSkipped[0]++;
                    return;
                }
                if (!data.has("items") || !data.get("items").isJsonArray()) {
                    LOGGER.error("Resource {} is missing required 'items' array field", id.toString());
                    filesSkipped[0]++;
                    return;
                }

                // First, parse the base materials (items or tags)
                List<Item> baseItems = new ArrayList<Item>();
                List<TagKey<Item>> baseTags = new ArrayList<TagKey<Item>>();
                
                for (int i = 0; i < data.getAsJsonArray("base").size(); i++) {
                    String baseEntry = data.getAsJsonArray("base").get(i).getAsString();
                    
                    try {
                        // Check if this is a tag (starts with #)
                        if (TagParsingHelper.isTagReference(baseEntry)) {
                            String tagId = TagParsingHelper.extractTagId(baseEntry);
                            TagKey<Item> tag = TagParsingHelper.createItemTagFromId(tagId);
                            baseTags.add(tag);
                            LOGGER.debug("Loaded tag-based reforge base: {} in {}", tagId, id);
                            tagsLoaded[0]++;
                        } else {
                            // Handle as direct item ID
                            Item item = TagParsingHelper.getValidItem(baseEntry);
                            if (item == null) {
                                LOGGER.debug("Resource {} skipped invalid item identifier in base list: {}", id.toString(), baseEntry);
                                continue;
                            }
                            baseItems.add(item);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Resource {} failed to process base entry '{}': {}", id.toString(), baseEntry, e.getMessage());
                        continue;
                    }
                }
                
                // Now process the items (store both direct items and tags without expansion)
                List<Identifier> directItems = new ArrayList<>();
                List<TagKey<Item>> itemTags = new ArrayList<>();
                
                for (int u = 0; u < data.getAsJsonArray("items").size(); u++) {
                    String itemEntry = data.getAsJsonArray("items").get(u).getAsString();
                    
                    try {
                        // Check if this is a tag (starts with #)
                        if (TagParsingHelper.isTagReference(itemEntry)) {
                            String tagId = TagParsingHelper.extractTagId(itemEntry);
                            TagKey<Item> itemTag = TagParsingHelper.createItemTagFromId(tagId);
                            itemTags.add(itemTag);
                            LOGGER.debug("Loaded tag-based reforge items: {} in {}", tagId, id);
                            tagsLoaded[0]++;
                        } else {
                            // Handle as direct item ID
                            Item item = TagParsingHelper.getValidItem(itemEntry);
                            if (item == null) {
                                LOGGER.debug("Resource {} skipped invalid item identifier in items list: {}", id.toString(), itemEntry);
                                continue;
                            }
                            directItems.add(new Identifier(itemEntry));
                        }
                    } catch (Exception e) {
                        LOGGER.error("Resource {} failed to process item entry '{}': {}", id.toString(), itemEntry, e.getMessage());
                        continue;
                    }
                }
                
                // Check if we have any valid items to register
                if (directItems.isEmpty() && itemTags.isEmpty()) {
                    LOGGER.warn("Resource {} has no valid items after processing. Skipping registration.", id.toString());
                    filesSkipped[0]++;
                    return;
                }
                
                // Check if we have any valid base materials
                if (baseItems.isEmpty() && baseTags.isEmpty()) {
                    LOGGER.warn("Resource {} has no valid base materials after processing. Skipping registration.", id.toString());
                    filesSkipped[0]++;
                    return;
                }
                
                // Create the reforge definition
                ReforgeBaseData baseData = new ReforgeBaseData(baseItems, baseTags);
                ReforgeDefinition definition = new ReforgeDefinition(directItems, itemTags, baseData);
                reforgeDefinitions.put(id, definition);
                filesProcessed[0]++;
                
                LOGGER.debug("Loaded reforge definition from {}: {} direct items, {} item tags, {} base items, {} base tags", 
                    id, directItems.size(), itemTags.size(), baseItems.size(), baseTags.size());
                
                // Also populate legacy maps for backward compatibility
                // Expand tags at load time for the legacy system
                List<Identifier> allItemIds = new ArrayList<>(directItems);
                for (TagKey<Item> tag : itemTags) {
                    List<Identifier> expandedIds = TagParsingHelper.expandItemTagToIds(tag);
                    allItemIds.addAll(expandedIds);
                    
                    if (expandedIds.isEmpty()) {
                        LOGGER.debug("Tag {} expanded to 0 items for legacy system in {}. Tags will still work at runtime.", tag.id(), id.toString());
                    }
                }
                
                for (Identifier itemId : allItemIds) {
                    reforgeIdentifiers.add(itemId);
                    reforgeBaseMap.put(itemId, baseItems); // Keep for backward compatibility
                    reforgeBaseDataMap.put(itemId, baseData);
                }
                
            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
                filesSkipped[0]++;
            }
        });
        
        // Log summary
        LOGGER.info("Loaded {} reforge definitions ({} skipped) with {} tags using lazy evaluation", 
            filesProcessed[0], filesSkipped[0], tagsLoaded[0]);
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
    
    /**
     * Check if an item can be reforged (matches any reforge definition)
     */
    public boolean canReforge(Item item) {
        for (ReforgeDefinition definition : reforgeDefinitions.values()) {
            if (definition.matchesItem(item)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get reforge definition that matches the given item (null if none)
     */
    public ReforgeDefinition getReforgeDefinitionFor(Item item) {
        for (ReforgeDefinition definition : reforgeDefinitions.values()) {
            if (definition.matchesItem(item)) {
                return definition;
            }
        }
        return null;
    }

}
