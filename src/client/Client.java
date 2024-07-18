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
	private ArrayList<Player> people= new ArrayList<Player>(5);
	
	private JPanel infoPanels=new JPanel();
	private GamePanel gamePanel=new GamePanel('0',people);
	
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
		try {
			System.out.println("in:"+player);
			client.playerOut.writeObject(player);
			client.playerOut.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//client.init();
		
		Hall hall=new Hall();
		boolean outJustOnes=true;
		while(true) {
			for(int hallth=0;hallth<=16;hallth++) {
				if(outJustOnes&&(Hall.room[hallth]==1)) {
					System.out.println("我告诉他{hall:"+hallth+"}");
					client.toServer("{hall:"+hallth+"}");
					outJustOnes=false;
					break;
				}
			}
			if(client.in) {
				break;
			}else {
				System.out.println("Hall wait");
				//System.out.println(client.in);
			}
		}
		
		
		System.out.println("进来了");
		ObjectInputStream playerIn;
		Player playerAnother;
		Object obj;
		//client.init();
		/*从文件中读取其他用户的信息*/
		try {
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
		}
		System.out.println("玩家数量："+client.people.size());
		Thread paintAgain=new Thread();
	}
	public void toServer(String type) {
		String message= "{}";
		if(type.equals("join")) {
			message="{join:"+people.get(0).getName()+"}";
		}else if(type.equals("attack1")) {
			message="{attack1:"+people.get(0).getName()+"}";
		}else if(type.equals("attack2")) {
			message="{attack2:"+people.get(0).getName()+"}";
		}else {
			/*直接传出*/
			serverOut.println(type);
			return;
		}
		serverOut.println(message);
	}
	public void init() {
		gamePanel.setSize(new Dimension(1300,800));
		this.getContentPane().add(gamePanel,BorderLayout.CENTER);
		/*InfoPanel 在右侧展示信息*/
		infoPanels.setLayout(new GridLayout(5,1));
		infoPanels.setPreferredSize(new Dimension(200,100));
		infoPanels.setBorder(new BevelBorder(BevelBorder.RAISED));
		
		for(Player player:people) {
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
		}
		
		this.getContentPane().add(infoPanels,BorderLayout.EAST);
		this.setVisible(true);
	}
	
	public boolean couldMove(char orient) {
		int x=people.get(0).getX();
		int y=people.get(0).getY();
		boolean die=drown(x,y);
		if(die) {
			people.get(0).setHeart(0);
			people.remove(0);
			repaint();
		}
		if(orient=='w') {
			if(y<50)
				return false;
		}else if(orient=='a') {
			if(x<0)
				return false;
		}else if(orient=='s') {
			//650
			//270~580
			if(y>650)
				return false;
		}else if(orient=='d'){
			//1000
			//550~750
			if(x>1000)
				return false;
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
				System.out.println("w1Right");
				people.get(0).addY();
				gamePanel.setX(people.get(0).getX());
				gamePanel.setY(people.get(0).getY());
				toServer("{motive:1,Name:"+people.get(0).getName()+",x:"+people.get(0).getX()+",y:"+people.get(0).getY());
				/*The method is right*/
				/*In fact , the position of people is changing*/
				/*
				 * System.out.println(people.get(0).getX());
				 * System.out.println(people.get(0).getY());
				 */
			}
			gamePanel.setstatus('w');
			gamePanel.repaint();
		}else if(code==65) {
			if(couldMove('a')) {
				people.get(0).minusX();
				gamePanel.setX(people.get(0).getX());
				gamePanel.setY(people.get(0).getY());
			}
			gamePanel.setstatus('a');
			gamePanel.repaint();
		}else if(code==83) {
			if(couldMove('s')) {
			people.get(0).minusY();
			gamePanel.setX(people.get(0).getX());
			gamePanel.setY(people.get(0).getY());
			
			}
			gamePanel.setstatus('s');
			gamePanel.repaint();
		}else if(code==68) {
			if(couldMove('d')) {
			people.get(0).addX();
			gamePanel.setX(people.get(0).getX());
			gamePanel.setY(people.get(0).getY());
			
			}
			gamePanel.setstatus('d');
			gamePanel.repaint();
		}else if(code==74){
			gamePanel.setX(people.get(0).getX());
			gamePanel.setY(people.get(0).getY());
			gamePanel.setBehaviour('j');
			gamePanel.bullets.add(new Bullet(people.get(0).getX(),people.get(0).getY(),10,people));
			toServer("attack1");
			gamePanel.repaint();
		}else if(code==75) {
			gamePanel.setX(people.get(0).getX());
			gamePanel.setY(people.get(0).getY());
			gamePanel.setBehaviour('k');
			gamePanel.bullets.add(new Bullet(people.get(0).getX(),people.get(0).getY(),20,people));
			toServer("attack2");
			gamePanel.repaint();
		}else if(code==85){//key u
			gamePanel.setX(people.get(0).getX());
			gamePanel.setY(people.get(0).getY());
			gamePanel.setBehaviour('u');
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
		@Override
		public void run(){
			try {
	            String message;
	            while ((message = serverIn.readLine()) != null) {
	            	String allMessage=message.substring(1, message.length()-1);
	            	String[] content=allMessage.split(",");
	            	for(String subContent:content) {
	            		String type=subContent.split(":")[0];
	            		if(subContent.split(":").length==1) {
	            			break;
	            		}
	            		String func=subContent.split(":")[1];
	            		if(type.equals("attacked1")) {
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
	            			System.out.println("收到信来");
	            			in=true;
	            		}
	            	}
	            	if(message.equals("{type:END}")) {
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
	private char behaviour;
	private char status;
	public GamePanel(char behaviour,ArrayList<Player> people) {
		this.behaviour=behaviour;
		this.status='s';
		this.people=people;
		/*Timer timer = new Timer(30, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	System.out.println("REPAINT");
                repaint();
            }
        });
        timer.start();*/
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(int i=0;i<1;i++) {
			Image image1;//人物贴图
			Image bg;//背景贴图
			File fileBg=new File("pic//background.png");
			System.out.println("i="+i);
			
			int x=people.get(i).getX();
			int y=people.get(i).getY();
			//Here is also right
			System.out.println("Up:"+x);
			System.out.println("Up:"+y);
			
			File file = new File("pic//"+status+".jpg");
			try {
				image1 = ImageIO.read(file);
				bg=ImageIO.read(fileBg);
				g.drawImage(bg,0, 0, this);
				System.out.println("Last:"+x);
				System.out.println("Last:"+y);
				g.drawImage(image1,x, y, this);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			switch(behaviour) {
				case 'j':
					g.setColor(Color.RED);
					for(Bullet bullet:bullets) {
					g.fillRect(bullet.getX(), bullet.getY(), 10, 10);
					if(status=='w') {
						bullet.jminusY();
					}else if(status=='a') {
						bullet.jminusX();
					}else if(status=='s') {
						bullet.jaddY();
					}else if(status=='d') {
						bullet.jaddX();
					}else {
						bullet.jaddX();
					}
					if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>1000) {
						this.behaviour='0';
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
					if(status=='w') {
						bullet.kminusY();
					}else if(status=='a') {
						bullet.kminusX();
					}else if(status=='s') {
						bullet.kaddY();
					}else if(status=='d') {
						bullet.kaddX();
					}else {}
						int index=bullet.hurt();
						int attack=bullet.getAttack();
						if(index!=-1) {
							people.get(index).minusHeart(attack);
						}
						if(bullet.getX()<0||bullet.getX()>1000||bullet.getY()<0||bullet.getY()>800) {
							this.behaviour='0';
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
							g.drawImage(Up,x-50, y-50, this);
						} catch (IOException e) {}
					}
					this.behaviour='0';
					repaint();
					break;
				default:
					break;
			}
		}
	}
	public void setX(int x) {
		this.people.get(0).setX(x);
	}
	public void setY(int y) {
		this.people.get(0).setY(y);
	}
	public void setBehaviour(char behaviour) {
		this.behaviour=behaviour;
	}
	public void setstatus(char status) {
		this.status=status;
	}
}
