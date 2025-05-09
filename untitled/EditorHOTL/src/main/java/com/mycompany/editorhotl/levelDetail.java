/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.editorhotl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author levi
 * This is a level descriptor. It has quite a few stuff that is needed to make up levels, such as where to get tiles, metatiles and floor data.
 * Some things i'm not sure what it does, but so far it's not needed. Later on, 
 * I want to use my own code to load levels, so most of these will not be used either.
 */
public class levelDetail {  //These are copied straight from the other code to define a level. Most of these are self explanatory.
    int roomNr;
    int _RAM_DE3E_MAX_LVL_LENGHT;       //This is useful, though I don't know if at the end, the
    // level handling code is actually using this.
    int _RAM_DE31_METATILE_BANK;
    int _RAM_DE29_METATILE_TILE_LOAD;
    int _RAM_DE32_;
    int _RAM_DE2F_;
    int _RAM_DE2E_BANKSWITCH_LEVEL;
    int _RAM_DE2A_;
    int _RAM_DE5E_FLOORFALLXCOORD;  //Also needed for the pits. The name is a bit misleading
    // though. This is the pointer from where the game will get the pits.
    int _RAM_DE60_DOOR_POINTER; //Where the game gets the door data.
    int _RAM_DE53_COMPASS;  //The compasses for the doors. Similar ROM pointer.
/*
* These print the level details in an unaltered ROM. You have to consult with the disassembly on where these are, and what are they doing.
* The original game has 88 rooms, but there's enough space for 128 or more, if the code is modified around the level loading.
* The trimmed RAM dump is easier to use.
* 4k has the tiles, offsets and metatiles. The disassy has more info on the level structure.
* First 2k are the tiles and tilemap attributes, such as flipping, priority and such things. The disassy points to things in the right direction.
* Tile, then $100 away the attribute byte.
* Second 2k is the metatile array.
* For easier time, the metatile array will be rearranged, to visualize the data easier.
* The level length will help trim the metatile data as well, so we can use that.
* Room and level are interchangeable, but the game has three main levels, and many rooms opening into each other.
* */
    public static byte[] trimLevelRAMFile(int room){
        //Room number will be the file that we'll load in.
        String fName="HOTL_mod_ah";
        String extension=".ram";
        rom newROM=new rom();
        String cFileName=fName+room+extension;  //This will concatenate the name, and we should be able to open it.
        byte[] result2=new byte[8048];   //One level is 4096 byte, or 4k long. The first 4k is the uncompressed, and editable data.
        result2=rom.readRom(newROM.path+cFileName);
        byte[]result=new byte[4096];
        //Copy the first 4k. The rest is not needed.
        System.arraycopy(result2, 0, result, 0, 4096);
        return result;
    }   //As the name suggests, this trims down a RAM dump of a given level. This is needed, as the whole RAM is not needed, just the first 4 kilobytes.
    public static levelDetail getlvlPointerDetails(int roomNr,byte[] rom){
        levelDetail result = null;   //I don't really know yet, if this needs to be a fixed array or not, but for now, this will hold the values from the data extraction.

        int lvlPointer=0x1343;	//This is the original ROM address for the level pointers.    	//The code will mimic the original written in assembly to extract the data the program will show later.    	//A is coming from the roomNR, which is the input parameter.    	short hl=lvlPointer-2;	//ld hl,_data_1343_lvl_pointers-2    	short de=(short)roomNr+roomNr;	//add a,a    	hl+=de;					//add hl,de    	byte aTemp=rom[hl];	//ld a,(hl)	We make a temp A for the result, but it will be even better later, i'm sure.    	hl++;				//inc hl
        //System.out.println(String.format("0x%02X", hl));
        //System.out.println(Integer.toBinaryString(hl));
        int Accumulator; //Z80 has an 8-bit accumulator, what is used for math.
        int H,L,D,E,B,C,I,X,Y =0;
        int HL,DE,BC,IX,IY=0;
        BC=0;
        int sHL=0,sDE=0,sBC=0,sAccumulator=0;
        int[] stack=new int[255];
        int stackpointer=0;
        int _RAM_DE3E_MAX_LVL_LENGHT; //Level length in the ROM.
        int _DATA_12E1_=0x12E1;
        int _RAM_DE31_METATILE_BANK;
        int _RAM_DE29_METATILE_TILE_LOAD;
        int _RAM_DE32_; //Metatile pointer?
        int _RAM_DE2F_; //This is some kind of offset, so what tiles plus this will be used by the metatiles.
        int _RAM_DE2E_BANKSWITCH_LEVEL;
        int _RAM_DE2A_; //This seems like some kind of ROM offset. It only changes on the Dragon Room to $A000. If this is changed to something else, then the metatiles are right, but the tiles of said metatiles.
        int _RAM_DE5E_FLOORFALLXCOORD;  //This is a ROM Address, where the further coordinates are.
        /*  So, this works like a pointer. It tells the game where to look int the ROM for the pits.
        It seems the pits are two bytes in size:
            -The first is the starting column. If the player reaches this, the pit is acknowledged, and you die.
            -The second byte seems to be how many columns wide the pit itself.
        If the next byte is a zero, then there are no more pits to look for, since column 0 is invalid\out of bounds.
        If the room has no pits, it defaults to ROM address 0x1761.
        */
        int _RAM_DE60_;
        int _RAM_DE53_COMPASS;
        int _RAM_C800_1ST_METATILE_ROW=0xC800;
        int _RAM_FFFF_=0;
        Accumulator=(byte)roomNr;   //ld a,(_RAM_DE52_ROOM_NR)
        HL=lvlPointer-2;            //ld hl,_DATA_1343_LVL_POINTERS - 2
        Accumulator+=Accumulator;   //add a,a
        E=Accumulator;              //ld e,a
        D=0;                        //ld d,$00
        DE = ((D & 0xFF) << 8) | (E & 0xFF);
        HL+=DE;                     //add hl, de
        Accumulator=rom[HL];        //ld a, (HL)
        HL++;                       //inc HL
        H=rom[HL];                  //ld h, (HL)
        L=Accumulator;
        HL = ((H & 0xFF) << 8) | (L & 0xFF);    //Combine to HL.
        Accumulator=rom[HL];                    //ld a, (HL)
        stack[stackpointer]=HL;                //Push HL
        stackpointer++;                         //Adjust stackpointer.
        L=Accumulator;                         //ld l, a
        H=0;                                    //ld h, $00
        HL = ((H & 0xFF) << 8) | (L & 0xFF);    //Combine to HL.
        HL+=HL;         //add hl, hl
        HL+=HL;         //add hl, hl
        HL+=HL;         //add hl, hl
        HL+=HL;         //add hl, hl
        DE=0x0100;      //ld de,$0100
        Accumulator&=Accumulator;   //and a This is to lose carry, but I don't know if this is needed in java at least.
        HL-=DE;         //SBC HL,DE
        _RAM_DE3E_MAX_LVL_LENGHT=HL;   //LD (_RAM_DE3E_MAX_LVL_LEN), hl
        stack[stackpointer]=Accumulator;    //push AF   We don't need F here.
        stackpointer--; //Adjust SP.            //Some code comes now that adjusts the camera, but that's not needed here.
        HL=stack[stackpointer]; //Get back HL, A is still $50, so we are good on that.
        Accumulator=Accumulator>>2;     //SRL a, SRL a      We combine the two shifts here.
        B=Accumulator;      //ld b,a
        HL++;   //inc HL
        Accumulator=rom[HL];    //ld a, (hl)
        HL++;       //inc HL
        stack[stackpointer]=HL;     //push HL
        stackpointer++;
        HL=_DATA_12E1_;      //LD hl,_DATA_12E1_
        Accumulator+=Accumulator;   //ld a,a
        E=Accumulator;      //ld e, a
        D=0;                //ld d,$00
        DE = ((D & 0xFF) << 8) | (E & 0xFF);
        HL+=DE;             //add HL, DE
        Accumulator=rom[HL];    //ld a, (HL)
        HL++;   //inc HL
        H=rom[HL];  //ld h, (hl)
        HL|=(H & 0xFF) << 8;
        L=Accumulator;  //ld l, a
        HL = ((H & 0xFF) << 8) | (L & 0xFF);    //Combine to HL.
        Accumulator=rom[HL];    //a ,(HL)
        _RAM_DE31_METATILE_BANK=Accumulator;
        HL++;
        Accumulator=rom[HL];
        _RAM_DE2E_BANKSWITCH_LEVEL=Accumulator;    //ld (_RAM_DE31_METATILE_BANK), a	;Metatile bank.
        HL++;       //inc hl
        Accumulator=rom[HL];
        _RAM_DE29_METATILE_TILE_LOAD=Accumulator;   //ld (_RAM_DE29_METATILE_TILE_LOAD), a	;05	This is the tiles for the metatiles.
        HL++;       //inc HL
        E=rom[HL];  //ld e, (HL)
        HL++;           //inc HL
        D=rom[HL];      //ld d,(HL)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        _RAM_DE32_=rom[DE]; //ld (_RAM_DE32_),DE
        HL++;   //inc hl
        E=rom[HL];  //ld e,(HL)
        HL++;   //inc HL
        D=rom[HL];  //ld d,(HL)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        _RAM_DE2F_=DE;  //ld (_RAM_DE2F_), de
        HL++;
        E=rom[HL];  //ld e,(HL)
        HL++;   //inc HL
        D=rom[HL];  //ld d,(HL)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        _RAM_DE2A_=rom[DE]; //ld (_RAM_DE2A_), de	;$8000.
        stackpointer--;
        HL=stack[stackpointer]; //pop hl
        E=rom[HL];  //ld e, (HL)
        HL++;   //inc HL
        D=rom[HL];  //d, (hl)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        HL++;
        stack[stackpointer]=DE; //push DE
        //stackpointer++; //We use this immediately.
        IX=DE;  //pop IX
        Accumulator=0;      //We skip the if, and during normal operation this will be zero.
        //Accumulator=roomNr; //ld a, (_RAM_DE52_ROOM_NR) //Not needed, as there was an if condition there, but that's not needed at the moment.
        //Here comes an if statement, though I'm not sure if it brings anything new here so far.
        E=rom[HL];  //ld e, (hl)
        HL++;   //inc hl
        D=rom[HL];  //ld d,(hl)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        HL++;   //inc HL
        _RAM_DE5E_FLOORFALLXCOORD=DE; //ld (_RAM_DE5E_FLOORFALLXCOORD), de
        E=rom[HL];  //ld e, (hl)
        HL++;   //inc hl
        D=rom[HL];  //ld d,(hl)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        HL++;   //inc HL
        _RAM_DE60_=DE;  //ld (_RAM_DE60_), de		;$1944.
        Accumulator=rom[HL]; //ld a, (hl)
        _RAM_DE53_COMPASS=Accumulator;  //
        int temp1,temp2=0;
        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.

        DE=_RAM_C800_1ST_METATILE_ROW;  //This is C800 ld DE, $C800
        BC=4;   //ld BC, $0004

        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.
        //An EXX back
        Accumulator=0;  //xor a
        int _RAM_DE5A_=Accumulator; //ld (_RAM_DE5A_), a
        int _RAM_DE59_LEFT_DEBUG_NR=Accumulator;    //ld (_RAM_DE59_LEFT_DEBUG_NR), a
//_LABEL_D29_
        stack[stackpointer]=BC; //push BC
        stackpointer++;
        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.
        //This is a manual EXX.
        HL=0x0004;  //ld hl, $0004
        HL+=DE; //add HL, DE
        temp1=HL;
        temp2=DE;
        HL=temp2;
        DE=temp1;   //ex hl,de

        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.

        Accumulator=_RAM_DE2E_BANKSWITCH_LEVEL; //ld a, (_RAM_DE2E_BANKSWITCH_LEVEL)
        _RAM_FFFF_=Accumulator; //ld (_RAM_FFFF_), a    This is the bankswitch part of the code, but this also holds the last bank you've used, and some code refers to this, so this is definetly coming here.
        DE=_RAM_DE2F_;  //ld de, (_RAM_DE2F_)
        Accumulator=rom[IX+0];  //ld a, (IX+0) The +0 does nothing, but why not include it for completeness?
        H = H >>> 1; // srl h
        L = (L >>> 1) | ((H & 0x1) << 7); // rr l
        H = H >>> 1; // srl h
        L = (L >>> 1) | ((H & 0x1) << 7); // rr l

        /*
         * Upon analyzing the game code more, it seems that the shifting is a simple 16-bit one. H is shifted right, so if there's something in
         * the last bit, it will be pushed into the carry flag.
         * We load L with zeroes, then shift the carry flag's contents into it, and then this is done for the second time.
         * This stuff above this is still cool as it is.
         */

        HL+=DE; //add HL, DE
        temp1=DE;
        temp2=HL;
        HL=temp1;
        DE=temp2;   //EX DE,HL
        B=8;    //ld B,$08
//_LABEL_D4F_:
        C=4;    //ld c, $04
        stack[stackpointer]=DE; //push DE
        stackpointer++;
//_LABEL_D52:
        BC = ((B & 0xFF) << 8) | (C & 0xFF);    //Combining the two.
        Accumulator=_RAM_DE2E_BANKSWITCH_LEVEL; //ld a, _RAM_DE2E_BANKSWITCH_LEVEL
        _RAM_FFFF_=Accumulator; //ld (_RAM_FFFF_),a
        Accumulator=DE; //ld a, (de)
        Accumulator+=0xD0;  //add a, $D0


        // result.roomNr=0;
        result.roomNr=roomNr;

        result._RAM_DE3E_MAX_LVL_LENGHT=_RAM_DE3E_MAX_LVL_LENGHT;

        result._RAM_DE31_METATILE_BANK=_RAM_DE31_METATILE_BANK;

        result._RAM_DE29_METATILE_TILE_LOAD=_RAM_DE29_METATILE_TILE_LOAD;

        result._RAM_DE32_=_RAM_DE32_;

        result._RAM_DE2F_=_RAM_DE2F_;

        result._RAM_DE2E_BANKSWITCH_LEVEL=_RAM_DE2E_BANKSWITCH_LEVEL;

        result._RAM_DE2A_=_RAM_DE2A_;

        result._RAM_DE5E_FLOORFALLXCOORD=_RAM_DE5E_FLOORFALLXCOORD;

        result._RAM_DE60_DOOR_POINTER =_RAM_DE60_;

        result._RAM_DE53_COMPASS=_RAM_DE53_COMPASS;


        return result;  //This result is a levelDetail data type.
    }   //This returns a levelDetail data type. See the class file for more.
    public static void printlvlPointerDetails(int roomNr,byte[] rom) {
        List<String> roomDetails=new ArrayList<>(); //What is printed will be returned in a List, so later on, a csv could handle this.
        /*
         * I think for the time, the command line part is also okay, and the data that we will put out would be useable for a GUI stuff.
         */

        //This is still work in progress, and now absolute trash.
        int lvlPointer=0x1343;	//This is the original ROM address for the level pointers.    	//The code will mimic the original written in assembly to extract the data the program will show later.    	//A is coming from the roomNR, which is the input parameter.    	short hl=lvlPointer-2;	//ld hl,_data_1343_lvl_pointers-2    	short de=(short)roomNr+roomNr;	//add a,a    	hl+=de;					//add hl,de    	byte aTemp=rom[hl];	//ld a,(hl)	We make a temp A for the result, but it will be even better later, i'm sure.    	hl++;				//inc hl
        //System.out.println(String.format("0x%02X", hl));
        //System.out.println(Integer.toBinaryString(hl));
        int Accumulator; //Z80 has an 8-bit accumulator, what is used for math.
        int H,L,D,E,B,C,I,X,Y =0;
        int HL,DE,BC,IX,IY=0;
        BC=0;
        int sHL=0,sDE=0,sBC=0,sAccumulator=0;
        int[] stack=new int[255];
        int stackpointer=0;
        int _RAM_DE3E_MAX_LVL_LENGHT; //Level length in the ROM.
        int _DATA_12E1_=0x12E1;
        int _RAM_DE31_METATILE_BANK;
        int _RAM_DE29_METATILE_TILE_LOAD;
        int _RAM_DE32_;
        int _RAM_DE2F_;
        int _RAM_DE2E_BANKSWITCH_LEVEL;
        int _RAM_DE2A_;
        int _RAM_DE5E_FLOORFALLXCOORD;
        int _RAM_DE60_;
        int _RAM_DE53_COMPASS;
        int _RAM_C800_1ST_METATILE_ROW=0xC800;
        int _RAM_FFFF_=0;
        Accumulator=(byte)roomNr;   //ld a,(_RAM_DE52_ROOM_NR)
        HL=lvlPointer-2;            //ld hl,_DATA_1343_LVL_POINTERS - 2
        Accumulator+=Accumulator;   //add a,a
        E=Accumulator;              //ld e,a
        D=0;                        //ld d,$00
        DE = ((D & 0xFF) << 8) | (E & 0xFF);
        HL+=DE;                     //add hl, de
        Accumulator=rom[HL];        //ld a, (HL)
        HL++;                       //inc HL
        H=rom[HL];                  //ld h, (HL)
        L=Accumulator;
        HL = ((H & 0xFF) << 8) | (L & 0xFF);    //Combine to HL.
        Accumulator=rom[HL];                    //ld a, (HL)
        stack[stackpointer]=HL;                //Push HL
        stackpointer++;                         //Adjust stackpointer.
        L=Accumulator;                         //ld l, a
        H=0;                                    //ld h, $00
        HL = ((H & 0xFF) << 8) | (L & 0xFF);    //Combine to HL.
        HL+=HL;         //add hl, hl
        HL+=HL;         //add hl, hl
        HL+=HL;         //add hl, hl
        HL+=HL;         //add hl, hl
        DE=0x0100;      //ld de,$0100
        Accumulator&=Accumulator;   //and a This is to lose carry, but I don't know if this is needed in java at least.
        HL-=DE;         //SBC HL,DE
        _RAM_DE3E_MAX_LVL_LENGHT=HL;   //LD (_RAM_DE3E_MAX_LVL_LEN), hl
        stack[stackpointer]=Accumulator;    //push AF   We don't need F here.
        stackpointer--; //Adjust SP.            //Some code comes now that adjusts the camera, but that's not needed here.
        HL=stack[stackpointer]; //Get back HL, A is still $50, so we are good on that.
        Accumulator=Accumulator>>2;     //SRL a, SRL a      We combine the two shifts here.
        B=Accumulator;      //ld b,a
        HL++;   //inc HL
        Accumulator=rom[HL];    //ld a, (hl)
        HL++;       //inc HL
        stack[stackpointer]=HL;     //push HL
        stackpointer++;
        HL=_DATA_12E1_;      //LD hl,_DATA_12E1_
        Accumulator+=Accumulator;   //ld a,a
        E=Accumulator;      //ld e, a
        D=0;                //ld d,$00
        DE = ((D & 0xFF) << 8) | (E & 0xFF);
        HL+=DE;             //add HL, DE
        Accumulator=rom[HL];    //ld a, (HL)
        HL++;   //inc HL
        H=rom[HL];  //ld h, (hl)
        HL|=(H & 0xFF) << 8;
        L=Accumulator;  //ld l, a
        HL = ((H & 0xFF) << 8) | (L & 0xFF);    //Combine to HL.
        Accumulator=rom[HL];    //a ,(HL)
        _RAM_DE31_METATILE_BANK=Accumulator;
        HL++;
        Accumulator=rom[HL];
        _RAM_DE2E_BANKSWITCH_LEVEL=Accumulator;    //ld (_RAM_DE31_METATILE_BANK), a	;Metatile bank.
        HL++;       //inc hl
        Accumulator=rom[HL];
        _RAM_DE29_METATILE_TILE_LOAD=Accumulator;   //ld (_RAM_DE29_METATILE_TILE_LOAD), a	;05	This is the tiles for the metatiles.
        HL++;       //inc HL
        E=rom[HL];  //ld e, (HL)
        HL++;           //inc HL
        D=rom[HL];      //ld d,(HL)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        _RAM_DE32_=rom[DE]; //ld (_RAM_DE32_),DE
        HL++;   //inc hl
        E=rom[HL];  //ld e,(HL)
        HL++;   //inc HL
        D=rom[HL];  //ld d,(HL)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        _RAM_DE2F_=DE;  //ld (_RAM_DE2F_), de
        HL++;
        E=rom[HL];  //ld e,(HL)
        HL++;   //inc HL
        D=rom[HL];  //ld d,(HL)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        _RAM_DE2A_=rom[DE]; //ld (_RAM_DE2A_), de	;$8000.
        stackpointer--;
        HL=stack[stackpointer]; //pop hl
        E=rom[HL];  //ld e, (HL)
        HL++;   //inc HL
        D=rom[HL];  //d, (hl)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        HL++;
        stack[stackpointer]=DE; //push DE
        //stackpointer++; //We use this immediately.
        IX=DE;  //pop IX
        Accumulator=0;      //We skip the if, and during normal operation this will be zero.
        //Accumulator=roomNr; //ld a, (_RAM_DE52_ROOM_NR) //Not needed, as there was an if condition there, but that's not needed at the moment.
        //Here comes an if statement, though I'm not sure if it brings anything new here so far.
        E=rom[HL];  //ld e, (hl)
        HL++;   //inc hl
        D=rom[HL];  //ld d,(hl)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        HL++;   //inc HL
        _RAM_DE5E_FLOORFALLXCOORD=DE; //ld (_RAM_DE5E_FLOORFALLXCOORD), de
        E=rom[HL];  //ld e, (hl)
        HL++;   //inc hl
        D=rom[HL];  //ld d,(hl)
        DE = ((D & 0xFF) << 8) | (E & 0xFF);        //Set DE completely.
        HL++;   //inc HL
        _RAM_DE60_=DE;  //ld (_RAM_DE60_), de		;$1944.
        Accumulator=rom[HL]; //ld a, (hl)
        _RAM_DE53_COMPASS=Accumulator;  //
        int temp1,temp2=0;
        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.

        DE=_RAM_C800_1ST_METATILE_ROW;  //This is C800 ld DE, $C800
        BC=4;   //ld BC, $0004

        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.
        //An EXX back
        Accumulator=0;  //xor a
        int _RAM_DE5A_=Accumulator; //ld (_RAM_DE5A_), a
        int _RAM_DE59_LEFT_DEBUG_NR=Accumulator;    //ld (_RAM_DE59_LEFT_DEBUG_NR), a
//_LABEL_D29_
        stack[stackpointer]=BC; //push BC
        stackpointer++;
        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.
        //This is a manual EXX.
        HL=0x0004;  //ld hl, $0004
        HL+=DE; //add HL, DE
        temp1=HL;
        temp2=DE;
        HL=temp2;
        DE=temp1;   //ex hl,de

        temp1=HL;
        temp2=sHL;
        HL=temp2;
        sHL=temp1;  //Exx HL.
        temp1=DE;
        temp2=sDE;
        DE=temp2;
        sDE=temp1;  //Exx DE.

        temp1=BC;
        temp2=sBC;
        BC=temp2;
        sBC=temp1;  //Exx BC.

        temp1=Accumulator;
        temp2=sAccumulator;
        Accumulator=temp2;
        sAccumulator=temp1;  //Exx Acc.

        Accumulator=_RAM_DE2E_BANKSWITCH_LEVEL; //ld a, (_RAM_DE2E_BANKSWITCH_LEVEL)
        _RAM_FFFF_=Accumulator; //ld (_RAM_FFFF_), a    This is the bankswitch part of the code, but this also holds the last bank you've used, and some code refers to this, so this is definetly coming here.
        DE=_RAM_DE2F_;  //ld de, (_RAM_DE2F_)
        Accumulator=rom[IX+0];  //ld a, (IX+0) The +0 does nothing, but why not include it for completeness?
        IX++;   //inc IX
        //L=0;    //ld l,$00
        //H=Accumulator;  //ld h,a
        //HL = ((H & 0xFF) << 8) | (L & 0xFF);    //Combine to HL.
        //H>>=2;
        //HL>>=2;
        //HL=emulateZ80Assembly(Accumulator);
        H = H >>> 1; // srl h
        L = (L >>> 1) | ((H & 0x1) << 7); // rr l

        H = H >>> 1; // srl h
        L = (L >>> 1) | ((H & 0x1) << 7); // rr l

        /*
         * Upon analyzing the game code more, it seems that the shifting is a simple 16-bit one. H is shifted right, so if there's something in
         * the last bit, it will be pushed into the carry flag.
         * We load L with zeroes, then shift the carry flag's contents into it, and then this is done for the second time.
         * This stuff above this is still cool as it is.
         */

        HL+=DE; //add HL, DE
        temp1=DE;
        temp2=HL;
        HL=temp1;
        DE=temp2;   //EX DE,HL
        B=8;    //ld B,$08
//_LABEL_D4F_:
        C=4;    //ld c, $04
        stack[stackpointer]=DE; //push DE
        stackpointer++;
//_LABEL_D52:
        BC = ((B & 0xFF) << 8) | (C & 0xFF);    //Combining the two.
        Accumulator=_RAM_DE2E_BANKSWITCH_LEVEL; //ld a, _RAM_DE2E_BANKSWITCH_LEVEL
        _RAM_FFFF_=Accumulator; //ld (_RAM_FFFF_),a
        Accumulator=DE; //ld a, (de)
        Accumulator+=0xD0;  //add a, $D0




        //System.out.println(Integer.toBinaryString(DE));
        System.out.println("Room Nr.: "+roomNr);
        System.out.println("DE: "+String.format("0x%02X", DE));
        System.out.println("HL: "+String.format("0x%02X", HL));
        System.out.println("IX: "+String.format("0x%02X", IX));
        System.out.println("Accumulator: "+String.format("0x%02X", Accumulator));
        System.out.println("Max. Level Length: "+String.format("0x%02X", _RAM_DE3E_MAX_LVL_LENGHT));

        System.out.println("_RAM_DE31_METATILE_BANK: "+String.format("0x%02X", _RAM_DE31_METATILE_BANK));
        System.out.println("_RAM_DE29_METATILE_TILE_LOAD: "+String.format("0x%02X", _RAM_DE29_METATILE_TILE_LOAD));
        System.out.println("_RAM_DE32_: "+String.format("0x%02X", _RAM_DE32_));
        System.out.println("_RAM_DE2F_: "+String.format("0x%02X", _RAM_DE2F_));

        System.out.println("RAM_DE2E_BANKSWITCH_LEVEL: "+String.format("0x%02X", _RAM_DE2E_BANKSWITCH_LEVEL));
        System.out.println("_RAM_DE2A_: "+String.format("0x%02X", _RAM_DE2A_));
        System.out.println("_RAM_DE5E_FLOORFALLXCOORD: "+String.format("0x%02X", _RAM_DE5E_FLOORFALLXCOORD));
        System.out.println("_RAM_DE60_: "+String.format("0x%02X", _RAM_DE60_));
        System.out.println("_RAM_DE53_COMPASS: "+String.format("0x%02X", _RAM_DE53_COMPASS));
        System.out.println("_RAM_DE60_: "+String.format("0x%02X", _RAM_DE60_));
        //roomDetails.add(static_cast<String>(roomNr));
        //Okay, these details seem to be good for now to get some details about the maps. The map loading itself is anything but trivial, for me at least.

        //System.out.println(Integer.toBinaryString(lTemp<<2));
        //int HL,DE,BC,IX,IY=0;
        //int sHL,sDE,sBC,sAccumulator;
    }   //Just displays the level details, that are in the ROM for each room.
    public static byte[][] metaTileArranged(byte[] trimmedLevel){
    byte[][] arrangedLevel=new byte[7][255];    //There are eight strips, and 255 blocks per stripe. If we arrange the other way, we could get the level data per column.
        for (int i=0;i<0xFF;i++){
            arrangedLevel[0][i]=0;
            arrangedLevel[1][i]=0;
            arrangedLevel[2][i]=0;
            arrangedLevel[3][i]=0;
            arrangedLevel[4][i]=0;
            arrangedLevel[5][i]=0;
            arrangedLevel[6][i]=0;
            arrangedLevel[7][i]=0;
        }   //Get the arrays in a known state.
        for (int i=0;i<0xFF;i++) {                       //We copy the metatiles from the original, into this much easier handleable array.
            arrangedLevel[0][i]=trimmedLevel[0x800+i];   //The first 2k is not needed here.
            arrangedLevel[1][i]=trimmedLevel[0x900+i];   //This is much easier to handle with HEX numbers.
            arrangedLevel[2][i]=trimmedLevel[0xA00+i];
            arrangedLevel[3][i]=trimmedLevel[0xB00+i];
            arrangedLevel[4][i]=trimmedLevel[0xC00+i];
            arrangedLevel[5][i]=trimmedLevel[0xD00+i];
            arrangedLevel[6][i]=trimmedLevel[0xE00+i];
            arrangedLevel[7][i]=trimmedLevel[0xF00+i];
        }   //Arrange into a bit more manageable matrix.
        return arrangedLevel;
    }   //Returns a matrix with the used metatiles in a more manageable form. The first dimension is the row, and the second is the block.
    public static byte[] getGFXChunks(int roomNr){
        byte[] result=new byte[0x1260];   //This will hold the graphics part, that we are extracting from the VRAM dumps.
        Arrays.fill(result, (byte) 0);  //Init the array with a known value.
        //From: $2180 To: $33E0 which means 0x1260 bytes, or 4704 in decimal. This totals to 147 tiles, and many rooms did not used this many, this is the maximum an unaltered game would
        //use.
        String path="//home//levi//devver//disas//HOTL//anotherexam//editor//room_vram//";
        String fName="HOTL_mod_ah";
        String extension="vrm";
        String fullFName=path+fName+roomNr+extension;   //Concat names, so we can use the right file.
        byte[] vramDump=new byte[16384];    //Make 16k array for the whole VRAM dump.
        vramDump=readRom(fullFName);    //Read the whole dump.
        for(int i=0;i<0x1260;i++) result[i] = vramDump[0x2180 + i];//Copy only the parts we are interested in.
        return result;
    }   //Opens the given level's graphics chunk, and returns the interesting parts with the stage graphics.
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
    //This is a direct copy of the normal read, but I have to modify it to accomodate for the VRAM dumps.



}
