package server;
//Line 113 Control number of people which could start the game
/*Server class
 * Link with Client
 */
import java.awt.*;
import java.time.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;

import client.*;

public class Server {
	private PrintWriter  out;
	private ArrayList<PrintWriter> clients=new ArrayList<PrintWriter>(100);
	private final static int port=4450;
    
	public JTextArea textclients=new JTextArea();
    public JTextArea textinfo=new JTextArea();
    public JPanel leftPanel = new JPanel();
    public JPanel rightPanel = new JPanel();
    
    /*用String[]存入每个加入信息*/
    private String[] joinMessageCache=new String[3];
    private ServerSocket serverSocket;
    
    public int hallClient=0;
    
    private Log log=new Log();
    /*start建立连接*/
    public void start() {
    	try {
    		//clear();
    		log.info("Server:start");
    		serverSocket = new ServerSocket(port);
    		System.out.println("Server starting successfully.");
    		while(true) {
    			Socket socket = serverSocket.accept();
            	out = new PrintWriter(socket.getOutputStream(), true);
            	clients.add(out);
            	textinfo.append("connected\n");
            	textinfo.append("Client "+clients.size()+" connected: " + socket.getInetAddress().getHostAddress());
            //	log.info("Client "+clients.size()+" connected: " + socket.getInetAddress().getHostAddress());
            	new Thread(new ClientHandler(socket)).start();
    		}
    	}catch(IOException e) {
    		log.warning(e.getMessage());
    	}
    }
	public Server() {
		
	}
	/*处理用户端发送的数据*/
	public void dealMessage(String example) {
		//example为未经处理的字符串
		String allMessage = example.substring(1, example.length() - 1);
		//allMessage为去掉两端大括号的字符串
		String[] messages = allMessage.split(",");
		//messages为将字符串分开为不同种类字符串的字符串数组
		log.info(allMessage);
		//out.println("word");
		if(allMessage.startsWith("motive")) {
			broadcastMessage(example);
		}
		for(String message:messages) {
			//message中是冒号分隔的一项行动的不同参数，其中第一项表示类型，第二项或后面的若干项表示参数
			String type=message.split(":")[0];
			//发现有时会有不正确的字符串传入传出，导致了数组越界bug，为避免传输时出现问题，检查字符串长度
			if(message.split(":").length==1) {
				break;
			}
			String content=message.split(":")[1];
			if(type.equals("join")) {
				textclients.append(allMessage+"\n");
				if(joinMessageCache[0]==null) {
					joinMessageCache[0]=allMessage;
				}else if(joinMessageCache[1]==null) {
					joinMessageCache[1]=allMessage;
				}else if(joinMessageCache[2]==null) {
					joinMessageCache[2]=allMessage;
				}
				
				//broadcastMessage(allMessage);
				if(joinMessageCache[0]!=null) {
					for(String joinMessageSub:joinMessageCache) {
						broadcastMessage("{"+joinMessageSub+"}");
					}
				}
			}else if (type.equals("attack1")){
				textinfo.append(allMessage+"\n");
			}else if(type.equals("attack2")) {
				textinfo.append(allMessage+"\n");
				//broadcastMessage("{"+allMessage+"}");
			}else if(type.equals("attacked0")) {
				textinfo.append(allMessage+"\n");
				broadcastMessage("{"+allMessage+"}");
			}else if(type.equals("attacked1")) {
				textinfo.append(allMessage+"\n");
				broadcastMessage("{"+allMessage+"}");
			}else if(type.equals("attacked2")) {
				textinfo.append(allMessage+"\n");
				broadcastMessage("{"+allMessage+"}");
			}else if(type.equals("die")) {
				textinfo.append(allMessage+"\n");
				broadcastMessage("{"+allMessage+"}");
			} else if(type.equals("hall")) {
				textinfo.append(allMessage+"\n");
				this.hallClient+=1;
				textinfo.append(this.hallClient+"\n");
				if(this.hallClient==2) {
					textinfo.append("hall is start.\n");
					broadcastMessage("{hall:-1}");
				}
				if(joinMessageCache[0]!=null) {
					for(String joinMessageSub:joinMessageCache) {
						broadcastMessage("{"+joinMessageSub+"}");
					}
				}
			}else if(type.equals("player")) {
				broadcastMessage(message);
			}
		}
	}
	/*launch() method 初始化GUI界面*/
	public void launch() {
		JFrame frame=new JFrame("Server Information");
		frame.setLocation(400, 200);
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textinfo.setPreferredSize(new Dimension(100,300));
        JScrollPane scrollPaneleft = new JScrollPane(textinfo);
        scrollPaneleft.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneleft.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JScrollPane scrollPaneright = new JScrollPane(textclients);
        textclients.setPreferredSize(new Dimension(100,100));
        scrollPaneright.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPaneright.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(Color.YELLOW);
        JLabel infoLabel=new JLabel("Information");
        infoLabel.setPreferredSize(new Dimension(0,30));
        leftPanel.add(infoLabel,BorderLayout.NORTH);
        leftPanel.add(scrollPaneleft,BorderLayout.CENTER);

        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBackground(Color.GRAY);
        JLabel clientLabel=new JLabel("Clients");
        clientLabel.setPreferredSize(new Dimension(0,30));
        rightPanel.add(clientLabel,BorderLayout.NORTH);
        rightPanel.add(scrollPaneright,BorderLayout.CENTER);
        
        /*使用GridBagLayout进行布局*/
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 左侧面板占据 2/3 的宽度
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 0.67; 
        gbc.weighty = 0.9;
        gbc.fill = GridBagConstraints.BOTH;
        frame.add(leftPanel, gbc);

        // 右侧面板占据 1/3 的宽度
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0.33;
        gbc.weighty=0.9;
        frame.add(rightPanel, gbc);

		frame.setVisible(true);
	}
	public static void main(String[] args) {
		Server server=new Server();
		server.launch();
		server.start();
	}
    private class ClientHandler implements Runnable{
        private Socket socket;
        private BufferedReader in;
        public ClientHandler(Socket socket) {
        	System.out.println("hello");
        	try {
        		this.socket = socket;
        		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	}catch (IOException e) {
        		log.warning(e.getMessage());
        	}
        }
        public void run() {
        	//System.out.println("hello");
            try {
            	//while(true) {
            	out=new PrintWriter(socket.getOutputStream(), true);
            	/*for(int z=0;z<10000;z++) {
            	out.println("Hello");
            	}*/
                String inputLine;
                //Clients input "quit" to "myself" to quit
                while (!((inputLine = in.readLine()).equals("{END}"))) {
                	//out.println("word");
                    dealMessage(inputLine);
                    //在处理后直接进行输出
                    //broadcastMessage(inputLine);
                }
            } catch (IOException e) {
            	log.warning(e.getMessage());
            } finally {
                    try {
                        socket.close();
                        in.close();
                        clients.remove(this);
                        /*System.out.println("Client "+socket.getInetAddress().getHostAddress()+" has gone.");*/
                    } catch (IOException ex) {
                    	log.warning(ex.getMessage());
                    }
            }
        }
    }
    
    //数据已经格式化，直接输出
    private void broadcastMessage(String message) {
        for (PrintWriter out : clients) {
            out.println(message);
        }
    }

}

