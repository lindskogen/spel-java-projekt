package render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import primitives.House;
import projekt.event.Keys;

/**
 *  World har koll på all grafik och saker som skall vara utlagda på nuvarande bana denna klassen skall ha subklasser utformade effter vad
 *  man vill ha för värld
 * @author johannes
 */
public class World implements Cloneable {

	int pos[] = new int[3];
	String errors = "";
	int radius = 16;
	/**
	 * berdden på världens layout bild
	 */
	public int width = 0;
	/**
	 * höjden på världens layout bild
	 */
	public int height = 0;
	/**
	 *path är en string till foldern där alla bilder och grafik är förvarade för nuvarande värld
	 */
	String path;
	public Character npcs[];
	/**
	 * vis består av all Grafik i denna världen
	 */
	ImageIcon layout;
	ImageIcon overlay;
	BufferedImage grafik;
	BufferedImage alpha;
	BufferedImage items;
	private int id = 0;
	public ArrayList<Player> players = new ArrayList<Player>();
	/**
	 * konstruktorn försöker öppna forldern med alla biler och bestämma dess width och height
	 */
	World(String i) {
		if (i == null) {
			throw new RuntimeException("The World is not defined");
		}
		this.path = i;
		//sound.playSound("opening.mp3");
		//if (getClass().getResource(i) == null) 
		//	throw new RuntimeException("World,"+this.path+", does not exist!");
		System.out.println("Loading...");

		this.loadWorld();
	}

	World copy() throws CloneNotSupportedException {
		return (World) super.clone();
	}

