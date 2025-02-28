package com.gettimhired.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    Logger log = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/")
    public String index(Model model) {
        log.info("GET / index");
        return "index";
    }
}
