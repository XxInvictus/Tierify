package elocindev.tierify.data;

import com.google.common.collect.Maps;
import com.google.gson.*;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
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
public class VerifierMappingLoader extends JsonDataLoader implements SimpleSynchronousResourceReloadListener {
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String PARSING_ERROR_MESSAGE = "Parsing error loading verifier mapping {}";
    private static final String LOADED_MESSAGE = "Loaded {} verifier mappings";
    private static final Logger LOGGER = LogManager.getLogger();
    
    // Maps base verifier string (e.g., "c:swords") to its mapping definition
    private Map<String, VerifierMapping> verifierMappings = new HashMap<>();
    
    public VerifierMappingLoader() {
        super(GSON, "verifier_mappings");
    }
    
    @Override
    protected void apply(Map<Identifier, JsonElement> loader, ResourceManager manager, Profiler profiler) {
        Map<String, VerifierMapping> readMappings = Maps.newHashMap();
        
        for (Map.Entry<Identifier, JsonElement> entry : loader.entrySet()) {
            Identifier identifier = entry.getKey();
            
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                
                // Parse base verifier
                String baseVerifier = json.get("base_verifier").getAsString();
                String baseVerifierType = json.get("base_verifier_type").getAsString();
                
                // Parse mapped verifiers
                List<VerifierMapping.MappedVerifier> mappedVerifiers = new ArrayList<>();
                JsonArray mappedArray = json.getAsJsonArray("mapped_verifiers");
                
                for (JsonElement element : mappedArray) {
                    JsonObject mappedObj = element.getAsJsonObject();
                    String verifier = mappedObj.get("verifier").getAsString();
                    String type = mappedObj.get("type").getAsString();
                    
                    mappedVerifiers.add(new VerifierMapping.MappedVerifier(verifier, type));
                }
                
                VerifierMapping mapping = new VerifierMapping(baseVerifier, baseVerifierType, mappedVerifiers);
                
                // Store by base verifier for easy lookup
                // If multiple mappings exist for the same base, merge them
                if (readMappings.containsKey(baseVerifier)) {
                    VerifierMapping existing = readMappings.get(baseVerifier);
                    List<VerifierMapping.MappedVerifier> combined = new ArrayList<>(existing.getMappedVerifiers());
                    combined.addAll(mappedVerifiers);
                    mapping = new VerifierMapping(baseVerifier, baseVerifierType, combined);
                }
                
                readMappings.put(baseVerifier, mapping);
                
                LOGGER.info("Loaded verifier mapping: {} -> {} mapped verifiers", 
                    baseVerifier, mappedVerifiers.size());
                
            } catch (IllegalArgumentException | JsonParseException exception) {
                LOGGER.error(PARSING_ERROR_MESSAGE, identifier, exception);
            }
        }
        
        verifierMappings = readMappings;
        LOGGER.info(LOADED_MESSAGE, readMappings.size());
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
    
    @Override
    public void reload(ResourceManager resourceManager) {
    }
}
