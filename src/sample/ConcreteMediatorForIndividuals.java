package sample;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  Concrete Mediator for individuals to do the interactions and hospital functionality stuff.
 */
public class ConcreteMediatorForIndividuals implements MediatorForIndividuals{
	
	static final int POPULATION = 500;
	static final int maxXPosition = 994;
	static final int maxYPosition = 594;
	static final int UP_DIRECTION = 0;
	static final int LEFT_DIRECTION = 1;
	static final int DOWN_DIRECTION = 2;
	static final int RIGHT_DIRECTION = 3;
	private final double spreading_factor;
	private final double mortality_rate;
	private final int timeIntervalAfterFirstInfection = 25;
	private final int timeIntervalAfterStayingHostpital = 10;
	private List<Individual> listOfIndividuals;
	Random randomGenerator;
	int numberOfDead;
	int waitForDie;
	ReentrantLock reentrantLockForVentilator = new ReentrantLock();
	private volatile int numberOfVentilator;
	private volatile boolean deadExist;
	private List<Integer> deadIndIndexList;
	ReentrantLock deadListLock = new ReentrantLock();
	private ExecutorService executerService;
	Timer timer;
	
	/**
	 * Creates new Concrete Mediator for Individuals
	 */
	public ConcreteMediatorForIndividuals(){
		randomGenerator = new Random();
		listOfIndividuals = new CopyOnWriteArrayList<>();
		numberOfDead = 0;
		spreading_factor = (randomGenerator.nextInt(6) + 5)/10.0;
		mortality_rate = (randomGenerator.nextInt(9) + 1)/10.0;
		numberOfVentilator = POPULATION / 100;
		executerService = Executors.newFixedThreadPool(100);
		deadExist = false;
		deadIndIndexList = new ArrayList<>();
		waitForDie = (int) (100 * (1 - mortality_rate));
		timer = new Timer();
		System.out.println("time to wait for die: " + waitForDie);
	}
	
	/**
	 * Shuts down the executer service and cancels the timer.
	 */
	public void exit(){
		executerService.shutdown();
		timer.cancel();
	}
	
	/**
	 * adds one individual
	 */
	@Override
	public void addIndividual() {
		
		if( listOfIndividuals.size() < POPULATION ){
			
			if( listOfIndividuals.size() == 0 ) {
				
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
				
				listOfIndividuals.add(new Individual(randomX, randomY, randomSpeed, randomSpendingTime, randomSocialDistance, randomMValue, true));
				
				timer.schedule(new HospitalTimerTask(0),0);
				
			}
			else {
				listOfIndividuals.add(listOfIndividuals.get(0).cloneIndividual());
			}
			
		}
		
	}
	
	/**
	 * adds 50 individuals at one time.
	 */
	@Override
	public void addIndividualsInBulk() {
		
		int numberOfInds;
		
		numberOfInds = listOfIndividuals.size();
		
		if( numberOfInds < POPULATION - 50 ){
			
			if(numberOfInds == 0){
				int randomX;
				int randomY;
				int randomSpeed;
				int randomSpendingTime;
				int randomSocialDistance;
				boolean randomBoolean;
				double randomMValue;
				
				randomX = randomGenerator.nextInt(maxXPosition);
				randomY = randomGenerator.nextInt(maxYPosition);
				randomSpeed = randomGenerator.nextInt(5) + 1;
				randomSpendingTime = randomGenerator.nextInt(5) + 1;
				randomSocialDistance = randomGenerator.nextInt(10);
				randomBoolean = randomGenerator.nextBoolean();
				if (randomBoolean)
					randomMValue = 0.2;
				else
					randomMValue = 1.0;
				
				listOfIndividuals.add(new Individual(randomX, randomY, randomSpeed, randomSpendingTime, randomSocialDistance, randomMValue, false));
				
				for(int i = 1; i < 50; ++i){
					listOfIndividuals.add(listOfIndividuals.get(0).cloneIndividual());
				}
			}
			else{
				for(int i = 0; i < 50; ++i){
					listOfIndividuals.add(listOfIndividuals.get(0).cloneIndividual());
				}
			}
			
		}
		else{
			if(numberOfInds < POPULATION ) {
				for (int i = numberOfInds; i < POPULATION; ++i) {
					int randomX;
					int randomY;
					int randomSpeed;
					int randomSpendingTime;
					int randomSocialDistance;
					boolean randomBoolean;
					double randomMValue;
					
					randomX = randomGenerator.nextInt(maxXPosition);
					randomY = randomGenerator.nextInt(maxYPosition);
					randomSpeed = randomGenerator.nextInt(5) + 1;
					randomSpendingTime = randomGenerator.nextInt(5) + 1;
					randomSocialDistance = randomGenerator.nextInt(10);
					randomBoolean = randomGenerator.nextBoolean();
					if (randomBoolean)
						randomMValue = 0.2;
					else
						randomMValue = 1.0;
					
					listOfIndividuals.add(new Individual(randomX, randomY, randomSpeed, randomSpendingTime, randomSocialDistance, randomMValue, false));
					
				}
				
			}
			
		}
		
		boolean infectedExist = false;
		for (int i = 0; i < listOfIndividuals.size(); ++i) {
			if (listOfIndividuals.get(i).isInfected()) {
				infectedExist = true;
				break;
			}
		}
		if (!infectedExist) {
			int randomIndex = randomGenerator.nextInt(listOfIndividuals.size());
			listOfIndividuals.get(randomIndex).setInfected(true);
			timer.schedule(new HospitalTimerTask(randomIndex), 0);
		}
		
	}
	
