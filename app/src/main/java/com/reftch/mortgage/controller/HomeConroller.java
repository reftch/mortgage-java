package com.reftch.mortgage.controller;

import com.reftch.annotation.Controller;
import com.reftch.annotation.Inject;
import com.reftch.annotation.Route;
import com.reftch.mortgage.service.LayoutService;

@Controller(basePath = "/")
public class HomeConroller {
    
    @Inject
    private LayoutService layoutService;

    @Route(method = "GET", path = "/")
    public String home() {
        return layoutService.getHome();
    }
}
