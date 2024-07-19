package client;

//396-397 is important of 3 people

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
import client.info.*;
import client.run.*;
import server.Server;
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
	
	private boolean in=false;
	
	private Socket connection;
	private PrintWriter serverOut;
	private BufferedReader serverIn;
	private ObjectOutputStream playerOut;
	
	//用于绘制每个人的位置，其中people[0]为client端玩家
	//3人对战
	private ArrayList<Player> people= new ArrayList<Player>(3);
	private Player waitPlayer;
	private boolean addNewPlayer=true;
	private JPanel infoPanels=new JPanel();
	private GamePanel gamePanel=new GamePanel(people,infoPanels);
	
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
		//client.init();
		
		/*将本用户写入文件中*/
		/*try {
			System.out.println("in:"+player);
			client.playerOut.writeObject(player);
			client.playerOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//client.init();
		
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
				//System.out.println("Hall wait");
				//System.out.println(client.in);
			}
		}
		
		ObjectInputStream playerIn;
		Player playerAnother;
		Object obj;
		//Use String sent by Server build object
		
		/*从文件中读取其他用户的信息*/
		/*try {
			FileInputStream fs=new FileInputStream("player.dat");
			playerIn = new ObjectInputStream(fs);
			while((obj=playerIn.readObject())!=null) {//invalid type code: AC
				playerAnother =(Player)obj;
				if (!playerAnother.getName().equals(player.getName())) {
					client.people.add(playerAnother);
				}
				fs.skip(4);
			}
			playerIn.close();
			//client.init();
		}catch(EOFException e) {
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			client.init();
			new AudioPlayer().playSound("tst.wav");
		}*/
		client.init();
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
			//hall内容，attacked内容
			serverOut.println(type);
			return;
		}
		serverOut.println(message);
	}
	public void init() {
		gamePanel.setSize(new Dimension(1300,800));
		this.getContentPane().add(gamePanel,BorderLayout.CENTER);
		/*InfoPanel 在右侧展示信息*/
		/*infoPanels.setLayout(new GridLayout(5,1));
		infoPanels.setPreferredSize(new Dimension(200,100));
		infoPanels.setBorder(new BevelBorder(BevelBorder.RAISED));*/
		
		/*for(Player player:people) {
			JPanel infoPanel=new JPanel();
			infoPanel.setLayout(new GridLayout(3,1));
			infoPanel.setPreferredSize(new Dimension(200,100));
			infoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			JLabel name=new JLabel("Name:"+player.getName());
			name.setFont(new Font("SansSerif", Font.BOLD, 25));
			JLabel heart=new JLabel("Heart:"+player.getHeart());
			heart.setFont(new Font("SansSerif", Font.BOLD, 25));
			JLabel attack=new JLabel("Attack:"+player.getAttack());
			attack.setFont(new Font("SansSerif", Font.BOLD, 25));
			infoPanel.add(name);
			infoPanel.add(heart);
			infoPanel.add(attack);
			infoPanels.add(infoPanel);
		}*/
		
		this.getContentPane().add(infoPanels,BorderLayout.EAST);
		this.setVisible(true);
	}
	
	public boolean couldMove(char orient) {
		int x=people.get(0).getX();
		int y=people.get(0).getY();
		//System.out.println("Thjis heart:"+people.get(0).getHeart());
		boolean die=drown(x,y);
		if(die) {
			people.get(0).setHeart(0);
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
			//650
			//270~580
			if(y>650) {
				return false;
			}else {
				return Obstacle.stops(x, y);
			}
		}else if(orient=='d'){
			//1000
			//550~750
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
			this.removeAll();
			this.getContentPane().add(new JLabel("You are lose"));
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
				people.get(0).setX(people.get(0).getX());
				people.get(0).setY(people.get(0).getY());
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
				people.get(0).setX(people.get(0).getX());
				people.get(0).setY(people.get(0).getY());
				toServer("{motive:"+people.get(0).getName()+":"+people.get(0).getX()+":"+people.get(0).getY()+":a}");
			}
			people.get(0).setStatus('a');
			people.get(0).setBehaviour('a');
			//gamePanel.setstatus('a');
			gamePanel.repaint();
		}else if(code==83) {
			if(couldMove('s')) {
			people.get(0).minusY();
			people.get(0).setX(people.get(0).getX());
			people.get(0).setY(people.get(0).getY());
			toServer("{motive:"+people.get(0).getName()+":"+people.get(0).getX()+":"+people.get(0).getY()+":s}");
			}
			people.get(0).setStatus('s');
			people.get(0).setBehaviour('s');
			gamePanel.repaint();
		}else if(code==68) {
			if(couldMove('d')) {
			people.get(0).addX();
			people.get(0).setX(people.get(0).getX());
			people.get(0).setY(people.get(0).getY());
			toServer("{motive:"+people.get(0).getName()+":"+people.get(0).getX()+":"+people.get(0).getY()+":d}");
			}
			people.get(0).setStatus('d');
			people.get(0).setBehaviour('d');
			//gamePanel.setstatus('d');
			gamePanel.repaint();
		}else if(code==74){
			people.get(0).setX(people.get(0).getX());
			people.get(0).setY(people.get(0).getY());
			gamePanel.bullets.add(new Bullet(people.get(0).getX(),people.get(0).getY(),10,people,people.get(0).getStatus()));
			toServer("attack1");
			people.get(0).setBehaviour('j');
			gamePanel.repaint();
		}else if(code==75) {
			people.get(0).setX(people.get(0).getX());
			people.get(0).setY(people.get(0).getY());
			gamePanel.bullets.add(new Bullet(people.get(0).getX(),people.get(0).getY(),20,people,people.get(0).getStatus()));
			toServer("attack2");
			people.get(0).setBehaviour('k');
			gamePanel.repaint();
		}else if(code==85){//key u
			people.get(0).setX(people.get(0).getX());
			people.get(0).setY(people.get(0).getY());
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
	    
	    
	    	
		try {
			playerOut= new ObjectOutputStream(new FileOutputStream("player.dat",true));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.addKeyListener(this);
		this.setSize(new Dimension(1300,800));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		
	    RemoteReader r = new RemoteReader();
	    Thread thread=new Thread(r);
	    thread.start();
	}

	
	
	/*Client类的内部类*/
	class RemoteReader implements Runnable{

		public void run(){
			try {
	            //
				String message;
				//
	            while ((message = serverIn.readLine()) != null) {
	            	/*for(int z=0;z<10000;z++) {
	            		System.out.println(message+"Client");
	            	}*/
	            	System.out.println(message+"Client");
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
}


class GamePanel extends JPanel{
	public ArrayList<Bullet> bullets=new ArrayList<Bullet>(20);
	public ArrayList<Player> people;
	public JPanel infoPanels;
	public GamePanel(ArrayList<Player> people,JPanel infoPanels) {
		this.people=people;
		this.infoPanels=infoPanels;
		Timer timer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	//System.out.println("Repaint");
                repaint();
            }
        });
        timer.start();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(new Font("SansSerif",Font.BOLD,25));
		Player player1=people.get(0);
			if(player1.getHeart()<=0) {
				lose();
			}
			g.drawString(" Name:"+player1.getName(), 1150, 50);
			g.drawString(" Heart:"+player1.getHeart(), 1150, 100);
			g.drawString(" Attack:"+player1.getAttack(), 1150, 150);
			
		
		/*infoPanels.removeAll();
		for(Player player:people) {
			JPanel infoPanel=new JPanel();
			infoPanel.setLayout(new GridLayout(3,1));
			infoPanel.setPreferredSize(new Dimension(200,100));
			infoPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			JLabel name=new JLabel("Name:"+player.getName());
			name.setFont(new Font("SansSerif", Font.BOLD, 25));
			JLabel heart=new JLabel("Heart:"+(player.getHeart()));
			heart.setFont(new Font("SansSerif", Font.BOLD, 25));
			JLabel attack=new JLabel("Attack:"+player.getAttack());
			attack.setFont(new Font("SansSerif", Font.BOLD, 25));
			infoPanel.add(name);
			infoPanel.add(heart);
			infoPanel.add(attack);
			infoPanels.add(infoPanel);
		}*/
		
		
		Image bg;//背景贴图
		Image image1;//人物贴图
		File fileBg=new File("pic//background.png");
		int x0=people.get(0).getX();
		int y0=people.get(0).getY();
			
		File file = new File("pic//"+people.get(0).getStatus()+".jpg");
		System.out.println(people.get(0).getStatus());
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
					people.get(0).minusHeart(1);
					System.out.println(people.get(0).getHeart());
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jminusY();
				}else {
					bullet.kminusY();
				}
			}else if(bullet.getStatus()=='a') {
				if(!Obstacle.stopa(bullet.getX(), bullet.getY())) {
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jminusX();
				}else {
					bullet.kminusX();
				}
				//bullet.jminusX();
			}else if(bullet.getStatus()=='s') {
				if(!Obstacle.stops(bullet.getX(), bullet.getY()-80)) {
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jaddY();
				}else {
					bullet.kaddY();
				}
				//bullet.jaddY();
			}else if(bullet.getStatus()=='d') {
				if(!Obstacle.stopd(bullet.getX()-80, bullet.getY())) {
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jaddX();
				}else {
					bullet.kaddX();
				}
				//bullet.jaddX();
			}else {
				if(!Obstacle.stopd(bullet.getX(), bullet.getY())) {
					people.get(0).minusHeart(1);
					bullets.remove(bullet);
				}
				if(bullet.getAttack()==10) {
					bullet.jaddX();
				}else {
					bullet.kaddX();
				}
				//bullet.jaddX();
			}
			if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>1000) {
				people.get(0).setBehaviour('0');
				bullets.remove(bullet);
			}
			int index=bullet.hurt();
			int attack=bullet.getAttack();
			if(index!=-1) {
				people.get(index).minusHeart(attack);
			}
			repaint();
			}
			/*switch(people.get(0).getBehaviour()) {
				case 'j':*/
			/*		g.setColor(Color.RED);
					for(Bullet bullet:bullets) {
					g.fillRect(bullet.getX(), bullet.getY(), 10, 10);
					if(bullet.getStatus()=='w') {
						bullet.jminusY();
					}else if(bullet.getStatus()=='a') {
						bullet.jminusX();
					}else if(bullet.getStatus()=='s') {
						bullet.jaddY();
					}else if(bullet.getStatus()=='d') {
						bullet.jaddX();
					}else {
						bullet.jaddX();
					}
					if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>1000) {
						people.get(0).setBehaviour('0');
						bullets.remove(bullet);
					}
					int index=bullet.hurt();
					int attack=bullet.getAttack();
					if(index!=-1) {
						people.get(index).minusHeart(attack);
					}
					repaint();
					}*/
				//	break;
				/*case 'k':*/
				/*	g.setColor(Color.BLUE);
					for(Bullet bullet:bullets) {
					g.fillRect(bullet.getX(), bullet.getY(), 20, 20);
					if(bullet.getStatus()=='w') {
						bullet.kminusY();
					}else if(bullet.getStatus()=='a') {
						bullet.kminusX();
					}else if(bullet.getStatus()=='s') {
						bullet.kaddY();
					}else if(bullet.getStatus()=='d') {
						bullet.kaddX();
					}else {}
						int index=bullet.hurt();
						int attack=bullet.getAttack();
						if(index!=-1) {
							people.get(index).minusHeart(attack);
						}
						if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>800) {
							people.get(0).setBehaviour('0');
							bullets.remove(bullet);
						}
						repaint();
					}
				*/
				/*	break;
				case 'u':
					for(int j=0;j<12;j++) {
						Image Up;
						File fileUp = new File("pic//Level Up Effect Frame"+j+".png");
						try {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {}
							image1 = ImageIO.read(fileUp);
							Up=ImageIO.read(fileUp);
							g.drawImage(Up,x0-50, y0-50, this);
						} catch (IOException e) {}
					}
					people.get(0).setBehaviour('0');
					repaint();
					break;
				default:
					break;
			}	*/
			
		/**Player 2*/
		Player player2=people.get(1);
		/*if(player2.getHeart()<=0) {
			lose();
		}*/
		g.drawString(" Name:"+player2.getName(), 1150, 250);
		g.drawString(" Heart:"+player2.getHeart(), 1150, 300);
		g.drawString(" Attack:"+player2.getAttack(), 1150, 350);
			/**People2*/
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
			switch(people.get(1).getBehaviour()) {
			case 'j':
				g.setColor(Color.RED);
				for(Bullet bullet:bullets) {
				g.fillRect(bullet.getX(), bullet.getY(), 10, 10);
				if(bullet.getStatus()=='w') {
					bullet.jminusY();
				}else if(bullet.getStatus()=='a') {
					bullet.jminusX();
				}else if(bullet.getStatus()=='s') {
					bullet.jaddY();
				}else if(bullet.getStatus()=='d') {
					bullet.jaddX();
				}else {
					bullet.jaddX();
				}
				if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>1000) {
					people.get(1).setBehaviour('0');
					bullets.remove(bullet);
				}
				int index=bullet.hurt();
				int attack=bullet.getAttack();
				if(index!=-1) {
					people.get(index).minusHeart(attack);
				}
				repaint();
				}
				break;
			case 'k':
				g.setColor(Color.BLUE);
				for(Bullet bullet:bullets) {
				g.fillRect(bullet.getX(), bullet.getY(), 20, 20);
				if(bullet.getStatus()=='w') {
					bullet.kminusY();
				}else if(bullet.getStatus()=='a') {
					bullet.kminusX();
				}else if(bullet.getStatus()=='s') {
					bullet.kaddY();
				}else if(bullet.getStatus()=='d') {
					bullet.kaddX();
				}else {}
					int index=bullet.hurt();
					int attack=bullet.getAttack();
					if(index!=-1) {
						people.get(index).minusHeart(attack);
					}
					if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>800) {
						people.get(1).setBehaviour('0');
						bullets.remove(bullet);
					}
					repaint();
				}

				break;
			case 'u':
				for(int j=0;j<12;j++) {
					Image Up;
					File fileUp = new File("pic//Level Up Effect Frame"+j+".png");
					try {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {}
						image1 = ImageIO.read(fileUp);
						Up=ImageIO.read(fileUp);
						g.drawImage(Up,x0-50, y0-50, this);
					} catch (IOException e) {}
				}
				people.get(0).setBehaviour('0');
				repaint();
				break;
			default:
				break;
		}	
			/**Player 3*/
			/*Player player3=people.get(2);
			if(player3.getHeart()<=0) {
				lose();
			}
			g.drawString(" Name:"+player3.getName(), 1150, 450);
			g.drawString(" Heart:"+player3.getHeart(), 1150, 500);
			g.drawString(" Attack:"+player3.getAttack(), 1150, 550);
				Image image3;//人物贴图
				int x2=people.get(2).getX();
				int y2=people.get(2).getY();
				
				File file2 = new File("pic//"+people.get(2).getStatus()+".jpg");
				try {
					image3 = ImageIO.read(file2);
					g.drawImage(image3,x2, y2, this);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				switch(people.get(2).getBehaviour()) {
				case 'j':
					g.setColor(Color.RED);
					for(Bullet bullet:bullets) {
					g.fillRect(bullet.getX(), bullet.getY(), 10, 10);
					if(bullet.getStatus()=='w') {
						bullet.jminusY();
					}else if(bullet.getStatus()=='a') {
						bullet.jminusX();
					}else if(bullet.getStatus()=='s') {
						bullet.jaddY();
					}else if(bullet.getStatus()=='d') {
						bullet.jaddX();
					}else {
						bullet.jaddX();
					}
					if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>1000) {
						people.get(2).setBehaviour('0');
						bullets.remove(bullet);
					}
					int index=bullet.hurt();
					int attack=bullet.getAttack();
					if(index!=-1) {
						people.get(index).minusHeart(attack);
					}
					repaint();
					}
					break;
				case 'k':
					g.setColor(Color.BLUE);
					for(Bullet bullet:bullets) {
					g.fillRect(bullet.getX(), bullet.getY(), 20, 20);
					if(bullet.getStatus()=='w') {
						bullet.kminusY();
					}else if(bullet.getStatus()=='a') {
						bullet.kminusX();
					}else if(bullet.getStatus()=='s') {
						bullet.kaddY();
					}else if(bullet.getStatus()=='d') {
						bullet.kaddX();
					}else {}
						int index=bullet.hurt();
						int attack=bullet.getAttack();
						if(index!=-1) {
							people.get(index).minusHeart(attack);
						}
						if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>800) {
							people.get(2).setBehaviour('0');
							bullets.remove(bullet);
						}
						repaint();
					}

					break;
				case 'u':
					for(int j=0;j<12;j++) {
						Image Up;
						File fileUp = new File("pic//Level Up Effect Frame"+j+".png");
						try {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {}
							image1 = ImageIO.read(fileUp);
							Up=ImageIO.read(fileUp);
							g.drawImage(Up,x0-50, y0-50, this);
						} catch (IOException e) {}
					}
					people.get(0).setBehaviour('0');
					repaint();
					break;
				default:
					break;
			}	
					*/
			
			
			
		}
	private void lose() {
		// TODO Auto-generated method stub
		this.setVisible(false);
		JFrame loseFrame=new JFrame();
		loseFrame.setSize(new Dimension(1000,1000));
		loseFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JLabel die=new JLabel("You are die");
		die.setFont(new Font("SansSerif",Font.BOLD,100));
		loseFrame.getContentPane().add(die);
		
		loseFrame.pack();
		loseFrame.setVisible(true);
	}
}