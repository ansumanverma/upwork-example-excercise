package com.upwork.example.jwt;

import java.util.HashMap;
import java.util.Map;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;

import com.upwork.example.domain.User;

public class JWTAuthManager {

    private static final String ISSUER = "com.upwork";
    private static final String BEARER_PREFIX = "bearer ";
    private static final Long EXPIRATION_MILLIS = 30L * 60 * 1000; //default 30 min
    private static RsaJsonWebKey rsaJsonWebKey;
    private static Map<String, Long> tokenExpirationMap = new HashMap();

    private static JWTAuthManager jwtAuthManager = null;

    public static JWTAuthManager getInstance() {
        if (null == jwtAuthManager) {
            jwtAuthManager = new JWTAuthManager();
            try {
                // Generate an RSA key pair, which will be used for signing and verification of the JWT, wrapped in a JWK
                rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
                // Give the JWK a Key ID (kid), which is just the polite thing to do
                rsaJsonWebKey.setKeyId("k1");
            } catch (JoseException e) {
                System.exit(1);
            }
        }
        return jwtAuthManager;
    }



    public String generateToken(User user) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(ISSUER);
        claims.setExpirationTimeMinutesInTheFuture(24 * 60);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(2);
        claims.setSubject(user.getUsername());
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        String jwt = jws.getCompactSerialization();
        tokenExpirationMap.put(jwt, System.currentTimeMillis());
        return jwt;
    }


    public JwtClaims validateToken(String jwt) throws JoseException {
    	
        if (!tokenExpirationMap.containsKey(jwt)) {
            return null;
        }
        if ((tokenExpirationMap.get(jwt) + EXPIRATION_MILLIS) < System.currentTimeMillis()) {
            tokenExpirationMap.remove(jwt);
            return null;
        }
       
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setRequireSubject()
                .setExpectedIssuer(ISSUER)
                .setVerificationKey(rsaJsonWebKey.getKey())
                .setJwsAlgorithmConstraints(
                        new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                AlgorithmIdentifiers.RSA_USING_SHA256))
                .build(); 

        try {
            JwtClaims claims = jwtConsumer.processToClaims(jwt);
            if(claims != null){
                tokenExpirationMap.put(jwt, System.currentTimeMillis());
            }
            return claims;
        } catch (InvalidJwtException e) {
            System.out.println("Invalid JWT! " + e);
            return null;
        }
    }

    public JwtClaims validateAuthBearer(String authentication) throws JoseException {
        if (authentication != null) {
            authentication = authentication.trim();
            if (authentication.toLowerCase().startsWith(BEARER_PREFIX)) {
                String token = authentication.substring(BEARER_PREFIX.length());
                return validateToken(token);
            }
        }
        return null;
    }
}
