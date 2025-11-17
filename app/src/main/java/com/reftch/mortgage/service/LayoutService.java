package com.reftch.mortgage.service;

import java.util.Map;

import com.reftch.annotation.Service;
import com.reftch.config.ConfigurationService;
import com.reftch.utilities.ResourceService;

@Service
public class LayoutService {

    private ResourceService resourceService = ResourceService.getInstance();
    private ConfigurationService config = ConfigurationService.getInstance();

    public String getHome() {
        var values = Map.of(
                "title", "Hypothekenrechner",
                "mode.isProduction", config.getValue("server.isProduction"));
        return resourceService.getFileContent("views/index.html", values);
    }

}
