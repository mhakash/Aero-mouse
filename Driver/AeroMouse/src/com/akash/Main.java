package com.akash;

import com.fazecast.jSerialComm.SerialPort;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    private static double Ax, Ay, Az, Gx, Gy, Gz, roll = 0;
    private static int button;
    private static boolean rightButtonPressed = false, leftButtonPressed = false;

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        boolean flag = true;
        double sensitivity = 4.0, ratio = 1.0 * screenSize.width / screenSize.height;
        double posX = screenSize.width / 2, posY = screenSize.height / 2;
        double delX, delY;
        int deShakeLimit = 25, deShake = 0;
        double tempX = posX, tempY = posY;
        String portName = "";

        robot.mouseMove((int) Math.round(posX), (int) Math.round(posY));

        try {
            File file = new File("config.txt");
            BufferedReader bf = new BufferedReader(new FileReader(file));

            portName = bf.readLine().trim();

            String str = bf.readLine().trim();
            sensitivity = Double.parseDouble(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("The following ports are available:");
        SerialPort[] ports = SerialPort.getCommPorts();

        for (SerialPort port : ports) {
            System.out.print(port.getSystemPortName() + ": " + port.getPortDescription());
            System.out.println();
        }
        System.out.println("---------------------------------------");
        SerialPort port = SerialPort.getCommPort(portName);

        for(int tries = 1; tries <= 5; tries++) {
            if (port.openPort()) {
                System.out.println("Port Opened: " + portName);
                //System.out.println(port.getSystemPortName());
                //System.out.println(port.getPortDescription());
                break;
            } else if(tries == 5){
                System.out.println("Unable to Open the Port: " + portName);
                return;
            }
        }
        System.out.println("---------------------------------------");

        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        port.setBaudRate(38400);
        Scanner data = new Scanner(port.getInputStream());

        try {
            while (data.hasNextLine()) {
                try {
                    String[] s = data.nextLine().split(",");

                    /*for(String str: s) {
                        System.out.printf("%10s", str);
                    }
                    System.out.println();*/

                    if (s.length != 7) {
                        System.out.println("Invalid String");
                        continue;
                    }

                    parseData(s);

                    if (button == 0) {
                        //no button pressed
                        if (leftButtonPressed) {
                            leftButtonPressed = false;
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        } else if (rightButtonPressed) {
                            rightButtonPressed = false;
                            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                        }
                    } else if (button == 1) {
                        //left button pressed
                        if (!leftButtonPressed) {
                            leftButtonPressed = true;
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                            flag = false;
                        }
                    } else if (button == 2) {
                        //right button pressed
                        if (!rightButtonPressed) {
                            rightButtonPressed = true;
                            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                            flag = false;
                        }
                    }

                    if (flag) {
                        roll = Math.atan2(-1 * Ax, Math.sqrt(Ay * Ay + Az * Az)) * 180 / Math.PI;

                        tempX += (Ax + 9.8 * Math.sin(roll * Math.PI / 180) - Gz) / sensitivity * ratio;
                        tempY -= (Az - 9.8 * Math.cos(roll * Math.PI / 180) + Gx) / sensitivity;

                        tempX = Math.round(tempX);
                        tempY = Math.round(tempY);

                        if (tempX > screenSize.width) {
                            tempX = screenSize.width;
                        } else if (tempX < 0) {
                            tempX = 0;
                        }

                        if (tempY > screenSize.height) {
                            tempY = screenSize.height;
                        } else if (tempY < 0) {
                            tempY = 0;
                        }

                        delX = Math.round(tempX - posX);
                        delY = Math.round(tempY - posY);

                        int threshold = 5;
                        if (Math.abs(delX) < threshold)
                            delX = 0;

                        if (Math.abs(delY) < threshold)
                            delY = 0;

                        if (delX != 0 || delY != 0) {
                            double i, j;
                            int steps = 10;
                            i = delX / steps;
                            j = delY / steps;

                            while (steps-- != 0) {
                                posX += i;
                                posY += j;
                                robot.mouseMove((int) Math.round(posX), (int) Math.round(posY));
                            }
                        }

                    } else {
                        deShake++;
                        if (deShake >= deShakeLimit) {
                            flag = true;
                            deShake = 0;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        /*if(Math.abs(Ax) < eps)
            Ax = 0;
        if(Math.abs(Ay) < eps)
            Ay = 0;
        if(Math.abs(Az) < eps)
            Az = 0;
        if(Math.abs(Gx) < eps)
            Gx = 0;
        if(Math.abs(Gy) < eps)
            Gy = 0;
        if(Math.abs(Gz) < eps)
            Gz = 0;*/
    }
}