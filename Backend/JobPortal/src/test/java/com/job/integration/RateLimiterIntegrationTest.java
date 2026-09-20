package com.job.integration;

import com.job.integration.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A dedicated Spring context (via {@code @TestPropertySource}) with a tiny rate-limit bucket so
 * the limiter's real behavior -- not just its defaults -- gets exercised. Each test uses its own
 * synthetic remote address so the shared {@code AuthRateLimitFilter} bucket cache (which lives for
 * the whole context, i.e. the whole class here) can't leak state between test methods.
 */
@TestPropertySource(properties = "auth.ratelimit.requests=3")
class RateLimiterIntegrationTest extends AbstractIntegrationTest {

    private ResultActions attemptSignIn(String remoteAddr, String xForwardedFor, String doConnectingIp)
            throws Exception {
        String body = "{\"username\":\"nonexistent-user\",\"password\":\"wrong-password\"}";
        var builder = MockMvcRequestBuilders.post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .with(request -> {
                    request.setRemoteAddr(remoteAddr);
                    // MockMvc's PathPatternParser-based dispatch leaves getServletPath() empty,
                    // unlike a real container serving DispatcherServlet's "/" mapping (where it
                    // returns the full path). AuthRateLimitFilter.shouldNotFilter() keys off
                    // getServletPath(), so it has to be set explicitly here to exercise the same
                    // code path a real deployment hits.
                    request.setServletPath("/auth/signin");
                    return request;
                });
        if (xForwardedFor != null) {
            builder = builder.header("X-Forwarded-For", xForwardedFor);
        }
        if (doConnectingIp != null) {
            builder = builder.header("do-connecting-ip", doConnectingIp);
        }
        return mockMvc.perform(builder);
    }

    @Test
    void fourthSignInFromSameRemoteAddressIsRateLimited() throws Exception {
        String remoteAddr = "10.90.1.1";
        for (int i = 0; i < 3; i++) {
            attemptSignIn(remoteAddr, null, null).andExpect(status().isUnauthorized());
        }
        attemptSignIn(remoteAddr, null, null)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
    }

    @Test
    void differentXForwardedForValuesDoNotCreateNewBuckets() throws Exception {
        String remoteAddr = "10.90.1.2";
        attemptSignIn(remoteAddr, "1.1.1.1", null).andExpect(status().isUnauthorized());
        attemptSignIn(remoteAddr, "2.2.2.2", null).andExpect(status().isUnauthorized());
        attemptSignIn(remoteAddr, "3.3.3.3", null).andExpect(status().isUnauthorized());
        // Still the same bucket (keyed by remote address) despite four different XFF values.
        attemptSignIn(remoteAddr, "4.4.4.4", null)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("TOO_MANY_REQUESTS"));
    }

    @Test
    void differentDoConnectingIpValuesGetSeparateBuckets() throws Exception {
        String remoteAddr = "10.90.1.3";
        for (int i = 0; i < 3; i++) {
            attemptSignIn(remoteAddr, null, "20.0.0.1").andExpect(status().isUnauthorized());
        }
        attemptSignIn(remoteAddr, null, "20.0.0.1").andExpect(status().isTooManyRequests());

        // A different do-connecting-ip value is a fresh bucket, so it is not blocked.
        attemptSignIn(remoteAddr, null, "20.0.0.2").andExpect(status().isUnauthorized());
    }
}
