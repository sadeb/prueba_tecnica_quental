package com.quental.rickmorty.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Envelope of every list endpoint of the source: { info: {...}, results: [...] }. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalPage<T> {

    private ExternalPageInfo info;
    private List<T> results;

    public ExternalPageInfo getInfo() {
        return info;
    }

    public void setInfo(ExternalPageInfo info) {
        this.info = info;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public boolean hasNext() {
        return info != null && info.getNext() != null && !info.getNext().isBlank();
    }
}
