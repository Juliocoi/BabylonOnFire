import java.awt.Color;
import robocode.*;
import static robocode.util.Utils.normalRelativeAngleDegrees;
import static robocode.util.Utils.normalRelativeAngle;
import robocode.util.Utils;
import java.awt.Color;

public class NabucodonosorFinal extends AdvancedRobot {

	boolean movingForward;
	boolean nextToWall;
	double bulletPower;


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

		// usando arvore de decisão
		Boolean decision7 = decisionTree7(distance, energy, getHeading(), bearing, getEnergy(), getGunHeading(), heading);

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

	public boolean decisionTree7(double Distance, double Energy, double MyHeading, double Bearing, double MyEnergie, double MyGunHeading, double Heading) {
		if (Distance <= 308.10) {
			if (Energy <= 9.88) {
				if (MyHeading <= 239.54) {
					if (Distance <= 151.95) {
						if (Bearing <= -116.61) {
							return false;
						} else {
							if (MyEnergie <= 60.93) {
								if (MyEnergie <= 31.84) {
									return true;
								} else {
									return false;
								}
							} else {
								return true;
							}
						}
					} else {
						return false;
					}
				} else {
					if (Distance <= 204.00) {
						return false;
					} else {
						return true;
					}
				}
			} else {
				if (MyHeading <= 272.69) {
					if (MyEnergie <= 95.65) {
						if (MyEnergie <= 67.38) {
							if (Distance <= 181.89) {
								if (MyHeading <= 243.22) {
									if (MyGunHeading <= 19.38) {
										if (MyHeading <= 100.78) {
											return false;
										} else {
											return true;
										}
									} else {
										if (Bearing <= -112.35) {
											if (Bearing <= -114.78) {
												return true;
											} else {
												return false;
											}
										} else {
											return true;
										}
									}
								} else {
									if (MyGunHeading <= 130.59) {
										if (Heading <= 135.99) {
											return true;
										} else {
											return false;
										}
									} else {
										return true;
									}
								}
							} else {
								if (Distance <= 232.68) {
									if (Heading <= 274.63) {
										if (MyGunHeading <= 51.37) {
											if (MyGunHeading <= 17.09) {
												return true;
											} else {
												return false;
											}
										} else {
											if (Bearing <= -97.57) {
												if (MyHeading <= 179.24) {
													return true;
												} else {
													return false;
												}
											} else {
												if (MyGunHeading <= 327.82) {
													return true;
												} else {
													return false;
												}
											}
										}
									} else {
										return false;
									}
								} else {
									if (Heading <= 92.17) {
										if (Heading <= 47.96) {
											return true;
										} else {
											if (MyHeading <= 135.64) {
												return false;
											} else {
												return true;
											}
										}
									} else {
										return true;
									}
								}
							}
						} else {
							if (Energy <= 91.67) {
								if (MyEnergie <= 69.60) {
									if (Energy <= 38.10) {
										return true;
									} else {
										return false;
									}
								} else {
									if (MyEnergie <= 94.99) {
										if (Bearing <= -100.94) {
											if (Distance <= 196.75) {
												if (MyGunHeading <= 340.17) {
													return true;
												} else {
													return false;
												}
											} else {
												return false;
											}
										} else {
											if (Energy <= 11.26) {
												if (Energy <= 10.45) {
													return true;
												} else {
													return false;
												}
											} else {
												if (MyHeading <= 5.44) {
													if (Distance <= 284.91) {
														return false;
													} else {
														return true;
													}
												} else {
													if (Distance <= 38.22) {
														return false;
													} else {
														if (Heading <= 357.25) {
															if (MyGunHeading <= 320.20) {
																if (Distance <= 301.60) {
																	if (MyGunHeading <= 316.48) {
																		if (Distance <= 216.74) {
																			if (Distance <= 214.89) {
																				if (Energy <= 75.50) {
																					if (Energy <= 63.40) {
																						if (Distance <= 97.00) {
																							if (Distance <= 87.37) {
																								if (Energy <= 25.32) {
																									if (MyHeading <= 141.46) {
																										return false;
																									} else {
																										return true;
																									}
																								} else {
																									if (Bearing <= -70.63) {
																										return true;
																									} else {
																										if (Distance <= 56.76) {
																											return true;
																										} else {
																											return false;
																										}
																									}
																								}
																							} else {
																								return false;
																							}
																						} else {
																							if (Distance <= 182.35) {
																								return true;
																							} else {
																								if (Distance <= 198.14) {
																									return false;
																								} else {
																									return true;
																								}
																							}
																						}
																					} else {
																						if (MyHeading <= 99.64) {
																							return false;
																						} else {
																							if (Energy <= 65.50) {
																								if (Bearing <= -72.51) {
																									return false;
																								} else {
																									return true;
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
																				return false;
																			}
																		} else {
																			if (Bearing <= -79.97) {
																				if (MyGunHeading <= 22.67) {
																					if (Energy <= 27.75) {
																						return false;
																					} else {
																						return true;
																					}
																				} else {
																					return true;
																				}
																			} else {
																				if (Distance <= 294.01) {
																					return true;
																				} else {
																					return false;
																				}
																			}
																		}
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
															return false;
														}
													}
												}
											}
										}
									} else {
										if (Heading <= 44.60) {
											return false;
										} else {
											if (Heading <= 231.28) {
												return true;
											} else {
												return false;
											}
										}
									}
								}
							} else {
								if (MyHeading <= 64.44) {
									if (MyEnergie <= 92.94) {
										return false;
									} else {
										return true;
									}
								} else {
									if (Energy <= 93.40) {
										return false;
									} else {
										if (Heading <= 42.93) {
											if (Heading <= 40.70) {
												return true;
											} else {
												return false;
											}
										} else {
											return true;
										}
									}
								}
							}
						}
					} else {
						if (MyEnergie > 95.65) {
							if (MyGunHeading <= 313.28) {
								if (Bearing <= -79.76) {
									if (MyGunHeading <= 42.27) {
										if (MyGunHeading <= 39.52) {
											if (Heading <= 334.36) {
												return true;
											} else {
												return false;
											}
										} else {
											return false;
										}
									} else {
										if (Heading <= 3.25) {
											if (Bearing <= -95.70) {
												return false;
											} else {
												return true;
											}
										} else {
											return true;
										}
									}
								} else {
									if (Bearing <= -79.40) {
										return false;
									} else {
										if (MyEnergie <= 118.75) {
											if (MyHeading <= 249.72) {
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
								if (MyHeading <= 78.51) {
									if (Distance <= 178.75) {
										return true;
									} else {
										if (MyEnergie <= 107.30) {
											return false;
										} else {
											return true;
										}
									}
								} else {
									if (MyGunHeading <= 313.42) {
										return false;
									} else {
										return true;
									}
								}
							}
						} else {
							if (MyHeading > 272.69) {
								if (MyHeading <= 276.77) {
									if (MyGunHeading <= 186.37) {
										if (MyEnergie <= 97.00) {
											if (Energy <= 80.50) {
												return false;
											} else {
												if (Distance <= 249.22) {
													return true;
												} else {
													return false;
												}
											}
										} else {
											return true;
										}
									} else {
										if (Bearing <= -80.07) {
											return true;
										} else {
											if (Distance <= 213.12) {
												if (Energy <= 44.94) {
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
									if (Heading <= 175.46) {
										if (Energy <= 58.10) {
											if (Bearing <= -116.73) {
												if (Energy <= 30.30) {
													return false;
												} else {
													return true;
												}
											} else {
												if (MyHeading <= 352.00) {
													if (Distance <= 278.37) {
														return true;
													} else {
														if (Distance <= 287.15) {
															return false;
														} else {
															return true;
														}
													}
												} else {
													if (MyHeading <= 354.23) {
														return false;
													} else {
														return true;
													}
												}
											}
										} else {
											if (MyEnergie <= 68.10) {
												return false;
											} else {
												if (MyEnergie <= 99.20) {
													if (Energy <= 60.00) {
														return false;
													} else {
														return true;
													}
												} else {
													if (MyHeading <= 321.61) {
														if (Heading <= 52.52) {
															return true;
														} else {
															return false;
														}
													} else {
														if (Distance <= 57.61) {
															return false;
														} else {
															return true;
														}
													}
												}
											}
										}
									} else {
										if (Heading <= 179.39) {
											return false;
										} else {
											if (Energy <= 13.70) {
												if (Distance <= 104.99) {
													return false;
												} else {
													return true;
												}
											} else {
												if (Distance <= 156.15) {
													if (MyEnergie <= 106.00) {
														if (MyHeading <= 282.17) {
															if (Distance <= 89.19) {
																return false;
															} else {
																return true;
															}
														} else {
															if (Distance <= 124.14) {
																return true;
															} else {
																if (Distance <= 124.74) {
																	return false;
																} else {
																	return true;
																}
															}
														}
													} else {
														return false;
													}
												} else {
													if (MyHeading <= 322.69) {
														if (MyEnergie <= 68.05) {
															if (MyGunHeading <= 208.60) {
																return true;
															} else {
																if (Energy <= 47.40) {
																	return true;
																} else {
																	return false;
																}
															}
														} else {
															if (MyEnergie <= 88.80) {
																return false;
															} else {
																if (MyGunHeading <= 215.73) {
																	return false;
																} else {
																	return true;
																}
															}
														}
													} else {
														if (Heading <= 321.43) {
															return true;
														} else {
															if (Bearing <= -85.34) {
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
								}
							}
						}
					}
				}
			}
		} else {
			if (Distance <= 674.24) {
				if (MyEnergie <= 68.10) {
					if (MyGunHeading <= 55.94) {
						return true;
					} else {
						if (Energy <= 75.50) {
							if (Energy <= 38.50) {
								if (MyGunHeading <= 140.83) {
									if (Distance <= 492.41) {
										if (MyEnergie <= 57.99) {
											return false;
										} else {
											if (MyHeading <= 161.52) {
												return false;
											} else {
												return true;
											}
										}
									} else {
										return true;
									}
								} else {
									if (Bearing <= -84.09) {
										if (MyEnergie <= 21.45) {
											return true;
										} else {
											if (MyEnergie <= 53.59) {
												if (Distance <= 372.53) {
													if (MyEnergie <= 40.59) {
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
										}
									} else {
										if (Distance <= 506.49) {
											if (Distance <= 455.11) {
												return true;
											} else {
												if (Heading <= 244.73) {
													return true;
												} else {
													return false;
												}
											}
										} else {
											return false;
										}
									}
								}
							} else {
								if (MyGunHeading <= 209.59) {
									if (Distance <= 481.71) {
										if (Heading <= 26.12) {
											return true;
										} else {
											if (MyGunHeading <= 106.50) {
												return true;
											} else {
												if (Distance <= 340.54) {
													if (Bearing <= -90.97) {
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
								} else {
									if (Heading <= 244.72) {
										if (Bearing <= -100.19) {
											return true;
										} else {
											if (MyHeading <= 5.55) {
												if (Distance <= 337.74) {
													return true;
												} else {
													return false;
												}
											} else {
												return false;
											}
										}
									} else {
										if (Bearing <= -82.18) {
											if (MyGunHeading <= 303.81) {
												if (MyEnergie <= 38.25) {
													if (Distance <= 490.12) {
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
											return false;
										}
									}
								}
							}
						} else {
							return true;
						}
					}
				} else {
					if (Distance <= 313.61) {
						if (MyEnergie <= 98.75) {
							if (Bearing <= -99.99) {
								return true;
							} else {
								return false;
							}
						} else {
							return true;
						}
					} else {
						if (MyHeading <= 255.04) {
							if (Heading <= 231.47) {
								if (MyEnergie <= 85.13) {
									if (MyEnergie <= 81.68) {
										if (Energy <= 43.20) {
											if (Energy <= 32.44) {
												return true;
											} else {
												return false;
											}
										} else {
											return true;
										}
									} else {
										if (Bearing <= -80.48) {
											if (Bearing <= -100.33) {
												return true;
											} else {
												return false;
											}
										} else {
											if (MyEnergie <= 84.68) {
												return true;
											} else {
												return false;
											}
										}
									}
								} else {
									if (Bearing <= -81.06) {
										if (MyGunHeading <= 3.82) {
											return false;
										} else {
											if (Energy <= 10.96) {
												return false;
											} else {
												if (MyGunHeading <= 269.92) {
													if (Distance <= 619.21) {
														if (Distance <= 370.24) {
															if (Distance <= 361.10) {
																if (Bearing <= -97.89) {
																	if (Bearing <= -98.94) {
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
															return true;
														}
													} else {
														if (MyEnergie <= 95.50) {
															return true;
														} else {
															return false;
														}
													}
												} else {
													if (Distance <= 345.25) {
														return true;
													} else {
														if (Distance <= 371.81) {
															if (Bearing <= -96.23) {
																return false;
															} else {
																if (MyHeading <= 20.16) {
																	return false;
																} else {
																	return true;
																}
															}
														} else {
															if (MyGunHeading <= 276.88) {
																if (MyEnergie <= 100.50) {
																	return false;
																} else {
																	return true;
																}
															} else {
																if (Heading <= 64.04) {
																	if (MyGunHeading <= 308.01) {
																		return true;
																	} else {
																		if (MyEnergie <= 93.00) {
																			return true;
																		} else {
																			return false;
																		}
																	}
																} else {
																	return true;
																}
															}
														}
													}
												}
											}
										}
									} else {
										if (Heading <= 186.76) {
											return true;
										} else {
											return false;
										}
									}
								}
							} else {
								if (MyEnergie <= 71.25) {
									if (Bearing <= -100.01) {
										return true;
									} else {
										return false;
									}
								} else {
									if (MyGunHeading <= 6.05) {
										if (MyEnergie <= 97.50) {
											return true;
										} else {
											return false;
										}
									} else {
										if (MyHeading <= 6.05) {
											if (MyHeading <= 4.85) {
												return true;
											} else {
												return false;
											}
										} else {
											return true;
										}
									}
								}
							}
						} else {
							if (MyEnergie <= 91.50) {
								if (Bearing <= -97.62) {
									if (MyEnergie <= 80.38) {
										if (MyHeading <= 260.39) {
											return true;
										} else {
											if (Heading <= 276.35) {
												return false;
											} else {
												return true;
											}
										}
									} else {
										return true;
									}
								} else {
									if (MyHeading <= 339.41) {
										return true;
									} else {
										if (MyHeading <= 343.57) {
											return false;
										} else {
											if (MyGunHeading <= 242.79) {
												if (MyEnergie <= 83.50) {
													return true;
												} else {
													return false;
												}
											} else {
												return true;
											}
										}
									}
								}
							} else {
								if (Heading <= 296.93) {
									if (Heading <= 37.64) {
										if (MyGunHeading <= 191.08) {
											return true;
										} else {
											if (MyEnergie <= 101.75) {
												if (Bearing <= -81.49) {
													return false;
												} else {
													if (Bearing <= -81.39) {
														return true;
													} else {
														return false;
													}
												}
											} else {
												return true;
											}
										}
									} else {
										if (MyHeading <= 293.48) {
											if (MyEnergie <= 95.50) {
												return true;
											} else {
												if (MyGunHeading <= 167.57) {
													return true;
												} else {
													return false;
												}
											}
										} else {
											if (Energy <= 97.97) {
												if (Distance <= 655.58) {
													if (Heading <= 262.55) {
														return true;
													} else {
														if (Energy <= 51.69) {
															return false;
														} else {
															return true;
														}
													}
												} else {
													if (MyGunHeading <= 245.35) {
														return true;
													} else {
														return false;
													}
												}
											} else {
												return false;
											}
										}
									}
								} else {
									if (MyHeading <= 355.20) {
										if (Distance <= 351.26) {
											return true;
										} else {
											if (MyGunHeading <= 224.88) {
												if (MyGunHeading <= 220.90) {
													return false;
												} else {
													if (Heading <= 347.67) {
														return true;
													} else {
														return false;
													}
												}
											} else {
												return false;
											}
										}
									} else {
										return true;
									}
								}
							}
						}
					}
				}
			} else {
				if (Heading <= 95.61) {
					if (Heading <= 28.78) {
						return false;
					} else {
						return true;
					}
				} else {
					if (MyEnergie <= 37.80) {
						return true;
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
		return false;
	}
}
