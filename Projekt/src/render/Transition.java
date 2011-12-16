package render;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author Johan Lindskogen
 */
public final class Transition {

	private int x = 0, y = 0, width = 415, height = 400;
	Graphics g;
	int index = 0;
	int speed;
	boolean dirBool = false;
	String type = null;

	public Transition(String type) { //Del av skärmen
		this.type = type;
	}

	Transition setType(String type) {
		this.type = type;
		return this;
	}

	void transition(Graphics g) {
		this.g = g;
		if (type.equals("fadeOutIn")) {
			fadeOutIn();
		} else if (type.equals("blank")) {
			blank();
		} else if (type.equals("slideUpDown")) {
			slideUpDown();
		} else if (type.equals("slideUp")) {
			slideUp();
		} else if (type.equals("stripes")) {
			stripes(100);
		}
	}

	private void slideUpDown() {
		g.setColor(Color.BLACK);
		speed = 10;
		int extra=height/2;
		if (index > 0) 
			dirBool = true;
		if (index >= height/2+extra) 
			slideUp();
		else if(index <=height/2){
			g.fillRect(0, 0, width, index);
			g.fillRect(0, width - index, width, index);
			index+=speed;
		}
		else{
			g.fillRect(0, 0, width, height);
			index+=speed;
		}
	}

	private void stripes(int antal) {
		g.setColor(Color.BLACK);
		speed = 20;
		int lineHeight = height / antal;
		for (int i = 0; i < antal; i++) {
			if (i % 2 == 0) {
				g.fillRect(0, lineHeight * i, index, lineHeight);
			} else {
				g.fillRect(width - index, lineHeight * i, index, lineHeight);
			}
		}

		index += speed;
		if (index > 0) {
			dirBool = true;
		}
		if (index >= 400) {
			dirBool = false;
			index = 0;
		}
	}

	private void fadeOutIn() {
		speed = 6;
		if (!dirBool) {
			index += speed;
		} else {
			index -= speed;
		}
		if (index == 255 - (255 % speed)) {
			index = 255;
			g.setColor(new Color(0, 0, 0, index));
			dirBool = true;
		} else if (dirBool && index < 0) {
			dirBool = false;
			index = 0;
		} else {
			g.setColor(new Color(0, 0, 0, index));
		}
		g.fillRect(x, y, width, height);
	}

	private void blank() {
		if (!dirBool) {
			index += speed;
		} else {
			index -= speed;
		}
		if (index == 255 - (255 % speed)) {
			index = 255;
			g.setColor(new Color(0, 0, 0));
			dirBool = true;
		} else if (dirBool && index < 0) {
			dirBool = false;
			index = 0;
		} else {
			g.setColor(new Color(0, 0, 0));
		}
		g.fillRect(x, y, width, height);
	}

	private void slideUp() {
		g.setColor(Color.BLACK);
		speed = 10;
		if(index+speed<height/2){
			g.fillRect(0, 0, width, height/2 - index);
			g.fillRect(0, height/2 + index, width, height/2 - index);

			index += speed;
			if (index > 0) 
				dirBool = true;
			if (index+speed >= height/2) {
				dirBool = false;
				index = 0;
			}
		}
		else{
			int extra=200;
			g.fillRect(0, 0, width, height - (index-extra));
			g.fillRect(0,  index-extra, width,  height - (index-extra));
			index += speed;
			if (index >= height+extra) {
				dirBool = false;
				index = 0;
			}
		}
	}
}