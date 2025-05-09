/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.editorhotl;

/**
 *
 * @author levi
 */
    public class door{//This will hold the door object that we are extracting from the ROM.
/*
	This defines an individual door in the ROM, where the player can go through.
*/
	int doorAddress;		//This is the same as the door type address as well. This is valid in hexa.
   doortype door=new doortype();	//Individual bits are needed to be handled this way.
    byte roomnumber=0;
    byte columnplyrStarts=0;
    byte doorStart=0;
    byte doorEnd=0;
/*
At this point, I suspect there is some pattern with these things, but I have to map them still.
	I was right. The compass byte consists the following bits:
	0: If this is 1, then it's an upward exit.
	1: If this is 1, then this is a downward exit.
	2: If this is 1, then it's handled as the falldown stage exit from level 1.
	3: A bit unsure, it does not seem to do anything on it's own, or with the above bytes.
	4: Waterfall bit. If this is 1, then the game will tell the player the wfall will cure wounds, then proceeds to the destination.
	5: No effect.
	6: No effect.
	7: The compass will not mark the door!
	Bits 7+4 will enable waterfall effect.
    Bits 7+6 no mark, but works normally.
    Same for 7+5
    Same for 7+6+5
    Same for 7+4
    Unused bits seems to do nothing combined together, but if this is not the case, i'll update this.
    So, the compass data is another object for the future.
 * 
 */

    /*
     * 2acc-190d is 11BF which is 4543 bytes. I'm sure this is not all the doors in the game, so there might be some other data in this bunch.
     * There are 88 room entries, so anything over that is invalid probably. The byte definitions are also in the applied hacks disassembly, but i'll
     * put those details here as well:
     * First byte is the compass type. You either can go up or down. $01 is up, $02 is down. There is a compass variable elsewhere that will tell how it should be drawn.
     * Second byte is the room nr where the player will go if (s)he takes the door.
     * Third byte is the column number where the player will end up after taking the door.
     * Fourth byte tells the game which column the door begins.
     * Fifth bye is the end.
     * The door end and beginning is that when the game recognizes some place of the room as a door, it will not draw the door itself.
     * 
     */


}
