package com.salaboy.conferences.c4p;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @Value("${version:0.0.0}")
    private String version;

    @GetMapping("/info")
    public String infoWithVersion() {
        return "{ \"name\" : \"C4P Service\", \"version\" : \"v" + version + "\", \"source\": \"https://github.com/salaboy/fmtok8s-c4p/releases/tag/v"+version+"\" }";
    }
}
