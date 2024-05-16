package BabylonFlames;
import robocode.*;
import java.io.*;

import java.awt.Color;

// API help : https://robocode.sourceforge.io/docs/robocode/robocode/Robot.html

/**
 * Nabucodonosor - a robot by (your name here)
 */
public class Nabucodonosor extends AdvancedRobot
{
	private PrintWriter writer;
	/**
	 * run: Nabucodonosor's default behavior
	 */
	public void run() {
		System.out.println("Robô iniciado");
        try {
            // Cria um RobocodeFileOutputStream para o arquivo CSV
            RobocodeFileOutputStream rfos = new RobocodeFileOutputStream(getDataFile("nabucodonosor_data_onScannedRobot.csv"));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rfos)));

            // Escreve o cabeçalho do CSV
            writer.println("Time,Event,EnemyName,Energy,Distance,Bearing, Heading");
        } catch (IOException e) {
            e.printStackTrace();
        }
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		setColors(Color.red,Color.black,Color.yellow); // body,gun,radar

		// Robot main loop
		while(true) {
			// Replace the next 4 lines with any behavior you would like
			ahead(100);
			turnGunRight(360);
		}
	}
	
	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Replace the next line with any behavior you would like
		String robotank = e.getName();
		double distance = e.getDistance();
		double bearing = e.getBearing();
		double heading = e.getHeading();
		double energy = e.getEnergy();
		logData("ScannedRobot", robotank, energy, distance, bearing, heading);
		System.out.println("Robo: " + robotank + "\ndistancia: " + distance + "\nBearing: " + bearing + "\nHeading: " + heading + "\nEnergia: " + energy);
		fire(1);
		// Estrutura da arvore implementada aqui
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
		System.out.println("Fui acertado por " + e.getName());
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
    private void logData(String eventType, String enemyName, double energy, double distance, double bearing, double heading) {
        if (writer != null) {
            writer.printf("%d,%s,%s,%.2f,%.2f,%.2f%n", getTime(), eventType, enemyName, energy, distance, bearing, heading);
            writer.flush(); // Certifique-se de que os dados são escritos no arquivo imediatamente
        }
    }
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(100);
		turnGunRight(360);
		turnRight(90);
		turnGunRight(100);
	}	
	public void onDeath(DeathEvent event) {
        // Fecha o writer quando o robô morre
        if (writer != null) {
            writer.close();
            writer = null; // Libera o recurso
        }
    }

    public void onWin(WinEvent event) {
        // Fecha o writer quando o robô ganha
        if (writer != null) {
            writer.close();
            writer = null; // Libera o recurso
        }
    }
}