	/**
	 *  finds interactions between the individuals and
	 *  does whatever it needs for hospital functionality.
	 */
	@Override
	public void play() {
		findInteractions();
	}
	
	/**
	 * returns a list of individuals.
	 * @return a list of individuals.
	 */
	@Override
	public List<Individual> getIndividuals() {
		
		List<Individual> list = new ArrayList<>();
		
		for(int i = 0; i < listOfIndividuals.size(); ++i){
			list.add(listOfIndividuals.get(i));
		}
		
		return list;
	}
	
	/**
	 * finds the interactions between the individuals.
	 *  If two individual collide, it creates a thread for each and makes both individuals wait at the same point for the specified time.
	 *  If any of the individual is infected, it creates a thread for it and waits for 25 seconds and took a ventilator if there is any.
	 *  If there is no ventilator available, then returns the canvas as infected.
	 */
	private void findInteractions() {
		
		deadListLock.lock();
		try {
			if(deadExist){
				removeDeads();
				deadExist = false;
				System.out.println("in deadExist lock");
			}
		}finally {
			deadListLock.unlock();
		}
		
		if( listOfIndividuals.size() > 0 ){
			
			if( listOfIndividuals.size() == 1 ){
				move(0);
			}
			else
			{
				for(int i = 0; i < listOfIndividuals.size(); ++i){
					
					boolean firstIndInteract = listOfIndividuals.get(i).isInteracting();
					
					if( !listOfIndividuals.get(i).isInteracting() ){
						
						for(int j = i + 1; j < listOfIndividuals.size(); ++j){
							
							if( !listOfIndividuals.get(j).isInteracting() ){
								
								int firstIndX = listOfIndividuals.get(i).getPositionX();
								int firstIndY = listOfIndividuals.get(i).getPositionY();
								int secondIndX = listOfIndividuals.get(j).getPositionX();
								int secondIndY = listOfIndividuals.get(j).getPositionY();
								
								int c_1 = listOfIndividuals.get(i).getSpendingTime();
								int c_2 = listOfIndividuals.get(j).getSpendingTime();
								
								int maxOfCs = Math.max(c_1, c_2);
								
								if (Math.abs(firstIndX - secondIndX) < 5 && Math.abs(firstIndY - secondIndY) < 5) {
									
									if( listOfIndividuals.get(i).isInfected() || listOfIndividuals.get(j).isInfected() ){
										
										double probabilityOfInfecting;
										int d_1 = listOfIndividuals.get(i).getSocialDistance();
										int d_2 = listOfIndividuals.get(j).getSocialDistance();
										double m_1 = listOfIndividuals.get(i).getWearMaskValue();
										double m_2 = listOfIndividuals.get(j).getWearMaskValue();
										
										int minOfDs = Math.min(d_1, d_2);
										
										probabilityOfInfecting = spreading_factor * (1 + maxOfCs / 10.0) * m_1 * m_2 * (1 - minOfDs / 10.0);
										
										System.out.println("Probability of infecting : " + probabilityOfInfecting);
										
										if( probabilityOfInfecting > 0.5 ) {
											
											if (listOfIndividuals.get(i).isInfected()) {
												if(!listOfIndividuals.get(j).isInfected()) {
													listOfIndividuals.get(j).setInfected(true);
													timer.schedule(new HospitalTimerTask(j),0);
												}
												
											}else{
												if(!listOfIndividuals.get(i).isInfected()){
													listOfIndividuals.get(i).setInfected(true);
													timer.schedule(new HospitalTimerTask(i),0);
												}
												
											}
										}
										
									}
									
									if(!listOfIndividuals.get(i).isInteracting()){
										listOfIndividuals.get(i).setInteracting(true);
										executerService.submit(new InteractingRunnable(i,maxOfCs));
									}
									if(!listOfIndividuals.get(j).isInteracting()){
										listOfIndividuals.get(j).setInteracting(true);
										executerService.submit(new InteractingRunnable(j,maxOfCs));
									}
									
									firstIndInteract = true;
									break;
									
								}
								
							}
							
						}
						
						if( !firstIndInteract ){
							move(i);
						}
						
					}
					
				}
			}
			
		}
		
	}
	
