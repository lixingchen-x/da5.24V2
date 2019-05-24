/*
COMP90020 project
group01
Xingchen li   935256
Jingjing Shan 743343
Changda Jiang 879725
Qianfan Chen  754824
 */

package Client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.*;

import javax.swing.JOptionPane;

import Server.FcServer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;



/*
This class is used for each client acts as a server to listen.
If the current client is not the leader, the thread will sleep.
*/

public class P2pConnection extends Thread{

    private Socket p2pSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private int peerID;
    private JSONObject tempGS=new JSONObject();
    public static List<P2pConnection> peers;


    public P2pConnection(Socket p2pSocket)
    {
        try
        {
            this.p2pSocket = p2pSocket;
            in = new DataInputStream(p2pSocket.getInputStream());
            out = new DataOutputStream(p2pSocket.getOutputStream());


        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run()
    {
        try
        {
            JSONParser parser=new JSONParser();
            while (!p2pSocket.isClosed())
            {
                if (in.available() > 0)
                {

                    Peer_manager.getInstance().peerConnected(this); // to solve

                    tempGS= (JSONObject)parser.parse(in.readUTF());


                    //get the peer's id, bind it with the connection.
                    int ID=Integer.valueOf(tempGS.get("incomingID").toString());
                    setPeerID(ID);
                    if(!FCclient.peers.contains(ID)&&ID!=FCclient.localID){
                        FCclient.peers.add(ID);
                    }

                    int incomingLeader=Integer.valueOf(tempGS.get("leaderID").toString());



                    if(incomingLeader==FCclient.localID){
                        int tempTurn=Integer.valueOf(tempGS.get("Turn").toString());

                        if(tempTurn==GameUI.nextTurn(peerID)){

                            //As the leader, when he received the msg from the player of the latest turn
                            // if he has not clicked the dice, he can continue update the gameState
                            // while, if he has clicked the dice, he has to ignore the msg because it will change the
                            // display of turn

                           receive : if(tempTurn!=FCclient.localID){
                                FCclient.updateTurnDisplay=0;
                                FCclient.currentTurn=tempTurn;
                                GameUI.txtWhosTurn.setText(GameUI.getWhoTurn());
                                GameUI.txtWhosLeader.setText(GameUI.getWhoLeader());
                                GameUI.gameState=tempGS;
                                FCclient.diceInfo=GameUI.gameState.get("diceInfo").toString();
                                GameUI.result1.setText(FCclient.diceInfo);
                                GameUI.gameState.put("Turn",FCclient.currentTurn);

                                // used for GameUI to choose the right listenLeader thread.
                                GameUI.gameState.put("incomingID",FCclient.localID);
                                if(!GameUI.gameState.get("Winner").toString().equals("null")) {
                                    sendToAll();
                                    JOptionPane.showMessageDialog(null, GameUI.gameState.get("Winner").toString() + " is winner");
                                    System.exit(0);
                                }

                                // The receiver is the leader
                                // Now he can put command to gameState
                                GameUI.setBoard();
                                System.out.println("P2P connection here");
                                System.out.println(GameUI.gameState);

                                sendToAll();
                            }else if((tempTurn==FCclient.localID)){
                                if(FCclient.updateTurnDisplay==1){
                                    break receive;
                                }else if(FCclient.updateTurnDisplay==0){
                                    FCclient.currentTurn=tempTurn;
                                    GameUI.txtWhosTurn.setText(GameUI.getWhoTurn());
                                    GameUI.txtWhosLeader.setText(GameUI.getWhoLeader());
                                    GameUI.gameState=tempGS;
                                    FCclient.diceInfo=GameUI.gameState.get("diceInfo").toString();
                                    GameUI.result1.setText(FCclient.diceInfo);
                                    GameUI.gameState.put("Turn",FCclient.currentTurn);
                                    GameUI.gameState.put("incomingID",FCclient.localID);
                                    if(!GameUI.gameState.get("Winner").toString().equals("null")) {
                                        sendToAll();
                                        JOptionPane.showMessageDialog(null, GameUI.gameState.get("Winner").toString() + " is winner");
                                        System.exit(0);
                                    }
                                    GameUI.setBoard();
                                    System.out.println("P2P connection here");
                                    System.out.println(GameUI.gameState);

                                    sendToAll();
                                }
                            }



                        }


                    }else if(incomingLeader!=FCclient.localID){

                        if(tempGS.get("Election").toString().equals("yes")){
                            if(tempGS.get("alive").toString().equals("yes")){
                                // a higher id process is alive,
                                // just wait for coordinator msg
                                FCclient.startElection=0;
                            }
                        }
                        else if(tempGS.get("Election").toString().equals("no")){
                            if(tempGS.get("finishElection").toString().equals("yes")){
                                FCclient.leaderID=Integer.valueOf(tempGS.get("leaderID").toString());
                                FCclient.startElection=0;
                                FCclient.catchFailure=0;
                            }
                        }
                    }


                }

                if(FCclient.localID==FCclient.leaderID){
                    write(GameUI.gameState);
                    sleep(1500);
                }else if(FCclient.localID!=FCclient.leaderID){
                    if(peerID==FCclient.leaderID){

                        // Start the heartbeat to monitor if leader is down
                        try {
                            out.writeUTF(GameUI.gameState.toJSONString());
                            out.flush();
                            sleep(1500);
                        }catch(Exception e) {
                            if(FCclient.catchFailure==0){
                                Peer_manager.getInstance().peerDisconnected(this);
                                FCclient.id_Socket.remove(FCclient.leaderID);
                                System.out.println("current leader: " + FCclient.leaderID +
                                        " is down, \nI want to be the leader");

                                // The process that detected the leader was down starts election
                                if((FCclient.leaderID-FCclient.localID)==1){
                                    sendCoordinatorMsg();
                                    System.out.println("My id is "+FCclient.localID+". I have the biggest id!" +
                                            "\nI will be the leader!");
                                }else if((FCclient.leaderID-FCclient.localID)>1){
                                    sendToHigherID();
                                    System.out.println("Send election msg to higher id processes!");
                                }
                                FCclient.catchFailure=1;
                            }

                        }
                    }else {
                        write(GameUI.gameState);
                        sleep(1500);
                    }
                }

                // used for higher id process to start election
                if(FCclient.startElection==1){
                    if((FCclient.leaderID-FCclient.localID)==1){
                        sendCoordinatorMsg();
                    }else if((FCclient.leaderID-FCclient.localID)>1){
                        sendToHigherID();
                    }
                    FCclient.startElection=0;
                }

            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private synchronized void write(JSONObject gameinfo)
    {
        GameUI.gameState.put("incomingID",FCclient.localID);
        try
        {
            out.writeUTF(gameinfo.toJSONString());
            out.flush();
        }
        catch (IOException e) {
            FCclient.id_Socket.remove(peerID);
            System.out.println("The destination is down!");
        }
    }


    private void setPeerID(int peerID){
        this.peerID=peerID;
    }
    private int getPeerID(){
        return peerID;
    }

    public static void sendToAll(){
        GameUI.gameState.put("incomingID",FCclient.localID);
        peers=Peer_manager.getInstance().getConnectedPeers();
        for(P2pConnection peer : peers) {
            peer.write(GameUI.gameState);
        }

    }
    public void sendCoordinatorMsg(){
        FCclient.leaderID=FCclient.localID;
        GameUI.txtWhosLeader.setText(GameUI.getWhoLeader());
        GameUI.gameState.put("incomingID",FCclient.localID);
        GameUI.gameState.put("leaderID",FCclient.localID);
        GameUI.gameState.put("Election","no");
        GameUI.gameState.put("finishElection","yes");
        FCclient.catchFailure=0;
        peers=Peer_manager.getInstance().getConnectedPeers();
        for(P2pConnection peer : peers) {
            peer.write(GameUI.gameState);
        }

    }

    public void sendToHigherID(){
        if(FCclient.localID<peerID){
            try {
                GameUI.gameState.put("incomingID",FCclient.localID);
                GameUI.gameState.put("Election", "yes");
                out.writeUTF(GameUI.gameState.toJSONString());
                out.flush();

            }catch(Exception e) {
                Peer_manager.getInstance().peerDisconnected(this);
                FCclient.id_Socket.remove(peerID);
                System.out.println("higher ID process is also down");

                if(higherAllDie(FCclient.peers)){
                    FCclient.catchFailure=0;
                    System.out.println("I'm the leader now");
                    sendCoordinatorMsg();
                }

            }
        }
    }

    public boolean higherAllDie(ArrayList peers){
        Collections.sort(peers);
        int maxID=(int)peers.get(peers.size()-1);
        if(FCclient.localID>maxID){
            return true;
        }else
            return false;
    }




}
