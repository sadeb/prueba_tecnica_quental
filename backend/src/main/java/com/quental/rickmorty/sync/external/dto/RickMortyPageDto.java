package com.quental.rickmorty.sync.external.dto;

import java.util.ArrayList;
import java.util.List;

public class RickMortyPageDto<T> {

    private RickMortyPageInfoDto info;
    private List<T> results = new ArrayList<>();

    public RickMortyPageInfoDto getInfo() { return info; }
    public void setInfo(RickMortyPageInfoDto info) { this.info = info; }
    public List<T> getResults() { return results; }
    public void setResults(List<T> results) { this.results = results; }
}
