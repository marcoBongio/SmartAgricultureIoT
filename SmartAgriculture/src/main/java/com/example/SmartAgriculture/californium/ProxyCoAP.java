package com.example.SmartAgriculture.californium;

import org.eclipse.californium.core.*;
import java.util.*;

public class ProxyCoAP extends CoapServer {
	private static ProxyCoAP server = null;
	private int NUM_NODES;
	private static String[] proxyCache; //cache of the Server where the temperature value will be stored - Maybe it has to become a list in order to store the value of every sensor
	private TemperatureResource[] t;
	public static List<Node> sensorList = new ArrayList<>();

	public static void startProxy() {
		System.out.print("\033[H\033[2J");  //"clear" the screen
		System.out.flush();

		// create server
		server = new ProxyCoAP(); //new ProxyCoAP(NUM_NODES);
		server.add(new RestInterface("registration"));
		server.start();
	}
}
