package com.mycompany.editorhotl;

/**
 *  This class modifies companion details and other data.
 *  -Can rename players.
 *  -Print a given char's stats.
 *  -Print char inventory.
 *  -Backup char stats.
 *  -Insert back stats in the ROM.
 */


public class companion {
    Item charItem=new Item();   //We can use now item names, since the array is in another class.
    int romoffset1=0x7D6C;  //This is the ROM offset for an unaltered ROM.
    int nameROMOffset=0x6D38;   //The unaltered retail ROM uses this address to store the Companion names.
    /*
    The char names are 16 bytes long. The very first byte is a zero, then the ASCII names, and lastly an $FF to close all of them.
    There are eight companions of course, and one extra mark a dead character. The last one does not really needed to be rewritten, but you'll get the chance anyway.
    * */
    /*Of course, you have to enter another value for a modified ROM.
    * */
    public byte[] changeNames(byte[]rom, byte[]names) {
        byte[] result=new byte[rom.length];
        result=rom;
        int nameROMOffset=0x6D38;   //The unaltered retail ROM uses this address to store the Companion names.
      System.arraycopy(names, 0, result, nameROMOffset, names.length);
return result;
    }
    public static void printchrstats (int chr){   //Prints the character's known stats on the screen.
        String[] charnames={"Goldmoon","Sturm","Caramon","Raistlin","Tanis","Tasslehoff","Riverwind","Flint"};
        rom LanceROM=new rom(); //Open the ROM, to get data from it.
        Item charItem=new Item();   //We can use now item names, since the array is in another class.
        byte[] gameROM=rom.readRom(LanceROM.outFull);   //Read the game rom in this way, use classes and stuff.-
        Item item=new Item();
        //TODO: Make sure, the code handles later on the proto ROM, and the modified hack.----
        byte[] chrStat=getcharstat(gameROM);    //Extract the character stats from an unaltered ROM, and this is only for the normal game, not the proto either.
                                                // I have not looked at that yet.
        //The character will be the offset for the array as well. One char is still 33 bytes.
        int offset=chr*33+16;   //Calculating the correct offset for each character. The sixteen bytes at the last position are the inventory part.
        System.out.println("Character to check: "+charnames[chr]);    //Check what char\companion we have, and then print the name.
        if(chr==4|chr==5|chr==6){ //Tanis, Tasslehoff, Riverwind
            System.out.println("Item The Companion starts with : "+charItem.itemNames[chrStat[offset]]+" Second item byte (Invalid):  "+(chrStat[offset+1]&0xFF));    //Print the item name that the characted is holding. This is two bytes, but the latter one is the same as the first, not sure why this is done this way.
        }else {
            System.out.println("Item The Companion starts with : " + charItem.itemNames[chrStat[offset]] + " Second item byte:  " + charItem.itemNames[chrStat[offset + 1]]);    //Print the item name that the characted is holding. This is two bytes, but the latter one is the same as the first, not sure why this is done this way.
        }
/*NOTE: The second byte is pointing to a non-existent item, and would otherwise cause my code to go on an error.) This is a small workaround.
* *///The next two bytes are skipped, as it's just to tell if the character inventory is full or not. The base game does not set this.
        System.out.println("Hit Points Minimum: "+chrStat[offset+4]+"   Hit Points Maximum: "+chrStat[offset+5]);   //This is always the same, but maybe not for the hack.
        System.out.println("Damage Points 1 Min: "+chrStat[offset+6]+"   Damage Points 1 Max: "+chrStat[offset+7]);
        System.out.println("Damage Points 2 Min: "+chrStat[offset+8]+"   Damage Points 2 Max: "+chrStat[offset+9]);
        System.out.println("Strength: "+chrStat[offset+10]+"   Intelligence: "+chrStat[offset+11]);
        System.out.println("Wisdom: "+chrStat[offset+12]+"   Constitution: "+chrStat[offset+13]);   //No idea if the code does anything with it, but I guess not.
        System.out.println("Dexterity: "+chrStat[offset+14]+"   Charisma: "+chrStat[offset+15]);    //Charisma does NOTHING in this game. There's no one to talk to.
        System.out.println("Unknown: "+chrStat[offset+16]); // I have no idea what this does, if it matters at all.
    }   //Of course, this prints the character stats,
    public static void printchrinv(int chr) {   //Does what it says on the can. Prints a character's inventory.
        String[] charnames = {"Goldmoon", "Sturm", "Caramon", "Raistlin", "Tanis", "Tasslehoff", "Riverwind", "Flint"};
        rom LanceROM = new rom(); //Open the ROM, to get data from it.
        Item charItem = new Item();   //We can use now item names, since the array is in another class.
        byte[] gameROM = rom.readRom(LanceROM.outFull);   //Read the game rom in this way, use classes and stuff.-
        Item item = new Item();
        //TODO: Make sure, the code handles later on the proto ROM, and the modified hack.----
        byte[] chrStat = getcharstat(gameROM);    //Extract the character stats from an unaltered ROM, and this is only for the normal game, not the proto either.

        //The character will be the offset for the array as well. One char is still 33 bytes.
        int offset = chr * 33 ;   //Calculating the correct offset for each character. We need this time the first 16 bytes, as that's the inventory. You can have eight items
        //so I don't know what's up with the two-byte items.
        System.out.println("Character to check: " + charnames[chr]);    //Check what char\companion we have, and then print the name.
        for (int i = 0; i < 7; i++) {
            System.out.println(charItem.itemNames[chrStat[offset+(i*2)]]);  //Every second byte is fine. For some strange reason both bytes have to be the same.
        }
    }//As the name suggests, it prints the character inventory.
    public static byte[] getcharstat (byte[] rom){  //Extracts character stats from the ROM.
        byte[] charstat=new byte[264];
        int romoffset1=0x7D6C;
        System.arraycopy(rom, romoffset1, charstat, 0, charstat.length);
        return charstat;
    }   //Get the character stats from the original unaltered ROM.
    public byte[] insCharStat(int companion, byte[] compstat, byte[]rom){
        int finalOffset=this.romoffset1+(companion*33); //Calc the final offset, where we'll put the data back.
        //With the calculated offset removed, we put back the new data into the compation stat.
        System.arraycopy(compstat, -this.romoffset1, rom, 0, 33);

        return rom; //Return the modified input.
    }   //Insert the modified character stat into the ROM. Preferably in RAM, then which we will write to disk.
                                                                                /* This also enters the inventory as well, if needed.
                                                                                *  You still have to somehow enter the inventory, even if it's not modified.*/
    public static byte[] bkpstats (byte[] orig){    //Backs up these stats to a backup array, so we will modify this one, instead of the actual one.
        byte[] work=new byte[orig.length];
        System.arraycopy(orig, 0, work, 0, orig.length);
        return work;
    } //Copies the original character stats into a backup table in RAM.

}