	/**
	 * moves the individual to random direction
	 * @param index of the individual list
	 */
	private void move(int index){
		
		int randomDirection;
		int currentX = listOfIndividuals.get(index).getPositionX();
		int currentY = listOfIndividuals.get(index).getPositionY();
		int speed = listOfIndividuals.get(index).getSpeed();
		
		
		randomDirection = randomGenerator.nextInt(4);
		boolean isMoved = false;
		
		while( !isMoved ){
			
			if( randomDirection == UP_DIRECTION ){
				
				if( currentY - speed >= 0 ){
					
					listOfIndividuals.get(index).setPositionY(currentY-speed);
					isMoved = true;
				}
				
			}
			else if( randomDirection == LEFT_DIRECTION ){
				
				if( currentX - speed >= 0 ){
					
					listOfIndividuals.get(index).setPositionX(currentX - speed);
					isMoved = true;
				}
			}
			else if( randomDirection == DOWN_DIRECTION ){
				
				if( currentY + speed < maxYPosition ){
					
					listOfIndividuals.get(index).setPositionY(currentY + speed);
					isMoved = true;
				}
			}
			else {
				
				if( currentX + speed < maxXPosition ){
					
					listOfIndividuals.get(index).setPositionX(currentX + speed);
					isMoved = true;
				}
			}
			
			if( !isMoved )
				randomDirection++;
			
		}
		
	}
	
	/**
	 * retuns the number of dead.
	 * @return the number of dead.
	 */
	@Override
	public int getNumberOfDead() {
		return numberOfDead;
	}
	
	/**
	 * increments the number of the dead individuals.
	 */
	private void incrementNumberOfDead(){
		++numberOfDead;
	}
	
	/**
	 * removes dead individuals from the list.
	 */
	private void removeDeads() {
		
		deadListLock.lock();
		try{
			int deadListSize = deadIndIndexList.size();
			for(int i = 0; i < deadListSize; ++i){
				listOfIndividuals.remove(deadIndIndexList.get(i));
				incrementNumberOfDead();
			}
			deadIndIndexList.clear();
		}finally {
			deadListLock.unlock();
		}
		
	}
	
	/**
	 * It is created when two individual collides at the canvas.
	 * It waits for along the time specified.
	 */
	class InteractingRunnable implements Runnable{
		int index;
		int spendTime;
		
		InteractingRunnable(int index, int spendTime){
			this.index = index;
			this.spendTime = spendTime;
		}
		
		/**
		 * When an object implementing interface {@code Runnable} is used
		 * to create a thread, starting the thread causes the object's
		 * {@code run} method to be called in that separately executing
		 * thread.
		 * <p>
		 * The general contract of the method {@code run} is that it may
		 * take any action whatsoever.
		 *
		 * @see Thread#run()
		 */
		@Override
		public void run() {
			try {
				Thread.sleep(spendTime*1000);
				listOfIndividuals.get(index).setInteracting(false);
				move(index);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * waits for 25 seconds and took a ventilator if there is any.
	 * If there is no ventilator available, then returns to the canvas as infected.
	 * If 100*(1-mortality_rate) is less than 25, then after 100*(1-mortality_rate) seconds the individual will die.
	 */
	class HospitalTimerTask extends TimerTask{
		
		int index;
		
		HospitalTimerTask(int ind){
			this.index = ind;
		}
		
		/**
		 * The action to be performed by this timer task.
		 */
		@Override
		public void run() {
			try {
				
				if( waitForDie >= timeIntervalAfterFirstInfection ) {
					Thread.sleep(timeIntervalAfterFirstInfection * 1000);
					boolean tookVentilator = false;
					
					reentrantLockForVentilator.lock();
					try {
						if (numberOfVentilator > 0) {
							
							listOfIndividuals.get(index).setHospitalized(true);
							
							--numberOfVentilator;
							tookVentilator = true;
						}
					} finally {
						reentrantLockForVentilator.unlock();
					}
					
					if (tookVentilator) {
						
						Thread.sleep(timeIntervalAfterStayingHostpital * 1000);
						
						listOfIndividuals.get(index).setHospitalized(false);
						listOfIndividuals.get(index).setInfected(false);
						
						reentrantLockForVentilator.lock();
						try {
							++numberOfVentilator;
						} finally {
							reentrantLockForVentilator.unlock();
						}
						
					} else {
						listOfIndividuals.get(index).setHospitalized(false);
					}
				}
				else{ // waitForDie < timeIntervalAfterFirstInfection
					Thread.sleep(waitForDie * 1000);
					
					deadListLock.lock();
					try{
						listOfIndividuals.get(index).setDead(true);
						deadIndIndexList.add(index);
						deadExist = true;
						
					}finally {
						deadListLock.unlock();
					}
					
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	
}
