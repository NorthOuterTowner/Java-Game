package client.hall;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
public class Hall{
	private JFrame frame=new JFrame("Hall");
	public static int[] room=new int[17];
	public static boolean in=false;
	
	public boolean toRoom(int th) {
		if(room[th]==3) {
			return true;
		}else {
			return false;
		}
	}
	public Hall() {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel hallPanel=new JPanel();
		JPanel headPanel=new JPanel();
		JLabel hallLabel=new JLabel("Choose Your Room");
		 Font font = new Font("Arial", Font.BOLD, 20); 
		hallLabel.setFont(font);
		headPanel.add(hallLabel);
		hallPanel.setLayout(new GridLayout(4,4));
		hallPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		for(int i=0;i<16;i++) {
			JPanel lonePanel=new JPanel();
			lonePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			lonePanel.setLayout(new BorderLayout());
			JLabel label=new JLabel(new ImageIcon("pic//room.png"));
			lonePanel.add(label,BorderLayout.CENTER);
			int reali=i+1;
			JButton enter=new JButton("ROOM"+reali);
			lonePanel.add(enter,BorderLayout.SOUTH);
			enter.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					//数组第一项多余，实际数据从1开始
					int th=enter.getText().charAt(enter.getText().length()-1)-'0';
					Hall.room[th]+=1;
					for(int j=1;j<=16;j++) {
						if(toRoom(j)) {
							System.out.println("OK");
							Hall.in=true;
							break;
						}
					}
				}
				
			});
			hallPanel.add(lonePanel);
			
		}

		frame.getContentPane().add(headPanel,BorderLayout.NORTH);
		frame.getContentPane().add(hallPanel,BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

	public void launch() {
	}
	public void close() {
		frame.setVisible(false);
	}
	
	/*public static void main(String[] args) {
		Hall hall=new Hall();
	}*/
}
