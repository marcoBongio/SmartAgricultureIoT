package com.example.SmartAgriculture.californium;

import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import org.json.JSONObject;

import java.lang.reflect.Proxy;

public class RestInterface extends CoapResource {
	private static int count = 2; //because id = 1 is the BR
	
	public RestInterface(String name) {
	    super(name);
	}
	
	//actuator look-up
	public void handleGET(CoapExchange exchange) {
		System.out.println("[ DBG ] actuator lookup... ");

		String actuatorIP = null; //"NONE";
		if (!ProxyCoAP.sensorList.isEmpty()) {
			String payload = new String(exchange.getRequestPayload());
			int min = Integer.MAX_VALUE;

			if (payload.equals("irrigator")) {
				for (Node n : ProxyCoAP.sensorList) {
					if (n.getNodeType().equals("actuator") && n.getNodeResource().equals("irrigator"))
						if (n.getLinkedNodes().size() < min) {
							actuatorIP = n.getNodeIP();
							min = n.getLinkedNodes().size();
						}
				}
			} else if (payload.equals("window")) {
				for (Node n : ProxyCoAP.sensorList) {
					if (n.getNodeType().equals("actuator") && n.getNodeResource().equals("window"))
						if (n.getLinkedNodes().size() < min) {
							actuatorIP = n.getNodeIP();
							min = n.getLinkedNodes().size();
						}
				}
			}

			if (actuatorIP != null) {
				for (Node n : ProxyCoAP.sensorList) {
					if (n.getNodeIP().equals(actuatorIP))
						n.addLinkedNode(exchange.getSourceAddress().getHostAddress());
					else if (n.getNodeIP().equals(exchange.getSourceAddress().getHostAddress()))
						n.addLinkedNode(actuatorIP);
				}
			}
		}

		Response response = new Response(ResponseCode.CONTENT);
		response.setPayload(actuatorIP);
		exchange.respond(response);
	}

	public boolean checkDouble(String ip) {
		for(Node n: ProxyCoAP.sensorList)
			if(n.getNodeIP().equals(ip))
				return true;
		return false;
	}

	public void handlePOST(CoapExchange exchange) {
		JSONObject contentJson = new JSONObject(new String(exchange.getRequestPayload()));

		if (contentJson != null){
			String nodeIP = exchange.getSourceAddress().getHostAddress();
			System.out.println("Node ["+nodeIP+"] registration...");

			//check for double registrations
			if(checkDouble(nodeIP)) {
				System.out.println("Node "+nodeIP+" is already registered!");
				return;
			}

			String nodeType = (String) contentJson.get("NodeType");
			String nodeResource = (String) contentJson.get("NodeResource");

			//send confirmation ACK to contiki node
			Response response = new Response(ResponseCode.CONTENT);
			response.setPayload("ACK");
			exchange.respond(response);

			String nodeName = "Node" + count++;

			CoapClient client = new CoapClient("coap://[" + nodeIP + "]/" + nodeResource);
			client.post("name="+nodeName,MediaTypeRegistry.TEXT_PLAIN); 

			Node newNode = new Node(nodeName, nodeType, nodeResource, nodeIP);
			System.out.println("nodeName=" + nodeName + ", nodeIP="+nodeIP+", nodeType=" + nodeType + ", nodeResource=" + nodeResource);

			ProxyCoAP.sensorList.add(newNode);
			coapClient(newNode);
		}
	}
	
	public static void coapClient(Node n) {
        CoapClient client = new CoapClient("coap://[" + n.getNodeIP() + "]/" + n.getNodeResource());
        client.observe(
            new CoapHandler() {
                public void onLoad(CoapResponse response) {
                	String tmp = response.getResponseText();
                	if(tmp == null) return;
                        try {
				JSONObject jobj = new JSONObject(tmp);
				String value = "";

				if (n.getNodeType().equals("sensor")){
					if (n.getNodeResource().equals("humidity"))
						value = jobj.get("humidity").toString();
					else
						value = jobj.get("temperature").toString();
				}
				else value = jobj.get("status").toString();

				ProxyCoAP.sensorList.get(ProxyCoAP.sensorList.indexOf(n)).setValues(value);

			} catch(Exception e) { System.out.println("Connection Error! Restart app."); }
                }
                public void onError() { System.err.println("Observing Failed"); }
            }
        );
    }

}
