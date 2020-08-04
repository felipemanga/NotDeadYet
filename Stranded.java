import images.Balloons;
import femto.mode.HiRes16Color;
import sounds.Pickup;

class Stranded extends Node {
    static final Balloons balloons = new Balloons();
    static final Pickup pickup = new Pickup(0);
    static final byte PROP = 0;
    static final byte BOAT = 1;
    static final byte CAR  = 2;
    byte type;
    boolean rescued;
        
    public Stranded(){
        super(meshes.WatercraftPack_017.bin());
    }
    
    boolean init(float x, float y, float z, float incline){
        if( incline > 1 )
            return false;

        this.rescued = false;
        this.rotX = 0;
        if( y < 1 ){
            type = BOAT;
            switch( Math.random(0, 3) ){
                case 0: mesh = meshes.WatercraftPack_017.bin(); break;
                case 1: mesh = meshes.WatercraftPack_019.bin(); break;
                case 2: mesh = meshes.WatercraftPack_028.bin(); break;
            }
            this.y = (y == 0) ? y + 10.0f : y;
        }else{
            type = CAR;
            mesh = meshes.CarHatchback.bin();
            this.y = (y != 0) ? y + 10.0f : y;
        }
        
        this.tint = Math.random(0, 0xF);
        this.x = x;
        this.z = z;
        this.scale = 0.5;
        this.rotY = Math.random()*Math.PI*2;

        return true;
    }
    
    void initProp(){
        this.rescued = true;
        this.type = PROP;
        this.tint = 0;
        this.rotX = 0;
    }

    static float rock;
    
    public void draw(HiRes16Color screen){
        if( type == BOAT ){
            this.rotX = Math.sin(rock*0.2f) * 0.3;
            rock += 1.0f;
        }

        super.draw(screen);
        
        if(rescued || Main.instance.player.state < Ship.FLYING)
            return;

        Node ship = Main.instance.player;
        float len = Node.pointA.load(x, y, z).sub(ship.x, ship.y, ship.z).lengthSq();
        if( len < 10000.0f ){
            Main.instance.player.showBeam();
            balloons.think();
            balloons.updateAnimation();
            if( balloons.currentFrame == balloons.endFrame ){
                pickup.channel = Main.channel++;
                pickup.play();
                rescued = true;
                Main.score += 100;
                Main.rescueCount++;
            }
        }else{
            balloons.alert();
            Node.pointA.x = 0;
            Node.pointA.y = 0;
            Node.pointA.z = 0;
            Node.pointA.mul(transform);
            Node.pointA.project();
            balloons.setPosition(Node.pointA.x, Node.pointA.y - 20);
            balloons.rotozoom(screen, 0, Node.pointA.z * 30);
        }
    }
    
}

