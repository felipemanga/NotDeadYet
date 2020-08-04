import femto.input.Button;
import femto.State;

import femto.mode.ScreenMode;
import static java.lang.System.memory.*;

class Cutscene extends State {
    int current, last;
    pointer[] narration;
    pointer data;

    Cutscene(pointer csdata, pointer[] narration){
        this.narration = narration;
        this.data = csdata;
        this.current = 0;
        this.last = getFrameCount();
    }

    int getFrameCount(){
        return LDRB(data);
    }
    
    static void draw(pointer data, ScreenMode screen, int frame, int sx, int sy){
        if( frame >= LDRB(data) )
            return;
        pointer frameData = data+1;
        while(frame-- > 0){
            int c = LDRB(frameData++);
            int w = LDRB(frameData++);
            int h = LDRB(frameData++) >> 3;
            frameData += h*w;
        }
        
        int c = LDRB(frameData++);
        int w = LDRB(frameData++);
        int h = LDRB(frameData++);
        for( int x=0; x<w; ++x ){
            for( int y=0; y<h; ){
                int b = LDRB(frameData++);
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
                screen.setPixel( (uint) (x + sx), (uint) (y++ + sy), (b&1) != 0 ? c : c>>4 ); b >>= 1;
            }
        }
    }

    static void draw2X(pointer data, ScreenMode screen, int frame, int sx, int sy, int cx){
        if( frame >= LDRB(data) )
            return;
        pointer frameData = data+1;
        while(frame-- > 0){
            int c = LDRB(frameData++);
            int w = LDRB(frameData++);
            int h = LDRB(frameData++) >> 3;
            frameData += h*w;
        }
        
        int c = LDRB(frameData++);
        int w = LDRB(frameData++);
        int h = LDRB(frameData++);
        for( int x=0; x<w; ++x ){
            for( int y=0; y<h; ){
                int b = LDRB(frameData++);
                for( int i=0; i<8; ++i, ++y, b >>= 1 ){
                    int p = (b&1) != 0 ? c : c>>4;
                    if(p == 0) continue;
                    p ^= cx;
                    screen.setPixel( (uint) (x*2 + sx), (uint) (y*2 + sy), p );
                    screen.setPixel( (uint) (x*2 + sx), (uint) (y*2+1 + sy), p );
                    screen.setPixel( (uint) (x*2+1 + sx), (uint) (y*2 + sy), p );
                    screen.setPixel( (uint) (x*2+1 + sx), (uint) (y*2+1 + sy), p );
                }
            }
        }
    }
    
    void update(){
        if( (current>>1) == last ){
            Main.activate();
            return;
        }
        
        Main.screen.clear(6);
        Main.screen.setTextColor(11);
        
        if( (current&1) == 1 ){
            draw(data, Main.screen, current>>1, 0, 0);
        }else{
            int width = Main.screen.textWidth(narration[current>>1]);
            int hwidth = width > 200 ? width / 2 + 1 : width;
            Main.screen.textLeftLimit = Main.screen.width()/2 - hwidth/2;
            Main.screen.textRightLimit = Main.screen.textLeftLimit + hwidth;
            Main.screen.setTextPosition(Main.screen.textLeftLimit, Main.screen.height()/2 - Main.screen.textHeight());
            Main.screen.println(narration[current>>1]);
        }
        
        current++;
        flushAndWait();
    }
    
    static void flushAndWait(){
        Main.screen.flush();
        Thread.sleep(30);
        Button.waitForPress();
    }
}
