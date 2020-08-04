import femto.mode.HiRes16Color;

public class Enemy extends Ship {
    static final int DEFAULT_TINT = 7;
    static final int ENEMY_COUNT = 3;
    static final Ship[] instances = new Ship[ENEMY_COUNT];
    int shiftTime;
    Node target;

    public static void setup(){
        for( int i=0; i<instances.length; ++i ){
            instances[i] = new Enemy();
        }
    }

    public Enemy(){
        super();
        state = UNSPAWNED;
        Main.addNode(this);
    }

    void init(float x, float z){
        mesh = Math.random(0, 100) > 50 ? meshes.Jet.bin() : meshes.Jet2.bin();
        HP = 10 + (Main.level > 0 ? Math.random(0, Main.level) : 0) * 10;
        fireDelay = Math.max(10, 30 - Main.level * 2);
        state = FLYING;
        this.x = x;
        this.z = z;
        this.y = Math.random() * 0.5f * Ship.CEILING + Ship.CEILING * 0.25f;

        target = Main.instance.stranded;
        Node player = Main.instance.player;

        float sd = Node.pointA
            .load(this.x, this.y, this.z)
            .sub(target.x, target.y, target.z)
            .lengthSq();

        float pd = Node.pointA
            .load(this.x, this.y, this.z)
            .sub(player.x, player.y, player.z)
            .lengthSq();
            
        if( target.z == -1 || pd < sd )
            target = player;

        tint = DEFAULT_TINT + Main.level;
    }

    void update(){
        if( Terrain.isOutOfBounds(x, z) ){
            die();
            return;
        }

        super.update();

        if( state != FLYING )
            return;
        
        shiftTime--;
        
        rotY = Math.atan2(
            x - target.x,
            z - target.z
            );

        float sd = Node.pointA
            .load(this.x, this.y, this.z)
            .sub(target.x, target.y, target.z)
            .lengthSq();

        float dx = -2 * Math.cos(rotY);
        float dy = -2 * Math.sin(rotY);

        if( (sd < 100000) ^ (shiftTime < 0) ){
            vx += dx;
            vz += dy;
        }else{
            vx += dy;
            vz += dx;
        }

        if( shiftTime < 0 )
            vy += Math.random() * 2;
        else {
            if( target.y > y ) vy += 1.0f;
            else if( target.y < y ) vy -= 1.0f;
            fire(Player.instances);
        }

        if( shiftTime < -30 )
            shiftTime = Math.random(10, 42);

        if( sd < 10000 ){
            hit(true);
            Main.instance.player.hit(false);
            vx += Math.random(-20, 20);
            vy += Math.random(-20, 20);
            vz += Math.random(-20, 20);
        }
    }
    
    public void hit(boolean isShot){
        if(!isShot){ 
            shiftTime = 0;
        }else{
            Main.killCount++;
            Main.score += 25;
        }
        super.hit(isShot);
    }

    void die(){
        z = -1;
        this.state = UNSPAWNED;
    }

    public static void updateAll(){
        for( var e : instances ){
            if( e.state == UNSPAWNED ) continue;
            e.update();
        }        
    }

    public static void spawn(float x, float z){
        Enemy e;

        for( var candidate : instances ){
            if( candidate.state == UNSPAWNED ){
                e = (Enemy) candidate;
                break;
            }
        }

        if( e == null )
            return;

        e.init(x, z);
    }
}
