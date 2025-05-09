/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.editorhotl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author levi
 * This class imports-exports pit data from csv files, and can export bin files with the pit
 * pointers and all.
 * The PitCoordinate class will be removed, as it has no use anymore.
 */
public class PitPointer {
  /*
  *   What we can do now:
  *     -Import new pits from a CSV file. You have to keep in mind which line is which room's.
  *     -Export pits into a file. You could use this to convert a csv to a bin file to be used by
  *  the assembler directly.
  *     -TODO:Maybe connect the two together. Not mandatory, but would be nice I guess.
  *     -Get the pit list from an unaltered ROM. (Does not print though.)
  *     -Prints pit details, but in an unaltered ROM. It uses the memory dumps of each level, so
  * you have to provide your own dumps, levels for this to work correctly. This would mean to use
  *  a memory file, and use it as a map or something, and then modify some pointers there.
  *
  * Technically this is all there is to it.
  * */
  public int[] getPitPointers(rom inputRom){
    int[] pitPointerArray=new int[89]; //There's more in this than how many levels are, but just in case.
    for (int i = 1; i < pitPointerArray.length; i++) {
      inputRom.getDumps(i);   //Open the given level's dumps.
      pitPointerArray[i]=bytesToInt(inputRom.ramDump, 0x1e5e,true);    //Get this one byte.
      if (pitPointerArray[i]==1761||pitPointerArray[i]==6117){
        System.out.println("The room has no pits\\no collision set.");
      }
      System.out.println("Pointer for room "+i+" is: "+Integer.toHexString(pitPointerArray[i]));
    }
    return pitPointerArray;
  }       //Gives back a list of pointers that are used per room. Done.
  public void printPitData(rom inputRom,int pitBasPointer){
    //This is always needed.
    PitPointer pitPointer=new PitPointer(); //We check the pits and pointers, so this is needed.
    pitPointer.getPitListFromPointer(inputRom, pitBasPointer);  //This is the default address in an
    // unaltered ROM. I also use this for the hack.
    byte[] test;
    int[]test2= pitPointer.getPitPointers(inputRom);
    
    System.out.println(test2.length);
    for (int i = 0; i < test2.length ; i++) {
      test= pitPointer.getPitListFromPointer(inputRom,test2[i]);
      System.out.println("---------------"+"This is for room: "+i+"---------------");
      for (int j = 0; j < test.length; j++) {
        test[j]&= (byte) 0xFF;  //Some stuff would still provide invalid vals, most probably
        // because the room has some invalid settings. It happens.
      }
      System.out.println(java.util.Arrays.toString(test));
      
    }
    
  } //It will print the pit data. But only for an unaltered ROM. Make your own dumps to let this
  // function correctly, otherwise it will produce garbage output.
  public byte[] getPitListFromPointer(rom inputRom,int pointer){
    int tempPointer=pointer;            //This needs here, since we don't want to modify the pointer we receive to check.
    byte pitByte=0;
    if (inputRom.gameRom[tempPointer]==0){  //If the pointer points to a zero (no pits in the room), then return a 1 element array.
      /*No pit levels use 0x1761. $00 marks the end of the pits. Maybe even a two byte return array is good. I've not used new type of pits.
       * */
      return new byte[]{0};
    }
    while(inputRom.gameRom[tempPointer]!=0){
      pitByte++;
      tempPointer++;
    }       //We'll get the future array's length based on how many non-zero bytes are there.
    tempPointer=pointer;    //Reset the pointer.
    byte[] pitData=new byte[pitByte];   //now get the array created that will hold the values we'll give back.
    //Copy the data.
    System.arraycopy(inputRom.gameRom, tempPointer, pitData, 0, pitByte);
    return pitData;
  }   //This gets the pit coordinates in a nice variable length byte array. Since a room can have more than one pit.
  public static int bytesToInt(byte[] byteArray, int startIndex,boolean swapBytes) {
    if (byteArray == null || byteArray.length < startIndex + 2 || startIndex < 0) {
      throw new IllegalArgumentException("Invalid input byte array or start index.");
    }
    if(swapBytes){
      return ((byteArray[startIndex + 1] & 0xFF) << 8) | (byteArray[startIndex] & 0xFF); // Notice the swapped indices
    }else {
      return ((byteArray[startIndex] & 0xFF) << 8) | (byteArray[startIndex + 1] & 0xFF);
    }
  }   //Returns Pit coordinate pointers from all rooms. Can go between the original byte format, and swapped for "normal" use.
  public int[] getBackNewPointers(byte[] input, int initPointer){
    ArrayList<Integer> romPointers=new ArrayList<>();
    romPointers.add(initPointer); //The first pointer is always the beginning of the array.
    for (int i = 2; i <input.length ; i++) {  //The fist two bytes are not needed.
    if (input[i]==0){
      romPointers.add(initPointer+i+1); //So, we add the position plus one to the init pointer.
      // This is needed as the pointer should be the next value, not the zero, that's what the
      // pit should point to.
    } //We got a nice list from the pointers, let's convert it back to an array.
    }
    int[]result=new int[romPointers.size()];  //Create an array for the return type.
    for (int i = 0; i <result.length ; i++) {
      result[i]= romPointers.get(i);
    } //Copy from the list to the array.
    return result;
  } //This returns an array of pointers, so a new pit array could be used more easily.
  public void expPitPointers(int[] input){
  rom justForPath=new rom();
  String savePath= justForPath.path+"pitPointer.bin";
    try {
      FileWriter w=new FileWriter(savePath);
      for (int j : input) {
        w.write(j);
      }
      w.close();
    }catch (IOException ex){
      System.out.println("Pit pointer file "+savePath+" was not saved successfully.");
      System.exit(-1);
    }
  
  } //Write the pit pointers into a binary file, that can be used by an assembler or something.
}
