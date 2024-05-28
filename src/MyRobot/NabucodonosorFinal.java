package BabylonOnFire;
import java.awt.Color;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import static robocode.util.Utils.normalRelativeAngle;
import robocode.util.Utils;

public class NabucodonosorFinal extends AdvancedRobot {

	boolean movingForward;
	boolean nextToWall;
	double bulletPower;


	public void run() {
		setColors(Color.RED, Color.YELLOW, Color.RED, Color.YELLOW, Color.RED);

		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		if (getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50
				|| getBattleFieldHeight() - getY() <= 50) {
			this.nextToWall = true;
		} else {
			this.nextToWall = false;
		}

		setAhead(40000);
		setTurnRadarRight(360);

		this.movingForward = true;

		while (true) {
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

			if (getRadarTurnRemaining() == 0.0) {
				setTurnRadarRight(360);
			}

			execute();
		}

	}

	public void onScannedRobot(ScannedRobotEvent e) {
		double distance = e.getDistance();
		double bearing = e.getBearing();
		double heading = e.getHeading();
		double energy = e.getEnergy();

		// usando arvore de decisão
		Boolean decision7 = DecisionTree.decisionTree7(distance, energy, getHeading(), bearing, getEnergy(), getGunHeading(), heading);

		if (e.getDistance() > 300) {
			bulletPower = 1;
		} else if (e.getDistance() > 100) {
			bulletPower = 1.5;
		} else {
			bulletPower = 3;
		}

		// Calcular a posição do robô inimigo com relação a mim
		double bulletVelocity = 20 - 3 * bulletPower;
		double enemyPositionInRadians = getHeadingRadians() + e.getBearingRadians();
		double enemyLateralVelocity = e.getVelocity() * Math.sin(e.getHeadingRadians() - enemyPositionInRadians); 
		//atira onde o inimigo estará
		setTurnGunRightRadians(normalRelativeAngle((enemyPositionInRadians - getGunHeadingRadians()) + (enemyLateralVelocity / bulletVelocity))); 

		double radarTurn = Utils.normalRelativeAngle(enemyPositionInRadians - getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);

		// Ajuste a curva do radar para que ele vá ainda mais longe na direção necessária.
		//Assim podemos avançar um pouco na frente do inimigo, pra dificultar a perda do target. Tipo o vagabundo do crazy
		radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);

		//Vira o radar
		setTurnRadarRightRadians(radarTurn);

		// decidindo se o robo vai atirar ou não
		if (decision7) {
			fire(bulletPower);
		}

		// giro para realizar movimento espiral ao inimigo
		// (90 levaria ao paralelo)
		if (this.movingForward) {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 80));
		} else {
			setTurnRight(normalRelativeAngleDegrees(e.getBearing() + 100));
		}

		// Tenta evitar coalisões com o inimigo.
		if(e.getDistance() < 100) {
			if(e.getBearing() > -90 && e.getBearing() < 90) {
				setBack(150);
			} else {
				setAhead(100);
			}
		}

	}

	public void onHitWall(HitWallEvent e) {
		reverseDirection();
	}

	public void onHitByBullet(HitByBulletEvent e) {
		reverseDirection();
	}

	public void onHitRobot(HitRobotEvent e) {
		if (e.isMyFault()) {
			reverseDirection();
		}
	}

	public void reverseDirection() {
		if (this.movingForward) {
			setBack(40000);
			this.movingForward = false;
		} else {
			setAhead(40000);
			this.movingForward = true;
		}
	}

}
