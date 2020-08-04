public class Traveler {
    String name;
    
    boolean isMale;
    int mood; // 0 - 1000. Increased risk of committing suicide at 0.
    int energy; // 0 - 1000. Low energy increases chance of fumbling.
    
    Traveler(boolean forceFemale){
        isMale = forceFemale ? false : Math.random(0, 100) > 50;
        mood = 500;
        energy = 500;
        name = createName();
    }
    
    String getNamePart(String name, int part){
        int start = 0;
        char c;
        
        while(part != 0){
            c = name[start++];
            if( c==0 )
                start = 0;
            else if( c == ',' )
                part--;
        }
        
        int end = start;
        while((c = name[end++]) != 0 && c != ',');

        return name.substring(start, end-1);
    }
    
    String createName(){
        String firstPart, midPart, endPart;

        if( isMale ){
            firstPart = "Jo,Bo,Bu,Ma,Fi,He,Ne,O,Ke";
            midPart = "rg,sh,ch,tch,n,lip,l,ck,v,x,r,tt";
            endPart = "e,o,on,er,in,,,";
        }else{
            firstPart = "Ann,Em,Yok,Ther,Brun,Mar,Del,Fran";
            midPart = "el,,,il,on";
            endPart = "ia,a,ta,isa,ie,dy,da,ine";
        }

        return getNamePart(firstPart, Math.random(0, 20)) + 
               getNamePart(midPart, Math.random(0, 20)) + 
               getNamePart(endPart, Math.random(0, 20));
    }
}