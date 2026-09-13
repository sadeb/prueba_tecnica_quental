package com.quental.rickmorty.external;

/** The source answered 404 for a page: past the last page (normal end of pagination). */
public class ExternalPageNotFoundException extends RuntimeException {

    public ExternalPageNotFoundException(String message) {
        super(message);
    }
}
