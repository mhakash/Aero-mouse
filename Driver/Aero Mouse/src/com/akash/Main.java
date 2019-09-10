package com.akash;

import com.fazecast.jSerialComm.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Scanner;

public class Main {
    private static double Ax, Ay, Az, Gx, Gy, Gz;
    private static int button;
    private static boolean rightButtonPressed, leftButtonPressed;

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("Height = " + screenSize.height);
        System.out.println("Width = " + screenSize.width);
        double i = 1, j = 1;
        int posx = screenSize.width / 2, posy = screenSize.height / 2;
        double tempx = posx, tempy = posy;

        robot.mouseMove(posx, posy);


        // write your code here
        System.out.println("ok");
        SerialPort[] ports = SerialPort.getCommPorts();


        for (SerialPort port : ports) {
            System.out.println(port.getSystemPortName());
//            if(port1.getPortDescription().equalsIgnoreCase("BthModem0")) {
//                port = port1;
//            }
        }

        SerialPort port = ports[0];


        if (port != null && port.openPort()) {
            System.out.println("port opened");
            System.out.println(port.getDescriptivePortName());
            System.out.println(port.getPortDescription());
        } else {
            System.out.println("unable to open port");
            return;
        }

        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        port.setBaudRate(38400);
        Scanner data = new Scanner(port.getInputStream());

        try {
            long beginTime = System.currentTimeMillis();
            //int timesDataReceived = 0;
            while (data.hasNextLine()) {
                try {
                    String[] s = data.nextLine().split(",");//assuming data comes like "m,100,200" format

                    /*for(String str: s) {
                        System.out.printf("%10s", str);
                    }
                    System.out.println();*/

                    if (s.length != 7) {
                        System.out.println("Invalid String");
                        continue;
                    }

                    parseData(s);
                    double elapsedTime = (System.currentTimeMillis() - beginTime) / 1000.0;
                    if (button == 0) {
                        //no button pressed
                        if (leftButtonPressed) {
                            leftButtonPressed = false;
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        } else if (rightButtonPressed) {
                            rightButtonPressed = false;
                            robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                        }
                    } else if (button == 1) {
                        //left button pressed
                        if (!leftButtonPressed) {
                            leftButtonPressed = true;
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        }
                    } else {
                        //right button pressed
                        if (!rightButtonPressed) {
                            rightButtonPressed = true;
                            robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                        }
                    }

                    tempx += (9.8 * Ax - Gz) / 4.0;  //Gz is negative on right side
                    tempy -= (9.8* Az - 9.8 + Gx) / 4.0 * 1.67; //subtracted gravity from Az
                    posx = (int) tempx;
                    posy = (int) tempy;
                    robot.mouseMove(posx, posy);


                    /*
                    double accX = Double.parseDouble(acc[2].trim()) + 0.37;
                    //System.out.println(" " + accX);
                    double accY = Double.parseDouble(acc[0].trim()) - 0.75;
                    System.out.printf("%.3f       %.3f\n", accX, accY);
                    tempx -= (accX/8);
                    tempy -= (accY/8);
                    posx = (int) tempx;
                    posy = (int) tempy;
                    robot.mouseMove(posx, posy);*/
                /*String[] parts = s.split(",");
                String mode = parts[0];
                tempx = (double)Integer.parseInt(parts[1]);
                tempy = (double)Integer.parseInt(parts[2]);
                System.out.println(mode+" "+tempx+" "+tempy);
                i = (tempx-posx)/1000;
                j = (tempy-posy)/1000;

                if(mode.equals("m")){
                    int t=1000;
                    while(t-- != 0){
                        posx += i;
                        posy += j;
                        System.out.println(posx+" "+posy);
                        robot.mouseMove((int)posx,(int)posy);
                    }
                    posx = tempx;
                    posy = tempy;
                }

                else if(mode.equals("l")){
                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                }

                else if(mode.equals("r")){
                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*timesDataReceived++;
                long elapsedTime = System.currentTimeMillis() - beginTime;
                System.out.println("    " + 1.0 * elapsedTime / timesDataReceived);*/
                beginTime = System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void parseData(String[] s) {
        button = Integer.parseInt(s[0]);

        Ax = Double.parseDouble(s[1]);
        Ay = Double.parseDouble(s[2]);
        Az = Double.parseDouble(s[3]);

        Gx = Double.parseDouble(s[4]);
        Gy = Double.parseDouble(s[5]);
        Gz = Double.parseDouble(s[6]);
    }
}