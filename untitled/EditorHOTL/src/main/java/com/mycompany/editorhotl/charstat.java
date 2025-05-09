package com.mycompany.editorhotl;
import org.apache.commons.lang3.ArrayUtils;
public class charstat {
    /*
    * This defines a character's details, stats, inventory and so on. Create a companion, and then assign one of this to it, and fill it with data.
    * */
    byte[] Inventory=new byte[16];
    byte[] startingItems=new byte[2];   //These have to be valid items. TODO: Do checks in this class or something.
    byte[] invFull=new byte[2]; //Is the inventory full? If this is non-zero, then the player won't be able to pick up items.
    byte currentHP;
    byte maxHP;
    byte dmg1Min;
    byte dmg1Max;
    byte dmg2Min;
    byte dmg2Max;
    byte strength;
    byte intelligence;
    byte wisdom;
    byte constitution;
    byte dexterity;
    byte charisma;
    byte unused;
    public static byte[] enterStartItems(byte item){
        byte[] result=new byte[2];
        Item itemList=new Item();
        if (item>itemList.itemNames.length){
            System.out.println("Invalid Item Nr.! Zeroes\\Unarmed were added");
            item=0;
        }
        else {
            System.out.println(itemList.itemNames[item]+"   Was added as starting items.");
        }
            result[0]=item;
            result[1]=item;
        return result;
    }//Changes the given charstat's starting item.
    public static byte[] invState(boolean isFull){
        byte[] result=new byte[2];
        if (isFull) {
            result[0]=1;
            result[1]=1;
        }/* else {
            //result[0]=0;
            //result[1]=0;
        }*/
        return result;
    }   /*Depending on the bool, it makes the player's inv. full is that's what needed.
    There might be a need for example to make a character unable to pick up any items for whatever reason. Otherwise, this can be left alone.
    */
    public  byte[] wrapCharStat(){
    byte[] result=new byte[17];

        result[0]=this.startingItems[0];
        result[1]=this.startingItems[1];
        result[2]=this.invFull[0];
        result[3]=this.invFull[1];
        result[4]=this.currentHP;
        result[5]=this.maxHP;
        result[6]=this.dmg1Min;
        result[7]=this.dmg1Max;
        result[8]=this.dmg2Min;
        result[9]=this.dmg2Max;
        result[10]=this.strength;
        result[11]=this.intelligence;
        result[12]=this.wisdom;
        result[13]=this.constitution;
        result[14]=this.dexterity;
        result[15]=this.charisma;
        result[16]=this.unused;
        result= ArrayUtils.addAll(this.Inventory,result);   //Eh, just concat the two arrays.
    return  result;
    } /*Gets data from a charstat object, then puts it in a byte array, so it can be easier to work with.
                                        This array would be passed on later to modify a ROM image.
                                        You have to give it a proper inventory as well. */
    public void modInv(byte position, byte item){
        Item check=new Item();
        if (position>8){
            System.out.println("Invalid Inventory position: "+position);
        }else{
            if (item>check.itemArrayLength){
        System.out.println("Invalid item type. Not in the list: "+item);
            }else{
                this.Inventory[position*2]=item;
                this.Inventory[(position*2)+1]=item;
                //Every inventory item uses two bytes.
            }
        }
    }   //As the name suggests, this replaces an item in the current character's inventory.
    public void listInv(){
        for (int i=0;i<16;i+=2){
            System.out.println(this.Inventory[i]);
        }
    }   //Lists *this* charstat's inventory.
}
