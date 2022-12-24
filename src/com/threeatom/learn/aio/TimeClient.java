package com.threeatom.learn.aio;

public class TimeClient {

	public static void main(String[] args) {
		int port=8080;
		if(args!=null&&args.length>0) {
			try {
				port=Integer.valueOf(args[0]);
			}catch(NumberFormatException e) {
				
			}
		}
		AsyncTimeClientHandler client=new AsyncTimeClientHandler("127.0.0.1", port);
		new Thread(client,"AIO-client-001").start();
		
	}
}
