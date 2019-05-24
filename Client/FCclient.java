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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FCclient {

    public static JFrame frame;
    private JTextField textField;
    private JTextField textField_1;
    private JTextField textField_2;

    public static JSONObject logState=new JSONObject();
    public static Socket client = null;
    public static DataInputStream fromServer;
    public static DataOutputStream toServer;
    private static String ipAddress;
    private static int port;
    public static boolean login=false;
    public static String localPlayer;
    public static GameUI gaming=null;

    public static ServerSocket p2pListening = null; // listening other players
    public static int p2pPort=6666;

    public static int leaderID=3;
    public static int localID=-1;
    public static int playerNumber=0;

    public static Map<Integer,String> idMap= new HashMap<Integer,String>();
    public static HashMap<Integer,Socket> id_Socket=new HashMap<>();

    // used for bully algorithm
    public static ArrayList peers =new ArrayList();
    public static int startElection=0;
    public static int catchFailure=0;
    public static int currentTurn=1;

    public static int updateTurnDisplay=0;
    public static String diceInfo="";



    public static void main(String[] args){
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FCclient window = new FCclient();
                    window.frame.setVisible(true);
                    initialJson(logState);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public FCclient() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Log in");
        frame.setBounds(100, 100, 479, 315);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);


        JLabel lblNewLabel = new JLabel("username");
        lblNewLabel.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        lblNewLabel.setBounds(74, 40, 99, 36);
        frame.getContentPane().add(lblNewLabel);

        JLabel lblIp = new JLabel("server ip");
        lblIp.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        lblIp.setBounds(74, 104, 99, 36);
        frame.getContentPane().add(lblIp);

        JLabel lblPort = new JLabel("port");
        lblPort.setFont(new Font("Times New Roman", Font.PLAIN, 20));
        lblPort.setBounds(74, 161, 99, 36);
        frame.getContentPane().add(lblPort);

        textField = new JTextField();
        textField.setBounds(219, 50, 126, 21);
        frame.getContentPane().add(textField);
        textField.setColumns(10);


        textField_1 = new JTextField();
        textField_1.setColumns(10);
        textField_1.setBounds(219, 114, 126, 21);
        frame.getContentPane().add(textField_1);

        textField_2 = new JTextField();
        textField_2.setColumns(10);
        textField_2.setBounds(219, 171, 126, 21);
        frame.getContentPane().add(textField_2);

        JButton btnNewButton = new JButton("connect");
        btnNewButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                logState.put("loginStatus", "no");
                if(!textField.getText().equals("")) {
                    logState.put("userName", textField.getText());
                    try {
                        String ipAddress=InetAddress.getLocalHost().getHostAddress();
                        logState.put("ip", ipAddress);
                    }catch(Exception e1)
                    {
                        e1.printStackTrace();
                    }


                    //read the ip and port
                    if((!textField_1.getText().equals(""))&&(!textField_2.getText().equals(""))) {
                        ipAddress=textField_1.getText();
                        Pattern pattern=Pattern.compile("[0-9]+");
                        Matcher isNumber=pattern.matcher(textField_2.getText());
                        if(isNumber.matches()) {
                            port=Integer.parseInt(textField_2.getText());

                            System.out.println("1");
                            // start to connect to the server.
                            connect(ipAddress,port);


                        }else {
                            JOptionPane.showMessageDialog(
                                    null, "Please input valid port number!", "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            textField_2.setText(null);
                        }

                    }else {
                        JOptionPane.showMessageDialog(
                                null, "Don't forget to input both ip address and port!",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        textField_1.setText(null);
                        textField_2.setText(null);
                    }
                }else {
                    JOptionPane.showMessageDialog(
                            null, "Please input a username!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }


            }
        });
        btnNewButton.setFont(new Font("Arial Black", Font.PLAIN, 15));
        btnNewButton.setBounds(161, 215, 142, 53);
        frame.getContentPane().add(btnNewButton);
    }

    private void connect(String ip,int port) {
        try {
            client = new Socket(InetAddress.getByName(ip), port);
            fromServer = new DataInputStream(client.getInputStream());
            toServer =new DataOutputStream(client.getOutputStream());
            toServer.writeUTF(logState.toJSONString());
            toServer.flush();
            System.out.println("2");
            JSONParser parser=new JSONParser();
            while(!login) {
                if(fromServer.available()>0) {

                    System.out.println("3");
                    logState=(JSONObject)parser.parse(fromServer.readUTF());
                    System.out.println(logState.get("loginStatus").toString());
                    FCclient.localID=Integer.valueOf(FCclient.logState.get("id").toString());
                    System.out.println("local ID is assigned to "+FCclient.localID);
                    if(logState.get("loginStatus").toString().equals("yes")) {
                        localPlayer=textField.getText();
                        System.out.println(localPlayer);

                        new Thread(new ListenServer(fromServer)).start();


                        //new Thread(new ListenPeers(GameUI.fromPeer)).start();
                        String playerlist=FCclient.logState.get("onlinePlayer").toString();

                        System.out.println(idMap);
                        FCclient.gaming.textArea.setText(playerlist);
                        FCclient.playerNumber=Integer.valueOf(FCclient.logState.get("playerNum").toString());
                        if(FCclient.playerNumber==3){
                            idMap=StringToMap(FCclient.logState.get("id_ip").toString());   // fix map not consistancy
                            System.out.println(idMap);
                            if(FCclient.gaming==null){
                                new Thread(new OpenServer(FCclient.p2pListening)).start();
                                FCclient.gaming=new GameUI();
                                FCclient.gaming.setVisible(true);
                                FCclient.frame.setVisible(false);
                                playerlist=FCclient.logState.get("onlinePlayer").toString();

                                GameUI.txtWhosTurn.setText(GameUI.getWhoTurn());
                                GameUI.txtWhosLeader.setText(GameUI.getWhoLeader());
                                FCclient.gaming.textArea.setText(playerlist);
                                GameUI.result1.setText(FCclient.diceInfo);


                                System.out.println("The player begins listening others");
                            }

                        }

                        login=true;




                    }else if(logState.get("loginStatus").toString().equals("no")) {
                        JOptionPane.showMessageDialog(
                                null, "The username is already used, please input another one",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        textField.setText(null);
                        client.close();
                        System.exit(0);
                        login=true;
                    }
                }
            }





        } catch (ConnectException e1) {
            JOptionPane.showMessageDialog(
                    null, "The server is not online.\nPlease try again later :)",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(
                    null, "Please input a valid ip address!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            //textField_1.setText(null);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private static void initialJson(JSONObject aJson) {
        aJson.put("loginStatus", "no");
    }

    public static Map<Integer,String> StringToMap(String str1){
        Map <Integer,String> map=new HashMap<Integer,String>();
        String str= str1.substring(1,str1.length()-1);
        StringTokenizer st = new StringTokenizer(str,"=, \":");
        while(st.hasMoreTokens()){
            map.put(Integer.parseInt(st.nextToken()),st.nextToken());
        }
        return map;
    }
}
