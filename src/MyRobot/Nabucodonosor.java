package MyRobot;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import static robocode.util.Utils.normalRelativeAngle;
import robocode.util.Utils;
import java.awt.Color;
import java.io.*;

public class Nabucodonosor extends AdvancedRobot {

	boolean movingForward; // É definida como true quando setAhead é chamada e vice-versa
	boolean nextToWall; // É verdade quando robô está perto da parede.
	private PrintWriter writer;
	private PrintWriter writerHitRobot;
	private PrintWriter writerHitWall;
	private PrintWriter writerHitByBullet;
	private double bulletPower;
	double energyChange;
	double previousEnergy = 100;
	public  Boolean hit = false;

	public void run() {
		//Chassi, canhão, radar, bala, arco
		setColors(Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW);

		System.out.println("Robô iniciado");
		try {
			// Cria um RobocodeFileOutputStream para o arquivo CSV
			RobocodeFileOutputStream scannerEventFile = new RobocodeFileOutputStream(getDataFile("onScannedRobot_data.csv"));
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(scannerEventFile)));
			// Escreve o cabeçalho do CSV
			writer.println("Time;Event;Name;MyEnergie;MyGunHeat;MyGunHeading;MyHeading;MyRadarHeading;MyVelocity;EnemyName;Energy;Distance;Bearing;Heading;Velocity;isHit");

			RobocodeFileOutputStream hitRobotFile = new RobocodeFileOutputStream(getDataFile("onHitRobotEvent_data.csv"));
			writerHitRobot = new PrintWriter(new BufferedWriter(new OutputStreamWriter(hitRobotFile)));
			writerHitRobot.println("Time;Event;Name;MyEnergie;MyGunHeat;MyGunHeading;MyHeading;MyRadarHeading;MyVelocity;EnemyName;Energy;Bearing;isMyFault");

			RobocodeFileOutputStream hitWallEventFile = new RobocodeFileOutputStream(getDataFile("hitWallEvent_data.csv"));
			writerHitWall = new PrintWriter(new BufferedWriter(new OutputStreamWriter(hitWallEventFile)));
			writerHitWall.println("Time;Event;Name;MyEnergie;MyGunHeat;MyGunHeading;MyHeading;MyRadarHeading;MyVelocity;Bearing");

