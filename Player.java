import femto.mode.HiRes16Color;
import femto.input.Button;
import sounds.Recharge;
import images.Beam;

public class Player extends Ship {
    byte power;
    byte isBeaming;
    Particles particles = new Particles(10);
    static final Beam beam = new Beam();
    static final Ship[] instances = new Ship[1];
    static final Recharge rechargeSound = new Recharge(0);
    long lastPowerDropTime, lastPressTime;
    static final int BRL = 4;
    static final int BRR = 5;

    Player(){
        super();
        instances[0] = this;
        init();
    }
    
    void recharge(){
        power = 100;
        lastPowerDropTime = System.currentTimeMillis();

        rechargeSound.channel = Main.channel++;
        rechargeSound.play();
    }
    
    void showBeam(){
        isBeaming = 1;
    }
    
    void init(){
        this.tint = 0;
        this.recharge();
        this.HP = 100;
        this.x = Terrain.maxX / 2;
        this.z = 20.0f;
        this.y = Ship.CEILING * 1.5;
        this.rotY = Math.PI;
        this.state = FLYING;
        this.particles.spawnRate = 0;
        vx = vy = vz = 0;
        
        beam.Tag();

        particles.startAge = 0;
        particles.endAge = 15;
        particles.ageRate = 2;
        particles.color = 5;
    }
    
    public void draw(HiRes16Color screen){
        this.z = 60;
        
        updateTransform();
        
        if(isBeaming != 0){
            isBeaming = 0;
            Node.pointA.x = 0;
            Node.pointA.y = 0;
            Node.pointA.z = 0;
            Node.pointA.mul(transform);
            Node.pointA.project();
            beam.setPosition(Node.pointA.x, Node.pointA.y + 15);
            beam.rotozoom(screen, 0, Node.pointA.z * 30);
        }
        
        render(screen);
        // super.draw(screen);
        
        particles.svx = (Math.random() - 0.5) * 2.0f;
        particles.svy = 0;
        particles.svz = -13;
        particles.draw(screen);
    }

    void left(){
        vx -= 3 * agility * (Button.B.isPressed() ? 0.5 : 1);
    }

    void right(){
        vx += 3 * agility * (Button.B.isPressed() ? 0.5 : 1);
    }

    void down(){
        vy -= 3 * agility * (Button.B.isPressed() ? 0.5 : 1);
    }

    void up(){
        vy += 2 * agility * (Button.B.isPressed() ? 0.5 : 1);
    }

    void update(){
        super.update();
        long now = System.currentTimeMillis();
        int delta = (now - lastPowerDropTime) * 4 / 1024;
        if( delta >= 1 ){
            power--;
            lastPowerDropTime = now;
        }
        
        if( state < FLYING ){
            particles.spawnRate = 0;
            return;
        }
        
        var portal = Main.instance.portal;
        if( portal.z > 0 && portal.tint == 0 && portal.z < z && portal.x > x - 60 && portal.x < x + 60 && y <= 220 && y >= 100 ){
            recharge();
            portal.tint++;
        }
        
        if( state == FLYING ){

            if( Button.Left.justPressed() ){
                if(now - lastPressTime < 200){ 
                    state = BRL;
                }
                lastPressTime = now;
            }
            
            if( Button.Right.justPressed() ){
                if(now - lastPressTime < 200){
                    state = BRR;
                }
                lastPressTime = now;
            }

            if( Button.Left.isPressed() ) left();
            else if( Button.Right.isPressed() ) right();
            if( Button.Up.isPressed() ) down();
            else if( Button.Down.isPressed() && power > 0 ) up();

            if( Button.B.isPressed() ) stop();
            else{
                forward();
                if( Button.A.isPressed() && power > 1 ){
                    power--;
                    fire(Enemy.instances);
                    
                }
            } 
    
        }else if(state == BRL){
            this.rotX = 0;
            this.rotZ -= 0.85;
            this.vx -= 5 * agility;
            if( now - lastPressTime > 200 )
                this.state = FLYING;
            forward();
        }else if(state == BRR){
            this.rotX = 0;
            this.rotZ += 0.85;
            this.vx += 5 * agility;
            if( now - lastPressTime > 200 )
                this.state = FLYING;
            forward();
        }
        if( power <= 0 ) vy -= 0.75;
        
        this.vz += 0.5f;
        
        Node.FOV = 80 - (vz - 10.0f) * 2.0f;

        this.particles.spawnRate = (int) (100 - vz*0.5f);
        this.particles.x = this.x - vx * 2;
        this.particles.y = this.y;
        this.particles.z = this.z - vz * 2;
    }    
}
