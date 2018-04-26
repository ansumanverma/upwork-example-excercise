package com.upwork.example.route;


import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.jose4j.jwt.JwtClaims;

import com.fasterxml.jackson.core.JsonParseException;
import com.upwork.example.jwt.JWTAuthManager;

public class AbstractRestServiceRouteBuilder extends SpringRouteBuilder {

    @Override
    public void configure() throws RuntimeCamelException {

        restConfiguration()
                .component("servlet")
                .bindingMode(RestBindingMode.json)
                .dataFormatProperty("prettyPrint", "true")
                .dataFormatProperty("json.in.disableFeatures", "FAIL_ON_UNKNOWN_PROPERTIES")
                .contextPath("/rest")
                .enableCORS(true)
                .corsHeaderProperty("Access-Control-Allow-Headers",
                        "Origin, Accept, " +
                                "X-Requested-With, " +
                                "Content-Type, " +
                                "Authorization, " +
                                "Access-Control-Request-Method, " +
                                "Access-Control-Request-Headers")
        ;


        // Handles errors due to an incorrect json syntax.
        onException(JsonParseException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setBody(constant(null))
                .stop();
        
        
    }

    /**
     * @param authorizationToken
     * @return
     */
    protected boolean authorize(String authorizationToken, String requiredRole) {
        try {
            JwtClaims jwtClaims = JWTAuthManager.getInstance().validateAuthBearer(authorizationToken);
            if (jwtClaims != null) {
                String role = jwtClaims.getClaimValue("role", String.class);
                if (requiredRole == null || requiredRole.equals(role)) {
                    log.debug("User {}: authorised", jwtClaims.getSubject());
                    return true;
                } else {
                    log.debug("User {}: not authorised, required role: {}", jwtClaims.getSubject(), requiredRole);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Error validating authentication header");
        }
        return false;
    }

    /**
     * @param authorizationToken
     * @return
     */
    protected boolean authorize(String authorizationToken) {
        return authorize(authorizationToken, null);
    }

}