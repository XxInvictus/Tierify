package elocindev.tierify.data;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads verifier mapping files from data/&lt;namespace&gt;/verifier_mappings/*.json
 * 
 * These mappings allow datapacks to extend existing verifiers (like "c:swords")
 * to also match additional tags or item IDs without modifying the base attribute files.
 * 
 * Example JSON format:
 * {
 *   "base_verifier": "c:swords",
 *   "base_verifier_type": "tag",
 *   "mapped_verifiers": [
 *     { "verifier": "c:halberds", "type": "tag" },
 *     { "verifier": "c:polearms", "type": "tag" },
 *     { "verifier": "spartanweaponry:wooden_halberd", "type": "id" }
 *   ]
 * }
 */
public class VerifierMappingLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading verifier mapping {}";
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Maps base verifier string (e.g., "c:swords") to its mapping definition
    private Map<String, VerifierMapping> verifierMappings = new HashMap<>();
    
    public VerifierMappingLoader() {
        super(GSON, "verifier_mappings");
    }
    
    @Override
    public Collection<Identifier> getFabricDependencies() {
        // Depend on tags being loaded first
        return Collections.singletonList(new Identifier("minecraft", "tags"));
    }
    
    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        Map<String, VerifierMapping> readMappings = Maps.newHashMap();
        
        int filesProcessed = 0;
        int filesSkipped = 0;
        int totalMappedVerifiers = 0;
        
        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            Identifier identifier = entry.getKey();
            
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                
                // Validate required fields exist
                if (!json.has("base_verifier") || json.get("base_verifier").isJsonNull()) {
                    LOGGER.error("Verifier mapping {} is missing required 'base_verifier' field", identifier);
                    filesSkipped++;
                    continue;
                }
                if (!json.has("base_verifier_type") || json.get("base_verifier_type").isJsonNull()) {
                    LOGGER.error("Verifier mapping {} is missing required 'base_verifier_type' field", identifier);
                    filesSkipped++;
                    continue;
                }
                if (!json.has("mapped_verifiers") || !json.get("mapped_verifiers").isJsonArray()) {
                    LOGGER.error("Verifier mapping {} is missing required 'mapped_verifiers' array field", identifier);
                    filesSkipped++;
                    continue;
                }
                
                // Parse base verifier
                String baseVerifier = json.get("base_verifier").getAsString();
                String baseVerifierType = json.get("base_verifier_type").getAsString();
                
                // Parse mapped verifiers
                List<VerifierMapping.MappedVerifier> mappedVerifiers = new ArrayList<>();
                JsonArray mappedArray = json.getAsJsonArray("mapped_verifiers");
                
                for (JsonElement element : mappedArray) {
                    JsonObject mappedObj = element.getAsJsonObject();
                    
                    // Validate mapped verifier fields
                    if (!mappedObj.has("verifier") || mappedObj.get("verifier").isJsonNull()) {
                        LOGGER.debug("Verifier mapping {} has mapped_verifier entry missing 'verifier' field, skipping", identifier);
                        continue;
                    }
                    if (!mappedObj.has("type") || mappedObj.get("type").isJsonNull()) {
                        LOGGER.debug("Verifier mapping {} has mapped_verifier entry missing 'type' field, skipping", identifier);
                        continue;
                    }
                    
                    String verifier = mappedObj.get("verifier").getAsString();
                    String type = mappedObj.get("type").getAsString();
                    
                    mappedVerifiers.add(new VerifierMapping.MappedVerifier(verifier, type));
                }
                
                if (mappedVerifiers.isEmpty()) {
                    LOGGER.warn("Verifier mapping {} has no valid mapped verifiers after processing, skipping", identifier);
                    filesSkipped++;
                    continue;
                }
                
                VerifierMapping mapping = new VerifierMapping(baseVerifier, baseVerifierType, mappedVerifiers);
                
                // Store by base verifier for easy lookup
                // If multiple mappings exist for the same base, merge them
                if (readMappings.containsKey(baseVerifier)) {
                    VerifierMapping existing = readMappings.get(baseVerifier);
                    List<VerifierMapping.MappedVerifier> combined = new ArrayList<>(existing.getMappedVerifiers());
                    combined.addAll(mappedVerifiers);
                    mapping = new VerifierMapping(baseVerifier, baseVerifierType, combined);
                    LOGGER.debug("Merged verifier mapping for {}: {} total mapped verifiers", baseVerifier, combined.size());
                } else {
                    LOGGER.debug("Loaded verifier mapping: {} -> {} mapped verifiers", baseVerifier, mappedVerifiers.size());
                }
                
                readMappings.put(baseVerifier, mapping);
                filesProcessed++;
                totalMappedVerifiers += mappedVerifiers.size();
                
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
                filesSkipped++;
            } catch (Exception exception) {
                LOGGER.error("Unexpected error loading verifier mapping {}: {}", identifier, exception.getMessage());
                filesSkipped++;
            }
        }
        
        verifierMappings = readMappings;
        LOGGER.info("Loaded {} verifier mappings ({} skipped) with {} total mapped verifiers", 
            filesProcessed, filesSkipped, totalMappedVerifiers);
    }
    
    /**
     * Get all verifier mappings
     */
    public Map<String, VerifierMapping> getVerifierMappings() {
        return verifierMappings;
    }
    
    /**
     * Get mapped verifiers for a specific base verifier
     * 
     * @param baseVerifier The base verifier string (e.g., "c:swords")
     * @return List of mapped verifiers, or empty list if no mapping exists
     */
    public List<VerifierMapping.MappedVerifier> getMappedVerifiers(String baseVerifier) {
        VerifierMapping mapping = verifierMappings.get(baseVerifier);
        return mapping != null ? mapping.getMappedVerifiers() : new ArrayList<>();
    }
    
    @Override
    public Identifier getFabricId() {
        return new Identifier("tiered", "verifier_mappings");
    }
}
