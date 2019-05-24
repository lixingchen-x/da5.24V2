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

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.plaf.basic.BasicArrowButton;

import java.net.*;
import java.io.IOException;


public class GameUI extends JFrame {

	public static JSONObject gameState=new JSONObject();
	public static String user;
	public static int gameSize = 38;
	public static JButton p[] = new JButton[gameSize];
	public static JButton btnDice;
	public static JTextArea result1;
	public static JLabel startPoint;
	public static JLabel endPoint;
	public static JTextField txtWhosTurn;
	public static JTextField txtWhosLeader;
	public static JTextArea textArea = new JTextArea();
	public static JLabel showCont;
	public JPanel contentPane;
	public static int initPosition = 0;
	public static int position = initPosition;


	public GameUI() {
		super("Flying Chess");
		initial_gameState();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		user = FCclient.localPlayer;
		setPane();
		updateBoard();

		setVisible(true);
		one_time_socket();
	}

// set the game board and put buttons and textfileds on it
	public void setPane(){

		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(null);
		setSize(1000,850);
		setResizable(false);

		startPoint = new JLabel("Start");
		startPoint.setBounds(25,30,100,20);
		contentPane.add(startPoint);
		
		endPoint = new JLabel("<-Destination");
		endPoint.setBounds(67,132,100,20);
		contentPane.add(endPoint);
		
		Font f = new Font("Times New Roman", Font.PLAIN, 20);
		Font middleF = new Font("Times New Roman", Font.BOLD, 20);
		Font bigF = new Font("Times New Roman", Font.BOLD, 40);

		result1 = new JTextArea(5,15);
		//result1.setBackground(Color.PINK);
		result1.setBounds(750, 50, 200, 640);
		result1.setEditable(false);


		contentPane.add(result1);

		for(int i = 0; i<9; i++) {
			BasicArrowButton arrow = new BasicArrowButton(BasicArrowButton.EAST);
			arrow.setBounds(60+70*i, 65, 20, 20);
			contentPane.add(arrow);
		}

		for(int i = 0; i<10;i++){
			p[i] = new JButton();
			p[i].setBounds(10+i*70, 50, 50, 50);
			contentPane.add(new BasicArrowButton(BasicArrowButton.EAST));
			contentPane.add(p[i]);
			p[i].setFont(f);
			p[i].setToolTipText("test"+ p[i].getText());
		}

		for(int i =0;i <10;i++) {
			BasicArrowButton arrow = new BasicArrowButton(BasicArrowButton.SOUTH);
			arrow.setBounds(655,100+70*i,20,20);
			contentPane.add(arrow);
		}

		for(int i =10;i<19;i++){
			p[i] = new JButton();
			p[i].setBounds(640, 120+(i-10)*70, 50, 50);
			contentPane.add(p[i]);
			p[i].setFont(f);
			p[i].setToolTipText("test"+p[i].getText());
		}
		for(int i =0;i <9;i++) {
			BasicArrowButton arrow = new BasicArrowButton(BasicArrowButton.WEST);
			arrow.setBounds(60+70*i,765,20,20);
			contentPane.add(arrow);
		}

		for(int i=19;i<29;i++){
			p[i] = new JButton();
			p[i].setBounds(640-(i-19)*70, 750, 50, 50);
			contentPane.add(p[i]);
			p[i].setFont(f);
			p[i].setToolTipText("test"+p[i].getText());
		}

		for(int i =0;i <9;i++) {
			BasicArrowButton arrow = new BasicArrowButton(BasicArrowButton.NORTH);
			arrow.setBounds(25,170+70*i,20,20);
			contentPane.add(arrow);
		}

		for(int i=29;i<38;i++){
			p[i] = new JButton();
			p[i].setBounds(10, 680-(i-29)*70, 50, 50);
			contentPane.add(p[i]);
			p[i].setFont(f);
			p[i].setToolTipText("test"+p[i].getText());
		}

		
        btnDice = new JButton("Dice");
        btnDice.setBounds(300, 500, 100, 100);
        contentPane.add(btnDice);


//This fucntion listens on the action of the dice button
//once its clicked, the user on the board will be moved and
//the updated game state will be send to the leader

		MouseListener clickDice = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.out.println("current turn is "+FCclient.currentTurn);
				if(isMyTurn()) {
					int diceStep = throwDice();
					position = getPosition();
					move(diceStep);

					int newTurn = nextTurn(FCclient.currentTurn);
					gameState.put("Turn",newTurn);
					if(FCclient.localID!=FCclient.leaderID){
						try {
							System.out.println(FCclient.id_Socket);
							DataOutputStream out = new DataOutputStream(FCclient.id_Socket.get(FCclient.leaderID).getOutputStream());
							out.writeUTF(gameState.toJSONString());
							out.flush();
							System.out.println(gameState);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}else if(FCclient.localID==FCclient.leaderID){
						FCclient.updateTurnDisplay=1;
						FCclient.currentTurn=newTurn;
						GameUI.txtWhosTurn.setText(GameUI.getWhoTurn());
						GameUI.txtWhosLeader.setText(GameUI.getWhoLeader());
						gameState.put("Turn",FCclient.currentTurn);
						P2pConnection.sendToAll();
						if(!GameUI.gameState.get("Winner").toString().equals("null")) {
							JOptionPane.showMessageDialog(null, GameUI.gameState.get("Winner").toString() + " is winner");
							System.exit(0);
						}
					}

				} else {
					JOptionPane.showMessageDialog(
							null, "It's not your turn, please wait!", "Error", JOptionPane.ERROR_MESSAGE);
				}



			}
		};


