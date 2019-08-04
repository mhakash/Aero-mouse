package com.akash;
import com.fazecast.jSerialComm.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception{
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("Height = " + screenSize.height);
        System.out.println("Width = " + screenSize.width);
        double i = 1, j = 1;
        double posx = 100, posy = 100;
        double tempx = 0, tempy = 0;

        robot.mouseMove((int)posx, (int)posy);


	// write your code here
        System.out.println("ok");
        SerialPort ports[] = SerialPort.getCommPorts();

        for(SerialPort port : ports){
            System.out.println(port.getSystemPortName());
        }

        SerialPort port = ports[0];

        if(port.openPort()){
            System.out.println("port opened");
            System.out.println(port.getDescriptivePortName());
        }else{
            System.out.println("unable to open port");
            return;
        }

        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER,0,0);
        Scanner data = new Scanner(port.getInputStream());

        try{
            while (data.hasNextLine()){
                String s = data.nextLine();//assuming data comes like "m,100,200" format

                String[] parts = s.split(",");
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
                }
            }
        }catch (Exception e){
            System.out.println(e);
        }

    }
}
