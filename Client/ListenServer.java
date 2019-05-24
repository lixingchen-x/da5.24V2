/*
COMP90020 project
group01
Xingchen li   935256
Jingjing Shan 743343
Changda Jiang 879725
Qianfan Chen  754824
 */

package Client;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.swing.*;
import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

// The class is used for informing all the players to log in.
public class ListenServer  extends Thread{

    public ListenServer (DataInputStream in)
    {
        FCclient.fromServer = in;

    }


    public void run()
    {
        try
        {

            JSONParser parser=new JSONParser();

            while (!FCclient.client.isClosed())
            {

                if (FCclient.fromServer.available() > 0)
                {


                    FCclient.logState = (JSONObject)parser.parse(FCclient.fromServer.readUTF());
                    String playerlist=FCclient.logState.get("onlinePlayer").toString();
                    FCclient.gaming.textArea.setText(playerlist);
                    FCclient.idMap=FCclient.StringToMap(FCclient.logState.get("id_ip").toString());

                    if(FCclient.logState.get("checkAlive").toString().equals("no")) { 
                    if(FCclient.logState.get("loginStatus").toString().equals("yes")) {
                        new Thread(new OpenServer(FCclient.p2pListening)).start();

                        FCclient.playerNumber=Integer.valueOf(FCclient.logState.get("playerNum").toString());
                        if(FCclient.playerNumber==3){

                            // fix map not consistancy

                            System.out.println(FCclient.idMap);

                            if (FCclient.gaming==null){
                                FCclient.gaming=new GameUI();
                                FCclient.gaming.setVisible(true);
                                FCclient.frame.setVisible(false);
                                playerlist=FCclient.logState.get("onlinePlayer").toString();
                                GameUI.txtWhosTurn.setText(GameUI.getWhoTurn());
                                GameUI.txtWhosLeader.setText(GameUI.getWhoLeader());
                                GameUI.result1.setText(FCclient.diceInfo);
                                FCclient.gaming.textArea.setText(playerlist);

                            }

                        }

                    }
                }


                }
            }

        }
        catch (SocketException e)
        {
            System.out.println("Socket closed because the user typed exit");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


}
