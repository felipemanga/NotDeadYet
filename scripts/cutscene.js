let palette;
APP.getPalette(pal=>{
    palette = pal;
    let index = {};
    Promise.all(
    dir("cutscenes")
        .filter( name=>/\.png$/i.test(name) )
        .map(name=>{
            return readImage(`cutscenes/${name}`)
                .then(img=>{
                    const parts = name.split("-");
                    (index[parts[0]] = index[parts[0]] || [])[parseInt(parts[1])-1] = img; 
                    return index;
                });
        })
    ).then(_=>Object.keys(index).forEach(key=>convert(index[key], key)));
});

function convert(images, name){
    let out = [images.length];
    images.forEach(img=>{
        const start = out.length;
        const w = img.width;
        const h = img.height;
        const u32 = new Uint32Array(img.data.buffer);
        let ca, cb;
        
        out.push(0);
        out.push(w);
        out.push(h);
        for( let x=0; x<w; ++x ){
            for( let y=0; y<h; y += 8 ){
                let acc = 0;
                for(let i=0; i<8; ++i){
                    let index = ((i+y)*w + x);
                    let rgba = u32[index];
                    
                    if( ca === undefined ){
                        ca = rgba;
                    }else if( cb === undefined && rgba != ca ){
                        cb = rgba;
                    }
                    
                    let bit = rgba != ca;
                    acc |= bit << i;
                }
                out.push(acc);
            }
        }
        
        out[start] = RGB(ca&0xFF, (ca>>8)&0xFF, (ca>>16)&0xFF) << 4;
        out[start] |= RGB(cb&0xFF, (cb>>8)&0xFF, (cb>>16)&0xFF);
    });
    
    write(name + ".bin", new Uint8Array(out));
    log(name + " " + (out.length/1024*10|0)/10 + "kB");
}

function RGB(r, g, b){
    let closestI = -1;
    let closestD = 0x7FFFFFFF;
    for(let i=0; i<palette.length; i++ ){
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
