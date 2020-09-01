package com.example.SmartAgriculture.californium;

import org.eclipse.californium.core.*;
import java.util.*;

public class ProxyCoAP extends CoapServer {
	private static ProxyCoAP server = null;
	//cache of the Server where the sensor values will be stored
	//in a more realistic scenario, time-continuous sensor data should be stored in an appropriate DB
	// such as mongoDB
	public static List<Node> sensorList = new ArrayList<>();

	public static void startProxy() {
		System.out.print("\033[H\033[2J");  //"clear" the screen
		System.out.flush();

		// create server
		server = new ProxyCoAP();
		server.add(new RestInterface("registration"));
		server.start();
	}
}
