package com.mycompany.editorhotl;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class levelRAMArray {

    /*
    * This is just the levels as how they are represented in the system memory, uncompressed.
    * I think I explained the level structure in the disassembly, but here's the gist of it.
    * The level is made from 16x16 blocks, eight slices. The levels are stored like rows\stripes if you will.
    * This means the maximum level length with this game engine is 256*16 blocks.
    * Every 16x16 metatile is made up from smaller tiles of course.
    * The first 256 bytes are the first tiles of said metatiles.
    * Then the second 256 bytes are offsets, if the game uses the first or the second set of tiles.
    * Then comes the second tile and the rest. This is 1k of data. Of course, there's a lot of free space in this, since the
    * game could not use that many tiles for BG graphics, and it's usually not that "artsy" anyway.
    * After this, the rest are the bigger metatiles, row by row.
    * The code that loads the level also gets which bank to load the graphics from, but that's not really something I wanted to jump into.
    *
    * The graphics in VRAM are loaded sequentially, after the HUD graphics. Four tiles are one metatile one after another starting at $2180.
    * At least the game loads there.
    * Based on what does the game load tiles, I don't know yet. The code that handles level loading is not easy to follow after a while. I may lack the
    * necessary skill to do it.
    *
    *
    * What I want to do is, to make up a tilemap from the mapdata. And with that, I could rebuild and show it as a VRAM dump converted to images to see the full levels.
    * This way, at least we can see what's used, and what's not. More than that, it would give an easier interface to build levels. Maybe to have a nice GUI, a HEX editor, and
    * some frequent refresh.
    *
    * The second 255 bytes handle extra data, such as tile flipping.
    *   -Byte 0:    0:Tile fetched from the first 256 tiles.    1:Tile fetched from the second 256 tiles.
    *   -Byte 1:    0:Tile is not flipped horizontally.   1:Tile is flipped.
    *   -Byte 2:    0:Tile is not flipped vertically. 1:Tile is flipped.
    *   -Byte 3:    This does not do anything, but I suspect that it controls which palette is used for the given tile.
    *                   Since the first and second palette uses 4-5 shared colors, this bit does nothing in this context.
    *   -Byte 4:    This controls the tile priority. 0 is behind sprites, 1 is in front of.
    * Other bits do nothing. Byte 0 is always 1, since all tile data for the background is coming from the second part of VRAM.
    * The game does not use tile priority, so that's one byte less to care about.
    * Flipping IS used, though not very extensively. So for the background, you need two bytes to handle everything.
    * */
    //These are the strips of 16x16 or 2x2 metatiles. This is how you "draw" a level.
    /*
    * The level drawing should be done this way maybe:
    *   Once we converted every level VRAM dump, with right colors and everything, then start to make an array from the metatiles, represented as normal tiledata.
    * First, the tiles fetched, then transformed according to the second bytes.
    * Once that's done, then we'll process the level line by line:
    *   TODO: Concatenate metatiles and handle that part in the export files. This also goes to be able to handle larger background objects. Not mandatory, but makes
    *    level making easier. Like having doors as an graphical object and so on.
    * */
    public static byte[] appendByteArrays(byte[] a, byte[] b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        byte[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    public static BufferedImage[] splitImageInto8x8Tiles(BufferedImage image, boolean exportTiles) {
        int tileWidth = 8;
        int tileHeight = 8;
        int cols = image.getWidth() / tileWidth;
        int rows = image.getHeight() / tileHeight;
        BufferedImage[] tiles = new BufferedImage[cols * rows];
        // Iterate over the image in 8x8 blocks (row by row, then column by column)
        int tileIndex = 0;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                BufferedImage tile = new BufferedImage(tileWidth, tileHeight, image.getType());
                for (int ty = 0; ty < tileHeight; ty++) {
                    for (int tx = 0; tx < tileWidth; tx++) {
                        int pixel = image.getRGB(x * tileWidth + tx, y * tileHeight + ty);
                        tile.setRGB(tx, ty, pixel);
                    }
                }
                tiles[tileIndex] = tile;
                if (exportTiles) {
                    try {
                        String outputPath = String.format("/media/MegaWork/devver/disas/HOTL/anotherexam/editor/tileImg/tile_%d_%d.png", y, x); // File name based on tile position (row, column)
                        ImageIO.write(tile, "png", new File(outputPath));
                        System.out.println("Exported: " + outputPath);
                    } catch (IOException e) {
                        System.err.println("Failed to export tile: " + e.getMessage());
                    }
                }
                tileIndex++;
            }
        }
        return tiles;
    }
    private static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
    public static BufferedImage handleTileAttr(BufferedImage input, byte attr){
        byte t256Mask=0b00000001;   //This is 1 for this game. If it's zero, then the first 256 tiles are used.
        byte horizontalFlipMask=0b00000010;
        byte verticalFlipMask=0b00000100;
        byte paletteMask=0b00001000;
        byte priorityMask=0b00010000; //Unused.
        //See above for explanation.
        byte tempAttr= (byte) (attr&t256Mask);      //TODO: Handle this so it would work. Not mandatory, but still.
        if ((byte)(attr&horizontalFlipMask)!=0){
            input=flipImageHorizontally(input);
        }
        if ((byte)(attr&verticalFlipMask)!=0){
            input=flipImageVertically(input);
        }
        if ((byte)(attr&paletteMask)==0){           //TODO: Different palette IS used, especially with some tiles, and the boxes in the game. Not needed to view the level though.
    }
        if ((byte)(attr&priorityMask)!=0){          //Unused in the game, but should be used while making new maps. TODO: Implement this with the GUI later.
    }
    return input;
    }
    public static BufferedImage flipImageVertically(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, originalImage.getType());
        AffineTransform transform = new AffineTransform();
        transform.translate(0, height);
        transform.scale(1, -1);
        Graphics2D g2d = flippedImage.createGraphics();
        g2d.setTransform(transform);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        return flippedImage;
    }
    public static BufferedImage flipImageHorizontally(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, originalImage.getType());
        AffineTransform transform = new AffineTransform();
        transform.translate(width, 0);
        transform.scale(-1, 1);
        Graphics2D g2d = flippedImage.createGraphics();
        g2d.setTransform(transform);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        return flippedImage;
    }
    public static BufferedImage combineTiles(BufferedImage[] tileArray, byte[] sourceData, boolean exportImage, int metaTileNr) {
        // Create a 16x16 BufferedImage to hold the combined result
        BufferedImage largerImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        // Define the positions of the tiles in the larger image
        int[][] tilePositions = {
                {0, 0},   // Tile 1: Upper-left
                {8, 0},   // Tile 2: Upper-right
                {0, 8},    // Tile 4: Lower-left
                {8, 8}    // Tile 3: Lower-right
        };
        for (int tileIndex = 0; tileIndex < 4; tileIndex++) {
            // Get the tile index from the source data (512 bytes apart)
            int sourceOffset = metaTileNr+tileIndex * 512;
            int tileOffset=metaTileNr+0x100+tileIndex*512;   //We need the tile attributes as well. It starts at 256 or $100 bytes, since the tile number comes first, then every 512 bytes.
            byte tileIndexByte = sourceData[sourceOffset];
            byte tileOffsetByte=sourceData[tileOffset];
            // Convert the byte to an integer index (unsigned byte)
            int tileArrayIndex = tileIndexByte & 0xFF;
            tileArrayIndex+=256;    //This should be handled along with the attributes, though the game never uses the first 256 tiles for level stuff.
            BufferedImage tile = tileArray[tileArrayIndex];
            tile=handleTileAttr(tile,tileOffsetByte);
            // Get the position of the tile in the larger image
            int destX = tilePositions[tileIndex][0];
            int destY = tilePositions[tileIndex][1];
            // Copy the tile into the larger image
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    int pixel = tile.getRGB(x, y); // Get the pixel from the tile
                    largerImage.setRGB(destX + x, destY + y, pixel); // Set the pixel in the larger image
                }
            }
        }
        if (exportImage) {
            try {
                File outputFile = new File("/media/MegaWork/devver/disas/HOTL/anotherexam/editor/tileImg/combined_image"+metaTileNr+".png");    //Changed, as we could import a lot of images.
                ImageIO.write(largerImage, "png", outputFile);
            } catch (IOException e) {
                System.err.println("Failed to export image: " + e.getMessage());
            }
        }
        return largerImage;
    }   //This builds bigger metatiles from smaller ones. You have to handle the tile flipping though.
    public static void exportMetaTiles(rom inputRom,int lvlNr) throws IOException {
    metaTile result=new metaTile();
        int metaTileAmount=256;
        String fn="mTile";
        String ex=".bin";
        String rm="room";
        String pt =inputRom.path+rm+lvlNr;
        File p=new File(pt);
        if (!p.isDirectory()){
        new File (pt).mkdirs();
            System.out.println(pt);
        }
        else{
            System.out.println(pt);
            if (!isDirEmpty(Path.of(pt))){
                System.out.println("Error: The metatile folder is not empty!");
                System.exit(-1);
            }
        }
        inputRom.getDumps(lvlNr);   //This was missing before. ALWAYS open the correct dumps into RAM before trying anything.
        BufferedImage vRAMDumpImage=ConvertVRAMDump(inputRom.VRAMdump);
        BufferedImage[]vRAMTiles=splitImageInto8x8Tiles(vRAMDumpImage,false);
        byte[]tn=new byte[4];
        byte[]temp2=new byte[4*32];
        for (int mTCount = 1; mTCount < 256; mTCount++) {
            for (int j = 0; j < 4; j++) {
                result.tAndAttr[j]=inputRom.ramDump[0x100+j*0x200+(mTCount-1)]; //This -1 is needed, as tiles here don't start from 0. Maybe I f'd up, whatever.
                tn[j]=inputRom.ramDump[j*0x200+(mTCount-1)];
                byte[] temp1=get32ByteSlice(inputRom.VRAMdump, tn[j]);
                for (int i = 0; i < 32; i++) {
                    assert temp1 != null;
                    temp2[j*32+i]=temp1[i];
                }
            }
            for (int i = 0; i < 4*32; i++) {
                result.tAndAttr[32+i]=temp2[i];
                temp2[i]=0;
            }
            try (FileOutputStream fos = new FileOutputStream(pt+"/" + fn + mTCount + ex)) {
                    fos.write(result.tAndAttr);}
        }   //This rolls through all metatiles, and export them as a file, so they can be rearranged later, use in a map list, or stuff like that.
        BufferedImage[]mTiles=new BufferedImage[metaTileAmount];
        for (int i = 0; i < metaTileAmount; i++) {
            mTiles[i]=combineTiles(vRAMTiles, inputRom.ramDump, false, i &0xFF);
            try {
                File outputFile = new File(pt+"/mTile"+i+".png");
                ImageIO.write(mTiles[i], "png", outputFile);
            } catch (IOException e) {
                System.err.println("Failed to export image: " + e.getMessage());
            }
        }    //Export the metatiles somewhere, so you can see what's what.
    }
    public static byte[] get32ByteSlice(byte[] byteArray, int sliceIndex) {
        sliceIndex+=256;    //Modded, we need the second half of the file.
        if (byteArray == null || byteArray.length < 32 * (sliceIndex + 1) || sliceIndex < 0) {
            return null; // Handle invalid input: null array, index out of bounds, or not enough bytes.
        }
        int startIndex = 32 * sliceIndex;
        int endIndex = startIndex + 32;
        return Arrays.copyOfRange(byteArray, startIndex, endIndex);
    }
    public static void getAllMetaTiles(rom inputRom, int lvlNr){
        BufferedImage vRAMDumpImage=ConvertVRAMDump(inputRom.VRAMdump);
        BufferedImage[]vRAMTiles=splitImageInto8x8Tiles(vRAMDumpImage,false);
        int metaTileAmount=256;     //Eh, we need this.
        BufferedImage[]mTiles=new BufferedImage[metaTileAmount];
        for (int i = 0; i < metaTileAmount; i++) { //This needs to be byte. Also, the game never uses even half of the metatiles a byte could hold.
            mTiles[i]=combineTiles(vRAMTiles, inputRom.ramDump, false, i &0xFF);   //Iterate through all metatiles, combine them, and store in this one.
        }
        BufferedImage result=stitchImages(mTiles, inputRom.ramDump);
        saveImageAsBMP(result, lvlNr);
    }
    public static void saveImageAsBMP(BufferedImage image, int lvlNr) {
        try {
            File outputFile = new File("/media/MegaWork/devver/disas/HOTL/anotherexam/editor/tileImg/combined_level"+lvlNr+".png");
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            System.err.println("Failed to export image: " + e.getMessage());
        }
    }
    public static BufferedImage stitchImages(BufferedImage[] sourcePieces, byte[] sourceArray) {
        // Constants
        int pieceSize = 16; // Each piece is 16x16
        int targetWidth = 4096; // Target image width
        int targetHeight = 144; // Target image height
        int piecesPerRow = 254;//targetWidth / pieceSize; // 256 pieces per row
        int numRows = 8;//targetHeight / pieceSize; // 9 rows

        // Create the target image
        BufferedImage targetImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = targetImage.createGraphics();

        // Offset in the sourceArray where the data begins
        int offset = 0x800;

        // Iterate over each row
        for (int row = 0; row < numRows; row++) {
            // Iterate over each column in the row
            for (int col = 0; col < piecesPerRow; col++) {
                // Calculate the index in the sourceArray for the current piece
                int index = offset + (row * 0x100) + col;

                // Ensure the index is within bounds
                if (index < sourceArray.length) {
                    // Get the piece index (unsigned byte)
                    int pieceIndex = sourceArray[index] & 0xFF;

                    // Ensure the pieceIndex is within bounds of the sourcePieces array
                    if (pieceIndex < sourcePieces.length) {
                        // Get the corresponding 16x16 piece from the sourcePieces array
                        BufferedImage piece = sourcePieces[pieceIndex];

                        // Calculate the position to place the piece in the target image
                        int destX = col * pieceSize;
                        int destY = row * pieceSize;

                        // Copy the piece to the target image
                        g2d.drawImage(piece, destX, destY, null);
                    } else {

                        System.out.println("Invalid piece index at row " + row + ", col " + col + ": " + pieceIndex);
                        BufferedImage defaultPiece = sourcePieces[0];
                        int destX = col * pieceSize;
                        int destY = row * pieceSize;
                        g2d.drawImage(defaultPiece, destX, destY, null);

                    }
                } else {
                    System.err.println("Source array index out of bounds at row " + row + ", col " + col);
                }
            }
        }

        g2d.dispose();
        return targetImage;
    }
    public static BufferedImage ConvertVRAMDump(byte[]vRamDump){
        int tileSize = 8; // Each tile is 8x8 pixels
        int imageWidth = 64; // Width of the output image
        int imageHeight = 512; // Height of the output image
        PAL gamePalette=new PAL();  //Use the general palette the game had.
        return decodeTiles(vRamDump, tileSize, imageWidth, imageHeight, gamePalette.colors);
    }   //This returns an image of the given SMS VRAM dump.
    public static BufferedImage decodeTiles(byte[] tileData, int tileSize, int imageWidth, int imageHeight, Color[] colorPalette) {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        int tilesPerRow = imageWidth / tileSize;
        int tilesPerColumn = imageHeight / tileSize;
        int tileIndex = 0;

        for (int tileY = 0; tileY < tilesPerColumn; tileY++) {
            for (int tileX = 0; tileX < tilesPerRow; tileX++) {
                int tileStartIndex = tileIndex * 32; // Each tile is 32 bytes
                for (int row = 0; row < tileSize; row++) {
                    int bitplane0 = tileData[tileStartIndex + row * 4] & 0xFF;
                    int bitplane1 = tileData[tileStartIndex + row * 4 + 1] & 0xFF;
                    int bitplane2 = tileData[tileStartIndex + row * 4 + 2] & 0xFF;
                    int bitplane3 = tileData[tileStartIndex + row * 4 + 3] & 0xFF;

                    for (int col = 0; col < tileSize; col++) {
                        int pixelValue = ((bitplane3 >> (7 - col)) & 1) << 3 |
                                ((bitplane2 >> (7 - col)) & 1) << 2 |
                                ((bitplane1 >> (7 - col)) & 1) << 1 |
                                ((bitplane0 >> (7 - col)) & 1);
                        Color color = colorPalette[pixelValue];
                        int x = tileX * tileSize + col;
                        int y = tileY * tileSize + row;
                        image.setRGB(x, y, color.getRGB());
                    }
                }
                tileIndex++;
            }
        }

        return image;
    }   //Used to decode raw binary into an image.

}
