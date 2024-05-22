package MyRobot;

import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import java.awt.Color;
import java.io.*;

public class Nabucodonosor extends AdvancedRobot {
	
	boolean movingForward; // É definida como true quando setAhead é chamada e vice-versa
	boolean nextToWall; // É verdade quando robô está perto da parede.
	private PrintWriter writer;

	public void run() {
		//Chassi, canhão, radar, bala, arco
		setColors(Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW);

		System.out.println("Robô iniciado");
		try {
			// Cria um RobocodeFileOutputStream para o arquivo CSV
			RobocodeFileOutputStream rfos = new RobocodeFileOutputStream(getDataFile("nabucodonosor_data_onScannedRobot.csv"));
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(rfos)));

			// Escreve o cabeçalho do CSV
			writer.println("Time;Event;Name;MyEnergie;MyGunHeat;MyGunHeading;MyHeading;MyRadarHeading;MyVelocity;EnemyName;Energy;Distance;Bearing;Heading;Velocity");
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
			// Caso não verificamos, inverte a direção e seta flag
			// para true.
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

			// Se o radar parou de girar, procure um inimigo
			if (getRadarTurnRemaining() == 0.0) {
				setTurnRadarRight(360);
			}

			execute();
		}
	}

	public void onHitWall(HitWallEvent e) {
		reverseDirection();
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		
		String robotank = e.getName();
		double distance = e.getDistance();
		double bearing = e.getBearing();
		double heading = e.getHeading();
		double energy = e.getEnergy();
		double velocity = e.getVelocity();
		
		logData("ScannedRobot", robotank, energy, distance, bearing, 
				heading, velocity);
		
		System.out.println("Robo: " + robotank + "\ndistancia: " + distance + 
				"\nBearing: " + bearing + "\nHeading: " + heading +
				 "\nEnergia: " + energy + "\nVelocidade" + velocity);

		// Calcular a posição do robô inimigo com relação a mim
		double enemyPosition = getHeading() + e.getBearing();

		// vire só o necessário e nunca mais do que uma volta...
		// vendo-se o angulo que fazemos com o robo alvo e descontando
		// o Heading e o Heading do Radar pra ficar com o angulo
		// correto, normalmente.
		double bearingFromGun = normalRelativeAngleDegrees(enemyPosition
				- getGunHeading());
		double bearingFromRadar = normalRelativeAngleDegrees(enemyPosition
				- getRadarHeading());

		// giro para realizar movimento espiral ao inimigo
		// (90 levaria ao paralelo)
		if (this.movingForward) {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
		} else {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 100));
		}

		// Se perto o suficiente, fogo!
		if (Math.abs(bearingFromGun) <= 4) {
			setTurnGunRight(bearingFromGun); // mantem o canhão centrado sobre o inimigo
			setTurnRadarRight(bearingFromRadar); // mantem o radar centrado sobre o inimigo

			// Quanto mais precisamente objetivo, maior será a bala.
			// Não dispare nos a deficiência, sempre salvar 0,1
			if (getGunHeat() == 0 && getEnergy() > .2) {
				fire(Math.min(
						4.5 - Math.abs(bearingFromGun) / 2 - e.getDistance() / 250, 
						getEnergy() - .1));
			}
		} // caso contrário, basta definir a arma para virar.
		else {
			setTurnGunRight(bearingFromGun);
			setTurnRadarRight(bearingFromRadar);
		}

		// se o radar não estiver girando, gera evento de giro (scanner)
		if (bearingFromGun == 0) {
			scan();
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {

		System.out.println("Fui acertado por " + e.getName());
	}

	// em contato com o robo, se tenha sido por nossa conta, inverte a direção
	public void onHitRobot(HitRobotEvent e) {
		if (e.isMyFault()) {
			reverseDirection();
		}
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

	private void logData(String eventType, String enemyName, double energy, 
			double distance, double bearing, double heading, double velocity) {
		if (writer != null) {
			writer.printf("%d;%s;%s;%.2f;%.2f;%.2f;%.2f;%.2f;%.2f;%s;%.2f;%.2f;%.2f;%.2f;%.2f%n", 
					getTime(), eventType, getName(), getEnergy(), 
					getGunHeat(),getGunHeading(), getHeading(), getRadarHeading(), getVelocity(),
					enemyName, energy, distance, bearing, heading, velocity);
			writer.flush(); // Certifique-se de que os dados são escritos no arquivo imediatamente
		}
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
