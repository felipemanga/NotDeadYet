let palette;
const skin = [255, 255, 255];
const w = 40;
const h = 100;

APP.getPalette(pal=>{
    palette = pal;
    dir("meshes")
        .filter( name=>/\.obj$/i.test(name) )
        //.filter( f=>f=="Jet.obj")
        .map( name => parseObj(read(`meshes/${name}`), name) )
        .forEach( mesh => exportMesh(mesh) );
});

function kv( str ){
    return str
        .split("\n")
        .map(line=>line.trim().split(/\s+/))
        .reduce((obj, line)=>{
            let cmd = line.shift();
            if( cmd.length && cmd != "#")
                (obj[cmd] = obj[cmd] || []).push(line);
            return obj;
        }, {});
}

function parseObj(str, name){
    let mtllib = {};
    let cmtl = null;
    
    let mesh = str
        .split("\n")
        .map(line=>line.trim().split(/\s+/))
        .reduce((obj, line)=>{
            let cmd = line.shift();
            if( cmd == "mtllib" ){
                
                read(`meshes/${line[0]}`)
                    .split(/\n(?=newmtl )/)
                    .forEach(str=>{
                        const mtl = kv(str);
                        if( mtl.newmtl )
                            mtllib[mtl.newmtl[0]] = mtl;
                    });
                    
            } else if( cmd == "usemtl" ) {
                
                cmtl = mtllib[line[0]];
                
            } else if( cmd == "f" ) {

                let c = 0;
                if( cmtl ){
                    if( !("c" in cmtl) ){
                        cmtl.c = RGB(... (cmtl.Kd[0].map(x=>255*x)) )|0;
                    }
                    c = cmtl.c;
                }

                line = line.map(p => p.split("/").map(i=>i|0) );
                line.c = c;
            }
            
            (obj[cmd] = obj[cmd] || []).push(line);
            return obj;
        }, {});
    
    mesh.v = mesh.v.map(v=>v.map(f=>parseFloat(f)));
    mesh.name = name;

    return mesh;
}

function exportMesh(mesh){
    let minX=0xFFFFFF, maxX = -0xFFFFFF;
    let minY=0xFFFFFF, maxY = -0xFFFFFF;
    let minZ=0xFFFFFF, maxZ = -0xFFFFFF;
    let remap = [];

    mesh.f = mesh.f.filter((p, fi)=>{
        const bad = p.findIndex(([i])=>mesh.v[i-1] === undefined);
        if( bad != -1 ){
            throw `${name} bad index: ${JSON.stringify(p)}[${bad}] on face ${fi}`;
        }
        
        p.y = 0;
        p.forEach(([i])=>{
            p.y += mesh.v[i-1][1];
            mesh.v[i-1].push(true);
        });

        return true;
    });
    
    let newI = 0;
    mesh.v = mesh.v.filter( ([x, y, z, u], i)=>{
        remap[i] = newI;
        if( !u ) return false;
        newI++;
        return true;
    });
    
    mesh.v.forEach(([x, y, z], i)=>{
        if( x < minX ) minX = x;
        if( x > maxX ) maxX = x;
        if( y < minY ) minY = y;
        if( y > maxY ) maxY = y;
        if( z < minZ ) minZ = z;
        if( z > maxZ ) maxZ = z;
    });

    let scaleX = 127 / (maxX - minX);
    let scaleY = 127 / (maxY - minY);
    let scaleZ = 127 / (maxZ - minZ);
    
    scaleX = scaleY = scaleZ = Math.min(scaleX, scaleY, scaleZ);

    const name = mesh.name.replace(/\.obj/g, '');

    log("Exporting ", name);
    if(mesh.f.length > 0xFFFF){
        log(`Too many faces in ${name}: ${mesh.f.length}`);
        mesh.f.length = 0xFFFF;
        return;
    }

    const vtxSize = 3;
    const totalSize = 3 + mesh.f.length * 4 + mesh.v.length * vtxSize;

    const bytes = new Uint8ClampedArray(totalSize);
    const sbytes = new Int8Array(bytes.buffer);
    
    let p = 0;
    bytes[p++] = mesh.f.length >> 8;
    bytes[p++] = mesh.f.length & 0xFF;
    bytes[p++] = mesh.v.length;
    
    mesh.f = mesh.f.sort((a, b) => a.y - b.y);

    mesh.f.forEach((f, c)=>{
        bytes[p++] = f.c|0;
        f.forEach(([i, uvi, ni], index)=>{
            if( i-1 > 255 ){
                throw new Error(`I overflow in ${name}: ${i}`);
            }
            if( index >= 3 ){
                throw new Error(`${name} not triangulated: ${i}`);
            } 
            bytes[p++] = remap[i - 1];
        });
    }); 
    
    let v0 = p;
    
    mesh.v.forEach(([x, y, z], i)=>{
        x = ((x - minX) - (maxX - minX)/2) * scaleX;
        y = ((y - minY) - (maxY - minY)/2) * scaleY;
        z = ((z - minZ) - (maxZ - minZ)/2) * scaleZ;
        if( x < -128 || x > 127 ) log("Invalid X: " + x);
        if( y < -128 || y > 127 ) log("Invalid Y: " + y);
        if( z < -128 || z > 127 ) log("Invalid Z: " + z);
        
        sbytes[p++] = x;
        sbytes[p++] = y;
        sbytes[p++] = z;
    });

    if( p != bytes.length )
        log(name + " size mismatch:", p, bytes.length);
        
    write(`meshes/${name[0].toUpperCase() + name.substr(1)}.bin`, bytes);
    // log(`Wrote: meshes/${name[0].toUpperCase() + name.substr(1)}.bin`)
}

function RGB(r, g, b){
    let closestI = 0;
    let closestD = 0x7FFFFFFF;
    for(let i=1; i<palette.length; i++ ){
        let [cr, cg, cb] = palette[i];
        cr -= r; cg -= g; cb -= b;
        let d = cr*cr + cg*cg + cb*cb;
        if( d < closestD ){
            closestD = d;
            closestI = i;
        }
    }
    return closestI;
}
