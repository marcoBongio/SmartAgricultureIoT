import java.net.SocketException;
import java.io.*;
import org.eclipse.californium.core.*;

import org.json.simple.*;
import org.json.simple.parser.ParseException;

public class ProxyCoAP extends CoapServer {

    private int NUM_NODES;
    private static String[] proxyCache; //cache of the Server where the temperature value will be stored - Maybe it has to become a list in order to store the value of every sensor
    private TemperatureResource[] t;

    //constructor
    public ProxyCoAP(int nn) throws SocketException {
    
    	this.NUM_NODES = nn;
    	proxyCache = new String[NUM_NODES];
    	t = new TemperatureResource[NUM_NODES];
    	
        for(int i = 0; i < NUM_NODES; i++) {
            t[i] = new TemperatureResource(i);
            this.add(new TemperatureResource(i));
        }

    }

    public static void writeCache(int index, String txt) { proxyCache[index] = txt; }

    public static void printCache() {
        
        System.out.print("[ ");
        for(int j=0; j<proxyCache.length; j++)
            System.out.print(proxyCache[j] + " ");
            
        System.out.println("]");
    }

    public static String getCache(int index){ return proxyCache[index]; }

    public int getNumNodes() { return NUM_NODES; }

    public static void main (String[] args) {
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	int NUM_NODES=1;
    	
    	System.out.print("\033[H\033[2J");  //"clear" the screen
	System.out.flush();
    	/*System.out.print("\nPlease insert the NUM_NODES: ");
    	try {
		NUM_NODES = Integer.parseInt(br.readLine());
	} catch (IOException e) { e.printStackTrace(); }*/
	
        try {
            // create server
            ProxyCoAP server = new ProxyCoAP(NUM_NODES);
            server.add(new RegistrationInterface("registration"));
            server.start();
            
/*         CoapClient[] resource = new CoapClient[server.getNumNodes()];

            for(int i=0; i<server.getNumNodes(); i++) {
            	
            	String hex=Integer.toHexString(i+2);
            	//String addr = "coap://[fd00::20"+hex+":"+hex+":"+hex+":"+ hex + "]/humidity";
            	//System.out.println(addr);
            	
            	resource[i] = new CoapClient(addr);
            	resource[i].observe(
                        new CoapHandler() {
                            @Override
                            public void onLoad(CoapResponse response) {
                            
                                String tmp = response.getResponseText();

                                try {
                                
                                    JSONObject jobj = (JSONObject) JSONValue.parseWithException(tmp);                                    
                                    String temperature = jobj.get("humidity").toString();

                                    int id = Integer.parseInt(jobj.get("id").toString());
                                    System.out.println("NOTIFICATION from node "+id+" (0x"+Integer.toHexString(id)+"): " + temperature);
                                    
                                    ProxyCoAP.writeCache((id-2), temperature);
                                    System.out.println("Data saved in position "+(id-2));
                                    
                                    ProxyCoAP.printCache();
                                    System.out.println("");
                                    
                                } catch(ParseException e) { e.printStackTrace(); }
                            }

                            @Override
                            public void onError() {
                                System.err.println("[ERROR] Observing Failed. Retrying...");
                            }
                        });
            }*/
        } catch (SocketException e) {    
            System.err.println("Failed to initialize server: " + e.getMessage());
        }
    }
}
