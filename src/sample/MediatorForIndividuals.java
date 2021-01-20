package sample;

import java.util.List;

/**
 *  Mediator interface for individuals to do the interactions and hospital functionality stuff.
 */
public interface MediatorForIndividuals {
	
	/**
	 *  finds interactions between the individuals and
	 *  does whatever it needs for hospital functionality.
	 */
	void play();
	
	/**
	 * returns a list of individuals.
	 * @return a list of individuals.
	 */
	List<Individual> getIndividuals();
	
	/**
	 * adds one individual
	 */
	void addIndividual();
	
	/**
	 * adds 50 individuals at one time.
	 */
	void addIndividualsInBulk();
	
	/**
	 * retuns the number of dead.
	 * @return the number of dead.
	 */
	int getNumberOfDead();
	
}
