package com.threeatom.learn.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TimeServer {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int port=8080;
		
		if(args!=null&&args.length>0) {
			try {
				port=Integer.valueOf(args[0]);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		ServerSocket server=null;
		
		try {
			server=new ServerSocket(port);
			System.out.println("The time server is start in port:"+port);
			Socket socket=null;
			TimeServerHandlerExecutePool singleExecutor=new TimeServerHandlerExecutePool(50, 10000);
			
			while(true) {
				socket=server.accept();
				//开启新线程处理socket
				System.out.println("client!!");
				singleExecutor.execute(new TimeServerHandler(socket));
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println(e.toString());
		} finally {
			if(server!=null) {
				System.out.println("The time server close");
				server.close();
				server=null;
			}
		}

	}

}
