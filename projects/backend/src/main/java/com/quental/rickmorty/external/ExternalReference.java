package com.quental.rickmorty.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** origin / location of a character: { name, url }; url is "" when unknown. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalReference {

    private String name;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
