import femto.mode.HiRes16Color;
import math.Vector4;

public class Terrain {
    static final int levelLength = 300;
    static final short[] point = new short[16*16*3];
    static final int XS = 64;
    static final int ZS = 64;
    static final float YS = 3;
    static int yOffset;
    static float z;
    static final int maxX = 16*XS;
    static final Node[] displayList = new Node[ 16 * 4 ];

    static int key;
    static int smooth = 1;
    static int waterLevel = 0x30;
    static int W0=0, W1=2, W2=4;
    static boolean valley = true;
    
    static boolean isOutOfBounds(float x, float z){
        return x < 0 || x > 16*XS || z < 0 || z > 16*ZS;
    }
    
    static void reset(int k){
        z = 0;
        yOffset = 0;
        key = (k * 1664525) ^ (k * 48271);

        waterLevel = noise(2, 3, 8) & 0x1F;
        W0 = noise(3, 4, 9) & 0x3;
        W1 = W0 + ((noise(3, 4, 9) & 0x3)+1);
        W2 = W1 + ((noise(4, 5, 10) & 0x3)+1);
        smooth = noise(7,12,13) % (1+W0); // ((3 - W0) >> 1);
        if(smooth < 0) smooth = 0;
        valley = (noise(5,6,11)&1) == 0;

        System.out.print("Terrain ");
        System.out.println(key);
        System.out.println("Smooth " + smooth);
        System.out.println(W0 + " " + W1 + " " + W2);
        System.out.println("Water " + waterLevel);
    }
    
    static void addToDisplayList(Node node){
        int c = (((int)node.z) >> 6) + 1;
        if( c < 0 ) return;// c = 0;
        if( c >= 15 ) c = 15;
        c *= 4;
        
        for( ; c<displayList.length; ++c ){
            var n = displayList[c];
            if( n == null ){
                displayList[c] = node;
                return;
            }else if( n.z > node.z ){
                displayList[c] = node;
                node = n;
            }
        }
    }
    
    static float getHeightAtPoint(float wx, float wz){
        float dx = (wx / XS);
        float dz = ((wz - z) / ZS);
        int ix = (int) dx;
        int iz = (int) dz;
        dx -= ix;
        dz -= iz;
        
        float height = 0.0f;
        if((dx + dz) < 1.0f){
            float a = getPoint(ix, iz) * YS;
            float b = getPoint(ix+1, iz) * YS;
            float c = getPoint(ix, iz+1) * YS;

            height = a;
            height += (b - a) * dx;
            height += (c - a) * dz;
        }else{
            float d = getPoint(ix+1, iz+1) * YS;
            float b = getPoint(ix+1, iz) * YS;
            float c = getPoint(ix, iz+1) * YS;

            height = d;
            height += (b - d) * (1.0f - dz);
            height += (c - d) * (1.0f - dx);
        }
        
        return height;
    }
    
    static boolean hasNew;
    
    static void update(float speed){
        z += speed;
        // shift
        if( z < 0 ){
            while( z < -ZS ){
                yOffset++;
                z += ZS;
                hasNew = true;
            }
        }else{
            while( z > ZS ){
                hasNew = true;
                yOffset--;
                z -= ZS;
            }
        }
    }
    
