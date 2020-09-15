package mineswapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

/**
 * 自动扫雷程序
 */
public class MineSwapper {

    Map map;
    int mineNum;
    HashSet<Point> foundMine;
    Queue<Point> unknowPoints;
    Queue<Point> tasks = new LinkedList<>(); //待扫的点
    Queue<Point> checkPoints = new LinkedList<>(); //假设时评判的点
    Stack<Operation> stack = new Stack<>();
    List<Point> invisiblePoints = new ArrayList<>();
    MineSwapper(Map map) {
        this.map = map;
        foundMine = new HashSet<>();
        unknowPoints = new LinkedList<>();
        mineNum = map.mineNumber;
        for (int i = 0; i < map.size; i++) {
            for (int j = 0; j < map.size; j++) {
                invisiblePoints.add(new Point(i, j));
            }
        }

    }

    static public void main(String[] args) {
//        Map map = new Map(16, 40);
//        map.recordMap();
        Map map = Map.readMap();
        if (map != null) {
            map.printMap();
            //System.out.println(map.mines);
            MineSwapper mineSwapper = new MineSwapper(map);
            mineSwapper.play();
        }
    }

    boolean random(int ix) {
        Random random = new Random();
        System.out.println("before random size :" + invisiblePoints.size());
        int index = ix == 0 ? random.nextInt(invisiblePoints.size()):ix;

        Point p = invisiblePoints.remove(index);
        if ( map.visionInfo[p.x][p.y]){
            System.out.println("????????????????????????????");
        }

            System.out.println("randommmmmmm x:" + p.x + ",y:" + p.y);
            if (map.isMine(p.x, p.y)) {
                System.out.println("oh mine!!! game over!");
                return false;
            } else {
                map.visionInfo[p.x][p.y] = true;
                if (map.mineDetail[p.x][p.y] == 0) {
                    expand(p.x, p.y);
                } else {
                    addTask(p.x, p.y);
                }
                return true;
            }


    }

    void play() {
        //随机选一个初始点
        boolean successful = random(11 * map.size + 12);
        if (successful) {
            while (mineNum > 0) {
                while (!tasks.isEmpty() && mineNum > 0) {
                    Point task = tasks.poll();
                    handletask(task.x, task.y,false);
                }

                while(mineNum > 0 &&!unknowPoints.isEmpty()){
                    Point unknow = unknowPoints.poll();
                    asume(unknow.x, unknow.y);
                }
                System.out.println("asume done");
                if (tasks.isEmpty() && mineNum > 0){
                    if (invisiblePoints.size() == 1){
                        System.out.println("left one !!!!!");
                        Point p = invisiblePoints.get(0);
                        clickOnMinePoint(p.x, p.y);
                    }else{
                        if (!random(0))
                            return;
                    }

                }
            }
        }
    }