	private void loadWorld() {
		ImageIcon temp = null;
		Graphics gr = null;
		Keys.status = "Loading world";
		System.out.println(this.path + "layout loaded.");
		this.layout = new ImageIcon(this.path + "/layout.png");

		System.out.println("overlay loaded.");
		this.overlay = new ImageIcon(this.path + "/overlay.png");

		System.out.println("graphics loaded.");
		temp = new ImageIcon(this.path + "/graphics.png");
		this.grafik = new BufferedImage(temp.getIconWidth(), temp.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		this.grafik.getGraphics().drawImage(temp.getImage(), 0, 0, null);

		System.out.println("alpha loaded.");
		temp = new ImageIcon(this.path + "/alpha.png");
		System.out.print(temp);
		this.alpha = new BufferedImage(temp.getIconWidth(), temp.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		gr = this.alpha.getGraphics();
		gr.drawImage(temp.getImage(), 0, 0, null);

		this.items = new BufferedImage(this.alpha.getWidth() * radius, this.alpha.getHeight() * radius, BufferedImage.TYPE_INT_ARGB);
		gr = this.items.getGraphics();
		for (int ix = 0; ix < this.alpha.getWidth(); ix++) {
			for (int iy = 0; iy < this.alpha.getHeight(); iy++) {
				int ixy = this.alpha.getRGB(ix, iy);
				if (((ixy >>> 16) & 0xff) == 255 && !(ixy == 0xff000000 || ixy == 0xffffffff)
						&& ((ixy >>> 8) & 0xff) * 20 + 20 < this.grafik.getWidth() && (ixy & 0xff) * radius + radius < this.grafik.getHeight()) {
					gr.drawImage(
							this.grafik.getSubimage(
							((ixy >>> 8) & 0xff) * radius,
							(ixy & 0xff) * 20,
							radius, 20),
							ix * radius, iy * radius - 8, null);
				}
				else if((ixy & 0xff) == 255 && ixy != 0xffffffff){
					Player p = new Player(ix,iy);
					p.setChar("/res/Players/player"+((ixy >> 16) &0xff)+".png");
					p.lvl = ((ixy >> 8) & 0xff);
					players.add(p);
				}
			}
		}
		this.width = this.alpha.getWidth();
		this.height = this.alpha.getHeight();
		Keys.status = "";
	}

	public int[] RGBtoCMYK(int rgb) {
		int ans[] = new int[4];
		int rgbs[] = new int[]{
			((rgb >>> 16) & 0xff) / 255, ((rgb >>> 8) & 0xff) / 255, (rgb & 0xff) / 255
		};
		if (rgbs[0] == 0 && rgbs[1] == 0 && rgbs[2] == 0) {
			ans[0] = ans[1] = ans[2] = 0;
			ans[3] = 1;
		} else {
			int w = Math.max(rgbs[0], Math.max(rgbs[1], rgbs[2]));
			for (int i = 0; i < 3; i++) {
				ans[i] = (w - (rgbs[i])) / w;
			}
			ans[3] = 1 - w;
		}
		return ans;
	}

	public void setWorldFromColor(String path, int curx, int cury) {
		if (this.pos[0] != -1 && this.pos[1] != -1) {
			Color c = this.getRGBA(this.pos[0], this.pos[1]);
			if (this.isPoortal(curx, cury)) { // RGB = Värld, x,y
				this.path = getClass().getResource(path + "/world" + c.getRed()).getPath();
				ImageIcon t = new ImageIcon(this.path + "/alpha.png");
				this.id = this.pos[2];
				if (t.getIconWidth() > this.pos[0] + radius && t.getIconHeight() > this.pos[1] + radius) {
					this.loadWorld();
				}
				/** @TODO LÖS ITEM, PLAYERS SÅ ATT DE INTE KOLLIDERAR MED SAMMA DATA!!!
				 * if (Color.RED = 255) new Item(Color.C,Color.M,Color.GREEN,Color.BLUE);
				 * also:
				 * if (Color.BLUE = 255) new Character(x,y,Color.RED,Color.GREEN);
				 */
				this.pos[0] = -1;
				this.pos[1] = -1;
				this.pos[2] = -1;
			}
		} else {
			// @TODO add code for no portal set here!!!1
		}
	}

	public void setWorld(String path, int curx, int cury) {

		if (this.pos[0] != -1 && this.pos[1] != -1) {
			if (this.isPoortal(curx, cury)) { // RGB = Värld, x,y
				this.path = path;
				ImageIcon t = new ImageIcon(this.path + "/alpha.png");
				this.id = this.pos[2];
				if (t.getIconWidth() >= this.pos[0] && t.getIconHeight() >= this.pos[1]) {
					this.loadWorld();
				}
				this.pos[0] = -1;
				this.pos[1] = -1;
				this.pos[2] = -1;
			}
		}
	}

	/**
	 * 
	 * @param rgb
	 * @return om charactärer elle item eller något annat är aktiverat (dvs de reserverade vädena 255,255,255)
	 */
	public boolean isEqual(int rgb) {
		return (((rgb>>> 16) & 0xff)==255) || (((rgb>>> 8) & 0xff) == 255) || (rgb & 0xff) ==255;
	}

	/**
	 * @param x positionen på x
	 * @param y positionen på y
	 * @return true om karaktären kan gå till punkten x,y annars returnerar den false
	 */
	public boolean canGo(int x, int y) {
		int argb = this.getRGBA(x, y).getRGB();
		return (argb == 0xffffffff) || this.isPoortal(x, y);
	}

	public boolean isPoortal(int x, int y) {
		if (this.getRGBA(x, y).getRGB() != 0xffffffff && this.getRGBA(x, y).getRGB() != 0xff000000
				&& !this.isEqual(this.getRGBA(x, y).getRGB())) {
			this.pos[0] = this.getRGBA(x, y).getGreen();//x
			this.pos[1] = this.getRGBA(x, y).getBlue(); //y
			this.pos[2] = this.getRGBA(x, y).getRed();//world number
			return true;
		}
		return false;
	}

	private BufferedImage isSolid(int x, int y, int w, int h) {
		BufferedImage b = new BufferedImage(this.layout.getIconWidth(), this.layout.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		b.getGraphics().drawImage(this.layout.getImage(), 0, 0, null);
		int[] pixelCol = new int[Math.max(w, h)];

		b.getRGB(x, y, w, h, pixelCol, 0, 0);
		int i = 1;
		while (pixelCol != null && pixelCol.length - 1 > i && pixelCol[i] != pixelCol[i - 1]) {
			//s+=", "+Integer.toString(pixelCol[i]);
			i++;
		}
		if (i >= pixelCol.length - 1) {
			return new BufferedImage(radius, radius, BufferedImage.TYPE_INT_BGR);
		}

		return b.getSubimage(x, y, w, h);
	}

	public Color getRGBA(int x, int y) {
		int pixelCol = this.alpha.getRGB(x, y);
		return new Color(
				(pixelCol >>> 16) & 0xff,
				(pixelCol >>> 8) & 0xff,
				pixelCol & 0xff,
				(pixelCol >>> 24) & 0xff);
	}
	ImageIcon bg = new ImageIcon(getClass().getResource("/res/treerothen.png").getPath());
	BufferedImage bm = null;

	void drawBackground(Graphics g, int offx, int offy, int lx, int ly) {
		if( !this.path.contains("/world11") ){
			if (bm == null ) {
				if (this.id == 1) // om världens röda färg är värdet 1
				{
					bg = new ImageIcon(getClass().getResource("/res/tree.png").getPath());
				}
				bm = new BufferedImage(lx, ly, BufferedImage.TYPE_INT_ARGB);
				int he = bg.getIconHeight();
				int wi = bg.getIconWidth();
				for (int i = 0; i * he < ly * 2; i++) {
					for (int j = 0; j * wi < lx; j++) {
						bm.getGraphics().drawImage(bg.getImage(), j * wi - 35, i * he / 2 - 35, null);
					}
				}
				this.id = 0;
			}
			g.drawImage(bm, offx - 32, offy - 32, null);
		}
	}

	void drawPlayers(Graphics g,int z,boolean over){
		for(int i=0;i<players.size();i++){
			if(!over)
				players.get(i).drawChar(g);
			else if(players.get(i).y2 >= z)
				players.get(i).drawChar(g);
		}
	}
	void paint(Graphics g, int x2, int y2, int width, int height) throws ArrayIndexOutOfBoundsException {
		if (this.getRGBA(x2 / radius, y2 / radius).equals(new Color(0xfbc815))) {
			Graphics ag = this.alpha.createGraphics();
			//Graphics ag=this.alpha.getImage().getGraphics().create();
			ag.setColor(Color.white);
			ag.fillRect(x2 / radius, y2 / radius, 1, 1);

			//BufferedImage im=(BufferedImage)this.alpha.getImage().getSource();//.getGraphics().fillRect(x2/radius, y2/radius, 1, 1);
		}
		//drawBackground(g, -x2 + 9, -y2 - 9, width * 2, height * 2);
		//g.drawImage(this.alpha, -x2+width/2, -y2 + height/2,this.alpha.getWidth()*radius,this.alpha.getHeight()*radius, null);
		g.drawImage(this.layout.getImage(), -x2 + width / 2, -y2 + height / 2, null);//0,0,width,height,null);//
		g.drawImage(this.items, -x2 + width / 2, -y2 + height / 2, null);
		//g.drawImage(this.isSolid(x2, y2, radius, radius), 0, 20, null);
		//g.setColor(new Color(this.isSolid(x2/radius, y2/radius, radius, radius)));
		//g.fillReFlashigare namn på bilder! Dolda ändringar som involverar utvecklingen av items.ct( width/2-2*radius, height/2-radius-radius/5+radius,radius,radius);
		if (!this.errors.equals("")) {
			g.drawString(errors, 3, 41);
		}
		if (this.path.contains("test")) {
			for (int i = 0; i < h.length; i++) {
				h[i].paint3D(g, x2, y2, width, height, radius);
			}
		}
		AffineTransform a =((Graphics2D)g).getTransform();
		g.translate(-x2 + width / 2, -y2 + height / 2);
		drawPlayers(g, y2,false);
		((Graphics2D)g).setTransform(a);
	}
	House h[] = new House[]{
		new House(0, 0),
		new House(0, 10),
		new House(10, 10),
		new House(10, 0),
		new House(20, 0)
	};
	
	void paintTop(Graphics g, double x2, double y2, int width, int height) {
		AffineTransform a =((Graphics2D)g).getTransform();
		g.translate((int)-x2 + width / 2, (int)-y2 + height / 2);
		g.drawImage(this.overlay.getImage(), 0, 0, null);
		drawPlayers(g, (int)y2,true);
		((Graphics2D)g).setTransform(a);
	}

	void paint3D(Graphics g, int x2, int y2, int width, int height) {
		Graphics2D g2 = ((Graphics2D) g);
		AffineTransform t = g2.getTransform();
		int w = 100, d = 200, h = 50;
		int x = -x2 + width / 2, y = -y2 + height / 2;

		g.drawRect(x, y, w, h);

		g2.translate(x, y);
		g2.scale(-((double) x2 - w / 2) / (double) width, 1);
		g2.shear(0, 0.5);
		g.drawRect(0, 0, d, h);
		g2.setTransform(t);

		g.drawRect((int) (x - (d * ((double) x2 - w / 2)) / (double) width), y + d / 2, w, h);

		g2.translate(x + w, y);
		g2.scale(-((double) x2 - w / 2) / (double) width, 1);
		g2.shear(0, 0.5);
		g.drawRect(0, 0, d, h);
		g2.setTransform(t);
	}
}
