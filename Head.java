import meshes.Idle;
import femto.sound.Procedural;
import femto.input.Button;
import sounds.Ah;
import sounds.Hua;
import sounds.Huh;
import sounds.Thud;

public class Head extends Node {
    static final Procedural[] grunt = new Procedural[]{
        new Ah(0),
        new Hua(0),
        new Huh(0)
    };
    
    static final Procedural thud = new Thud(1);
    
    float vx, vy, vz;
    
    float t;
    
    long tts;
    
    Head(){
        super( Idle.bin() );
        y = -40;
        scale = 3;
        tts = System.currentTimeMillis() + Math.random(1000, 4000);
    }
    
    void update(){
        t += 0.15;

        if( y < 0 && vy < 0 ){
            vy = -vy;
            y = 0;
            thud.play();
        }
        
        if( System.currentTimeMillis() > tts ){
            // grunt[ Math.random(0, grunt.length) ].play();
            tts = System.currentTimeMillis() + Math.random(1000, 4000);
        }
        
        vy -= 0.5f;
        
        if( Button.Left.isPressed() ) rotY += 0.1;
        if( Button.Right.isPressed() ) rotY -= 0.1;
        if( Button.B.justPressed() ) tint++;
        
        // x = 0; // (float) Math.sin(t) * 30.0f;
        // z = 0; // Math.cos(t) * 30.0f;
        // y += vy;
        // rotY = t - Math.PI;
        // Main.screen.drawCircle(x+110, z+88, 10, 4, true);
        draw(Main.screen);
    }
}