/*
COMP90020 project
group01
Xingchen li   935256
Jingjing Shan 743343
Changda Jiang 879725
Qianfan Chen  754824
 */

package Server;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;





public class Client_connection extends Thread{
	
	private Socket clientSocket;
	private DataInputStream in;
	private DataOutputStream out;
	private String userName=null;
	private String loginStatus;
	private static JSONObject logState=new JSONObject();
	private int new_client=0;
	private String ip;
	
	public Client_connection(Socket clientSocket)
	{
		try
		{
			this.clientSocket = clientSocket;
			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());

			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void run()
	{
		try
		{
			
			System.out.println(Thread.currentThread().getName()
					+ " - Reading messages from client's " + FcServer.username.size()  + " connection");
			JSONParser parser=new JSONParser();
			while (!clientSocket.isClosed())
			{
				if(new_client==0) {
				server_manager.getInstance().clientConnected(this);
				new_client=1;
				}
				if (in.available() > 0)
				{
					logState = (JSONObject)parser.parse(in.readUTF());
					List<Client_connection> clients = server_manager.getInstance().getConnectedClients();
					userName=logState.get("userName").toString();
					loginStatus=logState.get("loginStatus").toString();
					ip=logState.get("ip").toString();
					if(loginStatus.equals("no")){
                        if(!FcServer.username.contains(userName)) {
                            FcServer.username.add(userName);
                            FcServer.id_ip.put(getID(ip),ip);


                            logState.put("loginStatus", "yes");
							logState.put("playerNum",FcServer.username.size());
							logState.put("id_ip",FcServer.id_ip);
							logState.put("id",getID(ip));
							logState.put("onlinePlayer",showList(FcServer.username));
							logState.put("checkAlive","no");


							if(FcServer.username.size()==4){
								for(Client_connection client : clients) {
									logState.put("checkAlive","no");
									client.write(logState);
								}
							}else {
								logState.put("checkAlive","no");
								write(logState);
								System.out.println("1 "+logState);
								
							}
                        }else if(FcServer.username.contains(userName)){
                            logState.put("loginStatus", "no");
                            logState.put("checkAlive","no");
                            write(logState);
                            
                            server_manager.getInstance().clientDisconnected(this);
                            System.out.println("duplicate name "+userName);

                        }else {
                            continue;
                        }
                    }else if(loginStatus.equals("yes")){
						if(FcServer.username.size()==4){
							logState.put("checkAlive","no");
							write(logState);
						}
					}


				}
				System.out.println("sm size "+server_manager.connectedClients.size());
				System.out.println("user size "+FcServer.username.size());
				System.out.println("server send to "+userName);
				if(server_manager.connectedClients.size()==FcServer.username.size()) {
				logState.put("checkAlive","yes");
				System.out.println("2 "+logState);
				write(logState);
				sleep(2500);
				}



			}
						
			
		}
		catch (Exception e) {
			
			e.printStackTrace();

		}
	}
	
	private synchronized void write(JSONObject gameinfo)
	{
		try
		{
			out.writeUTF(gameinfo.toJSONString());
			out.flush();
		}
		catch (IOException e) {
			server_manager.getInstance().clientDisconnected(this);
			if(FcServer.username.contains(userName)) {
				FcServer.username.remove(userName);
				logState.put("onlinePlayer",showList(FcServer.username));
				FcServer.id_ip.remove(getID(ip));
				logState.put("id_ip",FcServer.id_ip);
				logState.put("playerNum",FcServer.username.size());
				System.out.println(userName + "is gone");
				System.out.println("user size "+FcServer.username.size());
				System.out.println("sm size "+server_manager.connectedClients.size());
				try {
					clientSocket.close();
					
				}catch (Exception e1) {
					System.out.println(userName+" socket close");
				}
				
			}
		}
	}

	private String showList(ArrayList e){
		StringBuilder onlinePlayer=new StringBuilder();
		for(int i=0;i<e.size();i++){
			onlinePlayer.append(e.get(i)+"\n");
		}
		return onlinePlayer.toString();
	}

	private int getID(String userName){
		if(FcServer.idMap.containsKey(userName)){
			return FcServer.idMap.get(userName);
		}else {
			FcServer.idAcc=FcServer.idAcc+1;
			FcServer.idMap.put(userName,FcServer.idAcc);
			return FcServer.idMap.get(userName);

		}
	}







}
