package com.quental.rickmorty.sync.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ExternalResourceUrlParserTest {

    @Test
    void extractsAnIdOnlyForTheExpectedResource() {
        assertThat(ExternalResourceUrlParser.extractId(
                "https://rickandmortyapi.com/api/episode/28", ResourceType.EPISODE))
                .contains(28L);

        assertThatThrownBy(() -> ExternalResourceUrlParser.extractId(
                "https://rickandmortyapi.com/api/location/28", ResourceType.EPISODE))
                .isInstanceOf(ExternalApiException.class)
                .hasMessageContaining("episode");
    }

    @Test
    void treatsTheExternalEmptyReferenceAsAbsent() {
        assertThat(ExternalResourceUrlParser.extractId("", ResourceType.LOCATION)).isEmpty();
    }
}
