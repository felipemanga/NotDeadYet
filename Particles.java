import femto.mode.HiRes16Color;

public class Particles {
    static final int DataSize = 7;
    float[] data;
    float x, y, z;

    float svx, svy, svz;
    float ax, ay, az;
    float ageRate = 1;
    float startAge = 1, endAge = 10;
    
    long lastSpawnTime;
    int spawnRate = 10;
    int color;
    
    Particles(int count){
        data = new float[count*DataSize];
    }
    
    void spawn(){
        int oldestId = 0;
        lastSpawnTime = System.currentTimeMillis();
        
        for( int i=0; i<data.length; i+=DataSize ){
            if( data[i] == endAge ){
                oldestId = i;
                break;
            }
            if( (ageRate > 0 && data[i] > data[oldestId]) || (ageRate < 0 && data[i] < data[oldestId]) )
                oldestId = i;
        }
        
        int i = oldestId;
        data[i++] = startAge;
        data[i++] = svx;
        data[i++] = svy;
        data[i++] = svz;
        data[i++] = x;
        data[i++] = y;
        data[i++] = z;
    }
    
    void draw(HiRes16Color screen){
        if( spawnRate > 0 ){
            long now = System.currentTimeMillis();
            if( now >= lastSpawnTime + spawnRate ){
                spawn();
            }
        }
        
        for( int i=0; i<data.length; ){
            int id = i;
            float age = data[i] + ageRate; data[i++] = age;
            float vx = data[i] + ax; data[i++] = vx;
            float vy = data[i] + ay; data[i++] = vy;
            float vz = data[i] + az; data[i++] = vz;
            float px = data[i] + vx; data[i++] = px;
            float py = data[i] + vy; data[i++] = py;
            float pz = data[i] + vz; data[i++] = pz;
            if( (ageRate > 0 && age >= endAge) || (ageRate < 0 && age <= endAge) ){
                data[id] = endAge;
                continue;
            }
            
            Node.pointA.x = px - Node.cameraX;
            Node.pointA.y = py - Node.cameraY;
            Node.pointA.z = pz - Node.cameraZ;
            Node.pointA.project();
            
            int radius = (int) age; // (int) (Node.pointA.z * age + 0.5);
            
            if( radius < 1 )
                continue;
            
            if( (id&1) == 0 )
                screen.drawCircle((int) Node.pointA.x, (int) Node.pointA.y, radius, color);
                
                
            int sx = (int) Node.pointA.x - radius;
            int ex = (int) Node.pointA.x + radius;
            int sy = (int) Node.pointA.y - radius;
            int ey = (int) Node.pointA.y + radius;
            final int m = 1;
            if( sx < m*2 ) sx = m*2;
            if( ex > screen.width() - m*2 ) ex = screen.width() - m*2;
            if( sy < m*2 ) sy = m*2;
            if( ey > screen.height() - m*2 ) ey = screen.height() - m*2;

            sx >>= 1;
            ex >>= 1;

            var buffer = screen.buffer;
            for( int py = sy; py < ey; py += 2 ){
                for( int px = sx; px < ex; ++px ){
                    int o = (py + Math.random(-m, m)) * 110 + px + Math.random(-m, m);
                    buffer[ o ] = buffer[py*110 + px];
                    buffer[ o + 110 ] = buffer[(py+1)*110 + px];
                }
            }
        }
    }
}