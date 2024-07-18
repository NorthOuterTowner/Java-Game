package client.run;

import java.io.*;
import java.lang.reflect.Constructor;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Sign{
	private boolean status=false;
	private JFrame frame=new JFrame("Sign");
	JTextField nameInput;
	public Sign() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridLayout(5,1));
		frame.setLocation(400, 200);
		JLabel nameLabel=new JLabel("UserName:");
		nameInput=new JTextField(20);
		JLabel passwordLabel=new JLabel("Password:");
		JTextField passwordInput=new JTextField(20);
		JLabel passwordAgainLabel=new JLabel("Input Again:");
		JTextField passwordAgainInput=new JTextField(20);
		
		
		JPanel name=new JPanel();
		name.add(nameLabel);
		name.add(nameInput);
		JPanel password=new JPanel();
		password.add(passwordLabel);
		password.add(passwordInput);
		JPanel passwordAgain=new JPanel();
		passwordAgain.add(passwordAgainLabel);
		passwordAgain.add(passwordAgainInput);
		
		JButton signIn=new JButton("Sign In");
		JPanel sign1=new JPanel();
		JPanel sign2=new JPanel();
		JButton signUp=new JButton("Sign Up");
		JButton changeToUp=new JButton("I don't have account.");
		JButton changeToIn=new JButton("I have account.");
		
		JPanel change1=new JPanel();
		JPanel change2=new JPanel();
		change1.add(changeToUp);
		change2.add(changeToIn);
		sign1.add(signIn);
		sign2.add(signUp);
		frame.add(name);
		frame.add(password);
		frame.add(sign1);
		frame.add(change1);
		frame.pack();
		frame.setVisible(true);
		
		changeToUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				frame.getContentPane().removeAll();
				frame.add(name);
				frame.add(password);
				frame.add(passwordAgain);
				frame.add(sign2);
				frame.add(change2);
				frame.pack();
				frame.getContentPane().repaint();
			}
		});
		
		changeToIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				frame.getContentPane().removeAll();
				frame.add(name);
				frame.add(password);
				frame.add(sign1);
				frame.add(change1);
				frame.pack();
				frame.getContentPane().repaint();
			}
		});
		
		signIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message=nameInput.getText()+" "+passwordInput.getText();
				signInJudge(message);
			}
		});
		signUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message=nameInput.getText()+" "+passwordInput.getText();
				signUpJudge(message);
			}
		});
	}
	public boolean signInJudge(String message) {
		try {
			BufferedReader reader=new BufferedReader(new FileReader("sign.txt"));
			String line;
            while ((line = reader.readLine()) != null) {
            	if(message.equals(line)) {
            		this.status=true;
            		reader.close();
            		frame.setVisible(false);
            		return true;
            	}
            }
            reader.close();
		}catch(IOException ex) {}
		return false;
	}
	public boolean signUpJudge(String message) {
		try {
			BufferedReader reader=new BufferedReader(new FileReader("sign.txt"));
			String line;
            while ((line = reader.readLine()) != null) {
            	if(message.equals(line)) {
            		reader.close();
            		return false;
            	}
            }
            reader.close();
            PrintWriter writer=new PrintWriter(new FileWriter("sign.txt",true));
            writer.println(message);
            writer.close();
		}catch(IOException ex) {}
		return true;
	}
	public boolean getStatus() {
		return this.status;
	}
	public String getName() {
		return nameInput.getText();
	}
}
