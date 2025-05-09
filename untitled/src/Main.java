import java.io.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        //System.out.printf("Hello and welcome!");
        byte[] chars,charsbkp =new byte[263]; //This is the amount of the whole array in the ROM. This is for all eight characters.
        byte[] rom=readRom("//media//work//devver//disas//HOTL//anotherexam//editor//Lance.sms");
        String[] charnames={"Goldmoon","Sturm","Caramon","Raistlin","Tanis","Tasslehoff","Riverwind","Flint"};
        String[] itemNames={    //The glitched names and other stuff is not included, just what's okay and won't glitch the game out.
                "No Ranged Weapon\\Empty",
                "Blue Crystal Staff",
                "Staff of Magius",
                "Bow",
                "Longsword",
                "Dagger",
                "Hoopak",
                "Jo Stick",
                "Hunting Knife",
                "Spear",
                "Two handed Sword",
                "Hand Axe",
                "Sword",
                "Sword",
                "Green Quiver",
                "Red Quiver",
                "Pouch",
                "Bracelet",
                "Shield",
                "Shield",
                "Shield",
                "Shield",
                "Shield",
                "Gem",
                "Gem",
                "Gem",
                "Gem",
                "Gem",
                "Gold Bar",
                "Silver Bar",
                "Silver Chalice",
                "Coins",
                "TBD",
                "TBD",
                "TBD",
                "TBD",
                "TBD",
                "TBD",
                "TBD",
                "Bow",
                "Longsword",
                "Sword",
                "Dagger",
                "Hunting Knife",
                "Scroll",
                "Scroll",
                "Green Potion",
                "Orange Potion",
                "Red Potion",
                "Blue Potion",
                "Yellow Potion",
                "Ring",
                "Gem Ring",
                "Wand",
                "Disks of Mishakal",
                "Brown Potion",
                "Game glitch",  //Probably not an item.
                "No Name",
                "Glitch name",
                "Glitch name",
                "Glitch name",
                "Glitch name",
                "Glitch name",
                "Sturm\\Falling Stone",
                "Caramon\\Falling Stone",
                "Raistlin\\Falling Stone",
                "Tanis\\Falling Stone",
                "Tasslehoff\\Small Falling Stones",
                "Riverwind\\Falling Stone",
                "Flint\\Blue rising Proj",
                "Dead Character\\Fire on ground",
                "Glitch\\Arrow Trap",
                "Glitch\\Falling Stone",
                "Glitch\\Falling Stone" //I guess the item\trap names are just falling stones from now.

        };
        for (int i = 0; i <= 16; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            //System.out.println("i = " + i);
            System.out.print(String.format("0x%02X",rom[i])+"    \n");

        }
        chars=getcharstat(rom);
        charsbkp=getcharstat(rom);
        //printchrinv(6,chars,itemNames);
        //printchrstats(1,chars,itemNames);
        byte[] items=extractItems(rom);
        printItems(items,itemNames);
        //saverom2(rom);
    }
    public static byte[] getcharstat (byte[] rom){
    byte[] charstat=new byte[263];
    int romoffset1=0x7D6C;
        for (int i = 0; i < charstat.length; i++) {
        charstat[i]=rom[i+romoffset1];
        }

    return charstat;
    }   //Get the character stats from the original unaltered ROM.
    public static byte[] bkpstats (byte[] orig){
    byte[] work=new byte[orig.length];
        for (int i = 0; i < orig.length; i++) {
        work[i]=orig[i];
        }
    return work;
    } //Copies the original character stats into a backup table in RAM.
    public static byte[] readRom (String filename){
        File file =new File(filename);
        int size=(int)file.length();
        byte[] rom=new byte[size];
        try {
            FileInputStream fis = new FileInputStream(new File(filename));
                fis.read(rom,0,rom.length);
                fis.close();
        } catch (IOException ex){

            ex.printStackTrace();
            System.out.println("There was a problem with the file.");
            System.exit(-1);
        }

        return rom;
    }   //Opens the ROM, and copies the contents into an array for further modification.
    public static void printselectchars(){
        System.out.println("Select a character: \n1: Goldmoon\n2: Sturm\n3: Caramon\n4: Raistlin\n5: Tanis\n6: Tasslehoff\n7: Riverwind\n8: Flint");

    }   //Prints the character selection text on the console.
    public static void saverom2(byte[] input){
try {
FileOutputStream fos=new FileOutputStream(new File("//media//work//devver//disas//HOTL//anotherexam//editor//Lance_mod.sms"));
fos.write(input);
fos.close();
}catch (IOException ex){
    ex.printStackTrace();

}

    }   //This works.
    public static void saverom(byte[] input) throws IOException {
        try {
            String name="//media//work//devver//disas//HOTL//anotherexam//editor//Lance_mod.sms";
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(name));
            outputStream.writeObject(input);

        }catch (IOException ex){
            ex.printStackTrace();
            System.out.println("Error while saving file.");
        }


    }   //Saves the result ROM into a file, but it's broken.
    public static void printchrinv(int chr,byte[] itemarray, String[] itemlist){
        String[] charnames={"Goldmoon","Sturm","Caramon","Raistlin","Tanis","Tasslehoff","Riverwind","Flint"};
        System.out.println("Character selected: "+charnames[chr]);
        var offset=chr*33;  //33 bytes per character.
        for (int i = 0; i < 16; i++) {
            System.out.println(i+": "+itemarray[i+offset]+"    "+itemlist[itemarray[i+offset]]);
        }   //Use this offset value, and print the 16 itemslots.
    }  //So far, this prints the inventory of the character, and nothing else.
    public static void printchrstats (int chr, byte[] stats,String[]itemlist){   //Prints the character's known stats on the screen.
        String[] charnames={"Goldmoon","Sturm","Caramon","Raistlin","Tanis","Tasslehoff","Riverwind","Flint"};
        //The character will be the offset for the array as well. One char is still 33 bytes.
        //As for the itemlist, it's the standard one in the game. We still need the name of the stuff.
        String[] choices={"No","Yes"};
        int offset=chr*33+16;
        System.out.println("Selected Character: "+charnames[chr]);
        System.out.println("Starting item: "+itemlist[stats[offset]]+"    -     "+itemlist[stats[offset+1]]); //Print the item name that is coming from the array of the whole shebang.
        //The second one is also this item thing, not sure why the game stores it like that.
        offset+=2;  //Adjust offset for the next value.
        System.out.println("Is Inventory full? "+choices[stats[offset]]+"   "+choices[stats[offset+1]]);
        offset+=2;
        System.out.println("Hitpoints MAX-MIN: "+stats[offset]+"    "+stats[offset+1]);
        offset+=2;
        System.out.println("Damage MIN-MAX "+stats[offset]+"    "+stats[offset+1]);
        offset+=2;
        System.out.println("Damage2 MIN-MAX "+stats[offset]+"    "+stats[offset+1]);      //I don't know these, but definetly some damage values, but not sure what they control. Maybe Physical and Magic Damage.
        offset+=2;
        System.out.println("Strength: "+stats[offset]);
        offset++;
        System.out.println("Intelligence: "+stats[offset]);
        offset++;
        System.out.println("Wisdom: "+stats[offset]);
        offset++;
        System.out.println("Constitution: "+stats[offset]);
        offset++;
        System.out.println("Dexterity: "+stats[offset]);
        offset++;
        System.out.println("Charisma: "+stats[offset]);
        offset++;
        System.out.println("Unknown: "+stats[offset]);  //Same, I have no idea what this is used for.

    }
    public static byte[] extractItems(byte[] rom){
        byte[] items=new byte[500]; //The array in the ROM is this size.
        /* The ROM has fifty items, plus one byte at the end, to mark the end of the item list. The code looks for that one byte, and loads some zeroes as well.
        Every item is five bytes long:
            -First byte is the item type.
            -Second is the chest visibility.
            -Third is the distance in tiles from the beginning of the floor\room.
            -The fourth byte is not yet known, but so far has no effect on the game. Possibly i'm wrong here.
            -Fifth byte marks if the chest can be made visible with the 'Detect Invisible' spell.
        This all is also in the code, but if you're not keen on combing that through, this is a small excerpt.
          */
    var offset=0x7B16; //In the unaltered ROM, this is the address where this array is.
        for (int i = 0; i < items.length; i++) {
            items[i]=rom[i+offset];
        }
        return items;
    }   //The array of items is copied from the ROM to an array, and that's returned. The last zero byte is not included.
    public static void printItems(byte[] items,String[] itemNames) {    //Prints all items in a CSV fashion on the screen.
    String[] visibility={"Visible","Invisible"};
    int roomnr;
    int distance;
    int fourththbyte;
    int chestvisible;
        for (int i = 0; i < items.length/5; i++) {
            roomnr=items[(i*5)+1] & 0xff;
            distance=items[(i*5)+2] & 0xff;
            fourththbyte=items[(i*5)+3] & 0xff;
            chestvisible=items[(i*5)+4] & 0xff;
            System.out.println(i+": "+itemNames[items[i*5]]+" | Room Nr.: "+roomnr+" \\Hex:"+String.format("$%02X",items[(i*5)+1])+" | Dist from room bgn in tiles: "+distance+"\\Hex:"+String.format("$%02X",distance)+" | 4th byte: "+fourththbyte+" \\ Hex: "+String.format("$%02X",fourththbyte)+" | Chest visible: "+chestvisible+" \\ Hex: "+String.format("$%02X",chestvisible));
        }
    }   //Prints the ROM's item and it seems to be trap content, where they are, and what is in the boxes.
}