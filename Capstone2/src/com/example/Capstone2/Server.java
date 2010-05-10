package com.example.Capstone2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	ServerSocket providerSocket;
	Socket connection = null;
	BufferedOutputStream out;
	BufferedInputStream in;
	InputStreamReader isr;
	StringBuffer instr;
	byte[] message;
	Server(){}
void waitForConnection() {
	try {
		providerSocket = new ServerSocket(30000, 10);
		System.out.println("Waiting for connection");
		connection = providerSocket.accept();
		System.out.println("Connection received from " + connection.getInetAddress().getHostName());
		in = new BufferedInputStream(connection.getInputStream());
		out = new BufferedOutputStream(connection.getOutputStream());
		isr = new InputStreamReader (in,"US-ASCII");
		
	} catch (IOException e) {
		e.printStackTrace();
	}
}

	

String read() {
	instr = new StringBuffer();
	try{
		//in = new BufferedInputStream(connection.getInputStream());
		int c;
	    
		while ( (c = isr.read()) != 10)
	        instr.append( (char) c);

		System.out.println(instr.toString());
		
	}
	catch(IOException ioException){
		ioException.printStackTrace();
	}
	return instr.toString();
	
}

protected void finalize() {
	try {
		in.close();
		out.close();
		connection.close();
		providerSocket.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}

void write(byte[] b) {
	try{
		//out = new BufferedOutputStream(connection.getOutputStream());
		out.flush();
		out.write(b);
		out.flush();
	}
	catch(IOException ioException){
		ioException.printStackTrace();
	}
	
}
}
