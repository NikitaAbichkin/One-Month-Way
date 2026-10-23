package com.company.order20;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionController {
    private final String version;

    public VersionController(@Value("${APP_VERSION:unknown}") String version) {
        this.version = version;
    }

    @GetMapping("/version")
    public String version() {
        return version;
    }
}
