package client;

import java.util.ArrayList;
import client.info.*;
public class Bullet {
	private int x;
	private int y;
	private int attack;
	private char status='s';
	private ArrayList<Player> people;
	public Bullet(int x,int y,int attack,ArrayList<Player> people,char status) {
		this.x=x;
		this.y=y;
		this.attack=attack;
		this.people=people;
		this.status=status;
	}
	public char getStatus() {
		return this.status;
	}
	public void setStatus(char status) {
		this.status=status;
	}
	public int getX() {
		return this.x;
	}
	public int getY() {
		return this.y;
	}
	public void jminusX() {
		this.x-=30;
	}
	public void jaddX() {
		this.x+=30;
	}
	public void kminusX() {
		this.x-=15;
	}
	public void kaddX() {
		this.x+=15;
	}
	
	public void jminusY() {
		this.y-=30;
	}
	public void jaddY() {
		this.y+=30;
	}
	public void kminusY() {
		this.y-=15;
	}
	public void kaddY() {
		this.y+=15;
	}
	public void setX(int x) {
		this.x=x;
	}
	public void setY(int y) {
		this.y=y;
	}
	public int getAttack() {
		return this.attack;
	}
	public void setAttack(int attack) {
		this.attack=attack;
	}
	public int hurt() {
		int i=0;
		for(Player player:people) {
			int perX=player.getX();
			int perY=player.getY();
			if((x>perX)&&(x<(perX+60))&&(y>perY)&&(y<(perY+80))) {
				return i;
			}
			i++;
		}
		return -1;
	}
}
