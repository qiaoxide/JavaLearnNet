package com.threeatom.learn.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable{

	private Selector selector;
	
	private ServerSocketChannel servChannel;
	
	private volatile boolean stop;
	
	public MultiplexerTimeServer(int port) {
		try {
			selector=Selector.open();
			servChannel=ServerSocketChannel.open();
			servChannel.configureBlocking(false);
			servChannel.socket().bind(new InetSocketAddress(port),1024);
			servChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("The time server is start in port:"+port);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void stop() {
		this.stop=true;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!stop) {
			try {
				System.out.println("开始进程任务");
				//这个select表示多路复用器每隔1s检查是否有事件发生
				selector.select(1000);
				System.out.println("开始查看selector");
				Set<SelectionKey> selectedKeys=selector.selectedKeys();
				Iterator<SelectionKey> it=selectedKeys.iterator();
				SelectionKey key=null;
				while(it.hasNext()) {
					key=it.next();
					it.remove();
					try {
						handleInput(key);
					}catch(Exception e) {
						if(key!=null) {
							key.cancel();
							if(key.channel()!=null) key.channel().close();
						}
					}
					
				}
			}catch(Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	private void handleInput(SelectionKey key) throws IOException{
		if(key.isValid()) {
			if(key.isAcceptable()) {
				ServerSocketChannel ssc=(ServerSocketChannel)key.channel();
				SocketChannel sc=ssc.accept();
				sc.configureBlocking(false);
				
				sc.register(selector, SelectionKey.OP_READ);
			}
			if(key.isReadable()) {
				SocketChannel sc=(SocketChannel)key.channel();
				ByteBuffer readBuffer=ByteBuffer.allocate(1024);
				//写入buffer中
				int readBytes=sc.read(readBuffer);
				if(readBytes>0) {
					//写完之后需要调用flip 将当前position设置成limit 然后将position设置成0 方便进行读取
					readBuffer.flip();
					byte[] bytes=new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body=new String(bytes,"UTF-8");
					System.out.println("The time server receive order:"+body);
					String currentTime="QUERY TIME ORDER".equalsIgnoreCase(body)?new Date().toString():"BAD ORDER";
					doWrite(sc, currentTime);
				}else if(readBytes<0) {
					key.cancel();
					sc.close();
				}
			}
		}
	}
	
	private void doWrite(SocketChannel channel,String response) throws IOException{
		if(response!=null&&response.trim().length()>0) {
			byte[] bytes=response.getBytes();
			ByteBuffer writeBuffer=ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
	}

}
