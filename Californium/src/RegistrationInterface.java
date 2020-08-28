import java.net.SocketException;
import java.io.*;
import org.eclipse.californium.core.*;

import org.json.simple.*;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

import org.json.simple.parser.ParseException;
import org.json.JSONObject;

public class RegistrationInterface extends CoapResource {
	private static int count = 2; //because id = 1 is the BR
	
	public RegistrationInterface(String name) {
	    super(name);
	}
	
	//actuator look-up
	public void handleGET(CoapExchange exchange) {
		if(ProxyCoAP.actuatorList.isEmpty()) return;
		
		Response response = new Response(ResponseCode.CONTENT);
		String actuatorIP = null;
		String payload = new String(exchange.getRequestPayload());
		if(payload.equals("irrigator")) {
			for (Node n : ProxyCoAP.actuatorList) {
				if (n.getNodeResource().equals("irrigator"))
					actuatorIP = n.getNodeIP();
			}
		}
		response.setPayload(actuatorIP);
		exchange.respond(response);
	}
	
	public void handlePOST(CoapExchange exchange) {
		
		byte[] request = exchange.getRequestPayload();

		String content = new String(request);
		JSONObject contentJson = null;
		contentJson = new JSONObject(content);
		
		System.out.println("Registration node...");


		if (contentJson != null){
			String nodeIP = (String) exchange.getSourceAddress().getHostAddress();
			String nodeType = (String) contentJson.get("NodeType");
			String nodeResource = (String) contentJson.get("NodeResource");

			//send confirmation ACK to contiki node
			Response response = new Response(ResponseCode.CONTENT);
			response.setPayload("ACK");
			exchange.respond(response);

			String nodeName = "Node" + count++;
			if(nodeType.equals("actuator")) nodeName += "[actuator]";

			CoapClient client = new CoapClient("coap://[" + nodeIP + "]/" + nodeResource);
			client.post("name="+nodeName,MediaTypeRegistry.TEXT_PLAIN); //a cosa serve?

			Node newNode = new Node(nodeName, nodeType, nodeResource, nodeIP);
			System.out.println("nodeName=" + nodeName + ", nodeIP="+nodeIP+", nodeType=" + nodeType + ", nodeResource=" + nodeResource);
			
			if(nodeType.equals("actuator")) ProxyCoAP.actuatorList.add(newNode);
			else ProxyCoAP.sensorList.add(newNode);

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
							JSONObject jobj = null;
							jobj = new JSONObject(tmp.toString());
							//System.out.println("Prova:"+jobj);
							String value = "";
							if (n.getNodeType().equals("sensor")) value = jobj.get("humidity").toString();
							else value = jobj.get("status").toString();

							System.out.println(n.getNodeName()+", "+n.getNodeResource()+": "+value);
							n.setValues(value);

						} catch(Exception e) { System.out.println("Ops!"); }
                }
                public void onError() { System.err.println("Failed"); }
            }
        );
    }

}
