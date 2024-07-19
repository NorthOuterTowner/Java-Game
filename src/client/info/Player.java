package client.info;

import java.io.*;
import java.util.*;

public class Player implements Serializable{
	private String name;
	private int x;
	private int y;
	private int heart=100;
	private int attack=10;
	//status记录用户状态，使用户在发射子弹时根据朝向发射
	private char status='s';
	//behaviour记录用户行为
	private char behaviour='s';
	public Player(String name) {
		this.name=name;
		this.x=100;
		this.y=100;
	}
	public void setBehaviour(char behaviour) {
		this.behaviour=behaviour;
	}
	public char getBehaviour() {
		return this.behaviour;
	}
	public void setStatus(char status) {
		this.status=status;
	}
	public char getStatus() {
		return this.status;
	}
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name=name;
	}
	public int getHeart() {
		return this.heart;
	}
	public void setHeart(int heart) {
		this.heart=heart;
	}

	public void setX(int x) {
		this.x=x;
	}
	public void setY(int y) {
		this.y=y;
	}
	public int getX() {
		return this.x;
	}
	public int getY() {
		return this.y;
	}
	public void addX() {
		this.x+=10;
	}
	public void addY() {
		System.out.println("w2");
		this.y-=10;
	}
	public void minusX() {
		this.x-=10;
	}
	public void minusY() {
		this.y+=10;
	}
	public int getAttack() {
		return this.attack;
	}
	public void setAttack(int attack) {
		this.attack=attack;
	}
	public void minusHeart(int attack) {
		this.heart-=attack;
	}
}
