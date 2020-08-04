import femto.mode.HiRes16Color;
import sounds.Explosion;

class AdjustableExplosion extends Explosion {
    float T, S, V;
    
    public AdjustableExplosion(){
        super(0);
    }
    
    public void reset(){
        T = 0;
        super.reset();
    }
    
    public void play(float volume){
        V = Math.min(volume, 1.0f);
        if( V<0.1 ) return;
        
        channel = Main.channel++;
        S = Math.random() * 0.25 + 0.75;
        
        super.play();
    }
    
    public ubyte update(){
        T += S;
        t = (int) T;
        return (ubyte) (int) ((super.update() - 128) * V + 128);
    }
}

public class Ship extends Node {
    long lastShotTime;
    float vx, vy, vz, agility = 1.0f;
    ubyte state, HP, fireDelay = 10;

    static final AdjustableExplosion explosion = new AdjustableExplosion();
    static final float CEILING = 250.0f;

    static final ubyte UNSPAWNED = 0;
    static final ubyte GROUNDED = 1;
    static final ubyte DEAD = 2;
    static final ubyte FLYING = 3;
        
    Ship(){
        super(meshes.Jet.bin());
        this.scale = 0.7f;
    }

    void update(){
        float groundY = Terrain.getHeightAtPoint(this.x, this.z);
        
        if(groundY > this.y){
            this.y = groundY;
            if( state >= FLYING )
                hit(false);
            if( state == GROUNDED )
                state = DEAD;
            vy += (vz+(float)Math.abs(vx)+1) * 0.5f;
            vz *= 0.8;
        }
        
        if( state == DEAD ){
            vy -= 1;
        }
        
        if( state == GROUNDED ){
            rotX = (rotX*9 + Math.PI) / 8;
            rotY += 0.3;
            vy -= 3;
            tint++;
        }
    }
    
    void stop(){
        vz *= 0.98;
    }

    void forward(){
        vz += 1.0 * agility;
    }

    public void hit(boolean isShot){
        if( HP >= 10 ) HP -= (ubyte) 10;
        else HP = 0;
        if( HP == 0 ){
            state = GROUNDED;
            explosion.play((1024 - z) / 1024);
        }
    }

    public void updateTransform(){
        this.x += vx;
        this.y += vy;
        this.z += vz;
        if(this.state <= FLYING){
            this.rotZ = vx * 0.05;
            this.rotX = -(((float)Math.abs(this.rotZ)) * 0.5 + vy * 0.05);
        }

        if( vx > 0 && this.x >= Terrain.maxX*0.9f ){
            this.x = Terrain.maxX*0.9f;
            vx = 0;
        }
        
        if( vx < 0 && this.x <= Terrain.maxX*0.1f ){
            vx = 0; 
            this.x = Terrain.maxX*0.1f;
        }
        
        vx *= 0.9;
        vy *= 0.9;
        if( this.y > CEILING )
            vy -= 2;        
        vz *= 0.9;
        
        super.updateTransform();
    }

    void fire(Ship[] targets){
        if( System.currentTimeMillis() > lastShotTime+((int)fireDelay)*10 ){
            lastShotTime = System.currentTimeMillis();
            Bullet.fire(this, vx, vz, tint, targets);
        }
    }
        
}
