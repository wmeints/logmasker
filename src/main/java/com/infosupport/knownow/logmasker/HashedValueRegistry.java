package com.infosupport.knownow.logmasker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class HashedValueRegistry {
    Map<String, String> values;

    public HashedValueRegistry() {
        values = new HashMap<>();
    }

    public String get(String username) {
        String hashedValue = null;
        String lookupKey = username.toLowerCase();

        if (!values.containsKey(lookupKey)) {
            hashedValue = hashValue(lookupKey);
            values.put(lookupKey, hashedValue);

            return hashedValue;
        } else {
            return values.get(lookupKey);
        }
    }

    private String hashValue(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(value.getBytes());

            return new String(java.util.Base64.getEncoder().encode(digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
