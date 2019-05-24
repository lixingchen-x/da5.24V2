/*
COMP90020 project
group01
Xingchen li   935256
Jingjing Shan 743343
Changda Jiang 879725
Qianfan Chen  754824
 */

package Client;

import java.util.*;

/*
This class is used for maintain all the online players(peers)
*/

public class Peer_manager {

    private static Peer_manager instance;
    private List<P2pConnection> connectedPeers;

    private Peer_manager()
    {
        connectedPeers = new ArrayList<>();
    }

    public static synchronized Peer_manager getInstance()
    {
        if (instance == null)
        {
            instance = new Peer_manager();
        }
        return instance;
    }

    public synchronized void peerConnected(P2pConnection peer) {
        connectedPeers.add(peer);
    }

    public synchronized void peerDisconnected(P2pConnection peer) {
        connectedPeers.remove(peer);
    }

    public synchronized List<P2pConnection> getConnectedPeers() {
        return connectedPeers;
    }

}
