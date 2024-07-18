package server;

/*Server class
 * Link with Client
 */
import java.awt.*;
import java.time.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;

import client.*;
public class Server {
	private ArrayList<PrintWriter> clients=new ArrayList<PrintWriter>(100);
	private final static int port=4450;
    JTextArea textclients=new JTextArea();
    JTextArea textinfo=new JTextArea();
    JPanel leftPanel = new JPanel();
    JPanel rightPanel = new JPanel();
    private ServerSocket serverSocket;
    private PrintWriter out;
    private PrintWriter logOut;
    public int hallClient=0;
    
    /*将文件数据清空*/
    private void clear() {
		String fileClear = "player.dat";
        try (FileOutputStream fos = new FileOutputStream(fileClear)) {
            
            fos.getChannel().truncate(0);
            System.out.println("File content cleared successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*start() method 建立连接*/
    public void start() {
    	try {
    		clear();
    		logOut=new PrintWriter(new FileWriter("log.txt"),true);
    		logOut.println("{Server:start}");
    		serverSocket = new ServerSocket(port);
    		System.out.println("Server starting successfully.");
    		while(true) {
    			Socket socket = serverSocket.accept();
            	out = new PrintWriter(socket.getOutputStream(), true);
            	clients.add(out);
            	System.out.println("Client "+clients.size()+" connected: " + socket.getInetAddress().getHostAddress());
            	new Thread(new ClientHandler(socket)).start();
    		}
    	}catch(IOException e) {
    		System.err.println(e.getMessage());
    	}
    }
	public Server() {
		
	}
	/*处理用户端发送的数据*/
	public void dealMessage(String example) {
		String allMessage = example.substring(1, example.length() - 1);
		String[] messages = allMessage.split(",");
		log(allMessage);
		for(String message:messages) {
			String type=message.split(":")[0];
			if(message.split(":").length==1) {
				break;
			}
			String content=message.split(":")[1];
			if(type.equals("join")) {
				textclients.append(allMessage+"\n");
			}else if (type.equals("attack1")){
				textinfo.append(allMessage+"\n");
			}else if(type.equals("attack2")) {
				textinfo.append(allMessage+"\n");
				broadcastMessage(allMessage);
			}else if(type.equals("attacked1")) {
				textinfo.append(allMessage+"\n");
				broadcastMessage(allMessage);
			}else if(type.equals("attacked2")) {
				textinfo.append(allMessage+"\n");
				broadcastMessage(allMessage);
			}else if(type.equals("hall")) {
				textinfo.append(allMessage+"\n");
				this.hallClient+=1;
				textinfo.append(this.hallClient+"\n");
				if(this.hallClient==2) {
					textinfo.append("确实判断出来了是两个");
					broadcastMessage("{hall:-1}");
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
        	try {
        		this.socket = socket;
        		System.out.println("1");
        		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	}catch (IOException e) {
        		System.err.println(e.getMessage());
        	}
        }
        public void run() {
            try {
                String inputLine;
                //Clients input "quit" to "myself" to quit
                while (!((inputLine = in.readLine()).equals("{END}"))) {
                    dealMessage(inputLine);
                    textinfo.append("f");
                    broadcastMessage(inputLine);
                }
            } catch (IOException e) {
            	System.err.println(e.getMessage());
            } finally {
                    try {
                        socket.close();
                        in.close();
                        out.close();
                        clients.remove(this);
                        /*System.out.println("Client "+socket.getInetAddress().getHostAddress()+" has gone.");*/
                    } catch (IOException ex) {
                    	System.err.println(ex.getMessage());
                    }
            }
        }
    }
    private void broadcastMessage(String message) {
    	textinfo.append("告诉他"+message+"\n");
    	int i=0;//temp test
        for (PrintWriter out : clients) {
        	i++;
        	textinfo.append("我告诉了"+i+"个人");
            out.println(message);
        }
        
    }
    public void log(String allMessage) {
    	String logOutput="{time:"+LocalDateTime.now()+",";
		logOutput+=allMessage;
		logOutput+="}";
    	logOut.println(logOutput);
    }
}

