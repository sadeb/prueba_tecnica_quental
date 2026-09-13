package com.quental.rickmorty.external;

import com.quental.rickmorty.common.InvalidExternalPayloadException;

/** Relations arrive as URLs (".../episode/28"); the numeric last segment is the external id (ADR-001). */
public final class ExternalIdParser {

    private ExternalIdParser() {
    }

    /** Null for a blank url (the source's "unknown" reference); exception for a non-numeric id. */
    public static Long fromUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        int slash = trimmed.lastIndexOf('/');
        String last = slash < 0 ? trimmed : trimmed.substring(slash + 1);
        try {
            long id = Long.parseLong(last);
            if (id <= 0) {
                throw new InvalidExternalPayloadException("Non-positive id in url '" + url + "'");
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new InvalidExternalPayloadException("Non-numeric id in url '" + url + "'");
        }
    }
}
