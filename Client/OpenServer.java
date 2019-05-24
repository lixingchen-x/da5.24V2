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
import java.net.Socket;
import java.net.SocketException;

/*
This class is used for every player to start a server thread,
and get ready for being a leader.
*/

public class OpenServer extends Thread{

    public OpenServer (ServerSocket p2pListening) {
        FCclient.p2pListening=p2pListening;
    }


    public void run()
    {
        if(FCclient.p2pListening==null){
            // Each client start a server process for p2p connection
            try{
                FCclient.p2pListening = new ServerSocket(FCclient.p2pPort);

                while (true){
                    // ready for other players to connect me.
                    Socket p2pSocket=FCclient.p2pListening.accept();
                    System.out.println("Ready for others to connect");
                    P2pConnection p2pConnection=new P2pConnection(p2pSocket);
                    p2pConnection.start();
                }


            }catch (Exception e1){

                e1.printStackTrace();
            }
        }


}}
