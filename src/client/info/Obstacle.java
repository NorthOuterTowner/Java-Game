package client.info;

public class Obstacle {
	public static final int[] oby= {270,580};
	public static final int[] obx= {550,750};
	public static final int[] oby1= {270,370};
	public static final int[] obx1= {200,350};
	
	public static boolean stopw(int x,int y) {
		if(x>obx[0]&&x<obx[1]&&y>oby[1]&&y<oby[1]+20) {
			return false;
		}
		if(x>obx1[0]&&x<obx1[1]&&y>oby1[1]&&y<oby1[1]+20) {
			return false;
		}
		return true;
	}
	public static boolean stopa(int x,int y) {
		if(y>oby[0]&&y<oby[1]&&x>obx[1]&&x<obx[1]+20) {
			return false;
		}
		if(y>oby1[0]&&y<oby1[1]&&x>obx1[1]&&x<obx1[1]+20) {
			return false;
		}
		return true;
	}
	public static boolean stops(int x,int y) {
		if(x>obx[0]&&x<obx[1]&&y<oby[0]&&y>oby[0]-20) {
			return false;
		}
		if(x>obx1[0]&&x<obx1[1]&&y>oby1[0]&&y<oby1[0]-20) {
			return false;
		}
		return true;
	}
	public static boolean stopd(int x,int y) {
		if(y>oby1[0]&&y<oby1[1]&&x<obx1[0]&&x>obx1[0]-20) {
			return false;
		}
		if(y>oby[0]&&y<oby[1]&&x<obx[0]&&x>obx[0]-20) {
			return false;
		}
		return true;
	}
}
