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
		if(ProxyCoAP.sensorList.isEmpty()) return;

		String actuatorIP = null;
		String payload = new String(exchange.getRequestPayload());
		int min = Integer.MAX_VALUE;
		if(payload.equals("irrigator")) {
			for (Node n : ProxyCoAP.sensorList) {
				if (n.getNodeType().equals("actuator") && n.getNodeResource().equals("irrigator"))
					if(n.getLinkedNodes().size() < min) {
						actuatorIP = n.getNodeIP();
						min = n.getLinkedNodes().size();
					}
			}
			if(actuatorIP == null) return;
			for (Node n : ProxyCoAP.sensorList) {
				if(n.getNodeIP().equals(actuatorIP))
					n.addLinkedNode(exchange.getSourceAddress().getHostAddress());
			}
		}
		//else if(payload.equals("window")) {}

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

	/*public void handlePUT(CoapExchange exchange) {
		System.out.println("PUT request received.");
		JSONObject contentJson = new JSONObject(new String(exchange.getRequestPayload()));

		if(contentJson != null) {
			String nodeIP = (String) contentJson.get("ip");
			for(Node n: ProxyCoAP.sensorList)
				if(n.getNodeIP().equals(nodeIP)) {
					n.setValues((String) contentJson.get("status"));
					CoapClient client = new CoapClient("coap://[" + nodeIP + "]/" + n.getNodeResource());
					client.put("status="+( (String)contentJson.get("status")), MediaTypeRegistry.TEXT_PLAIN);
				}

			Response response = new Response(ResponseCode.CONTENT);
			response.setPayload("ACK");
			exchange.respond(response);
		}
	}*/

	public void handlePOST(CoapExchange exchange) {
		JSONObject contentJson = new JSONObject(new String(exchange.getRequestPayload()));
		System.out.println("Node registration...");

		if (contentJson != null){
			String nodeIP = exchange.getSourceAddress().getHostAddress();

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
			client.post("name="+nodeName,MediaTypeRegistry.TEXT_PLAIN); //a cosa serve?

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

							if (n.getNodeType().equals("sensor")) value = jobj.get("humidity").toString();
							else value = jobj.get("status").toString();

							System.out.println(n.getNodeName()+", "+n.getNodeResource()+": "+value);
							//n.setValues(value);
							ProxyCoAP.sensorList.get(ProxyCoAP.sensorList.indexOf(n)).setValues(value);

						} catch(Exception e) { System.out.println("Ops!"); }
                }
                public void onError() { System.err.println("Failed"); }
            }
        );
    }

}
