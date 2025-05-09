/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.editorhotl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author levi
 */
    public class door{//This will hold the door object that we are extracting from the ROM.
      
      //*
      // This is one part, since this class does not handle the compass part, another important
      // thing. $DE53 shows how the compass should be drawn on the map.
  
      // *//
/*
	This defines an individual door in the ROM, where the player can go through.
*/
   public byte[] getDoorList(rom input,int doorPointer){
            byte doorbytes=0,currbyte;
            var dPc=doorPointer;
           do {
               currbyte= (byte) ( (input.gameRom[dPc])&0xFF);
               doorbytes++;
               dPc++;
            }while (currbyte!=0);
           dPc=doorPointer; //Restore pointer.
            byte[] doorBytes=new byte[doorbytes];//We have how many bytes we'll have.
            for (int i = 0; i <doorbytes ; i++) {
            doorBytes[i]=(byte) ( (input.gameRom[dPc+i])&0xFF);
            }   //Get the bytes that we'll return with dem bytes.
            return doorBytes;
        }   //Returns an array of bytes that represent raw door parameters. The zero is also
    // included at the end. This is final.
    public int[] getDoorPointers(rom input){
    int[]ptArr=new int[88];
    for (int i =1; i < ptArr.length ; i++) {   //Roll through all rooms.
    input.getDumps(i);
    ptArr[i]=((input.ramDump[0xDE60-0xC000+1] & 0xFF) << 8) | (input.ramDump[0xDE60-0xC000 ] & 0xFF);
    }
    return ptArr;
}   //Returns with all door addresses used in all RAM dumps. This seems to be final as well.
  static boolean checkKthBit(byte n, byte k) {
      // Value whose only kth bit
      // is set.
      byte val = (byte) (1 << k);
      // If AND operation of n and
      // value is non-zero, it means
      // k'th bit is set
      return (n & val) != 0;
    } //Checks if a certain bit is set or not. Unused at the moment.
    public static void printDoorDetails(byte[]doorArray) {
        /*
     * The bytes are like this, not as before:
     * -Start of the door.
     * -End of the door.
     * -Door enabled? If it's not 1, then it will not show up on the map. Maybe it's either 1 or
     * 2. Compass possibly?
     * -Which room  the door will lead you.
     * -Which target column from the start of the dest. room will the player spawned?
     */
      var howManyDoors=doorArray.length/5;
     if (doorArray.length%5>1){
            System.out.println("The door array has invalid amount of bytes at the end: "+doorArray.length%5);
            System.exit(-1);
     }  //The remaining one zero at the end should be always there, otherwise we have a problem.
      for (int i = 0; i <howManyDoors ; i++) {
        System.out.println("----------------DOOR DETAILS----------");
        System.out.println("Door Nr."+i);
        System.out.println("Compass byte: "+doorArray[i*5]);
        System.out.println("Door starts at metatile: "+doorArray[i*5+1]);
        System.out.println("Compass byte: "+doorArray[i*5+2]+"Also in binary: "+Integer.toBinaryString((doorArray[i*5+2] & 0xFF) + 0x100).substring(1)); //COMPASS BYTE:
       //if(checkKthBit(doorArray[i*5+2],0))
        if (checkKthBit(doorArray[i*5+2],(byte) 0)&checkKthBit(doorArray[i*5+2],(byte) 1)){
          System.out.println("Warning: Both up and down bits are set.");
         
        }else {        if (checkKthBit(doorArray[i*5+2],(byte)0)){System.out.println("The Compass" +
                " up" +
                " " +
                "bit is set.");}
          if (checkKthBit(doorArray[i*5+2],(byte) 1)){System.out.println("The Compass down bit is" +
                  " set" +
                  ".");}}
        if (checkKthBit(doorArray[i*5+2],(byte) 4)&checkKthBit(doorArray[i*5+2],(byte) 3)){System.out.println("Bytes 3 and 4 set, waterfall+falldown.");}
        if (checkKthBit(doorArray[i*5+2],(byte) 5)){System.out.println("5th waterfall only bit " +
                "enabled.");}
        if (checkKthBit(doorArray[i*5+2], (byte) 7)){System.out.println("Warning: Disable door " +
                "bit " +
                "enabled!");}
        /* Byte 0: Compass Up
          Byte 1: Compass Down
          Byte 2: Falldown+Alarm Bells in the distance screen.
          Byte 3: Waterfall screen. Byte 2+3 and a direction MUST be used.
          Byte 4: Alone and a direction will make this a waterfall+falldown screen. Used with
          others have no effect.
          Byte 5: Alone just the waterfall.
          Byte 6: Alarm bells.
          Byte 7: Disable door.
          
        * */
        System.out.println("Door leads to room: "+doorArray[i*5+3]);  //This is it.
        System.out.println("Player starts at column at dest. room : "+doorArray[i*5+4]);
        //Confirmed this is it.
      }
    }//This prints how a door is in a given array.
    //Prints and concatenates details about a door in the game that leads to a different area.
  public byte[] importDoorList(String fileName) throws IOException {
    // Read all lines and split by commas
    int[] dist=new int[88];
    List<String> allValues = Files.lines(Paths.get(fileName))
            .flatMap(line -> Stream.of(line.split(",")))
            .collect(Collectors.toList());
    var zeroAmount=0;
    // Convert each value to byte
    byte[] result = new byte[allValues.size()];
    for (int i = 0; i < allValues.size(); i++) {
      result[i] = Byte.parseByte(allValues.get(i).trim());
      if(result[i]==0){zeroAmount++;}
    }
    System.out.println("Zero amount in the file is: "+zeroAmount);
    int[]zeroPointers=new int[zeroAmount];
    if (zeroAmount==0){System.out.println("There's no zero in the door file!");System.exit(-1);}
    zeroAmount=0;
    for (int i = 0; i < result.length; i++) {
    if(result[i]==0){zeroPointers[zeroAmount]=i;zeroAmount++;}
    }
    for (int i = 0; i < zeroPointers.length-1; i++) {
    dist[i]=zeroPointers[i+1]-zeroPointers[i]-1;
    System.out.println("Dist between two zeroes: "+"I: "+i+" "+dist[i]);
    if(dist[i]%5!=0){System.out.println("The distance between two doors are not dividable by 5!");System.exit(-1);}
    }
    return result;
  }//Imports a CSV file to be used as a door array. Checks for some things as well.
  public int[] calcPointers(byte[] input,int basePointer){
     /*It needs a door array. Based on how many zeroes are in the array, it returns a list of
     pointers where the doors for a given room would start. The code assumes you have a CSV and
     knowledge what does where.
     * */
var zeroAmount=0;
List<Integer> ls=new ArrayList<>();
    for (int i = 0; i < input.length; i++) {
    if(input[i]==0){zeroAmount++;ls.add(i+basePointer);}
    }
    int[]result=new int[zeroAmount];
    for (int i = 0; i < zeroAmount; i++) {
    result[i]=ls.get(i);
    }
    return result;
}}  //Gives back a list of pointers to be used with a door file.
