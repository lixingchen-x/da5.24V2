/*
COMP90020 project
group01
Xingchen li   935256
Jingjing Shan 743343
Changda Jiang 879725
Qianfan Chen  754824
 */

package Server;


import java.util.*;

public class server_manager {
	
	private static server_manager instance;
	public static List<Client_connection> connectedClients;
	
	private server_manager()
	{
		connectedClients = new ArrayList<>();
	}
	
	public static synchronized server_manager getInstance()
	{
		if (instance == null)
		{
			instance = new server_manager();
		}
		return instance;
	}
	
	public synchronized void clientConnected(Client_connection client) {
		connectedClients.add(client);
	}
	
	public static synchronized void clientDisconnected(Client_connection client) {
		connectedClients.remove(client);
	}
	
	public synchronized List<Client_connection> getConnectedClients() {
		return connectedClients;
	}

}
