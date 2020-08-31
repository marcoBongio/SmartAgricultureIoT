package com.example.SmartAgriculture;

import com.example.SmartAgriculture.californium.ProxyCoAP;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.SmartAgriculture.californium.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MainController {

    @RequestMapping("/home")
    public String onLoad(Model model) {
        List<Node> sensors = new ArrayList<>();
        List<Node> actuators = new ArrayList<>();
        for(Node n: ProxyCoAP.sensorList)
            if(n.getNodeType().equals("sensor"))
                sensors.add(n);
            else actuators.add(n);

        model.addAttribute("nodes", sensors);
        model.addAttribute("actuators", actuators);

        return "home";
    }

    @GetMapping("/updateStatus")
    public String updateStatus(@RequestParam(required = true, value = "ip") String ip, @RequestParam(required = true, value = "resource") String resource, @RequestParam(required = true, value = "status") String status, Model m) throws URISyntaxException {
        System.out.println("Changing node "+ip+ " ("+resource+") status to "+status);

        CoapClient client = new CoapClient("coap://[" + ip + "]/" + resource);
        client.put("status="+status, MediaTypeRegistry.TEXT_PLAIN);

        return "home";
    }
    
    @RequestMapping("/refreshSensors")
    public String refreshSensors(Model model) {
        List<Node> sensors = new ArrayList<>();
        List<Node> actuators = new ArrayList<>();
        for(Node n: ProxyCoAP.sensorList)
            if(n.getNodeType().equals("sensor"))
                sensors.add(n);
            else actuators.add(n);

        model.addAttribute("nodes", sensors);
        model.addAttribute("actuators", actuators);

        return "home";
    }

    @GetMapping("/associatedNodes")
    @ResponseBody
    public String getAssociatedNodes(@RequestParam(required = true, value = "ip") String ip, Model model) {
        for(Node n: ProxyCoAP.sensorList)
            if(n.getNodeIP().equals(ip))
                return n.getLinkedNodes().toString();
        return "";
    }

}
//test (works)
        /*
        URI uri = new URI("coap://127.0.0.1:5683/registration");
        CoapClient client = new CoapClient(uri);

        Request req = new Request(CoAP.Code.GET);
        req.setPayload("test");

        CoapResponse response = client.advanced(req);
        System.out.println("\n CoapResponse: "+response);
*/;
