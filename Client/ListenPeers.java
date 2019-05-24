/*
COMP90020 project
group01
Xingchen li   935256
Jingjing Shan 743343
Changda Jiang 879725
Qianfan Chen  754824
 */

package Client;
import Server.FcServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.swing.JOptionPane;

/*
For the leader : the listener does nothing but heartbeat detection for peers ;
For each client, there are three listeners for three peers,
The listener for leader is used for update game state,
The listener for peer is used for election.
*/

public class ListenPeers extends Thread{
    private Socket tryP2p=null;
    private DataInputStream in=null;
    private DataOutputStream out=null;
    private JSONObject tempGS=new JSONObject();




    public ListenPeers(DataOutputStream out, DataInputStream in, Socket tryP2p)
    {
        this.in = in;
        this.tryP2p=tryP2p;
        this.out=out;

    }


    public void run()
    {
        try
        {

            JSONParser parser=new JSONParser();

            while (!tryP2p.isClosed())
            {
                if (in.available() > 0)
                {
                    tempGS= (JSONObject)parser.parse(in.readUTF());
                    int incomingID=Integer.parseInt(tempGS.get("incomingID").toString());
                    int incomingLeader=Integer.parseInt(tempGS.get("leaderID").toString());


                    if(incomingID!=FCclient.localID&&!FCclient.id_Socket.containsValue(tryP2p)){
                        FCclient.id_Socket.put(incomingID,tryP2p);
                    }

                    System.out.println(FCclient.id_Socket);

                    if(FCclient.localID!=FCclient.leaderID){

                        // receive the coordinator msg
                        if(tempGS.get("Election").toString().equals("no")){
                            if(tempGS.get("finishElection").toString().equals("yes")){
                                FCclient.leaderID=incomingLeader;
                                FCclient.startElection=0;
                                FCclient.catchFailure=0;
                            }
                        }

                        System.out.println("I'm "+FCclient.localID+" ,I got msg from "+incomingID);

                        if(incomingID==FCclient.leaderID){
                            GameUI.gameState=tempGS;
                            FCclient.currentTurn=Integer.valueOf(GameUI.gameState.get("Turn").toString());
                            FCclient.diceInfo=GameUI.gameState.get("diceInfo").toString();
                            GameUI.result1.setText(FCclient.diceInfo);
                            GameUI.txtWhosTurn.setText(GameUI.getWhoTurn());
                            GameUI.txtWhosLeader.setText(GameUI.getWhoLeader());
                        	if(!GameUI.gameState.get("Winner").toString().equals("null")) {
                            	JOptionPane.showMessageDialog(null, GameUI.gameState.get("Winner").toString() + " is winner");
                            	System.exit(0);

                        	}

                            GameUI.setBoard();
                            System.out.println("listen peers here");
                            System.out.println(GameUI.gameState);

                        }
                        // Election process
                        else if(incomingID!=FCclient.leaderID){
                            // if the higher id process received election msg from lower id process,
                            // then, he starts election and send back a "alive" msg to the lower id process
                            if(incomingID<FCclient.localID&&tempGS.get("Election").toString().equals("yes")){
                                System.out.println(incomingID+" wants to be the leader, but my id is "+FCclient.localID
                                        +"\nI want to be the leader!");
                                FCclient.id_Socket.remove(FCclient.leaderID);
                                FCclient.startElection=1;
                                GameUI.gameState.put("alive","yes");
                                GameUI.gameState.put("Election","yes");
                                out.writeUTF(GameUI.gameState.toJSONString());
                                out.flush();
                            }
                        }
                    }else if(FCclient.localID==FCclient.leaderID){
                        continue;
                    }

                }
            }

        }
        catch (SocketException e)
        {
            System.out.println("Server is down");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }

    
}
