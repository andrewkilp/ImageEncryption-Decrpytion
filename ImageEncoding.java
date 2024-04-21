import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
public class ImageEncoding {
    public static void encodeFile(String fileName) {
        File file = new File(fileName);
        int calculateW=1, calculateH=1, calculateTiles;
        calculateTiles = ((int) file.length()/3) +1;
        calculateW = ((int) Math.sqrt(calculateTiles)) +1;
        calculateH = ((int) Math.sqrt(calculateTiles)) +1;
        BufferedImage encodedFile = new BufferedImage(calculateW,calculateH, BufferedImage.TYPE_INT_RGB);
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            for(int row = 0; row< calculateH; row++) {
                for(int col = 0; col<calculateW; col++) {
                    byte[] hexcode = {dis.readByte(),dis.readByte(),dis.readByte()};
                    StringBuilder hexBuilder = new StringBuilder();
                    hexBuilder.append("0x");
                    for (byte b : hexcode) {
                        hexBuilder.append(String.format("%02X", b));
                    }
                    String hexString = hexBuilder.toString();
                    Color c = Color.decode(hexString);
                    encodedFile.setRGB(col, row, c.getRGB());
                }
            }
        } catch (EOFException ex) {
        } catch (IOException eIoException) {
            eIoException.printStackTrace();
        }
        try {
            File outputFile = new File(fileName+".png");
            ImageIO.write(encodedFile, "png", outputFile);
        }catch (IOException e) {
        }
    }
    public static void decodeFile(String fileName) {
        try {
            BufferedImage imageToBeDecoded = ImageIO.read(new File(fileName));
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName + ".txt"));
            for(int row = 0; row<imageToBeDecoded.getHeight(); row++) {
                for(int col = 0; col<imageToBeDecoded.getWidth(); col++) {
                    int value = imageToBeDecoded.getRGB(col, row);
                    if(value == -16777216) break;
                    byte byte1 = (byte) ((value >> 16) & 0xFF);
                    byte byte2 = (byte) ((value >> 8) & 0xFF);
                    byte byte3 = (byte) (value & 0xFF);
                    dos.write(byte1);
                    dos.write(byte2);
                    dos.write(byte3);
                }
            }
            dos.close();
        } catch (IOException e) {
        }
    }
    public static void encryptedEncodeFile(String fileName) {
        File file = new File(fileName);
        int calculateW=1, calculateH=1, calculateTiles;
        calculateTiles = ((int) file.length()/3) +1;
        calculateW = ((int) Math.sqrt(calculateTiles)) +1;
        calculateH = ((int) Math.sqrt(calculateTiles)) +1;
        BufferedImage encodedFile = new BufferedImage(calculateW,calculateH, BufferedImage.TYPE_INT_RGB);
        Random random = new Random();
        int seed = random.nextInt();
        random.setSeed(seed);
        System.out.println(seed);
        ArrayList<String> hexCodesPossible = new ArrayList<>();
        for (int i = 0; i <= 0xFFFFFF; i++) {
            String hex = String.format("%06X", i);
            hexCodesPossible.add(hex);
        }
        BufferedWriter decodeWriter=null;
        try {
            decodeWriter = new BufferedWriter(new FileWriter(fileName+".pngDecodingKey.txt"));
        } catch (IOException ex) {}
        HashSet<String> assignedValues = new HashSet<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(fileName))) {
            for(int row = 0; row< calculateH; row++) {
                for(int col = 0; col<calculateW; col++) {
                    byte[] hexcode = {dis.readByte(),dis.readByte(),dis.readByte()};
                    StringBuilder hexBuilder = new StringBuilder();
                    for (byte b : hexcode) {
                        hexBuilder.append(String.format("%02X", b));
                    }
                    int randomInt = random.nextInt(hexCodesPossible.size());
                    String hexCode = hexBuilder.toString();
                    if(!assignedValues.contains(hexCode)) assignedValues.add(hexCode);
                    String randomString = hexCodesPossible.get(randomInt);
                    decodeWriter.write(String.format("%s %s%n", randomString, hexCode));
                    Color c = Color.decode("0x"+randomString);
                    System.out.println(c.toString());
                    encodedFile.setRGB(col, row, c.getRGB());
                }
            }
        }catch (EOFException ex) {
            try{
                decodeWriter.close();
            } catch(IOException IOException) {}
        } catch (IOException eIoException) {
            eIoException.printStackTrace();
        }
        try {
            File outputFile = new File(fileName+".png");
            ImageIO.write(encodedFile, "png", outputFile);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void encryptedDecodeFile(String fileName) {
        HashMap<String, String> values = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(fileName+"DecodingKey.txt"));) {
            Pattern pattern = Pattern.compile("(\\w{6})\\s(\\w{6})");
            while(scanner.hasNextLine()) {
                Matcher matcher = pattern.matcher(scanner.nextLine());
                if (matcher.find()) {
                    String fistValue = matcher.group(1);
                    String secondValue = matcher.group(2);
                    values.put(fistValue, secondValue);
                }
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(fileName + ".txt"));
            BufferedImage fileToBeDecoded= ImageIO.read(new File(fileName));
            for(int row = 0; row<fileToBeDecoded.getHeight(); row++) {
                for (int col = 0; col<fileToBeDecoded.getWidth();col++) {
                    int value = fileToBeDecoded.getRGB(col, row);
                    byte byte1 = (byte) ((value >> 16) & 0xFF);
                    byte byte2 = (byte) ((value >> 8) & 0xFF);
                    byte byte3 = (byte) (value & 0xFF);
                    String hex = String.format("%02X%02X%02X", byte1, byte2, byte3);
                    System.out.println(hex);
                    if(hex.equals("000000")) break;
                    Color c = Color.decode("0x"+values.get(hex));
                    int val = c.getRGB();
                    byte byte1a = (byte) ((val >> 16) & 0xFF);
                    byte byte2a = (byte) ((val >> 8) & 0xFF);
                    byte byte3a = (byte) (val & 0xFF);
                    dos.writeByte(byte1a);
                    dos.writeByte(byte2a);
                    dos.writeByte(byte3a);
                }
            }
            dos.close();
        } catch(IOException e) {
        }
    }
}
