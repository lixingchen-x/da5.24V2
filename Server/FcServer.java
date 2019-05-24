/*
COMP90020 project
group01
Xingchen li   935256
Jingjing Shan 743343
Changda Jiang 879725
Qianfan Chen  754824
 */

package Server;

import java.net.*;
import java.util.*;
import java.util.ArrayList;



public class FcServer {
	
	private static int portnumber = 8888;
	private static ServerSocket listening = null;

	public static ArrayList username = new ArrayList();
	public static int idAcc=0;
	public static Map<String,Integer> idMap= new HashMap<String,Integer>();
	public static Map<Integer,String> id_ip= new HashMap<Integer,String>();




	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try
		{
			listening = new ServerSocket(portnumber); //create a server socket to listen
			System.out.println(Thread.currentThread().getName()+"-server listening on 8888 for a conncetion");
			System.out.println(InetAddress.getLocalHost().getHostAddress());

			while (true)
			{
				//Accept the incoming client connection request
				Socket clientsocket = listening.accept();
				System.out.println(Thread.currentThread().getName() 
						+ " - Client conection accepted");

				Client_connection clientconnection = new Client_connection(clientsocket);
				clientconnection.setName("Thread" + username.size());
				clientconnection.start();

			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
   
}
