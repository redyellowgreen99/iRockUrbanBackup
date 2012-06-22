package org.wintrisstech.erik.iaroc;

import android.os.SystemClock;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wintrisstech.irobot.ioio.IRobotCreateAdapter;
import org.wintrisstech.irobot.ioio.IRobotCreateInterface;
import org.wintrisstech.irobot.ioio.IRobotCreateScript;
import org.wintrisstech.sensors.UltraSonicSensors;

public class Ferrari extends IRobotCreateAdapter implements Runnable {

    private static final String TAG = "Ferrari";
    int hugFactor = 4;
    double version = 4.1;
    boolean rylexIsAwesome = true;
    private final UltraSonicSensors sonar;
    private final Dashboard dashboard;
    /*
     * The maze can be thought of as a grid of quadratic cells, separated by
     * zero-width walls. The cell width includes half a pipe diameter on each
     * side, i.e the cell edges pass through the center of surrounding pipes.
     * <p> Row numbers increase northward, and column numbers increase eastward.
     * <p> Positions and direction use a reference system that has its origin at
     * the west-most, south-most corner of the maze. The x-axis is oriented
     * eastward; the y-axis is oriented northward. The unit is 1 mm. <p> What
     * the Ferrari knows about the maze is:
     */
    private final static int NUM_ROWS = 12;
    private final static int NUM_COLUMNS = 4;
    private final static int CELL_WIDTH = 712;
    /*
     * State variables:
     */
    private int speed = 500; // The normal speed of the Ferrari when going straight
    // The row and column number of the current cell. 
    private int row;
    private int column;
    private boolean running = true;
    private final static int SECOND = 1000; // number of millis in a second
    private int mode = 0;
    private int forwardDistance = 0;
    private int defaultSpeed = 400;// Change to 200
    private int defaultBackwardDistance = 100;
    private int backwardDistance = defaultBackwardDistance;
    private boolean shouldTurn = false;
    private boolean adjustingRight = false;
    private boolean adjustingLeft = false;
    int rylexWins;
    int someDistnce = 0;

    /**
     * Constructs a Ferrari, an amazing machine!
     *
     * @param ioio the IOIO instance that the Ferrari can use to communicate
     * with other peripherals such as sensors
     * @param create an implementation of an iRobot
     * @param dashboard the Dashboard instance that is connected to the Ferrari
     * @throws ConnectionLostException
     */
    public Ferrari(IOIO ioio, IRobotCreateInterface create, Dashboard dashboard) throws ConnectionLostException {
        super(create);
        sonar = new UltraSonicSensors(ioio);
        this.dashboard = dashboard;
    }

