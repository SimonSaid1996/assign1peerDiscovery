
/*
 *  GreaterServerProtocol.java
 *
 *  A server side protocol implementation using socklib.ServerListener
 *  See also: GreeterServerExample.java
 *
 *  (C) 2022 Ali Jannatpour <ali.jannatpour@concordia.ca>
 *
 *  This code is licensed under GPL.
 *
 */

package com.comp6231.lab3.server;

import com.comp6231.lab3.models.Dict;
import com.comp6231.socklib.ListenerInfo;
import com.comp6231.socklib.SimpleSocketProtocol;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

public class GreeterServerProtocol extends SimpleSocketProtocol {
    static Dict dict=new Dict();
    public GreeterServerProtocol(Socket s, ListenerInfo info) {
        super(s, info);
    }

    public static String broadcastAndAskFromOtherReposetories (String command,String repid) throws IOException,SocketTimeoutException {
        DatagramSocket dSocket;
        String respond ="error";
        int res_counter=0;
        try {
            //Open a random port to send the package
            dSocket = new DatagramSocket();
            dSocket.setSoTimeout(1000);
            dSocket.setBroadcast(true);
            byte[] sendData = ("PEER_REQUEST "+repid).getBytes();
            // Broadcast the message over all the network interfaces
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) { continue; } // Omit loopbacks
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) { continue; } //Don't send if no broadcast IP.
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 6231);
                        dSocket.send(sendPacket); // Send the broadcast package!
                    }
                    catch (Exception e) {
                    }
                    System.out.println("\n> Request sent to IP: "
                            + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                }
            }

            System.out.println("\n> Done looping through all interfaces. Now waiting for a reply!");





            byte[] recvBuf = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);

            while(true) {

                dSocket.receive(receivePacket); //Wait for a response
                //We have a response
                System.out.println("\n> Received packet from " + receivePacket.getAddress().getHostAddress() + " : " + receivePacket.getPort());
                String msg = new String(receivePacket.getData()).trim();
                String[] splited = msg.split(" ");
                if (splited[0].equals("PEER_RESPONSE")) {
                    if(res_counter>0){
                        throw new SocketTimeoutException();
                    }
                    res_counter+=1;
                    System.out.println("\n> Ready to connect! ");
                    //TODO implement TCP binding with server
                    Socket clientSocket = new Socket(receivePacket.getAddress().getHostAddress(), Integer.valueOf(splited[1]));  //Integer.valueOf(splited[1])
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    out.println(command);
                    Scanner scanner = new Scanner(clientSocket.getInputStream());
                    respond= scanner.nextLine();
                }


//			dSocket.close();  //Close the port!
            }
        }
        catch (SocketTimeoutException e) {
            if(res_counter>1){
                respond="error";
            }

            System.out.println(e.getMessage());
        }
        catch (IOException ex) {
            System.out.println("Hey, there is an error!!!");
            System.out.println(ex.getMessage());
        }finally {
            return respond;
        }


    }
    public void run() throws IOException {

        while (isRunning() && isConnected()) {
            String data = recvln();
            System.out.println("Recieved msg: "+data);
            String[] command=data.split(" ");
            if (command.length>3){//inter if we have a dsum command
                int dsum = dict.sum(command[1]);
                int i;
                for (i = 3; i < command.length; i++) {
                    if(command[i].equals(GreeterServerExample.repid)) {
                        sendln("you dont need to put repid of this rep as well");
                        continue;
                    }
                    String res = broadcastAndAskFromOtherReposetories("SUM " + command[1], command[i]);
                    if (res == "error") {
                        sendln("SERVER: ERR Non-existence or ambiguous repository " + command[i]);
                        break;
                    }
                    dsum += Integer.valueOf(res.split(" ")[1]);

                }if(i == command.length)
                    sendln("OK " + dsum);

            }else{

                String[] keyParts = {""};
                if (command.length>1) {
                    keyParts = command[1].split("\\.");
                }
                if (keyParts.length > 1) {//multy part key
                    command[1] = keyParts[1];
                }
                if ((keyParts.length > 1) && !(keyParts[0].equals(GreeterServerExample.repid))) {//comand is for other repo
                    String res = broadcastAndAskFromOtherReposetories(String.join(" ", command), keyParts[0]);

                    if (res == "error") {
                        res = "SERVER: ERR Non-existence or ambiguous repository " + keyParts[0];
                    }
                    sendln(res);

                    System.out.println("----------------------");
                } else {//command is for this repo
                    switch (command[0].toUpperCase()) {
                        case "OK":
                            sendln(data);
                            break;
                        case "SET":
                            System.out.println("in the set");
                            ArrayList<Integer> values = new ArrayList<>(1);
                            for (int i = 2; i < command.length; i++) {
                                values.add(Integer.valueOf(command[i]));
                            }
                            dict.set(command[1], values);

                            sendln("OK");
                            break;
                        case "DELETE":
                            Integer v = dict.getValue(command[1]);
                            if (v != null) {  //only delete values that exist in the map
                                dict.delete(command[1]);
                            }

                            sendln("OK");
                            break;
                        case "ADD":    //kind of fixed

                            boolean vExist = dict.addValue(command[1], Integer.valueOf(command[2]));
                            if (vExist)
                                sendln("OK");
                            else
                                sendln("OK, BUT THE KEY DOESN'T EXIST YET");

                            break;

                        case "GET":
                            ArrayList<Integer> value = dict.getValues(command[1]);
                            if (value != null)
                                sendln("OK " + value);
                            else
                                sendln("OK, BUT NO VALUES DETECTED ");//might need to change this part later


                            break;
                        case "LISTKEY":   //might need to change the case name later, ask the prof

                            sendln("OK, KEYS ARE" + dict.list_keys().toString());

                            break;
                        case "RESET":

                            dict.reset();

                            sendln("OK, ALL DATA RESET");
                            break;
                        case "GETANY":

                            sendln("OK " + dict.getValue(command[1]));

                            break;
                        case "SUM":
                            int sumV = 0;

                            sumV = dict.sum(command[1]);  //update the sum value

                            sendln("OK " + sumV);
                            break;

                        case "MAX":
                            int maxV = 0;// dict.max(command[1]);  //update the max value

                            maxV = dict.max(command[1]);

                            sendln("OK " + maxV);
                            break;

                        case "MIN":
                            int minV = 0;//dict.min(command[1]);  //update the min value

                            minV = dict.min(command[1]);

                            sendln("OK " + minV);
                            break;
                        case "BYE":
                            sendln("CIAO Arrivederci!");
                            close();
                            return;
                        //case "EXIT":
                        case "QUIT":
                            sendln("BYE, it was nice seeing you.");
                            close();
                            return;
                        default:
                            sendln(data + "=> is wrong");
                            System.out.println("def");
                            break;
                    }
                }
            }

        }
        close();
    }
}
