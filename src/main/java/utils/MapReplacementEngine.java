package utils;

import java.util.HashMap;
import java.util.Map;

public class MapReplacementEngine implements TokenDeterminer {

    protected Map<String, String> tokenMap = new HashMap<>();

    public MapReplacementEngine(Map<String, String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    public String resolveToken (String tokenName) {
        return this.tokenMap.get(tokenName);
    }
}