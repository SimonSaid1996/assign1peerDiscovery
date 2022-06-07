
package com.comp6231.socklib;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class ServerPDPExample {
	DatagramSocket dSocket = null;
	private int udpPort = 6231;
	private int tcpPort;

	private String remoteId;
	public void start(int port,String repid) {
	new Thread(new Runnable() {
		@Override
		public void run(){
			tcpPort = port;
			remoteId=repid;
			runSdu();
			dSocket.close();
			}
		}).start();
	}
	private void runSdu() {
		try {
			byte[] receiveBuff = new byte[1024]; //receiving buffer
			dSocket = new DatagramSocket(udpPort);
			dSocket.setBroadcast(true);
			DatagramPacket dPacket = new DatagramPacket(receiveBuff, receiveBuff.length);
			System.out.println("\nStarted UDP server, listening on Broadcast IP, port 6231\n");
			while (true) {
				System.out.println("> Ready to receive b-cast packets...");
				dSocket.receive(dPacket); //receiving data
				System.out.println("> Received packet from " + dPacket.getAddress().getHostAddress() 
					+ ":" + dPacket.getPort());
				String msg = new String(dPacket.getData(), dPacket.getOffset(), dPacket.getLength());
				String[] splited = msg.split(" ");
				for(int i=1;i<splited.length;i++)

				if (splited[0].equals("PEER_REQUEST")) {
					if(remoteId.equals(splited[1])){
						String srvResponse = ("PEER_RESPONSE "+tcpPort);

						System.out.println(srvResponse);
						byte[] sendBuff = srvResponse.getBytes();
						DatagramPacket dPacket2 = new DatagramPacket(sendBuff, sendBuff.length, dPacket.getAddress(), dPacket.getPort());
						dSocket.send(dPacket2); 	//Send a response
						System.out.println(getClass().getName() + "> Sent response to client IP: "
								+ dPacket.getAddress().getHostAddress() + ":" + dPacket.getPort());
					}

				}
				Thread.sleep(2000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  }
