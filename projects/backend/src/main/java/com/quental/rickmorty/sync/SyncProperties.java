package com.quental.rickmorty.sync;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "sync")
public class SyncProperties {

    /** Launch a full sync when the application starts (default false; README explains POST /api/admin/sync). */
    private boolean onStartup = false;
    @Valid
    private Topics topics = new Topics();
    @NotBlank
    private String dltSuffix = ".DLT";

    public boolean isOnStartup() {
        return onStartup;
    }

    public void setOnStartup(boolean onStartup) {
        this.onStartup = onStartup;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public String getDltSuffix() {
        return dltSuffix;
    }

    public void setDltSuffix(String dltSuffix) {
        this.dltSuffix = dltSuffix;
    }

    public String dltOf(String topic) {
        return topic + dltSuffix;
    }

    public static class Topics {
        @NotBlank
        private String characters = "rm.characters";
        @NotBlank
        private String episodes = "rm.episodes";
        @NotBlank
        private String locations = "rm.locations";

        public String getCharacters() {
            return characters;
        }

        public void setCharacters(String characters) {
            this.characters = characters;
        }

        public String getEpisodes() {
            return episodes;
        }

        public void setEpisodes(String episodes) {
            this.episodes = episodes;
        }

        public String getLocations() {
            return locations;
        }

        public void setLocations(String locations) {
            this.locations = locations;
        }
    }
}
