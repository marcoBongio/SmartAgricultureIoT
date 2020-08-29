package com.example.SmartAgriculture;

import com.example.SmartAgriculture.californium.ProxyCoAP;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @GetMapping("/home")
    public String onLoad(Model model) {
        model.addAttribute("nodes", ProxyCoAP.sensorList);
        return "home";
    }

}
