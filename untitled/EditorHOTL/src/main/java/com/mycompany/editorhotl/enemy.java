package com.mycompany.editorhotl;

public class enemy {
    //TODO: Enemies have health and things like that, maybe it should be mapped, and code to modify them.
    public static void printNme(byte[] rom){

        String[] nmeTypes={
                "Nothing\\Empty",
                "Goldmoon",
                "Sturm",
                "Caramon",
                "Raistlin",
                "Tanis",
                "Tasslehoff",
                "Riverwind",
                "Flint",
                "Baaz (Grey sword gargoyle)",
                "Blue Gargoyle",
                "Troll",
                "Blue Ghost",
                "Transp. Soldier",
                "Large Dwarf",
                "Dwarf",
                "Soldier",
                "Spider",
                "Small Dragon",
                "Endboss Dragon",
                "Empty\\glitch",
                "Confined Dragon",
                "nothing\\glitch",
                "glitch"
        };
        var arrayoffset=0x06;   //In the code, enemies are stored in the array in six byte groups.
        var offset=0x72d1;      //In the unaltered ROM, this is where the array\\list of the enemies are. Other stuff is also there, but for now, I'm only interested in the room nr, and the monster type, but
        var nmeOffset=offset-1;
        int arraysize=110;
        offset++;           //The room number is the second byte, and every sixth byte after that is the next one.
        var roomnr=0;
        var nmetype=0;
        var nmexcoord=0;
        var screennr=0;
        System.out.println(String.format("0x%02X",rom[offset]));
        for (int i = 0; i < arraysize; i++) {
            roomnr=rom[offset+(i*arrayoffset)]; //Calculate the Room number from the array.
            nmetype=rom[(offset-1)+(i*arrayoffset)];
            nmetype+=8;
            nmexcoord=rom[(offset+1)+(i*arrayoffset)]&0xff;
            screennr=rom[(offset+2)+(i*arrayoffset)]&0xff;
            System.out.println(i+" Room Nr.: "+roomnr+" \\Hex: "+String.format("0x%02X",roomnr)+"   Monster Type: "+nmetype+"   "+nmeTypes[nmetype]+"   "+"Enemy coordinate: "+nmexcoord+"    \\Hex: "+String.format("0x%02X",nmexcoord)
                    +"  Screen nr.: "
                    +screennr
            );
        }
    }   //Prints monsters, and where they are. A separate monster class would be nice for this.
public void checkNMEArray(byte[]NMEArray){
    enemy dataFrom=new enemy();
    var arrayoffset=0x06;   //In the code, enemies are stored in the array in six byte groups.
    var offset=0x72d1;      //In the unaltered ROM, this is where the array\\list of the enemies are. Other stuff is also there, but for now, I'm only interested in the room nr, and the monster type, but
    var nmeOffset=offset-1;
    int arraysize=110;
    offset++;           //The room number is the second byte, and every sixth byte after that is the next one.
    var roomnr=0;
    var nmetype=0;
    var nmexcoord=0;
    var screennr=0;
byte[] oneNME=new byte[6];
    //We have to check the enemy array size, if it's right.
    if (NMEArray.length!=660){System.out.println("The size of the array is not right. "+NMEArray.length);System.exit(-1);}  //If the size is not right, then just exit.
    for (int i = 0; i < 110; i+=6) {
    oneNME[i]=NMEArray[i];
    oneNME[i+1]=NMEArray[i+1];

    }

}
    }
