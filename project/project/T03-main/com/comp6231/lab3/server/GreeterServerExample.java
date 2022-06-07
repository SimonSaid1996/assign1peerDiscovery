
/*
 *  GreeterServerExample.java
 *
 *  A sample server example using socklib.ServerListener
 *  See also: GreaterServerProtocol.java
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package com.comp6231.lab3.server;

import com.comp6231.socklib.ServerListener;
import com.comp6231.socklib.ServerPDPExample;

import java.util.Scanner;

public class GreeterServerExample {
	public static int port;
	public static String repid;


	public static void main(String[] args) {
        try {
//		port = 55000;
		port = Integer.valueOf(args[1]);
		repid=args[0];
		ServerListener server = new ServerListener("GreeterServer", port, GreeterServerProtocol::new);
		server.start();
		ServerPDPExample spdp = new ServerPDPExample();
		spdp.start(port,repid);
		System.out.println("\nStarted TCP server port "+port+"\nYou may open multiple simultaneous connections.");
		System.out.println("\nPress hit ENTER if you wish to stop the server. Note that the service may NOT stop immediately.");
		new Scanner(System.in).nextLine(); // waiting for EOL from the console to terminate
		server.stop();
		//spdp.stop();
        }
        catch (Exception ex) {
            System.out.println("FATAL ERROR: " + ex.getMessage());
        }
    }

}
