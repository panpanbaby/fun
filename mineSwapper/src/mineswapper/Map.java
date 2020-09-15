package mineswapper;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Map {

    int size;
    int mineNumber;
    public static short MINE = 9;
    short[][] mineDetail;//显示周围有多少个雷的矩阵，最大为8，9表示为雷
    boolean[][] visionInfo;//可见度矩阵，true表示可见，false表示不可见
    HashSet<Point> mines;
    Map (){

    }

    Map(int size, int mineNumber){
        this.size = size;
        this.mineNumber = mineNumber;
        mineDetail = new short[size][size];
        visionInfo = new boolean[size][size];
        mines = new HashSet<>();
        initMine();
    }
    void initMine(){
        Random random = new Random();
        for (int i = 0; i < mineNumber; i++) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            if(!isMine(x,y)) {
                updateMineDetail(x, y);
                mines.add(new Point(x, y));
            }else{
                i--;
            }
        }
    }
    void updateMineDetail(int x, int y){
        mineDetail[x][y] = MINE;

        for (int i = -1;i <= 1 ;i++){
            for (int j = -1; j <= 1; j++) {
                if(x+i > -1 && x+i <size && y+j > -1 && y+j <size)
                    mineDetail[x+i][y+j] += 1;
            }
        }

    }
    boolean isMine(int x, int y){
        return mineDetail[x][y] >= MINE;
    }
    boolean visible(int x, int y){
        return visionInfo[x][y];
    }
    boolean isNumber(int x, int y){
        return visionInfo[x][y] && mineDetail[x][y] > 0 && mineDetail[x][y] < MINE;
    }
    void printMap(){
        System.out.println();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(mineDetail[i][j]);
                if (visionInfo[i][j])
                    System.out.print("t\t\t");
                else {
                    System.out.print("F\t\t");
                }
            }
            System.out.println();
        }
    }

    void printVisionInfo(){
        System.out.println();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(visionInfo[i][j]);
                System.out.print("\t\t");
            }
            System.out.println();
        }
    }
    static Map  readMap(){
        File file =new File("map.txt");


        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(file));
            List<String> datas = new ArrayList<String>();
            int columnSize = 0;
            String line;
            while((line = fileReader.readLine()) != null){

                if( columnSize == 0) {
                    String[] nums = line.split("\t");
                    for (int i = 0; i < nums.length; i++) {
                        if (nums[i].length() != 0) {
                            columnSize++;
                        }
                    }
                }
                datas.add(line);
            }
            int rowSize = datas.size();
            if( rowSize == columnSize){

                Map map = new Map();
                map.size = rowSize;
                map.mineDetail = new short[map.size][map.size];
                map.visionInfo = new boolean[map.size][map.size];
                map.mineNumber = 0;
                map.mines = new HashSet<>();
                int j = 0;
                for (String l: datas
                     ) {
                    String[] items = l.split("\t");

                    for (int i = 0; i < map.size; i++) {
                        map.mineDetail[j][i] = Short.parseShort(items[i]);
                        if (map.isMine(j, i)) {
                            map.mineNumber++;
                            map.mines.add(new Point(j, i));
                            //System.out.println("ismine x:"+ (j) + " y: "+ (i));
                        }

                    }
                    j++;
                }

                return map;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(fileReader != null){
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return  null;

    }
    void recordMap()  {
        File file =new File("map.txt");

        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //使用true，即进行append file

        BufferedWriter fileWritter = null;
        try {
            fileWritter = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    fileWritter.write(String.valueOf(mineDetail[i][j]));
                    fileWritter.write('\t');
                }
                fileWritter.write('\n');
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fileWritter != null)
                    fileWritter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }



    }
//    public void render(){
//        int times = 5;
//        Color emptyColor =  new Color(255, 255, 255, 0);
//        Color wallColor = new Color(255,128,0);
//        // 创建image
//        BufferedImage image = new BufferedImage(lengthofSize * times + 2 * times, lengthofSize * times + 2 * times, BufferedImage.TYPE_INT_ARGB);
//        // 创建画笔
//        Graphics graphics = image.getGraphics();
//        for (int i = 0; i <= lengthofSize + 1; i++) {
//            for (int j = 0; j <= lengthofSize + 1; j++) {
//                if (i == 0 || i == lengthofSize + 1 || j == 0 || j == lengthofSize + 1)
//                    graphics.setColor(wallColor);
//                else
//                    graphics.setColor(grid[transfer(i-1,j-1)] ? emptyColor : wallColor);
//                graphics.fillRect(i * times,j * times, times, times);
//            }
//        }
//
//        try{
//            ImageIO.write(image, "png", new File("C:\\Users\\庞超\\Documents\\code\\TestInJava\\algorithm\\Maze.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            graphics.dispose();
//        }
//
//    }
}