    /**
     * Main method that gets the Ferrari running.
     *
     */
    public void run() {
        while (true) {
            try {
                readSensors(SENSORS_GROUP_ID6);
                someDistnce += getDistance();
                //            dashboard.log("LOOP********************");
                if (isBumpLeft() && isBumpRight())//Do NOT delete
                {
                    try {
                        //                SystemClock.sleep(4000);
                        driveDirect(-500, -500);
                        SystemClock.sleep(250);
                        spinRight(70);
                    } catch (ConnectionLostException ex) {
                    }
                } else if (isBumpLeft())//Do NOT delete
                {
                    try {
                        // SystemClock.sleep(4000);
                        driveDirect(-500, -500);
                        SystemClock.sleep(100);
                        spinRight(10);
                        // inAcorner = 0; //:)
                    } catch (ConnectionLostException ex) {
                    }
                } else if (isBumpRight()) {
                    try {
                        //                SystemClock.sleep(4000);
                        driveDirect(-500, -500);
                        SystemClock.sleep(100);
                        jogRight(0);
                        SystemClock.sleep(250);
                    } catch (ConnectionLostException ex) {
                    }
                } else {
//                    if (someDistnce >= 125) {

                    jogLeft(0);
//                        someDistnce = 0;
//                    } else {
//                        driveDirect(500, 500);
//                    }
                    //                dashboard.log("1010101010101010101010101010");
                }
            } catch (ConnectionLostException ex) {
                Logger.getLogger(Ferrari.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    /**
     * To run this test, place the Ferrari in a cell surrounded by 4 walls. <p>
     * Note: The sensors draw power from the Create's battery. Make sure it is
     * charged.
     */
    private void testUltraSonicSensors() {
        dashboard.log("Starting ultrasonic test.");
        long endTime = System.currentTimeMillis() + 20 * SECOND;
        while (System.currentTimeMillis() < endTime) {
            try {
                sonar.readUltrasonicSensors();
            } catch (ConnectionLostException ex) {
                //TODO
            } catch (InterruptedException ex) {
                //TODO
            }
            SystemClock.sleep(500);
        }
        dashboard.log("Ultrasonic test ended.");
    }

    /**
     * Tests the rotation of the Ferrari.
     */
    private void testRotation() {
        dashboard.log("Testing rotation");
        try {
            turnAndGo(10, 0);
            SystemClock.sleep(500);
            turnAndGo(80, 0);
            SystemClock.sleep(80);
            turnAndGo(-90, 0);
            SystemClock.sleep(80);
            turnAndGo(180, 0);
            SystemClock.sleep(80);
            turnAndGo(-90, 0);
            SystemClock.sleep(80);
            turnAndGo(-180, 0);
            SystemClock.sleep(80);
            turnAndGo(180, 0);
            SystemClock.sleep(80);
        } catch (ConnectionLostException ex) {
        } catch (InterruptedException ex) {
        }

    }

    /**
     * Turns in place and then goes forward.
     *
     * @param angle the angle in degrees that the Ferrari shall turn. Negative
     * values makes clockwise turns.
     * @param distance the distance in mm that the Ferrari shall run forward.
     * Must be positive.
     */
    private void turnAndGo(int angle, int distance)
            throws ConnectionLostException, InterruptedException {
        IRobotCreateScript script = new IRobotCreateScript();
        /*
         * The Create overshoots by approx. 3 degrees depending on the floor
         * surface. Note: This is speed sensitive.
         */
        // TODO: Further tweaks to make the Ferrari make more precise turns.  
        if (angle < 0) {
            angle = Math.min(0, angle + 3);
        }
        if (angle > 0) {
            angle = Math.max(0, angle - 3);
        }
        if (angle != 0) {
            script.turnInPlace(100, angle < 0); // Do not change speed!
            script.waitAngle(angle);
        }
        if (distance > 0) {
            script.driveStraight(speed);
            script.waitDistance(distance);
        }
        if (angle != 0 || distance > 0) {
            script.stop();
            playScript(script.getBytes(), false);
            // delay return from this method until script has finished executing
        }
    }

    /**
     * Closes down all the connections of the Ferrari, including the connection
     * to the iRobot Create and the connections to all the sensors.
     */
    public void shutDown() {
        closeConnection(); // close the connection to the Create
        sonar.closeConnection();
    }

    //// Methods made public for the purpose of the Dashboard ////
    /**
     * Gets the left distance to the wall using the left ultrasonic sensor
     *
     * @return the left distance
     */
    public int getLeftDistance() {
        return sonar.getLeftDistance();
    }

    /**
     * Gets the front distance to the wall using the front ultrasonic sensor
     *
     * @return the front distance
     */
    public int getFrontDistance() {
        return sonar.getFrontDistance();
    }

    /**
     * Gets the right distance to the wall using the right ultrasonic sensor
     *
     * @return the right distance
     */
    public int getRightDistance() {
        return sonar.getRightDistance();
    }

    /**
     * Checks if the Ferrari is running
     *
     * @return true if the Ferrari is running
     */
    public synchronized boolean isRunning() {
        return running;
    }

    private synchronized void setRunning(boolean b) {
        running = false;
    }

    public boolean closeToBeacon() {
        if (getInfraredByte() == 244 || getInfraredByte() == 248 || getInfraredByte() == 252)//Red, green
        {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ***********************************************************************
     * Rylex AwesomeApi
     * ***********************************************************************
     */
    public void jogLeft(int sleepyTime) {
        try {
            //                roomba.playSong(1);
            driveDirect(500, 190);
//            SystemClock.sleep(sleepyTime);
//            driveDirect(speed, speed);
        } catch (ConnectionLostException ex) {
            Logger.getLogger(Ferrari.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void jogRight(int sleepyTime) {
        try {
            //                roomba.playSong(1);
            driveDirect(190, 500);
            //        leds[6].setOn();
//            SystemClock.sleep(sleepyTime);
            //        driveDirect(speed, speed);
            //        driveDirect(speed, speed);
        } catch (ConnectionLostException ex) {
            Logger.getLogger(Ferrari.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void spinRight(int turnAngle) throws ConnectionLostException {
        int angleTurned = 0;
        driveDirect(-speed, speed);
        while (Math.abs(angleTurned) < turnAngle) {
            readSensors(SENSORS_GROUP_ID6);// read all sensors into array.
            angleTurned += getAngle();
            System.out.println("spin right angle turned = " + angleTurned);
        }
        driveDirect(speed, speed);
    }

    public void spinLeft(int turnAngle) throws ConnectionLostException {
        int angleTurned = 0;
        driveDirect(speed, -speed);
        while (Math.abs(angleTurned) < turnAngle) {
            readSensors(SENSORS_GROUP_ID6);// read all sensors into array.
            angleTurned += getAngle();
            System.out.println("spin left angle turned = " + angleTurned);
        }
        driveDirect(speed, speed);
    }

    void initialize() throws ConnectionLostException {
        dashboard.log("===========Start===========");
        //readSensors(SENSORS_GROUP_ID6);//Resets all counters in the Create to 0.
        //driveDirect(speed, speed);
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    void loop() {
        // throw new UnsupportedOperationException("Not yet implemented");
    }

    void stop() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
