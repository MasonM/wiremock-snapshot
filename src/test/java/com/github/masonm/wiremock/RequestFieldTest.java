package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.matching.MockRequest;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.matching.MockRequest.mockRequest;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public class RequestFieldTest {
    public static RequestField aRequestField(String field) {
        return new RequestField(field);
    }

    @Test
    public void isHeaderReturnsTrueForHeader() {
        assertTrue(aRequestField("Accept").isHeader());
    }

    @Test
    public void isHeaderReturnsFalseForNonHeaders() {
        assertFalse(aRequestField("url").isHeader());
        assertFalse(aRequestField("method").isHeader());
    }

    @Test
    public void headerValueReturnsValueForExistingHeader() {
        MockRequest request = mockRequest().header("foo", "bar");
        assertEquals("bar", aRequestField("foo").headerValue(request));
    }

    @Test(expected = IllegalStateException.class)
    public void headerValueReturnsThrowsExceptionForMissingHeader() {
        assertNull(aRequestField("foo").headerValue(mockRequest()));
    }

    @Test
    public void equalsReturnsTrueForSameRequestField() {
        RequestField one = aRequestField("url");
        RequestField two = aRequestField("url");
        assertTrue(one.equals(two));
        assertTrue(two.equals(one));
        assertTrue(one.equals(one));
    }

    @Test
    public void equalsReturnsTrueForSameString() {
        assertTrue(aRequestField("url").equals("url"));
    }

    @Test
    public void equalsReturnsFalseForDifferentField() {
        RequestField one = aRequestField("url");
        RequestField two = aRequestField("method");
        assertFalse(one.equals(two));
        assertFalse(one.equals("method"));
        assertFalse(one.equals(null));
    }

    @Test
    public void equalsReturnsFalseForSameString() {
        assertFalse(aRequestField("method").equals("url"));
    }

    @Test
    public void compareUrlWithEqualUrls() {
        RequestField field = aRequestField("url");
        RequestPattern one = newRequestPattern().withUrl("foo").build();

        assertEquals(0, field.compare(one, one));
        assertEquals(0, field.compare(one, newRequestPattern().withUrl("FOO").build()));
    }

    @Test
    public void compareUrlWithUnequalUrls() {
        RequestField field = aRequestField("url");
        RequestPattern one = newRequestPattern().withUrl("foo").build();
        RequestPattern two = newRequestPattern().withUrl("bar").build();

        assertThat(0, greaterThan(field.compare(two, one)));
        assertThat(0, lessThan(field.compare(one, two)));
    }

    @Test
    public void compareMethod() {
        RequestField field = aRequestField("method");
        RequestPattern one = newRequestPattern(RequestMethod.GET, null).build();
        RequestPattern two = newRequestPattern(RequestMethod.TRACE, null).build();

        assertEquals(0, field.compare(one, one));
        assertEquals(0, field.compare(one, newRequestPattern(RequestMethod.GET, null).build()));

        assertNotEquals(0, field.compare(one, two));
        assertNotEquals(0, field.compare(two, one));
    }

    @Test
    public void compareHeaderWithOneMissingAllHeaders() {
        RequestField field = aRequestField("Accept");
        RequestPattern one = newRequestPattern().withHeader("foo", equalTo("bar")).build();
        RequestPattern two = newRequestPattern().build();

        assertEquals(1, field.compare(one, two));
        assertEquals(-1, field.compare(two, one));
    }

    @Test
    public void compareHeaderWithOneMissingHeader() {
        RequestField field = aRequestField("Accept");
        RequestPattern one = newRequestPattern().withHeader("foo", equalTo("bar")).build();
        RequestPattern two = newRequestPattern().withHeader("Accept", equalTo("bar")).build();

        assertEquals(-1, field.compare(one, two));
        assertEquals(1, field.compare(two, one));
    }

    @Test
    public void compareHeaderNotEqual() {
        RequestField field = aRequestField("Accept");
        RequestPattern one = newRequestPattern().withHeader("Accept", equalTo("foo")).build();
        RequestPattern two = newRequestPattern().withHeader("Accept", equalTo("bar")).build();

        assertThat(0, greaterThan(field.compare(two, one)));
        assertThat(0, lessThan(field.compare(one, two)));
    }

    @Test
    public void compareHeaderEqual() {
        RequestField field = aRequestField("Accept");
        RequestPattern one = newRequestPattern().withHeader("Accept", equalTo("foo")).build();
        RequestPattern two = newRequestPattern().withHeader("Accept", equalTo("FOo")).build();

        assertEquals(0, field.compare(one, one));
        assertEquals(0, field.compare(one, two));
        assertEquals(0, field.compare(two, one));
    }
}
