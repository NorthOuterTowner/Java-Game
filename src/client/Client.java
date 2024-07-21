package client;

/*
 * Client.java
 * |
 * |--Client.class
 * |	`-RemoteReader.class
 * |
 * `--GamePanel.class
 */

import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import client.info.*;
import client.run.*;
import server.Server;
import util.MailConst;
import util.MailSenderUtil;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import client.hall.*;

public class Client extends JFrame implements KeyListener{
	
	private static final int drownMinX=830;
	private static final int drownMinY=370;
	private static final int drownMaxY=670;
	
	private static final int port=4450;
	private boolean livePlayer2=true;
	private boolean livePlayer3=true;
	private boolean in=false;
	
	private Socket connection;
	private PrintWriter serverOut;
	private BufferedReader serverIn;
	
	//用于绘制每个人的位置，其中people[0]为client端玩家
	//3人对战
	private ArrayList<Player> people= new ArrayList<Player>(3);
	private Player waitPlayer;
	private boolean addNewPlayer=true;
	//private JPanel infoPanels=new JPanel();
	private GamePanel gamePanel=new GamePanel(people/*,infoPanels*/);
	
	public ArrayList<Player> getPeople(){
		return this.people;
	}
	
	public static void main(String[] args) {
		Client client=new Client();
		client.setVisible(false);
		client.setName("Fighting");
		
		//Sign Part
		Sign sign=new Sign();
		while(true) {
			if(sign.getStatus()) {
				System.out.println("Success");
				break;
			}else {
				System.out.println("Wait...");
			}
		}
		client.launch();
		Player player=new Player(sign.getName());//From sign
		client.people.add(player);
		client.toServer("join");
		//大厅判断人数
		Hall hall=new Hall();
		boolean outJustOnes=true;
		while(true) {
			for(int hallth=0;hallth<=16;hallth++) {
				if(outJustOnes&&(Hall.room[hallth]==1)) {
					client.toServer("{hall:"+hallth+"}");
					outJustOnes=false;
					break;
				}
			}
			//System.out.println(client.in);
			if(client.in) {
				hall.close();
				break;
			}else {
				System.out.println("Hall wait");
			}
		}

		client.init();
		//播放背景音乐
		new AudioPlayer().playSound("tst.wav");
	}
	public void toServer(String type) {
		String message= "";
		if(type.equals("join")) {
			message="{join:"+people.get(0).getName()+"}";
		}else if(type.equals("attack1")) {
			message="{attack1:"+people.get(0).getName()+"}";
		}else if(type.equals("attack2")) {
			message="{attack2:"+people.get(0).getName()+"}";
		}else {
			/*直接传出*/
			//hall内容，attacked内容，*heart内容，*die内容
			serverOut.println(type);
			return;
		}
		serverOut.println(message);
	}
	public void init() {
		
		
		
        JMenuBar menuBar = new JMenuBar();
        JMenu aboutMenu = new JMenu("About");
        JMenuItem aboutAuthorItem = new JMenuItem("Author");
        JMenuItem suggestItem = new JMenuItem("Suggest");
        aboutMenu.add(aboutAuthorItem);
        aboutMenu.add(suggestItem);
        menuBar.add(aboutMenu);
        this.setJMenuBar(menuBar);

        aboutAuthorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JFrame authorFrame=new JFrame("Author");
            	authorFrame.getContentPane().add(new JLabel("Author@LRZ"));
            	JButton exButton=new JButton("EXIT");
            	authorFrame.getContentPane().add(exButton);
            	exButton.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent ex) {
            			authorFrame.setVisible(false);
            		}
            	});
            	authorFrame.pack();
            	authorFrame.setVisible(true);
            	
            }
        });
        suggestItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	JFrame suggestFrame=new JFrame("Suggest");
            	//suggestFrame.getContentPane().add(new JLabel("Welcome Giving me suggestion:"));
            	JLabel tipLabel=new JLabel("Welcome Giving me suggestion:");
            	JTextArea suggestArea=new JTextArea(1,10);
            	JButton submitButton=new JButton("SUBMIT");
            	JButton exButton=new JButton("EXIT");
            	JPanel pane=new JPanel();
            	
            	pane.add(tipLabel,BorderLayout.NORTH);
            	pane.add(suggestArea,BorderLayout.CENTER);
            	pane.add(submitButton,BorderLayout.SOUTH);
            	//pane.add(exButton);
            	//suggestFrame.getContentPane().add(suggestArea);
            	//suggestFrame.getContentPane().add(submitButton);
            	suggestFrame.getContentPane().add(pane);
            	exButton.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent ex) {
            			suggestFrame.setVisible(false);
            		}
            	});
            	submitButton.addActionListener(new ActionListener() {
            		public void actionPerformed(ActionEvent ex) {
            			ArrayList<String> emailArray = new ArrayList<>();
            			emailArray.add("lrz08302005@163.com");
            			String addInfo=suggestArea.getText();
            	        MailSenderUtil.sendMailToUserArray(emailArray,MailConst.NOTIFICATION_MAIL_TITLE,MailConst.NOTIFICATION_MAIL_CONTENT+addInfo);
            			//MailSenderUtil.sendMailTOSingleUser(MailConst.MAIL_HOST, MailConst.NOTIFICATION_MAIL_TITLE, MailConst.NOTIFICATION_MAIL_CONTENT);
            		}
            	});
            	suggestFrame.pack();
            	suggestFrame.setVisible(true);
            	
            }
        });
		
		
		
		
		gamePanel.setSize(new Dimension(1300,800));
		this.getContentPane().add(gamePanel,BorderLayout.CENTER);
		
		//this.getContentPane().add(/*infoPanels,*/BorderLayout.EAST);
		this.setVisible(true);
	}
	
	public boolean couldMove(char orient) {
		int x=people.get(0).getX();
		int y=people.get(0).getY();
		boolean die=drown(x,y);
		if(die) {
			people.get(0).setHeart(0);
			toServer("{die:"+people.get(0).getName()+"}");
			lose();
			people.remove(0);
			repaint();
		}
		if(orient=='w') {
			if(y<50) {
				return false;
			}else {
				return Obstacle.stopw(x, y);
			}
		}else if(orient=='a') {
			if(x<0) {
				return false;
			}else {
				return Obstacle.stopa(x, y);
			}
		}else if(orient=='s') {
			if(y>650) {
				return false;
			}else {
				return Obstacle.stops(x, y);
			}
		}else if(orient=='d'){
			if(x>1000) {
				return false;
			}else {
				return Obstacle.stopd(x, y);
			}
		}
		return true;
	}
	private boolean drown(int x,int y) {
		if(x>this.drownMinX&&y<this.drownMaxY&&y>this.drownMinY) {
			return true;
		}
		return false;
	}
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int code=e.getKeyCode();
		
		/*人物的移动*/
		if(code==87) {
			if(couldMove('w')) {
				people.get(0).addY();
				toServer("{motive:"+people.get(0).getName()+":"+people.get(0).getX()+":"+people.get(0).getY()+":w}");
				
				/*Example: {motive:lrz:100:90:w}
				/*The method is right*/
				/*In fact , the position of people is changing*/
				/*
				 * System.out.println(people.get(0).getX());
				 * System.out.println(people.get(0).getY());
				 */
			}
			people.get(0).setStatus('w');
			people.get(0).setBehaviour('w');
			//gamePanel.setstatus('w');
			gamePanel.repaint();
		}else if(code==65) {
			if(couldMove('a')) {
				people.get(0).minusX();
				toServer("{motive:"+people.get(0).getName()+":"+people.get(0).getX()+":"+people.get(0).getY()+":a}");
			}
			people.get(0).setStatus('a');
			people.get(0).setBehaviour('a');
			//gamePanel.setstatus('a');
			gamePanel.repaint();
		}else if(code==83) {
			if(couldMove('s')) {
			people.get(0).minusY();
			toServer("{motive:"+people.get(0).getName()+":"+people.get(0).getX()+":"+people.get(0).getY()+":s}");
			}
			people.get(0).setStatus('s');
			people.get(0).setBehaviour('s');
			gamePanel.repaint();
		}else if(code==68) {
			if(couldMove('d')) {
			people.get(0).addX();
			toServer("{motive:"+people.get(0).getName()+":"+people.get(0).getX()+":"+people.get(0).getY()+":d}");
			}
			people.get(0).setStatus('d');
			people.get(0).setBehaviour('d');
			gamePanel.repaint();
		}else if(code==74){
			gamePanel.bullets.add(new Bullet(people.get(0).getX(),people.get(0).getY(),10,people,people.get(0).getStatus()));
			toServer("attack1");
			people.get(0).setBehaviour('j');
			gamePanel.repaint();
		}else if(code==75) {
			gamePanel.bullets.add(new Bullet(people.get(0).getX(),people.get(0).getY(),20,people,people.get(0).getStatus()));
			toServer("attack2");
			people.get(0).setBehaviour('k');
			gamePanel.repaint();
		}else if(code==85){//key u
			people.get(0).setBehaviour('u');
		}
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void launch() {
	    try {
            connection = new Socket("127.0.0.1", port);
            serverIn = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            serverOut = new PrintWriter(connection.getOutputStream(),true);
        } catch (IOException e) {
        	System.err.println(e.getMessage());
        }
	    
		this.addKeyListener(this);
		this.setSize(new Dimension(1300,800));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
	    RemoteReader r = new RemoteReader();
	    ThreadPool.add(r);
	    //Thread thread=new Thread(r);
	    //thread.start();
	}

	public static void lose() {
		JFrame frame = new JFrame("Background Image Example");
        frame.setSize(1600, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon backgroundImage = new ImageIcon("pic/lose.jpg");
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), null);
            }
        };
        JButton exitButton = new JButton("Exit The Game");
        exitButton.addActionListener(e -> System.exit(0)); 
        exitButton.setBounds(600, 400, 100, 50);
        panel.add(exitButton);
        frame.setContentPane(panel);
        frame.setVisible(true);
	}
	
	
	
	/*Client类的内部类*/
