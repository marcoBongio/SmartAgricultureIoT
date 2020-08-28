import java.net.SocketException;
import java.io.*;
import org.eclipse.californium.core.*;

import org.json.simple.*;
import org.json.simple.parser.ParseException;

import java.util.*;

public class ProxyCoAP extends CoapServer {

    private int NUM_NODES;
    private static String[] proxyCache; //cache of the Server where the temperature value will be stored - Maybe it has to become a list in order to store the value of every sensor
    private TemperatureResource[] t;
	public static Node actuatorList = null;
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
	
        try {
            // create server
            ProxyCoAP server = new ProxyCoAP(NUM_NODES);
            server.add(new RegistrationInterface("registration"));
            server.start();
            
        } catch (SocketException e) {    
            System.err.println("Failed to initialize server: " + e.getMessage());
        }
    }
}
