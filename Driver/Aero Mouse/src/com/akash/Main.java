package com.akash;

import com.fazecast.jSerialComm.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Scanner;

public class Main {

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
        SerialPort ports[] = SerialPort.getCommPorts();
        //SerialPort port = null;

        for (SerialPort port : ports) {
            System.out.println(port.getSystemPortName());
//            if(port1.getPortDescription().equalsIgnoreCase("BthModem0")) {
//                port = port1;
//            }
        }

        SerialPort port = ports[3];
        //SerialPort port = SerialPort.getCommPort("COM8");


        if (port != null && port.openPort()) {
            System.out.println("port opened");
            System.out.println(port.getDescriptivePortName());
            System.out.println(port.getPortDescription());
        } else {
            System.out.println("unable to open port");
            return;
        }

        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        Scanner data = new Scanner(port.getInputStream());

        try {
            while (data.hasNextLine()) {
                try {
                    String s = data.nextLine();//assuming data comes like "m,100,200" format
                    //System.out.println(s);
                    String[] acc = s.split(",");
                    /*for(String str: acc) {
                        System.out.println(str);
                    }*/
                    //System.out.print(acc[0]);
                    double accX = Double.parseDouble(acc[2].trim()) + 0.37;
                    //System.out.println(" " + accX);
                    double accY = Double.parseDouble(acc[0].trim()) - 0.75;
                    System.out.printf("%.3f       %.3f\n", accX, accY);
                    tempx -= (accX/8);
                    tempy -= (accY/8);
                    posx = (int) tempx;
                    posy = (int) tempy;
                    robot.mouseMove(posx, posy);
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

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
