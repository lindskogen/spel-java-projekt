package render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import projekt.event.Dialogs;
/**
 * Projekt är en subklass av Render vilket innebär att det är här all grafik egentligen skrivs ut på skärmen
 * @author johannes
 */
public class Projekt extends Render{
	/**
	 * världens nuvarande egenskaper sparas och ändras i denna variabel.
	 */
	public World world;
	/**
	 * focus innehåller information om var karaktären befinner sig och hur världen skall relatera sig till var karaktären är(görs ex med att sätta world.setOffset(int x,int y)
	 */
	public character focus;
	/**
	 * konstruktorn för spelet
	 */
	public Projekt(){
		focus=new character(2,2,radius);
		String[] s =new String[4];
		for(int i=0;i<4;i++)
			s[i]="/res/CharMain/ts"+i+".png";
		focus.setChar("/res/CharMain/firehero.gif");
		this.world=new World(this.getClass().getResource("/res/worlds/world200").getPath());
                focus.setOnWalkCallback(new OnWalkCallback() {

                    public boolean onWalk() {
                        return true;
                    }

                    public boolean onStartWalk() {
                        return false;
                    }

                    public boolean onEndWalk() {
                        Projekt.this.focus.freeze=false;
                        return true;
                    }
                });
	}
	int delay=0;
	/**
	 * tick är medlem av Render och anropas varje loopintervall och här kollar vi då om det har tryckts ner någon tangent
	 * @param k boolean[]
	 */
	@Override
	public void tick(boolean[] k){
		boolean left=(k[KeyEvent.VK_A]||k[KeyEvent.VK_LEFT]) && !focus.freeze;
		boolean right=(k[KeyEvent.VK_D]||k[KeyEvent.VK_RIGHT]) && !focus.freeze;
		boolean up=(k[KeyEvent.VK_W]||k[KeyEvent.VK_UP]) && !focus.freeze;
		boolean down=(k[KeyEvent.VK_S]||k[KeyEvent.VK_DOWN]) && !focus.freeze;
		if(k[KeyEvent.VK_ESCAPE]||this.exitCode==1){
			this.exitCode=1;
			return;
		}
		//**** pickup ****//
		if(k[KeyEvent.VK_CONTROL])
			 this.pickup();

                //**** pickup ****//
                if(k[KeyEvent.VK_CONTROL])
                    this.pickup();
		if (!(left || right || up || down)){
			//**** Delayen ****//
			delay++;
		}
		else
			delay=0;
		if(delay>=radius)
			this.focus.frame=0;
		//X-axeln
		if(this.focus.y-this.focus.y2/radius==0&&!up&&!down){
			if((left&&!right)&&(this.focus.x-this.focus.x2/radius>=0)&&focus.x>1&&this.world.canGo(this.focus.x-1, this.focus.y)){
				focus.x--;
			}
			if((right&&!left)&&(this.focus.x-this.focus.x2/radius<=0)&&focus.x2<world.width*radius-radius&&this.world.canGo(this.focus.x+1, this.focus.y)){
				focus.x++;
			}
		}
		//Y-axeln
		if(this.focus.x-this.focus.x2/radius==0){
			if((up&&!down)&&(this.focus.y-this.focus.y2/radius>=0)&&focus.y>1&&this.world.canGo(this.focus.x, this.focus.y-1)){
				focus.y--;
			}
			if((down&&!up)&&(this.focus.y-this.focus.y2/radius<=0)&&focus.y>=1&&focus.y2<world.height*radius-radius&&this.world.canGo(this.focus.x, this.focus.y+1)){
				focus.y++;
			}
		}
		/* Directions!!!*/
		if((this.focus.x)*radius-(this.focus.x2)==0){
                        this.focus.stand=false;
			if((left&&!right)){
				focus.direciton=1;
			}
			if((right&&!left)){
				focus.direciton=2;
			}
		}
		if((this.focus.y)-(this.focus.y2)/radius==0){
			if((up&&!down)){
				focus.direciton=3;
			}
			if((down&&!up)){
				focus.direciton=0;
			}
		}
	}
	private void pickup() {
		int direction=this.focus.direciton;
		int x=0,y=0;
		if(direction==1||direction==2)
			x=(int)direction*2-3;
		else if(direction==3||direction==0)
			y=(-2*direction)/3+1;
		Color temp = this.world.getRGBA((int)this.focus.x2/radius+x, (int) this.focus.y2/radius+y);
		if(!(temp.equals(new Color(0xff000000))||temp.equals(new Color(0xffffffff)))){
			System.out.println(Dialogs.hello.sayHello);
			Graphics2D ag =this.world.alpha.createGraphics();
			ag.setColor(Color.white);
			ag.fillRect((int)this.focus.x2/radius+x, (int)this.focus.y2/radius+y, 1, 1);

			ag = this.world.items.createGraphics ();
			AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
			Composite c = ag.getComposite();
			ag.setComposite(composite);
			ag.setColor(new Color(0, 0, 0,0));
			ag.fillRect(((int)this.focus.x2/radius+x)*radius, ((int)this.focus.y2/radius+y)*radius, radius, radius);
			ag.setComposite(c);
			int b=temp.getBlue();
			if(b!=0) this.focus.addItem(b);
		}
	}
	/**
	 *  Denna funktionen ritar allt i hela spelet (inklusive det world genererar) som en bild
	 * @param g Graphics
	 */
	@Override
	public void paint(Graphics g){
		if((focus.x*focus.radius!=focus.x2))
			focus.slowMove(0);
		else if(focus.y*focus.radius!=focus.y2)
			focus.slowMove(1);
		if(world.isPoortal((int)(this.focus.x2/radius-0.1+1), (int)(this.focus.y2/radius-0.1+1))){
			/** @TODO switch worlds!!! */
                        focus.freeze = true;
			try{
				try {
					this.focus.addWorld(this.world.copy());
				} catch (CloneNotSupportedException ex) {
					//Logger.getLogger(Projekt.class.getName()).log(Level.SEVERE, null, ex);
				}
				String pa=getClass().getResource("/res/worlds/world"+this.world.getRGBA(this.world.pos[0], this.world.pos[1]).getRed()).getPath();
				if(this.focus.getWorldFromPath(pa)==-1)
					this.world.setWorldFromColor("/res/worlds");
				else
					this.world=this.focus.getWorld(this.focus.getWorldFromPath(pa));
				if(this.world.canGo(this.focus.y+1, this.focus.x))
					this.focus.y++;
				else if(this.world.canGo(this.focus.y-1, this.focus.x))
					this.focus.y--;
				else if(this.world.canGo(this.focus.y, this.focus.x-1))
					this.focus.x--;
				else if(this.world.canGo(this.focus.y, this.focus.x+1))
					this.focus.x++;
			}
			catch(ArrayIndexOutOfBoundsException b){this.exitCode=1;}
		}
		world.paint(g,(int)this.focus.x2,(int)this.focus.y2,this.getWidth(),this.getHeight());
              BufferedImage t = focus.c.getSubimage(radius*(((int)focus.frame)%4), 20*focus.direciton, radius, 20);
		g.drawImage(t,this.getWidth()/2-t.getWidth()/2-radius/2+radius, this.getHeight()/2-t.getHeight()-radius/5+radius, this);
		world.paintTop(g,(int)this.focus.x2,(int)this.focus.y2,this.getWidth(),this.getHeight());
              g.setColor(new Color(0x666666));
              g.setFont(new Font("Lucida Typewriter Regular",Font.BOLD, 12));
              this.drawShadowWithString("Version: \u03B1 0.1", 2, 12, Color.white, new Color(0x666666));
		this.drawDialog("hejjkjlkjlkfjewlkdwjflkdjflksjdflkjdsflkjdslkfjdslkfjdslkfjlkdsjflkdsjfkldsjflkjdskfjkdjflkds");
	}
	public void drawDialog(String message){
		int b=5;
		int m=18;
		int y=this.getHeight()/4;
		this.dbg.fillRect(0, y*3, this.getWidth(), y+this.getHeight()%4);
		this.dbg.setColor(Color.black);
		this.dbg.drawRect(b, y*3+b, this.getWidth()-2*b, y+this.getHeight()%4-2*b);
		
              this.dbg.setFont(new Font(Font.DIALOG,Font.BOLD, 10));
		String[] lines=message.split("\n");
		for(int i=0;i<lines.length;i++){
			FontMetrics fm = this.getFontMetrics(this.dbg.getFont());
			int width = fm.stringWidth(lines[i]);
			if(width<this.getWidth()-(2*b))
				this.drawShadowWithString(lines[i],b+b, 3*y+b+m+i*(12),new Color(0x222222), new Color(0xeeeeee));
		}
	}
        public void drawShadowWithString(String string, int x, int y, Color c1, Color c2){
		this.dbg.setColor(c2);
		this.dbg.drawString(string, x+1, y);
		this.dbg.drawString(string, x-1, y);
		this.dbg.drawString(string, x, y+1);
		this.dbg.drawString(string, x, y-1);
		this.dbg.setColor(c1);
		this.dbg.drawString(string, x, y);
        }
}