private class RemoteReader implements Runnable{

		public void run(){
			try {
				String message;
	            while ((message = serverIn.readLine()) != null) {
	            	if(message.equals("{null}")) {
	            		continue;
	            	}
	            	//去掉大括号
	            	String allMessage=message.substring(1, message.length()-1);
	            	if(allMessage.startsWith("motive")) {
	            		//deal message in different way
	            		String[] motiveString=allMessage.split(":");
	            		String name=motiveString[1];
	            		int x=Integer.parseInt(motiveString[2]);
	            		int y=Integer.parseInt(motiveString[3]);
	            		char st=motiveString[4].charAt(0);
	            		for(Player player:people) {
	            			if (player.getName().equals(name)) {
	            				player.setX(x);
	            				player.setY(y);
	            				player.setBehaviour(st);
	            				player.setStatus(st);
	            				break;
	            			}
	            		}
	            	}
	            	String[] content=allMessage.split(",");
	            	for(String subContent:content) {
	            		String type=subContent.split(":")[0];
	            		if(subContent.split(":").length==1) {
	            			break;
	            		}
	            		String func=subContent.split(":")[1];
	            		//考虑可能进行的添加
	            		if(type.equals("join")) {
	            			String addName=subContent.split(":")[1];
	            			
	            			/**2 Version
	            			 * for(Player player:people) {
	            				if(!addName.equals(player.getName())) {
	            					waitPlayer=new Player(addName);
	            				}
	            			}
	            			 */	
	            			addNewPlayer=true;
	            			for(Player player:people) {
	            				if(addName.equals(player.getName())) {
	            					addNewPlayer=false;
	            				}
	            			}
	            			if(addNewPlayer) {
	            				waitPlayer=new Player(addName);
	            			}
	            			
	            			if(waitPlayer!=null) {
	            				people.add(waitPlayer);
	            			}
	            		}else if(type.equals("attacked0")) {
	            			for(Player player:people) {
	            				if(func.equals(player.getName())) {
	            					player.minusHeart(1);
	            				}
	            			}
	            		}else if(type.equals("attacked1")) {
	            			for(Player player:people) {
	            				if(func.equals(player.getName())) {
	            					player.minusHeart(10);
	            				}
	            			}
	            		}else if(type.equals("attacked2")) {
	            			for(Player player:people) {
	            				if(func.equals(player.getName())) {
	            					player.minusHeart(20);
	            				}
	            			}
	            		}else if(type.equals("hall")&&(func.equals("-1"))) {
	            			in=true;
	            		}
	            	}
	            	if(message.equals("{END:1}")) {
	            		serverOut.close();
	            		serverIn.close();
	            		connection.close();
	            		System.exit(0);
	            	}
	            }
	        } catch (IOException e) {
	        	System.err.println(e.getMessage());
	        }
		}
	}

