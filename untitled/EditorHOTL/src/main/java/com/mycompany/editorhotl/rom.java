package com.mycompany.editorhotl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

//TODO: These manual paths should be really a parameter or something. These things are not existing on a computer that's not mine, so it's a bit odd.
/*
* This will open\save ROM and RAM\VRAM dumps.
* Opens a normal ROM, and also makes a copy just in case.
* Opens both VRAM and RAM dumps into the current 'rom' instance.
* */
public class rom {
    String path="//media//MegaWork//devver//disas//HOTL//anotherexam//editor//"; //Home path where stuff is, at least on my machine.
    String fileName="Lance.sms";    //An original ROM on my machine, normal unaltered PAL rom.
    String outputFile="Lance_mod.sms";  //This would be the output file, after modifications are done.
    String outFull=path+outputFile;
    String input="/media/MegaWork/devver/disas/HOTL/anotherexam/editor/room_vram/";
    String inputRAM="/media/MegaWork/devver/disas/HOTL/anotherexam/editor/room_ram/";
    String mtPerRoom="/media/MegaWork/devver/disas/HOTL/anotherexam/editor/room";
    String ext1=".vrm"; //This is the extension for the Video RAM dump files, for the graphics.
    String ext2=".ram";    //Truncated RAM dumps. Only covers the levels in the game. Other details are in the ROM still.
    String fname="HOTL_mod_ah"; //How these dump files are called. Only the extension is different. You need the Room\Level number as well.
    byte[] VRAMdump=new byte[16384];
    byte[] ramDump=new byte[8192];
    byte[] gameRom=readRom(path+fileName);      //An origianal ROM, not for modification.
    byte[] gameRomTemp=gameRom;                         //This is to modify and export things.
    public String[] combineDumpNames(int roomNr ){

        String sb = this.input + this.fname +
                roomNr +
                ext1;
        String sb2 = this.inputRAM + this.fname +
                roomNr +
                ext2;

        return new String[]{sb, sb2};
    }
    public void saverom2(byte[] input){
        String outputFile="Lance_mod.sms";
        try {
            FileOutputStream fos=new FileOutputStream(this.path+outputFile);
            fos.write(input);
            fos.close();
        }catch (IOException ex){
            System.exit(-1);
        }
    }   //This works as saving the result in the ROM. It does not overwrite the original.
    public static byte[] readRom (String filename){
        File file =new File(filename);
        int size=(int)file.length();
        byte[] rom=new byte[size];
        try {
            FileInputStream fis = new FileInputStream(filename);
            fis.read(rom,0,rom.length);
            fis.close();
        } catch (IOException ex){

            ex.printStackTrace();
            System.out.println("There was a problem with the file.");
            System.exit(-1);
        }

        return rom;
    }   //Opens the ROM, and stores it in a temp array.
//Returns if the file is the orig size or not. Currently, not in use.
    public void getDumps(int roomNr){

        String[] paths=combineDumpNames(roomNr);        //We have the file paths now.
        this.VRAMdump=readRom(paths[0]);                //Open one
        this.ramDump=readRom(paths[1]);                 //two files into the same object we created otherwise.
        System.out.println("Dumps are in!");

    }   //Loads the correct dumps into the current rom object. Give it which rooms you want, and it will do the rest.
    public static byte[] csvToUnsignedByteArray(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<Byte> byteList = new ArrayList<>();
        boolean firstLine = true;
        
        for (String line : lines) {
            if (!firstLine) {
                byteList.add((byte) 0); // Add zero between lines
            } else {
                firstLine = false;
            }
            
            String[] numbers = line.split(",");
            for (String numStr : numbers) {
                try {
                    int num = Integer.parseInt(numStr.trim());
                    if (num < 0 || num > 255) {
                        throw new IOException("Number out of unsigned byte range (0-255): " + num);
                    }
                    byteList.add((byte) (num & 0xFF));
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid number in CSV: " + numStr);
                }
            }
        }
        
        // Ensure the array ends with a zero (if not already)
        if (byteList.isEmpty() || byteList.get(byteList.size() - 1) != 0) {
            byteList.add((byte) 0);
        }
        
        // Convert List<Byte> to byte[]
        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = byteList.get(i);
        }
        
        return result;
    }
    
}