			RobocodeFileOutputStream hitByBulletEventFile = new RobocodeFileOutputStream(getDataFile("hitByBulletEventFile_data.csv"));
			writerHitByBullet = new PrintWriter(new BufferedWriter(new OutputStreamWriter(hitByBulletEventFile)));
			writerHitByBullet.println("Time;Event;Name;MyEnergie;MyGunHeat;MyGunHeading;MyHeading;MyRadarHeading;MyVelocity;EnemyName;Bearing;Heading;BulletPower;BulletVelocity");



		} catch (IOException e) {
			e.printStackTrace();
		}

		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		// Verifique se o robô está próximo da parede.
		if (getX() <= 50 || getY() <= 50
				|| getBattleFieldWidth() - getX() <= 50
				|| getBattleFieldHeight() - getY() <= 50) {
			this.nextToWall = true;
		} else {
			this.nextToWall = false;
		}

		setAhead(40000);
		setTurnRadarRight(360);

		this.movingForward = true; // chamamos setAhead, então movingForward é verdade

		while (true) {
			// Verifica se estamos perto da parede e se já verificamos positivo.
			// Caso não verificamos, inverte a direção e seta flag para true.

			if (getX() > 50 && getY() > 50
					&& getBattleFieldWidth() - getX() > 50
					&& getBattleFieldHeight() - getY() > 50
					&& this.nextToWall == true) {
				this.nextToWall = false;
			}
			if (getX() <= 50 || getY() <= 50
					|| getBattleFieldWidth() - getX() <= 50
					|| getBattleFieldHeight() - getY() <= 50) {
				if (this.nextToWall == false) {
					reverseDirection();
					nextToWall = true;
				}
			}

			// Gira o radar se perdermos o target das inimigas
			if (getRadarTurnRemaining() == 0.0) {
				setTurnRadarRight(360);
			}

			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {

		String robotank = e.getName();
		double distance = e.getDistance();
		double bearing = e.getBearing();
		double heading = e.getHeading();
		double energy = e.getEnergy();
		double velocity = e.getVelocity();
		double isHit = hit ? 1 : 0;

		logDataScanner("ScannedRobot", robotank, energy, distance, bearing, 
				heading, velocity, isHit);

		System.out.println("Robo: " + robotank + "\ndistancia: " + distance + 
				"\nBearing: " + bearing + "\nHeading: " + heading +
				"\nEnergia: " + energy + "\nVelocidade" + velocity + "\nHit" + isHit);

		hit = false;

		if (e.getDistance() > 350) {
			bulletPower = 1;
		} else if (e.getDistance() > 100) {
			bulletPower = 1.5;
		} else {
			bulletPower = 3;
		}

		// Linear Predictive targeting
		double bulletVelocity = 20 - 3 * bulletPower;
		// Calcular a posição do robô inimigo com relação a mim
		// Head-on Targety strategy
		double enemyPositionInRadians = getHeadingRadians() + e.getBearingRadians();
		// The enemy robots velocity perpendicular to the robot.
		double enemyLateralVelocity = e.getVelocity() * Math.sin(e.getHeadingRadians() - enemyPositionInRadians); 
		// Turns the gun ahead of the enemy, to the point where they will be when the bullet reaches them.
		setTurnGunRightRadians(normalRelativeAngle((enemyPositionInRadians - getGunHeadingRadians()) + (enemyLateralVelocity / bulletVelocity))); 

		//subtract current radar heading to get turn required to face the enemy, be sure it is normalized.
		double radarTurn = Utils.normalRelativeAngle(enemyPositionInRadians - getRadarHeadingRadians());

		//Distance we want to scan from middle of enemy to either side
		//the 36.0 is how many units from the center of the ebemy robot it scan
		double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);

		// Ajuste a curva do radar para que ele vá ainda mais longe na direção necessária.
		//Assim podemos avançar um pouco na frente do inimigo, pra dificultar a perda do target. Tipo o vagabundo do crazy
		radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);

		//Vira o radar
		setTurnRadarRightRadians(radarTurn);


		if (getGunHeat() == 0 && (getEnergy() - bulletPower) >= 0.2) {
			fire(bulletPower);
		}

		// giro para realizar movimento espiral ao inimigo
		// (90 levaria ao paralelo)
		if (this.movingForward) {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
		} else {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 100));
		}

		energyChange = previousEnergy - e.getEnergy();
		if (energyChange > 0 && energyChange <= 3) {
			reverseDirection();
		}

	}

	public void onHitWall(HitWallEvent e) {

		double bearing = e.getBearing();

		reverseDirection();

		logDataHitWall("HitWallEvent", bearing);
	}

	public void onHitByBullet(HitByBulletEvent e) {

		String robotank = e.getName();
		double bearing = e.getBearing();
		double heading = e.getHeading();
		double bulletPower = e.getPower();
		double bulletVelocity  = e.getVelocity();

		System.out.println("Fui acertado por " + e.getName());

		logDataHitByBullet("HitBybullet", robotank, bearing, heading, bulletPower, bulletVelocity);
		reverseDirection();
	}

	// em contato com o robo, se tenha sido por nossa conta, inverte a direção
	public void onHitRobot(HitRobotEvent e) {

		String robotank = e.getName();
		double energy = e.getEnergy();
		double bearing = e.getBearing();
		boolean isMyFault = e.isMyFault();

		if (e.isMyFault()) {
			reverseDirection();
		}

		logDataHitRobot("HitRobot", robotank, energy, bearing, isMyFault);
	}

	public void onBulletHit(BulletHitEvent e) {
		this.hit = true;
	}

	// mudar de frente para trás e vice-versa
	public void reverseDirection() {
		if (this.movingForward) {
			setBack(40000);
			this.movingForward = false;
		} else {
			setAhead(40000);
			this.movingForward = true;
		}
	}

	private void logDataScanner(String eventType, String enemyName, double energy, 
			double distance, double bearing, double heading, double velocity, double isHit) {
		if (writer != null) {
			writer.printf("%d;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f%n", 
					getTime(), eventType, getName(), getEnergy(), 
					getGunHeat(),getGunHeading(), getHeading(), getRadarHeading(), getVelocity(),
					enemyName, energy, distance, bearing, heading, velocity, isHit);
			writer.flush(); // Certifique-se de que os dados são escritos no arquivo imediatamente
		}
	}

	private void logDataHitRobot(String eventType, String enemyName, double energy, 
			double bearing, boolean isMyFault) {
		if (writerHitRobot != null) {
			writerHitRobot.printf("%d;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%s;%.2f;%.2f;%.2b%n", 
					getTime(), eventType, getName(), getEnergy(), 
					getGunHeat(),getGunHeading(), getHeading(), getRadarHeading(), getVelocity(),
					enemyName, energy, bearing, isMyFault);
			writerHitRobot.flush(); // Certifique-se de que os dados são escritos no arquivo imediatamente
		}
	}

	private void logDataHitWall(String eventType, double bearing) {
		if (writerHitWall != null) {
			writerHitWall.printf("%d;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f%n", 
					getTime(), eventType, getName(), getEnergy(), 
					getGunHeat(),getGunHeading(), getHeading(), getRadarHeading(), getVelocity(), bearing);
			writerHitWall.flush(); // Certifique-se de que os dados são escritos no arquivo imediatamente
		}
	}

	private void logDataHitByBullet(String eventType, String enemyName, double bearing, 
			double heading, double bulletPower, double bulletVelocity) {
		if (writerHitByBullet != null) {
			writerHitByBullet.printf("%d;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%s;%.2f;%.2f;%.2f;%.2f%n", 
					getTime(), eventType, getName(), getEnergy(), 
					getGunHeat(),getGunHeading(), getHeading(), getRadarHeading(), getVelocity(),
					enemyName, bearing, heading, bulletPower, bulletVelocity);
			writerHitByBullet.flush(); // Certifique-se de que os dados são escritos no arquivo imediatamente
		}
	}

	public void onDeath(DeathEvent event) {
		// Fecha o writer quando o robô morre
		if (writer != null) {
			writer.close();
			writer = null; // Libera o recurso
		}

		if(writerHitRobot != null) {
			writerHitRobot.close();
			writerHitRobot = null;
		}

		if(writerHitWall != null) {
			writerHitWall.close();
			writerHitWall = null;
		}

		if(writerHitByBullet != null) {
			writerHitByBullet.close();
			writerHitByBullet = null;
		}
	}

	public void onWin(WinEvent event) {
		// Fecha o writer quando o robô ganha
		if (writer != null) {
			writer.close();
			writer = null; // Libera o recurso
		}

		if(writerHitRobot != null) {
			writerHitRobot.close();
			writerHitRobot = null;
		}

		if(writerHitWall != null) {
			writerHitWall.close();
			writerHitWall = null;
		}

		if(writerHitByBullet != null) {
			writerHitByBullet.close();
			writerHitByBullet = null;
		}
	}
}