class ThreadPool{
	private static ExecutorService executor = Executors.newFixedThreadPool(5);
	public static void add(Runnable task) {
		executor.submit(task);
	}
	
}
private class GamePanel extends JPanel{
	public ArrayList<Bullet> bullets=new ArrayList<Bullet>(20);
	//public JPanel infoPanels;
	public Thread timerThread;
	public GamePanel(ArrayList<Player> people/*,JPanel infoPanels*/) {
		//this.infoPanels=infoPanels;
        
		timerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(30);
                        repaint();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
		/*timer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });*/
		//
		ThreadPool.add(timerThread);
        //timer.start();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(new Font("SansSerif",Font.BOLD,25));
		Player player1=people.get(0);
			if(player1.getHeart()<=0) {
				Client.lose();
				timerThread.interrupt();
				//timerThread.stop();
				//timer.stop();
			}
			g.drawString(" Name:"+player1.getName(), 1150, 50);
			g.drawString(" Heart:"+player1.getHeart(), 1150, 100);
			g.drawString(" Attack:"+player1.getAttack(), 1150, 150);
		
		Image bg;//背景贴图
		Image image1;//人物贴图
		File fileBg=new File("pic//background.png");
		int x0=people.get(0).getX();
		int y0=people.get(0).getY();
			
		File file = new File("pic//"+people.get(0).getStatus()+".jpg");
		try {
			image1 = ImageIO.read(file);
			bg=ImageIO.read(fileBg);
			g.drawImage(bg,0, 0, this);
			g.drawImage(image1,x0, y0, this);
				
			} catch (IOException e) {
				e.printStackTrace();
		}
		for(Bullet bullet:bullets) {
			if(bullet.getAttack()==10) {
				g.setColor(Color.RED);
				g.fillRect(bullet.getX(), bullet.getY(), 10, 10);
			}else {
				g.setColor(Color.BLUE);
				g.fillRect(bullet.getX(), bullet.getY(), 20, 20);
			}
			
			if(bullet.getStatus()=='w') {
				if(!Obstacle.stopw(bullet.getX(), bullet.getY())) {
					toServer("{attacked0:"+people.get(0).getName()+"}");
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jminusY();
				}else {
					bullet.kminusY();
				}
			}else if(bullet.getStatus()=='a') {
				if(!Obstacle.stopa(bullet.getX(), bullet.getY())) {
					toServer("{attacked0:"+people.get(0).getName()+"}");
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jminusX();
				}else {
					bullet.kminusX();
				}
			}else if(bullet.getStatus()=='s') {
				if(!Obstacle.stops(bullet.getX(), bullet.getY()-80)) {
					toServer("{attacked0:"+people.get(0).getName()+"}");
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jaddY();
				}else {
					bullet.kaddY();
				}
			}else if(bullet.getStatus()=='d') {
				if(!Obstacle.stopd(bullet.getX()-80, bullet.getY())) {
					toServer("{attacked0:"+people.get(0).getName()+"}");
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jaddX();
				}else {
					bullet.kaddX();
				}
			}else {
				if(!Obstacle.stopd(bullet.getX(), bullet.getY())) {
					toServer("{attacked0:"+people.get(0).getName()+"}");
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jaddX();
				}else {
					bullet.kaddX();
				}
			}
			if(bullet.getX()<0||bullet.getX()>1200||bullet.getY()<0||bullet.getY()>1000) {
				people.get(0).setBehaviour('0');
				bullets.remove(bullet);
			}
			
			
			//击中其他玩家
			int index=bullet.hurt();
			int attack=bullet.getAttack();
			if(index!=-1) {
				if(attack==10) {
					toServer("{attacked1:"+people.get(index).getName()+"}");
				}else if(attack==20) {
					toServer("{attacked2:"+people.get(index).getName()+"}");
				}
				bullets.remove(bullet);
			}
			
			
			repaint();
			}

		/**Player 2*/
		if(livePlayer2) {
		Player player2=people.get(1);
		//if(player2.getHeart()<=0) {
		//	lose();
		//}
		g.drawString(" Name:"+player2.getName(), 1150, 250);
		g.drawString(" Heart:"+player2.getHeart(), 1150, 300);
		g.drawString(" Attack:"+player2.getAttack(), 1150, 350);
			Image image2;//人物贴图
			int x1=people.get(1).getX();
			int y1=people.get(1).getY();
			
			File file1 = new File("pic//"+people.get(1).getStatus()+".jpg");
			try {
				image2 = ImageIO.read(file1);
				g.drawImage(image2,x1, y1, this);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			/**Player 3*/
		if(livePlayer3) {
			Player player3=people.get(people.size()-1);
			if(player3.getHeart()<=0) {
				lose();
			}
			g.drawString(" Name:"+player3.getName(), 1150, 450);
			g.drawString(" Heart:"+player3.getHeart(), 1150, 500);
			g.drawString(" Attack:"+player3.getAttack(), 1150, 550);
				Image image3;//人物贴图
				int x2=people.get(people.size()-1).getX();
				int y2=people.get(people.size()-1).getY();
				
				File file2 = new File("pic//"+people.get(people.size()-1).getStatus()+".jpg");
				try {
					image3 = ImageIO.read(file2);
					g.drawImage(image3,x2, y2, this);
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		}
}
}