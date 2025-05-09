import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Random;
import java.util.Scanner;
public class editorjava {

    public static void main(String[] args) {
        System.out.println("Ayylmao!");
        //System.out.prbyteln(3+3);
        byte[] inventory=new byte[16];  //Create a new array for one inventory.
        byte[] startingItem=new byte[2]; //What does the character starts with.
        byte[] inventoryFull =new byte[2];
        byte[] hitpobytes=new byte[2];
        byte[]   dmgPhys=new byte[2];
        byte[]   dmgMgc=new byte[2];  //min-max
        byte     Strength;
        byte byteelligence;
        byte Wisdom;
        byte Constitution;
        byte Dexterity;
        byte Charisma;
        byte   unknown5;
        int statSize=264;   //how big the character definition length.
        int plyrstatsOffset=0x7D6C; //Offset where the stats are in the ROM.
        String[] charnames={"Goldmoon","Sturm","Caramon","Raistlin","Tanis","Tasslehoff","Riverwind","Flint"};
        String[] itemNames={    //The glitched names and other stuff is not included, just what's okay and won't glitch the game out.
        "No Ranged Weapon",
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
        "Brown Potion"
                    };
        //RandomAccessFile in =new RandomAccessFile("/media/work/devver/disas/HOTL/anotherexam/editor/editor/Lance.sms", "r");
    
        File myObj = new File("/media/work/devver/disas/HOTL/anotherexam/editor/Lance.sms");
        if (myObj.exists()) {
          System.out.println("File name: " + myObj.getName());
          System.out.println("Absolute path: " + myObj.getAbsolutePath());
          System.out.println("Writeable: " + myObj.canWrite());
          System.out.println("Readable " + myObj.canRead());
          System.out.println("File size in bytes " + myObj.length());
        } else {
          System.out.println("The file does not exist.");
          System.exit(-1);//If the file does not exist, then there should be no need to continue at all.
        }   
//The file is there, so we are good.
int romSize=(int) myObj.length();//Get the file size.
byte rom[]=new byte[romSize]; //Create an array with the size of the ROM.
byte stats[]=new byte[statSize];
byte statsOrig[]=new byte[statSize];
int offset;
int i=0;
int choice,choiceEdit;
try {
    // create a reader
    FileInputStream fis = new FileInputStream(new File("Lance.sms"));

    // read one byte at a time
    int ch;
    while ((ch = fis.read()) != -1) {
        //System.out.print((char) ch);
        rom[i]=(byte)ch;
        //System.out.println(rom[i]);
    i++;
    }
    System.out.println("Rom Read finished.");
    getOrigStats(stats,rom); //Load the transformable stuff into a different array, so we can work with it more easier, and change it without messing up the original.
    for (int j = 0; j < statsOrig.length; j++) { //Make a shadow copy that we shall not modify.
        statsOrig[j]=stats[j];
    }
    // close the reader
    fis.close();

} catch (IOException ex) {
    ex.printStackTrace();}
clearScreen();      //Do as it says on the tin.
System.out.println("Select the Character to view:\n");
for (int j = 0; j < charnames.length; j++) {
    System.out.println(j+": "+charnames[j]);
}
Scanner console=new Scanner(System.in);
    while (true) { //We would like to have a loop here, and check the number of the character we want to edit\view whatever.
        choice=console.nextInt();
        if (choice>7) {
        System.out.println("Invalid character to edit.");
        
        }    
        else{   //If the valid character is selected, then exit the loop, there is no problem.
            break;
        }

    }
System.out.println("Selected character: "+charnames[choice]);
System.out.println("What do you want to do?\n0: Reset\n1: View\n2: Change");
    while (true) {//Check for what we want to do with said character's inventory and character details.
    choiceEdit=console.nextInt();
    if (choiceEdit>2) {
        System.out.println("Invalid menu item selected.");
    }
    else{
        break;
    }
}
    switch (choiceEdit) {
        case 0: //Reset the character's stats.
            for (int j = 0; j < statsOrig.length; j++) {
                stats[j]=statsOrig[j];
            }
            System.out.println("The stats were reset.");
            break;
        case 1: //View the character's stats.
            while (true) { //Make a nice loop here. Basically list the stuff the character has, and map numbers to item names.
                for (int j = 0; j < inventory.length; j++) { 
                    
                }
            }
            break;
        case 2: //Change the character's stats.

            break;

        }

}
    public static void clearScreen() {  
    System.out.print("\033[H\033[2J");  
    System.out.flush();  
}  
    static int adjustplyrOffset(int plyr){
        int offset=plyr*33;

        return offset;

    }
    //The ROM is in the memory. There is no need yet to convert anything, it's fine the way it is.
    static void getOrigStats(byte[] stats, byte[] rom) {//Load the details from the original ROM already in RAM. This duplication is needed, so I can handle stuff independently.
        int plyrstatsOffset=0x7D6C; //Offset where the stats are in the ROM.
    for (int i = 0; i < stats.length; i++) {
        stats[i]=rom[plyrstatsOffset+i];
        System.out.println(stats[i]);
        
    }  
}
}