		btnDice.addMouseListener(clickDice);

        txtWhosTurn = new JTextField();
    	txtWhosTurn.setForeground(Color.BLACK);
    	txtWhosTurn.setBounds(80, 200, 300, 50);
    	txtWhosTurn.setFont(bigF);
    	contentPane.add(txtWhosTurn);
    	txtWhosTurn.setColumns(10);
    	txtWhosTurn.setEditable(false);
    	
    	txtWhosLeader = new JTextField();
    	txtWhosLeader.setForeground(Color.BLACK);
    	txtWhosLeader.setBounds(80, 400, 450, 70);
    	txtWhosLeader.setFont(bigF);
    	contentPane.add(txtWhosLeader);
    	txtWhosLeader.setColumns(10);
    	txtWhosLeader.setEditable(false);

		textArea.setBounds(760, 700, 185, 150);
		textArea.setFont(middleF);
		contentPane.add(textArea);

		
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);

				int makeSure=JOptionPane.showConfirmDialog(null, "Do you want to leave this game?",
						"Attention!",JOptionPane.YES_NO_OPTION);
				if(makeSure==0){
					try {
						if(FCclient.localID!=FCclient.leaderID){
							try {
								DataOutputStream out = new DataOutputStream(FCclient.id_Socket.get(FCclient.leaderID).getOutputStream());
								out.writeUTF(gameState.toJSONString());
								out.flush();
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						}else if(FCclient.localID==FCclient.leaderID){
							P2pConnection.sendToAll();
						}

						FCclient.client.close();
						FCclient.fromServer.close();
						FCclient.toServer.close();
						System.exit(0);

					}catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

	}

	
	
//generate a random number between 1-6 as the result of throw dice
	public static int throwDice() {
		int step = (int)(Math.random()*6+1);
		System.out.println(user+ " dice--" + step);
		return step;
	}

//After the dice is clicked, this function will be called
//to let user move on the board
	public static void move(int diceStep){
		int newposition = position +diceStep;
		System.out.println("leader's position is "+position);
		int a = getPosition();
		System.out.println("leader's position by getPosition"+a);

		if (newposition < gameSize) {
			String result = user + " dice--" + diceStep + " ,now in postition " + newposition;
			FCclient.diceInfo=FCclient.diceInfo+"\n"+result;
			GameUI.gameState.put("diceInfo",FCclient.diceInfo);
			result1.setText(FCclient.diceInfo);

			StringTokenizer st = null;
			ArrayList<String> list = new ArrayList<>();

			if(p[newposition - 1].getText()!=null){
				st = new StringTokenizer(p[newposition - 1].getText(),", ");
				while(st.hasMoreTokens()){
					list.add(st.nextToken());
				}
			}

			if (p[newposition - 1].getText() == null) {
				p[newposition - 1].setText(user);
			} else if(list.contains(user)){

			} else{
				p[newposition - 1].setText(p[newposition - 1].getText() + "," + user);
			}

			if(position == 0){
				System.out.println("position is 0000");

			}else{
				p[position-1].setText(null);
			}

			position = newposition;
			updateBoard();
		} else if (newposition >= gameSize) {
			position = newposition;
			System.out.println(user + " is winner");
			result1.setText(user + " is winner");
			gameState.put("Winner", user);
		}
	}

	// get the gameState for all users in the format of <user:position>,
	// the position stored is the actual position on board

	public static void updateBoard() {
		Map<String,String> map = new HashMap<String,String>();
		for(int i=0;i<gameSize;i++) {
			if(!(p[i].getText() == null)) {
				StringTokenizer st = new StringTokenizer(p[i].getText(),",");
				while(st.hasMoreTokens()) {
					map.put(st.nextToken(), Integer.toString(i+1));
				}
			}
		}
		gameState.put("GameBoard",map) ;
	}


	// when getting a game state map, set the gameboard to the gamestate
	public static void setBoard() {
		for(int i=0;i<gameSize;i++){
			p[i].setText(null);
		}

		Map<String,String> map = StringToMap(gameState.get("GameBoard").toString());

		for(String user: map.keySet()) {

			StringTokenizer st = null;
			ArrayList<String> b = new ArrayList<>();
			if(p[Integer.parseInt(map.get(user))-1].getText()!=null){
				st = new StringTokenizer(p[Integer.parseInt(map.get(user))-1].getText(),", ");
				while(st.hasMoreTokens()){
					b.add(st.nextToken()) ;
				}
			}

			if(p[Integer.parseInt(map.get(user))-1].getText() == null) {
				p[Integer.parseInt(map.get(user))-1].setText(user);
			}else if(b.contains(user)){
				System.out.println("Duplicate user");
			}else{
				p[Integer.parseInt(map.get(user))-1].setText(p[Integer.parseInt(map.get(user))-1].getText()+ "," + user);
			}
		}
		System.out.println("set board funtion game state"+gameState);
	}


	// by searching the gameboard, get the user's own position, if the user is not on the board,
	// the function will return 0
	// the position returned is the actual position on board
	public static int getPosition(){
		for(int i =0;i<gameSize;i++){
			ArrayList<String> userlist = new ArrayList<>();
			if(!(p[i].getText()==null)){
				StringTokenizer st = new StringTokenizer(p[i].getText(),",");
				while(st.hasMoreTokens()){
					userlist.add(st.nextToken());
				}
				if(userlist.contains(user)) {
					position = i+1;
					return position;
				}
			}
		}
		return 0;
	}



	public static void one_time_socket () {
		int port= 6666;

		for (Map.Entry<Integer,String> entry: FCclient.idMap.entrySet()) {
			int temp=entry.getKey();
			int temp2=FCclient.localID;
			if (temp!=temp2) {
				try{
					Socket tryP2P=null;
					tryP2P=new Socket (InetAddress.getByName(entry.getValue()),port);
					DataOutputStream out=new DataOutputStream(tryP2P.getOutputStream());
					DataInputStream in=new DataInputStream(tryP2P.getInputStream());
					System.out.println("connect with: "+entry.getValue());
					gameState.put("game",1);
					// The first assumption of the bully algorithm:
					// "Each node knows identifiers of all other nodes"
					// So, for the game for four players, the first leader id is 3 (0,1,2,3).
					//gameState.put("leaderID",4);
					gameState.put("peerID",FCclient.localID);
					gameState.put("incomingID",FCclient.localID);
					out.writeUTF(gameState.toJSONString());
					out.flush();
					System.out.println("flush successfully");
					new Thread(new ListenPeers(out,in,tryP2P)).start();

				}catch (SocketException e1){
					System.out.println("socket exception");
				}catch (IOException e2) {
					System.out.println("IOexception");
				}
			}
		}

	}


	//after play one turn, writing the game state into JSON format information
	public static Map<String,String> StringToMap(String playerIP){
		Map <String,String> map=new HashMap<String,String>();
		String str= playerIP.substring(1,playerIP.length()-1);
		StringTokenizer st = new StringTokenizer(str,"=, \":");
		while(st.hasMoreTokens()){
			map.put(st.nextToken(),st.nextToken());
		}
		return map;
	}


//This function will return turn if its this user's turn and return false if its not himself's turn
    public static Boolean isMyTurn(){
	    Boolean tmp=false;
	    if(FCclient.currentTurn==FCclient.localID){
	        tmp=true;
        }
        return tmp;
    }




//This function returns the user's turn
    public static String getWhoTurn(){
        String whoTurn=null;
        whoTurn="Player " + FCclient.currentTurn + "'s turn";
        return whoTurn;
    }



//This function will return the leader id
	public static String getWhoLeader() {
		String whoLeader = null;
		if(FCclient.id_Socket.size()!=0) {
			int leader = FCclient.leaderID;
			whoLeader = "Coordinator is player" +leader;
			return whoLeader;
		}
		return whoLeader;
		
	}

//This function initialize the game state. after game started, the game state will be updated and sent.
	public void initial_gameState() {
	    Map<String,String> iniGB = new HashMap<>();
	    //iniGB = initialGameBoard();
		gameState.put("leaderID", "3");
		gameState.put("HeartBeat","no");
		//gameState.put("localID",null);
		gameState.put("Turn", "1");
		gameState.put("GameBoard","null");
		gameState.put("Winner", "null");
		gameState.put("alive","null");
		gameState.put("Election","no");
		gameState.put("finishElection","null");
		gameState.put("diceInfo","^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^ ^_^");
	}

	 public static int nextTurn(int myTurn){
		    int nextTurn=-1;
		    if(myTurn==1){
		        if(FCclient.idMap.containsKey(2)){
	                nextTurn=2;
	            }else if(!FCclient.idMap.containsKey(2)&&FCclient.idMap.containsKey(3)){
	                nextTurn=3;
	            }
	        }
	        else if(myTurn==2){
	            if(FCclient.idMap.containsKey(3)){
	                nextTurn=3;
	            }else if(!FCclient.idMap.containsKey(3)&&FCclient.idMap.containsKey(1)){
	                nextTurn=1;
	            }
	        }
	        else if(myTurn==3){
	            if(FCclient.idMap.containsKey(1)){
	                nextTurn=1;
	            }else if(!FCclient.idMap.containsKey(1)&&FCclient.idMap.containsKey(2)){
	                nextTurn=2;
	            }
	        }
	        return nextTurn;
	    }

}