    //模拟鼠标左键，点开非地雷点
    void clickOnNormalPoint(int x, int y) {
        map.visionInfo[x][y] = true;
        //System.out.println("size :"+invisiblePoints.size());
        invisiblePoints.remove(new Point(x, y));
        //System.out.println("after remove size :"+invisiblePoints.size());
        if (map.mineDetail[x][y] > Map.MINE) {
            System.out.println("error  !!!!!!!!!!!!!!!!!!!!!!");
        }
        if (map.mineDetail[x][y] == 0) {
            expand(x, y);
        } else {
            addTask(x, y);
        }
    }
    void clickOnMinePoint(int x, int y){
        map.visionInfo[x][y] = true;
        invisiblePoints.remove(new Point(x, y));
        boolean isMine = map.isMine(x, y);

        map.mineDetail[x][y] += Map.MINE;
        foundMine.add(new Point(x, y));
        mineNum--;
        if (isMine) {
            Point mine = new Point(x, y);
            //System.out.println("mine x :" + x + " y : "+ y);
            if (map.mines.contains(mine)) {
                System.out.println("挖到一个，坐标为" + (x + 1) + "," + (y + 1) + "剩余" + mineNum + "个");
            }


        } else {
            System.out.println("坐标为" + (x + 1) + "," + (y + 1) + ",挖错了。。。。");
//                                map.visionInfo[x][y] = false;
//                                map.mineDetail[x][y] -= Map.MINE;
//                                foundMine.remove(new Point(x, y));
//                                mineNum++;
//                                handletask(x, y);
        }
        addTask(x, y);
    }
    boolean handletask(int x, int y, boolean check) {
        //System.out.println("handler x:" + x + ",y:" + y);
        if (map.visible(x, y))
            return true;
        //利用周边的数字信息确认是否为地雷
        int finalresearchResult = -1;
        bianli:
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                //遍历周边的数字方格，看能否通过其中一个确定中心方格是否为地雷
                if (x + i > -1 && x + i < map.size && y + j > -1 && y + j < map.size && map.isNumber(x + i, y + j)) {
                    int researchResult = research(x + i, y + j);
                    if( researchResult >= 0){
                        if (finalresearchResult < 0){
                            finalresearchResult = researchResult;
                        }else{
                            if (finalresearchResult != researchResult){
                                return false;
                            }
                        }
                    }else if (researchResult == -1){
                        continue;
                    } else {
                        //碰到异常情况，说明假设不成立立即返回，退栈
                        System.out.println("异常");
                        return false;
                    }

                    if(!check)
                        //当不在检查状态时，得到一个结果就退出循环
                        //当在检查状态，就会继续循环进行检查
                        break bianli;
                }
            }
        }

        if (finalresearchResult == 1) {
            if(check){

                map.visionInfo[x][y] = true;
                map.mineDetail[x][y] -= Map.MINE;
                stack.push(new Operation(new Point(x, y), (short)-Map.MINE));
                addCheck(x, y);
            }else {
                clickOnNormalPoint(x, y);
            }

        } else if (finalresearchResult == 0) {

            if(check){
                map.visionInfo[x][y] = true;
                map.mineDetail[x][y] += Map.MINE;
                stack.push(new Operation(new Point(x, y),(short)Map.MINE));
                addCheck(x, y);
            }else {
                clickOnMinePoint(x, y);
            }
        }


        if (!check) {
            //如果无法根据周边格子获取信息，将该点放到待定集合中
            if (!map.visible(x, y)) {
                unknowPoints.add(new Point(x, y));
            }
        }else {

        }
        return true;
    }

    //以某个可视数字方格为中心调查周边埋雷情况
    //有三种正常情况，两种异常情况；
    // 三种正常情况
    // 一是周围已扫除的地雷数等于方格上的数字,返回1
    //二是已扫除的地雷数小于方格上的数字，但是加上周边不可视方格的数量正好等于，返回0
    //三是已扫除的地雷数小于方格上的数字，但是加上周边不可视方格的数量又大于，返回-1
    //两种异常情况
    // 一是周围已扫除的地雷数大于方格上的数字,返回-2
    // 二是已排除的地雷加上不可见的小于方格上标的数字，返回-3
    int research(int x, int y) {
        int currentMineNum = 0;
        int invisibleNum = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (x + i > -1 && x + i < map.size && y + j > -1 && y + j < map.size) {
                    if (map.visible(x + i, y + j)) {
                        if (map.isMine(x + i, y + j)) {
                            currentMineNum++;
                        }
                    } else {
                        invisibleNum++;
                    }
                }
            }
        }
        if (currentMineNum > map.mineDetail[x][y]){
            return -2;
        } else if (currentMineNum == map.mineDetail[x][y]) {
            return 1;
        } else if (currentMineNum + invisibleNum == map.mineDetail[x][y]) {
            return 0;
        } else if (currentMineNum + invisibleNum > map.mineDetail[x][y]){
            return -1;
        }else{
            return -3;
        }
    }

    //利用广度遍历算法，点亮所有的空白格
    void expand(int x, int y) {
        Queue<Point> points = new LinkedList<>();
        expandEmpty(points, x, y);
        while (!points.isEmpty()) {
            Point point = points.remove();
            expandEmpty(points, point.x, point.y);

        }

    }

    //将空白单元格四周的空白格子放入队列,同时点亮四周8个格子,并且将带数字格子上下左右的未知格添加到任务队列
    void expandEmpty(Queue<Point> points, int x, int y) {

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (x + i > -1 && x + i < map.size && y + j > -1 && y + j < map.size && !map.visible(x + i, y + j)) {
                    map.visionInfo[x + i][y + j] = true;
                    invisiblePoints.remove(new Point(x+i, y+j));
                    if (map.mineDetail[x + i][y + j] == 0)
                        points.add(new Point(x + i, y + j));
                    else {
                        addTask(x + i, y + j);
                    }
                }

            }
        }
    }

    void addCheck(int x, int y) {
        boolean add = false;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (x + i > -1 && x + i < map.size && y + j > -1 && y + j < map.size && !map.visible(x + i, y + j)) {
                    checkPoints.add(new Point(x + i, y + j));
                    add = true;
                }

            }
        }
        //System.out.println(add);
    }


    void addTask(int x, int y) {
        boolean add = false;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (x + i > -1 && x + i < map.size && y + j > -1 && y + j < map.size && !map.visible(x + i, y + j)) {
                    tasks.add(new Point(x + i, y + j));
                    unknowPoints.remove(new Point(x + i, y + j));
                    add = true;
                }

            }
        }
        //System.out.println(add);
    }

    void asume(int x, int y) {
        stack.clear();
        checkPoints.clear();
        System.out.println("asume x:"+x+" y: "+y);
        //map.printMap();
        map.visionInfo[x][y] = true;
        map.mineDetail[x][y] += Map.MINE;
        stack.push(new Operation(new Point(x, y), Map.MINE));
        addCheck(x, y);

        while (!checkPoints.isEmpty()) {
            Point p = checkPoints.poll();
            if (!handletask(p.x, p.y, true)){
                rollBack(stack);
                clickOnNormalPoint(x, y);
                System.out.println("sure one x:" +x+" y:"+y);
                break;
            }

        }
        rollBack(stack);

    }

    void rollBack(Stack<Operation> stack) {
        while(!stack.empty()){
            Operation o = stack.pop();
            map.mineDetail[o.p.x][o.p.y] -= o.valueChange;
            map.visionInfo[o.p.x][o.p.y]  = false;
        }
    }
}

class Operation{
    Point p;
    short valueChange;
    Operation(Point p, short valueChange){
        this.p = p;
        this.valueChange = valueChange;
    }
}

class Point{
    int x;
    int y;
    Point(int x, int y){
        this.x = x;
        this.y = y;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Point){
            Point obj1 = (Point)obj;
            return x == obj1.x && y == obj1.y;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}