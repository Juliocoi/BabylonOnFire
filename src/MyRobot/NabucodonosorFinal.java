import robocode.AdvancedRobot;

import java.awt.Color;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import static robocode.util.Utils.normalRelativeAngle;
import robocode.util.Utils;
import java.awt.Color;
import java.io.*;

public class NabucodonosorFinal extends AdvancedRobot {

	boolean movingForward;
	boolean nextToWall;
	private double bulletPower;
	double energyChange;
	double previousEnergy = 100;

	public void run() {
		setColors(Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW);

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
		double velocity = e.getVelocity();


		Boolean decision4 = decisionTree4(getGunHeat(), velocity, getRadarHeading(), heading, getVelocity(), energy,
				distance, getEnergy(), getHeading(), bearing, getGunHeading());
		
		Boolean decision5 = decisionTree5(distance, energy, getEnergy(), bearing, getGunHeading(), heading, getHeading());
		
		if (e.getDistance() > 300) {
			bulletPower = 1.5;
		} else {
			bulletPower = 3;
		}

		// Linear Predictive targeting
		// Calcular a posição do robô inimigo com relação a mim
		double bulletVelocity = 20 - 3 * bulletPower;
		double enemyPositionInRadians = getHeadingRadians() + e.getBearingRadians();
		double enemyLateralVelocity = e.getVelocity() * Math.sin(e.getHeadingRadians() - enemyPositionInRadians); 
		setTurnGunRightRadians(normalRelativeAngle((enemyPositionInRadians - getGunHeadingRadians()) + (enemyLateralVelocity / bulletVelocity))); 

		double radarTurn = Utils.normalRelativeAngle(enemyPositionInRadians - getRadarHeadingRadians());
		double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);

