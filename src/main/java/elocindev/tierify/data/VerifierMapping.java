package elocindev.tierify.data;

import java.util.List;

/**
 * Represents a mapping between a base verifier (tag or item ID) and additional verifiers
 * that should be treated as equivalent for attribute assignment purposes.
 * 
 * This allows datapacks to extend support for modded items without modifying
 * existing attribute definitions.
 * 
 * Example: Map "c:swords" to ["c:halberds", "c:polearms", "spartanweaponry:wooden_halberd"]
 * so that any attribute checking for "c:swords" will also match halberds and polearms.
 */
public class VerifierMapping {
    
    private final String baseVerifier;
    private final String baseVerifierType; // "tag" or "id"
    private final List<MappedVerifier> mappedVerifiers;
    
    public VerifierMapping(String baseVerifier, String baseVerifierType, List<MappedVerifier> mappedVerifiers) {
        this.baseVerifier = baseVerifier;
        this.baseVerifierType = baseVerifierType;
        this.mappedVerifiers = mappedVerifiers;
    }
    
    public String getBaseVerifier() {
        return baseVerifier;
    }
    
    public String getBaseVerifierType() {
        return baseVerifierType;
    }
    
    public List<MappedVerifier> getMappedVerifiers() {
        return mappedVerifiers;
    }
    
    /**
     * Represents a single verifier that should be treated as equivalent to the base verifier.
     */
    public static class MappedVerifier {
        private final String verifier;
        private final String type; // "tag" or "id"
        
        public MappedVerifier(String verifier, String type) {
            this.verifier = verifier;
            this.type = type;
        }
        
        public String getVerifier() {
            return verifier;
        }
        
        public String getType() {
            return type;
        }
    }
}
