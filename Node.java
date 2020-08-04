import femto.mode.HiRes16Color;
import static java.lang.System.memory.*;
import math.Vector4;
import math.Matrix4;

public class Node {
    pointer mesh;

    static final Matrix4 transform = new Matrix4();
    static final Vector4 pointA = new Vector4();
    static final Vector4 pointB = new Vector4();
    static final Vector4 pointC = new Vector4();
    
    static float cameraX = 0, cameraY = 150;
    static final float cameraZ = -Terrain.ZS/2;
    static float FOV = 64;
    static final float near = 5;
    int tint;

    public Node(pointer mesh){
        this.mesh = mesh;
    }
    
    float x = 0, y = 0, z = 0;
    float rotZ = 0;
    float rotY = 0;
    float rotX = 0;
    float scale = 1;
    
    int lastFrameId;
    static int frameId;
    
    void updateTransform(){
        transform.setTranslation(x-cameraX, y-cameraY, z-cameraZ);
        if( rotX != 0 || rotY != 0 || rotZ != 0 )
            transform.rotate(rotX, rotY, rotZ);
        if( scale != 1 )
            transform.scale(scale);
    }

    public void draw(HiRes16Color screen){
        if( lastFrameId == frameId ){
            System.out.println("Double Draw");
            return;
        }
        lastFrameId = frameId;
        
        updateTransform();
        render(screen);
    }
    
    void render(HiRes16Color screen){
        int faceCount = (((int)LDRB(mesh)) << 8) + (int)LDRB(mesh+1);
        int vtxCount = LDRB(mesh+2);
        pointer faceOffset = mesh + 3;
        pointer vtxOffset = faceOffset + faceCount*4;

        var A = pointA;
        pointer Ai = null;
        var B = pointB;
        pointer Bi = null;
        var C = pointC;
        pointer Ci = null;
        boolean Ab, Bb, Cb;

        for( int face = 0; face<faceCount; ++face ){
            int color = LDRB(faceOffset++);
            pointer indexA = vtxOffset + ((int)LDRB(faceOffset++)) * 3;
            pointer indexB = vtxOffset + ((int)LDRB(faceOffset++)) * 3;
            pointer indexC = vtxOffset + ((int)LDRB(faceOffset++)) * 3;
            
            Ab = Bb = Cb = false;
            
            if( indexA == Ai ){ Ab = true; }
            else if( indexA == Bi ){ A = pointB; Bb = true; }
            else if( indexA == Ci ){ A = pointC; Cb = true; }
            else A = null;

            if( indexB == Bi ){ Bb = true; }
            else if( indexB == Ai ){ B = pointA; Ab = true; }
            else if( indexB == Ci ){ B = pointC; Cb = true; }
            else B = null;

            if( indexC == Ci ){ Cb = true; }
            else if( indexC == Bi ){ C = pointB; Bb = true; }
            else if( indexC == Ai ){ C = pointA; Ab = true; }
            else C = null;
            
            if( A==null ){
                if( !Ab ){ A = pointA; Ab = true; }
                else if( !Bb ){ A = pointB; Bb = true; }
                else if( !Cb ){ A = pointC; Cb = true; }
                
                A.x = (byte)LDRB( indexA++ );
                A.y = (byte)LDRB( indexA++ );
                A.z = (byte)LDRB( indexA );
                A.mul(transform);
                if(A.z <= near) continue;
                A.project();
            }

            if( B==null ){
                if( !Ab ){ B = pointA; Ab = true; }
                else if( !Bb ){ B = pointB; Bb = true; }
                else if( !Cb ){ B = pointC; Cb = true; }
    
                B.x = (byte)LDRB( indexB++ );
                B.y = (byte)LDRB( indexB++ );
                B.z = (byte)LDRB( indexB );
                B.mul(transform);
                if(B.z <= near) continue;
                B.project();
            }

            if( C==null ){
                if( !Ab ){ C = pointA; Ab = true; }
                else if( !Bb ){ C = pointB; Bb = true; }
                else if( !Cb ){ C = pointC; Cb = true; }

                C.x = (byte)LDRB( indexC++ );
                C.y = (byte)LDRB( indexC++ );
                C.z = (byte)LDRB( indexC );
                C.mul(transform);
                if(C.z <= near) continue;
                C.project();
            }

            if( (A.x - B.x)*(A.y - C.y) - (A.y - B.y)*(A.x - C.x) < 0 ) 
                continue;

            screen.fillTriangle(
                             (int) A.x, (int) A.y, 
                             (int) B.x, (int) B.y, 
                             (int) C.x, (int) C.y, 
                             (color + tint) & 0xF
                            );
        }
        
    }
}