		// Ajuste a curva do radar para que ele vá ainda mais longe na direção necessária.
		//Assim podemos avançar um pouco na frente do inimigo, pra dificultar a perda do target. Tipo o vagabundo do crazy
		radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);

		//Vira o radar
		setTurnRadarRightRadians(radarTurn);

		if (decision5) {
			if (getGunHeat() == 0 && (getEnergy() - bulletPower) >= 0.2) {
				fire(bulletPower);
			}
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


	public boolean decisionTree4(double MyGunHeat, double Velocity, double MyRadarHeading, double Heading,
			double MyVelocity, double Energy, double Distance, double MyEnergie, double MyHeading, double Bearing,
			double MyGunHeading) {
		if (MyGunHeat <= 1.15) {
			if (Velocity <= -0.25) {
				if (MyRadarHeading <= 128.32) {
					if (Heading <= 66.00) {
						return false;
					} else { 
						if (MyGunHeat <= 0.15) {
							return false;
						} else { 
							if (Heading <= 325.19) {
								return true;
							} else { 
								if (MyVelocity <= 4.00) {
									return false;
								} else { 
									return true;
								}
							}
						}
					}
				} else { 
					if (Energy <= 79.20) {
						if (MyRadarHeading <= 359.01) {
							if (Distance <= 427.74) {
								if (MyRadarHeading <= 299.93) {
									return false;
								} else {
									if (Bearing <= -89.20) {
										return false;
									} else {
										return true;
									}
								}
							} else { 
								if (Distance <= 440.42) {
									return true;
								} else { 
									if (MyEnergie <= 98.00) {
										if (MyHeading <= 275.88) {
											return false;
										} else { 
											if (MyRadarHeading <= 220.81) {
												return true;
											} else {
												if (Heading <= 343.02) {
													return false;
												} else { 
													if (MyRadarHeading <= 254.07) {
														return true;
													} else {
														return false;
													}
												}
											}
										}
									} else {
										return true;
									}
								}
							}
						} else { 
							return true;
						}
					} else {
						if (MyRadarHeading <= 216.44) {
							if (MyEnergie <= 77.25) {
								return true;
							} else { 
								return false;
							}
						} else { 
							if (Heading <= 64.04) {
								return false;
							} else {
								if (Energy <= 96.50) {
									return true;
								} else { 
									return false;
								}
							}
						}
					}
				}
			} else { 
				if (Distance <= 272.34) {
					if (MyEnergie <= 96.55) {
						return false;
					} else { 
						if (MyHeading <= 246.90) {
							return true;
						} else { 
							return false;
						}
					}
				} else {
					if (MyEnergie <= 87.00) {
						if (MyRadarHeading <= 205.66) {
							if (Energy <= 3.66) {
								return false;
							} else {
								if (MyHeading <= 261.18) {
									if (MyRadarHeading <= 14.38) {
										if (Distance <= 440.18) {
											return false;
										} else { 
											return true;
										}
									} else {
										return true;
									}
								} else { 
									if (MyRadarHeading <= 168.64) {
										return false;
									} else {
										return true;
									}
								}
							}
						} else {
							if (Energy <= 24.84) {
								if (MyRadarHeading <= 232.99) {
									return false;
								} else {
									if (MyGunHeat <= 0.75) {
										return false;
									} else {
										return true;
									}
								}
							} else {
								if (MyEnergie <= 21.40) {
									if (MyGunHeat <= 1.00) {
										return false;
									} else { 
										return true;
									}
								} else { 
									if (MyGunHeading <= 358.57) {
										if (MyHeading <= 16.38) {
											if (MyHeading <= 13.44) {
												return false;
											} else { 
												return true;
											}
										} else { 
											if (MyEnergie <= 82.60) {
												return false;
											} else { 
												if (Velocity <= 5.88) {
													if (Bearing <= -97.99) {
														return false;
													} else { 
														return true;
													}
												} else { 
													return false;
												}
											}
										}
									} else { 
										return true;
									}
								}
							}
						}
					} else { 
						if (MyEnergie <= 91.75) {
							return true;
						} else {
							if (Energy <= 35.25) {
								if (Heading <= 208.31) {
									if (Heading <= 46.60) {
										return false;
									} else { 
										return true;
									}
								} else {
									return false;
								}
							} else {
								if (Distance <= 712.69) {
									if (Distance <= 372.00) {
										if (MyVelocity <= 0.00) {
											return false;
										} else {
											return true;
										}
									} else { 
										if (MyGunHeat <= 1.05) {
											if (MyRadarHeading <= 219.68) {
												return true;
											} else { 
												return false;
											}
										} else { 
											if (Energy <= 96.58) {
												if (MyGunHeading <= 241.24) {
													if (MyGunHeading <= 237.61) {
														if (Distance <= 527.14) {
															return true;
														} else { 
															if (Bearing <= -96.95) {
																return true;
															} else { 
																return false;
															}
														}
													} else { 
														return false;
													}
												} else {
													return true;
												}
											} else {
												if (MyVelocity <= 0.67) {
													if (Energy <= 98.01) {
														return false;
													} else { 
														return true;
													}
												} else { 
													return true;
												}
											}
										}
									}
								} else { 
									return false;
								}
							}
						}
					}
				}
			}
		} else {
			if (MyRadarHeading <= 319.72) {
				if (Energy <= 10.99) {
					if (MyEnergie <= 73.05) {
						if (MyEnergie <= 34.01) {
							if (Heading <= 98.18) {
								return false;
							} else {
								return true;
							}
						} else {
							if (MyHeading <= 25.28) {
								return true;
							} else {
								return false;
							}
						}
					} else {
						return true;
					}
				} else { 
					if (Distance <= 139.72) {
						if (Energy <= 12.00) {
							return false;
						} else {
							if (Heading <= 35.09) {
								if (MyGunHeading <= 237.96) {
									return true;
								} else { 
									return false;
								}
							} else {
								return true;
							}
						}
					} else {
						if (MyVelocity <= 6.50) {
							if (MyHeading <= 5.54) {
								return false;
							} else {
								if (Distance <= 142.43) {
									return false;
								} else {
									if (Heading <= 241.01) {
										if (Distance <= 345.51) {
											if (MyGunHeading <= 51.53) {
												if (MyHeading <= 156.08) {
													return true;
												} else {
													return false;
												}
											} else { 
												return true;
											}
										} else { 
											if (MyEnergie <= 65.85) {
												return false;
											} else {
												return true;
											}
										}
									} else { 
										if (Heading <= 267.57) {
											if (MyHeading <= 274.21) {
												return true;
											} else { 
												return false;
											}
										} else { 
											if (MyEnergie <= 96.80) {
												if (Energy <= 26.97) {
													return false;
												} else { 
													return true;
												}
											} else { 
												return false;
											}
										}
									}
								}
							}
						} else {
							if (Distance <= 211.40) {
								if (Bearing <= -79.99) {
									if (MyRadarHeading <= 300.97) {
										if (Bearing <= -80.03) {
											return false;
										} else {
											if (Energy <= 62.22) {
												return true;
											} else {
												return false;
											}
										}
									} else {
										return true;
									}
								} else { 
									return true;
								}
							} else {
								if (Bearing <= -83.85) {
									return true;
								} else { 
									if (Distance <= 289.54) {
										if (MyGunHeading <= 304.88) {
											if (Distance <= 228.81) {
												if (MyHeading <= 222.08) {
													return true;
												} else {
													return false;
												}
											} else { 
												return true;
											}
										} else { 
											return false;
										}
									} else { 
										if (Energy <= 15.60) {
											return true;
										} else { 
											return false;
										}
									}
								}
							}
						}
					}
				}
			} else {
				if (MyEnergie <= 92.25) {
					if (MyEnergie <= 64.20) {
						if (MyRadarHeading <= 322.75) {
							return false;
						} else { 
							return true;
						}
					} else {
						if (Energy <= 97.70) {
							return false;
						} else {
							if (MyVelocity <= -4.50) {
								return true;
							} else {
								return false;
							}
						}
					}
				} else {
					return true;
				}
			}
		}
	}

	public static boolean decisionTree5(double Distance, double Energy, double MyEnergie, double Bearing,
			double MyGunHeading, double Heading, double MyHeading) {
		if (Distance <= 295.20) {
			if (Energy <= 9.98) {
				if (MyEnergie <= 65.05) {
					if (MyEnergie <= 34.35) {
						if (Bearing <= -90.92) {
							return true;
						} else {
							return false;
						}
					} else {
						return false;
					}
				} else {
					return true;
				}
			} else {
				if (MyEnergie <= 65.75) {
					if (Distance <= 269.96) {
						if (Bearing <= -81.93) {
							return true;
						} else {
							if (Bearing <= -79.61) {
								if (MyGunHeading <= 239.06) {
									return false;
								} else {
									return true;
								}
							} else { 
								return true;
							}
						}
					} else { 
						if (Heading <= 139.60) {
							return false;
						} else { 
							return true;
						}
					}
				} else {
					if (MyGunHeading <= 171.97) {
						if (MyEnergie <= 77.17) {
							if (Distance <= 164.84) {
								if (Bearing <= -78.00) {
									return true;
								} else {
									return false;
								}
							} else {
								return false;
							}
						} else {
							if (MyGunHeading <= 30.26) {
								if (Distance <= 252.80) {
									return false;
								} else {
									return true;
								}
							} else {
								if (Heading <= 3.50) {
									if (Heading <= 1.88) {
										return true;
									} else {
										return false;
									}
								} else { 
									if (Heading <= 275.76) {
										return true;
									} else {
										if (Bearing <= -99.11) {
											if (Bearing <= -101.09) {
												return true;
											} else {
												return false;
											}
										} else {
											if (MyEnergie <= 83.95) {
												return false;
											} else {
												return true;
											}
										}
									}
								}
							}
						}
					} else {
						if (Heading <= 311.05) {
							if (MyHeading <= 304.90) {
								if (Energy <= 72.60) {
									if (MyEnergie <= 78.33) {
										if (MyHeading <= 45.88) {
											return true;
										} else { 
											if (Heading <= 286.73) {
												return false;
											} else {
												if (MyEnergie <= 76.75) {
													return true;
												} else {
													return false;
												}
											}
										}
									} else {
										if (Distance <= 50.68) {
											return false;
										} else {
											if (Heading <= 57.43) {
												if (Energy <= 60.74) {
													if (Distance <= 126.45) {
														if (Bearing <= -99.56) {
															return false;
														} else {
															return true;
														}
													} else {
														return false;
													}
												} else {
													return true;
												}
											} else {
												if (MyEnergie <= 118.75) {
													if (MyHeading <= 5.44) {
														return false;
													} else {
														if (Bearing <= -63.92) {
															return true;
														} else {
															if (Bearing <= -50.88) {
																return false;
															} else {
																return true;
															}
														}
													}
												} else {
													return false;
												}
											}
										}
									}
								} else { 
									if (MyEnergie <= 84.30) {
										return true;
									} else {
										if (MyGunHeading <= 278.67) {
											if (MyHeading <= 280.66) {
												return true;
											} else {
												return false;
											}
										} else {
											return false;
										}
									}
								}
							} else {
								if (MyEnergie <= 66.40) {
									return false;
								} else {
									if (Heading <= 236.04) {
										if (Bearing <= -77.60) {
											return true;
										} else {
											if (MyHeading <= 333.73) {
												return false;
											} else {
												return true;
											}
										}
									} else {
										if (MyEnergie <= 74.50) {
											return true;
										} else {
											if (Distance <= 107.89) {
												return true;
											} else {
												return false;
											}
										}
									}
								}
							}
						} else {
							if (MyGunHeading <= 263.24) {
								if (MyGunHeading <= 251.42) {
									return false;
								} else {
									return true;
								}
							} else {
								return false;
							}
						}
					}
				}
			}
		} else { 
			if (MyEnergie <= 84.30) {
				if (Energy <= 36.97) {
					if (Energy <= 2.54) {
						return false;
					} else {
						if (MyEnergie <= 21.45) {
							return true;
						} else { 
							if (MyEnergie <= 54.59) {
								if (MyHeading <= 297.06) {
									if (MyGunHeading <= 89.41) {
										return true;
									} else {
										return false;
									}
								} else {
									if (MyEnergie <= 47.89) {
										return true;
									} else {
										return false;
									}
								}
							} else {
								if (MyGunHeading <= 51.65) {
									return false;
								} else {
									return true;
								}
							}
						}
					}
				} else { 
					if (MyGunHeading <= 150.13) {
						if (Distance <= 624.44) {
							if (Heading <= 292.29) {
								return true;
							} else {
								return false;
							}
						} else { 
							return false;
						}
					} else { 
						if (Heading <= 4.99) {
							return true;
						} else {
							if (Energy <= 82.49) {
								if (Heading <= 282.10) {
									if (MyEnergie <= 19.90) {
										if (MyGunHeading <= 263.59) {
											return true;
										} else { 
											return false;
										}
									} else { 
										if (MyHeading <= 16.90) {
											if (MyHeading <= 14.01) {
												return false;
											} else { 
												return true;
											}
										} else { 
											if (MyGunHeading <= 163.01) {
												if (MyGunHeading <= 162.22) {
													return false;
												} else {
													return true;
												}
											} else {
												return false;
											}
										}
									}
								} else {
									if (Energy <= 51.50) {
										return true;
									} else {
										if (Heading <= 293.26) {
											return true;
										} else {
											return false;
										}
									}
								}
							} else { 
								if (Distance <= 842.40) {
									return true;
								} else {
									return false;
								}
							}
						}
					}
				}
			} else { 
				if (Distance <= 668.21) {
					if (Energy <= 35.25) {
						if (MyEnergie <= 92.25) {
							if (Energy <= 27.33) {
								return true;
							} else { 
								return false;
							}
						} else { 
							if (MyEnergie <= 110.20) {
								return false;
							} else { 
								return true;
							}
						}
					} else { 
						if (Bearing <= -79.97) {
							if (Energy <= 73.49) {
								if (Heading <= 359.30) {
									if (Bearing <= -81.70) {
										if (Distance <= 303.26) {
											if (MyHeading <= 102.43) {
												return false;
											} else {
												return true;
											}
										} else {
											return true;
										}
									} else {
										if (Heading <= 180.00) {
											return true;
										} else { 
											return false;
										}
									}
								} else {
									return false;
								}
							} else { 
								if (Energy <= 79.26) {
									if (MyGunHeading <= 174.74) {
										return true;
									} else {
										return false;
									}
								} else {
									if (Heading <= 203.50) {
										if (Distance <= 372.00) {
											if (Bearing <= -90.59) {
												return false;
											} else { 
												if (MyHeading <= 19.10) {
													return false;
												} else { 
													return true;
												}
											}
										} else { 
											if (Distance <= 552.86) {
												if (MyEnergie <= 101.50) {
													if (Heading <= 64.04) {
														if (Bearing <= -99.58) {
															return true;
														} else { 
															return false;
														}
													} else { 
														return true;
													}
												} else {
													return false;
												}
											} else {
												if (Bearing <= -99.92) {
													if (MyGunHeading <= 188.97) {
														return false;
													} else {
														return true;
													}
												} else {
													return false;
												}
											}
										}
									} else {
										return true;
									}
								}
							}
						} else {
							return false;
						}
					}
				} else {
					if (Distance <= 869.05) {
						return false;
					} else {
						return true;
					}
				}
			}
		}
	}

}
