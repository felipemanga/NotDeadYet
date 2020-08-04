import femto.Game;
import femto.State;
import femto.font.Koubit;
import femto.mode.HiRes16Color;
import femto.mode.Direct;
import femto.sound.Mixer;
import femto.sound.Procedural;
import femto.input.Button;
import femto.Prompt;
import femto.Sprite;
import images.BG;
import sounds.Thud;

class Score extends femto.Cookie {
    Score(){
        super();
        begin("test");
    }
    
    int hiscore;
    char n1, n2, n3, n4;
    char f1, f2, f3, f4;
    char p1, p2, p3, p4;
}

class Main extends State {
    static Main instance;
    static int channel;
    
    static final var save = new Score();
    static final HiRes16Color screen = new HiRes16Color(Bubblegum16.palette(), Koubit.font()); // the screenmode we want to draw with
    
    final Node portal = new Node(meshes.PipeStand.bin());
    final Node[] trees = new Node[3];
    final Stranded stranded = new Stranded();

    static int level = 0;
    static int score;
    static int totalScore;
    static int rescueCount = 0;
    static int killCount = 0;
    Node[] nodes;
    final Player player = new Player();
    static final var bg = new BG();
    static final var hud = new HUD();
    int count = 64;
    
    public static void main(String[] args){
        Mixer.init();
        Game.run( Koubit.font(), new IntroCutscene() );
    }
    
    public static void addNode(Node n){
        for( int i=0; i<instance.nodes.length; ++i ){
            if( instance.nodes[i] == null ){
                instance.nodes[i] = n;
                return;
            }
        }
    }
    
    public static void activate(){
        if( instance != null ) Game.changeState(instance);
        else Game.changeState(new Main());
    }

    void reset(){
        count = 64;
        rescueCount = 0;
        killCount = 0;
        score = 0;
        player.init();
        Terrain.reset((level+1) * 0xBC8F);
        for( var node : nodes )
            node.z = -1;
    }
    
    void init(){
        instance = this;
        portal.scale = 2;
        if( nodes == null ){
            nodes = new Node[
                2 + 
                Bullet.bulletCount + 
                Enemy.ENEMY_COUNT +
                trees.length
                ];

            addNode(portal);
            addNode(stranded);

            Bullet.init();
            Enemy.setup();
            
            for( int i=0; i<trees.length; ++i )
                addNode( trees[i] = new Node(meshes.NaturePack_051.bin()) );
        }
        
        reset();
    }

    void shutdown(){
        instance = null;
    }
    
    void initTreeMesh( Node tree, float x, float y, float z ){
        switch(Math.random(0, 8)){
        case 0: tree.mesh = meshes.NaturePack_051.bin(); break;
        case 1: tree.mesh = meshes.NaturePack_074.bin(); break;
        case 2: tree.mesh = meshes.NaturePack_071.bin(); break;
        case 3: tree.mesh = meshes.NaturePack_084.bin(); break;
        case 4: tree.mesh = meshes.NaturePack_114.bin(); break;
        case 5: tree.mesh = meshes.NaturePack_068.bin(); break;
        case 6: tree.mesh = meshes.NaturePack_061.bin(); break;
        case 7: tree.mesh = meshes.Plant.bin(); break;
        }
        tree.rotY = tree.rotZ = tree.rotX = 0;
        tree.scale = Math.random() * 0.5 + 0.75;
        tree.x = x;
        tree.y = y + tree.scale * 50.0f;
        tree.z = z;
    }
    
    static int rowsUntilPortalSpawn;
    void newTile( float x, float z, float d, int tx, int tz ){
        if(tx == 0) rowsUntilPortalSpawn--;
        
        if( tx < 3 || tx > 14 ) return;
        int r = Math.random(0, 1024);
        float y = Terrain.getHeightAtPoint(x, z);

        r -= 3 + level*2;
        if( r <= 0 ){
            Enemy.spawn(x, z);
            return;
        }
        
        if(d > 30)
            return;

        r -= 15;
        if( r <= 0 ){
            if( y > 0 ){
                for( var tree : trees ){
                    if( tree.z < 0 ){
                        initTreeMesh(tree, x, y, z);
                        return;
                    }
                }
            }
            return;
        }

        if( rowsUntilPortalSpawn<0 ){
            r -= 25;
            if( r <= 0 ){
                if( portal.z < 0 ){
                    rowsUntilPortalSpawn = Math.random((level+10)*3, (level+10)*5);
                    portal.tint = 0;
                    portal.x = x;
                    portal.y = y;
                    portal.z = z;
                }
                return;
            }
        }else if(portal.z == z && y < portal.y){
            portal.y = y;
        }else{
            portal.y = 110.0f;
        }
        
        r -= 2;
        if( r <= 0 ){
            if( y < Ship.CEILING && stranded.z < 0 && !stranded.init(x, y, z, d) ){
                stranded.initProp();
                initTreeMesh(stranded, x, y, z);
            }
            return;
        }

    }
    
    void update(){
        Node.frameId++;
        /*
        if( count-- == 0 ){
            System.out.print((pointer) "FPS: ");
            System.out.print((long) (int) screen.fps());
            System.out.print((pointer) " RAM: ");
            System.out.println(Runtime.getRuntime().freeMemory());
            count = 64;
        }
        */
        
        if(Button.C.justPressed()){
            Pause.activate();
            return;
        }
        
        if(/*Button.C.justPressed() ||*/ Terrain.yOffset > Terrain.levelLength){
            player.HP = 100;
            player.power = 100;
            EndLevel.activate();
            level++;
            reset();
            return;
        }

        bg.draw(screen, 0, 0);
        screen.setTextPosition(0, 0);

        float worldSpeed;
        player.update();
        worldSpeed = -player.vz;
        if( player.state == Ship.DEAD ){
            if(count == 0){
                GameOver.activate();
                reset();
                return;
            }
            count--;
        }

        Enemy.updateAll();
        Bullet.updateAll();
        
        for( var node : nodes ){
            if( node.z < 0 ) 
                continue;
                
            node.z += worldSpeed;
            
            if( node.z > 0 )
                Terrain.addToDisplayList(node);
        }
        Terrain.addToDisplayList(player);
        Terrain.update(worldSpeed);
        Terrain.draw(screen);

        Node.cameraX = (player.x - Terrain.maxX/2) * 0.7f + Terrain.maxX/2;
        float y = Terrain.getHeightAtPoint(Node.cameraX, Node.cameraZ+70.0f) + 70.0f;
        Node.cameraY = (Node.cameraY*7.0f + (float) Math.max(y, player.y+10)) / 8.0f;
        hud.draw();

        screen.flush();
    }
}
