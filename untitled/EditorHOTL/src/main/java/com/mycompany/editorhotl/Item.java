package com.mycompany.editorhotl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.ArrayUtils;
public class Item {

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
    int offset=0x7B16; //In the unaltered ROM, this is the address where this array is.
int itemArrayLength=itemNames.length;//If needed to be used, this is fine. Maybe if we insert too many items or something.
/*
* I thought maybe I'll start with the items to sort out this thing.
* Items are in boxes, and these boxes obviously appear on the levels.
* These have various stats, that will be filled in as I go on.
*
* -The ROM has originally support for fifty items, as mentioned below. These for the future are not going to be enough, so there should be some support for another
* bank, where the stuff can be stored more freely.
* */
public byte[] extractItems(byte[] rom){
    byte[] items=new byte[500]; //The array in the ROM is this size.
        /* The ROM has fifty items, plus one byte at the end, to mark the end of the item list. The code looks for that one byte, and loads some zeroes as well.
        Every item is five bytes long:
            -First byte is the item type.
            -Second is room Nr.
            -Third is the distance in tiles from the beginning of the floor\room.
            -The fourth byte is not yet known, but so far has no effect on the game. Possibly i'm wrong here.
            -Fifth byte marks if the chest can be made visible with the 'Detect Invisible' spell.
        This all is also in the code, but if you're not keen on combing that through, this is a small excerpt.
          */
    //var offset=0x7B16; //In the unaltered ROM, this is the address where this array is.

    for (int i = 0; i < items.length; i++) {
        items[i]=rom[i+this.offset];
    }
    return items;
}   //The array of items is copied from the ROM to an array, and that's returned. The last zero byte is not included.
public byte[] importItemList(String filepath) throws FileNotFoundException {
    /*This will import a CSV file which contains a fifty item list. The byte descriptions are in this file.
    It will return a 500 byte array, which then can be used to modify a normal ROM.
    Of course, if anything else is needed, then that address has to be used.
    This should be doing the following:
        -Check if the input has the right size.
            -If the list is less than what's needed, then pad it with zeroes, and warn the user.
            -If the list has more items than what's allowed, then the program should run on an error, and warn the user.
        -Check data validity.
            -Push errors to the user's face that the problem is on x-y line, and what's the issue exactly.

     The maximum number of rooms defined in the engine are 128.
     *
     * */

    byte[] result=new byte[500];    //We need this, as the import file needs to have 50 entries. See above for the byte definitions.
//-------------------------------------------------This is the CSV import.----------------------------------------------
    List<List<String>> records = new ArrayList<>();     //This may not even needed.
    byte[] temp = new byte[5];
    try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
        byte lines=0;
        while (br.readLine() != null) lines++;
        if (lines!=50){
            System.out.println("The input file does not have exactly 50 lines. It has: "+lines);
            System.exit(-1);
        }
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            temp=checkAndTransform(values);
            result=ArrayUtils.addAll(result,temp);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
//----------------------------------------------------------------------------------------------------------------------
    return result;
}
public byte[] checkAndTransform(String[] source){
byte[]result=new byte[5];
    if (Byte.parseByte(source[0])>this.itemArrayLength){
        //If the item nr. is an undefined item, then throw and warning.
    System.out.println("Invalid item nr.! "+Byte.parseByte(source[0]));
    System.exit(-1);

    }       //Check for item validity. If the item Nr. is too high, then it's not valid.
    if (Integer.parseInt(source[1])>127) {
        System.out.println("Invalid room Nr.! " +Byte.parseByte(source[1]));
        System.exit(-1);
    }   //Room Nr. check. If it's higher than the max, then throw an error.
        for (int i = 0; i < result.length; i++) {
            result[i]=Byte.parseByte(source[i]);
        }
        /*
        Other byte checks are not needed. Since we don't have any errors at this point, we can just continue.
        * */
    return result;
}   //This is to check the input, and to transform it to an appropriate format.
public void printItems(byte[] items) {    //Prints all items in a CSV fashion on the screen.
        String[] visibility={"Visible","Invisible"};
        int roomnr;
        int distance;
        int fourththbyte;
        int chestvisible;
        String chv;

        for (int i = 0; i < items.length/5; i++) {
            roomnr=items[(i*5)+1] & 0xff;
            distance=items[(i*5)+2] & 0xff;
            fourththbyte=items[(i*5)+3] & 0xff;
            chestvisible=items[(i*5)+4] & 0xff;
            if (chestvisible!=0){

            }
            System.out.println(i+": "+this.itemNames[items[i*5]]+" | Room Nr.: "+roomnr+" \\Hex:"+String.format("$%02X",items[(i*5)+1])+" | Dist from room bgn in tiles: "+distance+"\\Hex:"+String.format("$%02X",distance)+" | 4th byte: "+fourththbyte+" \\ Hex: "+String.format("$%02X",fourththbyte)+" | Chest visible: "+chestvisible+" \\ Hex: "+String.format("$%02X",chestvisible));
        }
    }   //Prints the ROM's items and it seems to be trap content, where they are, and what is in the boxes. Basically we can put traps and items in the same array, and it will get processed. The game's code is pretty flexible this way.
    //A separate item\trap class is needed later on for this.

}
