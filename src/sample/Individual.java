package sample;

import java.util.Random;

/**
 * Represents the individual in the 2D canvas.
 *
 */
public class Individual {
	
	private int positionX;
	private int positionY;
	private int speed;
	private int spendingTime;
	private int socialDistance;
	private double wearMaskValue;
	public static int size = 5;
	static final int maxXPosition = 994;
	static final int maxYPosition = 594;
	private boolean infected;
	private boolean hospitalized;
	private boolean isInteracting;
	private boolean dead;
	
	/**
	 * Creates a new Individual
	 * @param positionX the position of x in the 2D canvas.
	 * @param positionY the position of y in the 2D canvas.
	 * @param speed speed (pixels/second).
	 * @param spendingTime spending time for interaction to other individual.
	 * @param socialDistance social distance from other individual.
	 * @param wearMaskValue 0.2 if it wears a mask. 1.0 otherwise.
	 * @param infected true if it is infected.
	 */
	public Individual(int positionX, int positionY, int speed, int spendingTime, int socialDistance, double wearMaskValue, boolean infected) {
		this.positionX = positionX;
		this.positionY = positionY;
		this.speed = speed;
		this.spendingTime = spendingTime;
		this.socialDistance = socialDistance;
		this.wearMaskValue = wearMaskValue;
		this.infected = infected;
		this.hospitalized = false;
		this.isInteracting = false;
		this.dead = false;
	}
	
	/**
	 * Returns the dead field.
	 * @return the dead field.
	 */
	public boolean isDead() {
		return dead;
	}
	
	/**
	 * Sets the dead field.
	 * @param dead the dead value
	 */
	public void setDead(boolean dead) {
		this.dead = dead;
	}
	
	/**
	 * Returns the position of x.
	 * @return the position of x.
	 */
	public  int getPositionX() {
		return positionX;
	}
	
	/**
	 * Sets the position of x.
	 * @param positionX the position of x.
	 */
	public  void setPositionX(int positionX) {
		this.positionX = positionX;
	}
	
	/**
	 * Returns the position of y.
	 * @return the position of y.
	 */
	public  int getPositionY() {
		return positionY;
	}
	
	/**
	 * Sets the position of y.
	 * @param positionY the position of y.
	 */
	public  void setPositionY(int positionY) {
		this.positionY = positionY;
	}
	
	/**
	 * Returns the speed.
	 * @return the speed.
	 */
	public int getSpeed() {
		return speed;
	}
	
	/**
	 * Returns the spending time.
	 * @return the spending time.
	 */
	public int getSpendingTime() {
		return spendingTime;
	}
	
	/**
	 * Returns the social distance value.
	 * @return the social distance value.
	 */
	public int getSocialDistance() {
		return socialDistance;
	}
	
	/**
	 * Returns the wear mask value.
	 * @return the wear mask value.
	 */
	public double getWearMaskValue() {
		return wearMaskValue;
	}
	
	/**
	 * Returns if it is infected.
	 * @return if it is infected.
	 */
	public  boolean isInfected() {
		return infected;
	}
	
	/**
	 * Sets whether it is infected or not.
	 * @param infected whether it is infected or not.
	 */
	public  void setInfected(boolean infected) {
		this.infected = infected;
	}
	
	/**
	 * Returns if it is in the hospital.
	 * @return if it is in the hospital.
	 */
	public  boolean isHospitalized() {
		return hospitalized;
	}
	
	/**
	 * Sets whether it is in the hospital.
	 * @param hospitalized whether it is in the hospital.
	 */
	public  void setHospitalized(boolean hospitalized) {
		this.hospitalized = hospitalized;
	}
	
	/**
	 * Returns if it is interacting to other individual.
	 * @return if it is interacting to other individual.
	 */
	public  boolean isInteracting() {
		return isInteracting;
	}
	
	/**
	 * Sets if it is interacting to other individual.
	 * @param interacting if it is interacting to other individual.
	 */
	public  void setInteracting(boolean interacting) {
		isInteracting = interacting;
	}
	
	/**
	 * Returns the clone of the individual but in a randomly different spot at the canvas
	 *  with randomly different speed, spending time, distance and wear mask value.
	 * @return the clone of the individual but in a randomly different spot at the canvas
	 *  with randomly different speed, spending time, distance and wear mask value.
	 */
	public Individual cloneIndividual(){
		Individual ind;
		Random randomGenerator = new Random();
		
		int randomX;
		int randomY;
		int randomSpeed;
		int randomSpendingTime;
		int randomSocialDistance;
		boolean randomBoolean;
		double randomMValue;
		
		randomX = randomGenerator.nextInt(maxXPosition);
		randomY = randomGenerator.nextInt(maxYPosition);
		randomSpeed = randomGenerator.nextInt(5)+1;
		randomSpendingTime = randomGenerator.nextInt(5)+1;
		randomSocialDistance = randomGenerator.nextInt(10);
		randomBoolean = randomGenerator.nextBoolean();
		if(randomBoolean)
			randomMValue = 0.2;
		else
			randomMValue = 1.0;
		
		ind = new Individual(randomX,randomY,randomSpeed,randomSpendingTime,randomSocialDistance,randomMValue,false);
		
		return ind;
	}
	
}
