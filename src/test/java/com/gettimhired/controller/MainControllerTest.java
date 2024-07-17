package com.gettimhired.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MainControllerTest {

    private MainController mainController;
    private Model model;

    @BeforeEach
    public void init() {
        model = mock(Model.class);
        mainController = new MainController();
    }

    @Test
    public void testThatRootRouteReturnsTheIndexPage() {
        assertEquals("index", mainController.index(model));
    }
}