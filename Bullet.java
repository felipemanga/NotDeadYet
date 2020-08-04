import sounds.Shoot;

class AdjustableShoot extends Shoot {
    float T, S, V;
    
    public AdjustableShoot(){
        super(0);
    }
    
    public void reset(){
        T = 0;
        super.reset();
    }
    
    public void play(float volume){
        V = Math.min(volume * 0.25, 1.0f);
        if( V<0.1 ) return;
        
        channel = Main.channel++;
        S = Math.random() * 0.25 + 0.1;
        
        super.play();
    }
    
    public ubyte update(){
        T += S;
        t = (int) T;
        return (ubyte) (int) ((super.update() - 128) * V + 128);
    }
}

public class Bullet extends Node {
    static final AdjustableShoot sound = new AdjustableShoot();
    static final int bulletCount = 8;
    static final Bullet[] bullets = new Bullet[bulletCount];
    float vx, vy, vz;
    Ship[] targets;

    public Bullet(){
        super(meshes.Bullet.bin());
        scale = 2;
        Main.addNode(this);
    }

    public static void init(){
        for( int i=0; i<bullets.length; ++i )
            bullets[i] = new Bullet();
    }
    
    public static void updateAll(){
        for( var i=0; i<bulletCount; ++i ){
            var bullet = bullets[i];
            if( bullet.z <= -1 )
                continue;
            bullet.update();
        }        
    }

    public void update(){
        x += vx;
        y += vy;
        z += vz;

        if( Terrain.isOutOfBounds(x, z) ){
            z = -1;
        }

        if( targets != null ){
            for(Ship s : targets){
                if(s == null || s.state <= Ship.DEAD)
                    continue;

                float sd = Node.pointA
                    .load(this.x, this.y, this.z)
                    .sub(s.x, s.y, s.z)
                    .lengthSq();

                if(sd > 5000)
                    continue;

                s.hit(true);
                z = -1;
            }
        }
    }
    
    public static Node fire(Node src, float vx, float vz, int recolor, Ship[] targets){
        for( var i=0; i<bulletCount; ++i ){
            var bullet = bullets[i];
            if( bullet.z >= 0 )
                continue;
                
            sound.play((1024-src.z) / 1024);
                
            bullet.tint = recolor;
            bullet.targets = targets;
            bullet.x = src.x;
            bullet.y = src.y;
            bullet.z = src.z;
            
            bullet.rotX = -src.rotX*1.1f; // Math.atan2(src.vy, src.vz);
            bullet.rotY = Math.atan2(vx, vz) * 1.05;

            Node.transform.setRotation(bullet.rotX, bullet.rotY, 0);
            Node.pointA.load(0, 0, 80).mul(Node.transform);
            bullet.vx = Node.pointA.x;
            bullet.vy = Node.pointA.y;
            bullet.vz = Node.pointA.z;
            
            bullet.update();

            return bullet;
        }
        return null;
    }
    
}
