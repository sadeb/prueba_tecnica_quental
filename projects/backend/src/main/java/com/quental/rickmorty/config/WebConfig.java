package com.quental.rickmorty.config;

import com.quental.rickmorty.character.CharacterGender;
import com.quental.rickmorty.character.CharacterStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Case-insensitive binding of enum query params (?status=alive). Unknown values still fail -> 400. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, CharacterStatus.class,
                source -> source.isBlank() ? null : CharacterStatus.fromValue(source));
        registry.addConverter(String.class, CharacterGender.class,
                source -> source.isBlank() ? null : CharacterGender.fromValue(source));
    }
}