    static void draw(HiRes16Color screen){
        int color;
        for( int y=0; y<16; ++y ){
            for( int x=0; x<16; ++x ){
                int i = (y*16+x)*3;
                float a = getPoint(x, y);
                float Z = Vector4.projectZ(y * ZS + z - Node.cameraZ);
                point[i] = (int) Vector4.projectX(x * XS - Node.cameraX, Z);
                point[i+1] = (int) Vector4.projectY(a * YS - Node.cameraY, Z);
                point[i+2] = (int) a;
            }
        }

        if( hasNew ){
            hasNew = false;
            for( int x=0; x<14; ++x ){
                int a = point[(14*16+x)*3+2];
                Main.instance.newTile(
                    x*XS+(XS/2), 
                    14.5*ZS, 
                    Math.abs(a - point[(15*16+x)*3+2]) + Math.abs(a - point[(14*16+x+1)*3+2]) + Math.abs(a - point[(15*16+x+1)*3+2]),
                    x,
                    yOffset+14
                );
            }
        }

        int dli = displayList.length - 1;
        
        for( int y = 14; y >= 0; --y ){

            for( int x=0; x<15; ++x ){
                color = 1;
                float a = point[(y*16 + x)*3+2];
                float b = point[(y*16 + x+1)*3+2];
                float c = point[((y+1)*16 + x)*3+2];
                float d = point[((y+1)*16 + x+1)*3+2];
                
                if(a+b+c+d==0) color = 13;
                else color = 1;

                Node.pointA.x = point[(y*16 + x)*3];
                Node.pointA.y = point[(y*16 + x)*3+1];
                Node.pointB.x = point[(y*16 + x+1)*3];
                Node.pointB.y = point[(y*16 + x+1)*3+1];
                Node.pointC.x = point[((y+1)*16 + x)*3];
                Node.pointC.y = point[((y+1)*16 + x)*3+1];
                if( (Node.pointA.x - Node.pointB.x)*(Node.pointA.y - Node.pointC.y) - (Node.pointA.y - Node.pointB.y)*(Node.pointA.x - Node.pointC.x) < 0 ){
                    screen.fillTriangle(
                                 (int) Node.pointA.x, (int) Node.pointA.y, 
                                 (int) Node.pointB.x, (int) Node.pointB.y, 
                                 (int) Node.pointC.x, (int) Node.pointC.y, 
                                 color
                                );
                }

                Node.pointA.y = point[((y+1)*16 + x+1)*3+1];
                Node.pointA.x = point[((y+1)*16 + x+1)*3];
                if( (Node.pointA.x - Node.pointB.x)*(Node.pointA.y - Node.pointC.y) - (Node.pointA.y - Node.pointB.y)*(Node.pointA.x - Node.pointC.x) > 0 ){
                    screen.fillTriangle(
                                 (int) Node.pointA.x, (int) Node.pointA.y, 
                                 (int) Node.pointB.x, (int) Node.pointB.y, 
                                 (int) Node.pointC.x, (int) Node.pointC.y, 
                                 color+1
                                );
                }
            }

            for( int i=0; i<4; ++i, --dli ){
                if( displayList[dli] == null ) 
                    continue;
                displayList[dli].draw(screen);
                displayList[dli] = null;
            }
       }
    }
    
    static int noise(int x, int y, int t){
        int pkey = key * t;
        x *= 22695477; // 1947;
        y *= 1103515245; // 59;
        return ((pkey * ((x<<2) ^ (x >> 6) ^ (y << 1) ^ (y >> 7))) >> 8) & 0x7F;
        // return ((pkey^x)*(pkey^y))&0x7F;
    }
    
    static byte getPoint(int x, int y){
        y += yOffset;
        
        if( (y < 16 && x >= 7 && x <= 9) || y > levelLength )
            return 1;
            
        int v =
            + (noise((x>>4), (y>>4)-(y>>2), 91))
            + (noise((x>>2), (y>>2), 13)>>W0)
            + (noise((x>>1), (y>>1)+1, 47)>>W1)
            ;
        v >>= smooth;
        v -= waterLevel;

/**/
        int P = (noise(99, y>>2, 17)>>1) 
              + (noise(42*y, y>>1, 31)>>2)
              + (noise(71*y, y, 9)>>3);
        P >>= 2;
        if( P > 14 ) P = 14;
        else if( P < 1 ) P = 1;
        if( P > x ) P -= x;
        else P = x - P;
        P-=2;
        if( P<0 ) P = 0;
        P = P*P*P + 20;
        while(v > P){
            v >>= 1;
        }
/**/

        if( v > 0x7F ) return 0x7F;
        if( v < 0 ) return 0;
        return v;
    }
    
}
