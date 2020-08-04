import images.Bar;
import images.Bar2;
// import images.Top;

public class HUD extends images.HUD {
    int score;
    static final var bar = new Bar();
    static final var bar2 = new Bar2();
    // static final var top = new Top();
    
    static int digits(int v){
        int d = 1;
        int c = 10;
        if(v < 0){
            v = -v;
            d++;
        }
        
        while(c <= v){
            d++;
            c *= 10;
        }
        
        return d;
    }
    
    void draw(){
        int hudY = Main.screen.height() - height();
        images.HUD.draw(Main.screen, 0, hudY);
        // top.draw(Main.screen, 73, 0);

        if(Main.instance.player.HP > 0){
            bar.currentFrame = Math.max(0, Math.min(4 * Main.instance.player.HP / 100, 4));
            bar.draw(Main.screen, 48, hudY+20);
        }
        
        if(Main.instance.player.power > 0){
            bar2.currentFrame = Math.max(0, Math.min((int) Math.round(4 * Main.instance.player.power / 100.0f), 4));
            bar2.draw(Main.screen, 159, hudY+20);
        }
        
        int color = 11;
        if( score < Main.score ){
            score++;
            color = 5;
        }else if( score > Main.score ) 
            score = Main.score;
        
        int x = 110 - digits(score) * 6 / 2;
        Main.screen.setTextColor(10);
        Main.screen.setTextPosition(x, 13);
        Main.screen.print(score);
        Main.screen.setTextColor(color);
        Main.screen.setTextPosition(x, 12);
        Main.screen.print(score);
    }    
}