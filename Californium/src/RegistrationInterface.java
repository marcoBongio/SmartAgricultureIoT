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
		Response response = new Response(ResponseCode.CONTENT);

		//String name = exchange.getQueryParameter("actuatorName");
		String actuatorIP = ProxyCoAP.actuatorList.getNodeIP();
		
		System.out.println("Actuator IP: "+actuatorIP);
		response.setPayload(actuatorIP);

		exchange.respond(response);
	}
	
	public void handlePOST(CoapExchange exchange) {
		
		byte[] request = exchange.getRequestPayload();

		String content = new String(request);
		JSONObject contentJson = null;
		contentJson = new JSONObject(content.toString());
		
		System.out.println("Registration node...");


		if (contentJson != null){
			String nodeIP = (String) exchange.getSourceAddress().getHostAddress();
			String nodeType = (String) contentJson.get("NodeType");
			String nodeResource = (String) contentJson.get("NodeResource");

			Response response = new Response(ResponseCode.CONTENT);
			response.setPayload("ACK");
			exchange.respond(response);

			String nodeName = "Node" + count++;
			CoapClient client = new CoapClient("coap://[" + nodeIP + "]/" + nodeResource);
			client.post("name="+nodeName,MediaTypeRegistry.TEXT_PLAIN);
			Node newNode = new Node(nodeName, nodeType, nodeResource,nodeIP);
			
			System.out.println("nodeName=" + nodeName + ", nodeIP="+nodeIP+", nodeType=" + nodeType + ", nodeResource=" + nodeResource);
			
			if(nodeType.equals("actuator")) ProxyCoAP.actuatorList = newNode;

			coapClient(nodeIP, nodeName, nodeType, nodeResource);

		}

	}
	
	public static void coapClient(String moteIP, String nodeName, String nodeType, String moteResource) {
        CoapClient client = new CoapClient("coap://[" + moteIP + "]/" + moteResource);
        client.observe(
            new CoapHandler() {
                public void onLoad(CoapResponse response) {
                	String tmp = response.getResponseText();
                        try {
				JSONObject jobj = null;
                    		jobj = new JSONObject(tmp.toString()); 
		    		System.out.println("Prova:"+jobj); 
                        
			if (nodeType.equals("sensor")){          
                        	String value = jobj.get("humidity").toString();
				System.out.println(nodeName+") Humidity: "+value);
			} else{
				String value = jobj.get("status").toString();
				System.out.println(nodeName+") Status: "+value);
			}
		} catch(Exception e) { System.out.println("Ops!"); }

                }

                public void onError() {
                    System.err.println("Failed");
                }
            }
        );
    }

}
