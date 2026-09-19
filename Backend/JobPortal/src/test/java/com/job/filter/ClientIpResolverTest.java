package com.job.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Plain unit test (no Spring context) for ClientIpResolver. */
class ClientIpResolverTest {

    private static final String HEADER = "do-connecting-ip";
    private static final String REMOTE_ADDR = "10.0.0.99";

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(REMOTE_ADDR);
        return request;
    }

    @Test
    void headerPresentValidIpv4UsesHeaderValue() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "203.0.113.10");

        assertEquals("203.0.113.10", new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void headerPresentValidIpv6UsesHeaderValue() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "2001:db8::1");

        assertEquals("2001:db8::1", new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void xForwardedForPresentHeaderAbsentUsesRemoteAddress() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "198.51.100.5");

        assertEquals(REMOTE_ADDR, new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void xForwardedForPresentAndHeaderPresentUsesHeaderValue() {
        MockHttpServletRequest request = request();
        request.addHeader("X-Forwarded-For", "198.51.100.5");
        request.addHeader(HEADER, "203.0.113.10");

        assertEquals("203.0.113.10", new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void commaSeparatedListFallsBackToRemoteAddress() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "1.2.3.4, 5.6.7.8");

        assertEquals(REMOTE_ADDR, new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void nonIpTextFallsBackToRemoteAddress() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "not-an-ip");

        assertEquals(REMOTE_ADDR, new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void tooLongValueFallsBackToRemoteAddress() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "1".repeat(200));

        assertEquals(REMOTE_ADDR, new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void emptyValueFallsBackToRemoteAddress() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "");

        assertEquals(REMOTE_ADDR, new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void valueWithNewlineFallsBackToRemoteAddress() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "1.2.3.4\nX-Injected: evil");

        assertEquals(REMOTE_ADDR, new ClientIpResolver(HEADER).resolve(request));
    }

    @Test
    void blankConfiguredHeaderNameAlwaysUsesRemoteAddress() {
        MockHttpServletRequest request = request();
        request.addHeader(HEADER, "203.0.113.10");

        assertEquals(REMOTE_ADDR, new ClientIpResolver("").resolve(request));
        assertEquals(REMOTE_ADDR, new ClientIpResolver("   ").resolve(request));
        assertEquals(REMOTE_ADDR, new ClientIpResolver(null).resolve(request));
    }
}
