package com.github.masonm.wiremock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a collection of request fields to be used for sorting StubMappings and determining what to include in
 * the RequestPattern for a stub mapping (i.e. whitelisting fields)
 */
public class RequestFields implements Comparator<StubMapping> {
    private final List<RequestField> fields;

    // For the tests
    public RequestFields(List<RequestField> fields) {
        this.fields = fields;
    }

    @JsonCreator
    public RequestFields(@JsonProperty("fields") String ...fields) {
        this.fields = new ArrayList<>(fields.length);
        for (String field : fields) {
            this.fields.add(new RequestField(field));
        }
    }

    public int compare(StubMapping one, StubMapping two) {
        int result = 0;
        for (RequestField field : fields) {
            result = field.compare(one.getRequest(), two.getRequest());
            if (result != 0) {
                break;
            }
        }
        return result;
    }
}
