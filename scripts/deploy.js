//!APP-HOOK: onCompileComplete

if( DATA.os == "windows") windows();
else if( DATA.os == "linux" ) linux();
else APP.error("Unsupportd OS");

function $(str){
    return invoke(...str.split(" "));
}

// windows
async function windows(){
    const list = await $(`wmic logicaldisk list brief`);
    const line = list.split("\n").find(s=>s.indexOf("CRP DISABLD") != -1);
    if( !line ) return;
    const drive = line.split(/\s+/)[0];
    const target = path.join(drive, "firmware.bin");
    const src = fs.readFileSync(path.join(DATA.projectPath, DATA.projectName + ".bin"));
    fs.writeFileSync(target, src);
    log("Pokitto updated");
}

// linux
async function linux(){
    const [,bus,,device] = (await $(`lsusb -d 1fc9:0017`)).split(/\s+/);
    const udev = `/dev/bus/usb/${bus}/${device.replace(':', '')}`;
    const info = await $(`udevadm info -q property -n ${udev}`);
    const pass = info.indexOf("\nID_MODEL=LPC1XXX_IFLASH\n") != -1;
    if( !pass ){
        APP.error("Unknown device");
        APP.log(info);
        return;
    }
    
    const devpath = (await $(`udevadm info -q path -n ${udev}`)).trim();
    const disks = fs.readdirSync("/dev/")
                  .filter(name=>/sd[a-z]+/.test(name));
    const devpaths = await Promise.all(disks.map(disk => $(`udevadm info -q path -n ${disk}`)));
    const devdiskNum = devpaths.findIndex( path => path.startsWith(devpath));
    if( devdiskNum == -1 ){
        APP.error("Could not find " + devpath);
        return;
    }
    
    const disk = `/dev/${disks[devdiskNum]}`;
    const mount = (await $(`mount`))
        .split("\n")
        .filter(line=>line.startsWith(disk))
        [0] || "";
        
    if(mount === ""){
        APP.error("Not mounted " + disk);
        return;
    }
    
    const target = (mount.match(/ on (.*?) type vfat/)||[])[1] + "/firmware.bin";
    const dd = await invoke("dd", "bs=1024", "conv=nocreat,notrunc", "if=${projectPath}/${projectName}.bin", "of="+target);
    const umount = await invoke("umount", disk);
    log("Pokitto updated");
}